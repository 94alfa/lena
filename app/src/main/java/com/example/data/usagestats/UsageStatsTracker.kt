package com.example.data.usagestats

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.example.data.database.AppCategory
import com.example.data.database.AppUsageRecord
import com.example.data.database.InteractionEvent
import java.util.Calendar

object UsageStatsTracker {

    /**
     * Verifies if the user has authorized PACKAGE_USAGE_STATS in Android system settings.
     */
    fun hasUsageAccessPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        @Suppress("DEPRECATION")
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Opens the Android system settings screen where the user grants Usage Access.
     */
    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Queries Android UsageEvents via UsageStatsManager between startTime and endTime,
     * assembling concrete usage sessions for third-party apps.
     */
    fun queryUsageSessions(
        context: Context,
        startTime: Long,
        endTime: Long,
        categories: List<AppCategory>
    ): List<AppUsageRecord> {
        if (!hasUsageAccessPermission(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        val openAppTimes = mutableMapOf<String, Long>()
        val interactionCounts = mutableMapOf<String, Int>()
        val records = mutableListOf<AppUsageRecord>()
        val pm = context.packageManager

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue

            // Filter out internal android system components and keyboard
            if (isInternalSystemPackage(pkg)) continue

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    openAppTimes[pkg] = event.timeStamp
                    interactionCounts[pkg] = (interactionCounts[pkg] ?: 0) + 1
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val openedAt = openAppTimes.remove(pkg)
                    if (openedAt != null && event.timeStamp > openedAt) {
                        val durationMs = event.timeStamp - openedAt
                        // Only count meaningful sessions (at least 2 seconds)
                        if (durationMs >= 2000L) {
                            val durationSec = durationMs / 1000L
                            val interactions = interactionCounts.remove(pkg) ?: 1
                            val appLabel = resolveAppLabel(pm, pkg)
                            val isSocial = resolveIsSocial(appLabel, pkg, categories)

                            records.add(
                                AppUsageRecord(
                                    packageName = pkg,
                                    appName = appLabel,
                                    startTime = openedAt,
                                    endTime = event.timeStamp,
                                    durationSeconds = durationSec,
                                    isSocial = isSocial,
                                    interactionCount = interactions,
                                    detectedVia = "USAGE_STATS"
                                )
                            )
                        }
                    }
                }
                UsageEvents.Event.USER_INTERACTION -> {
                    interactionCounts[pkg] = (interactionCounts[pkg] ?: 0) + 1
                }
            }
        }

        // Handle any still-open active foreground session up to endTime
        for ((pkg, openedAt) in openAppTimes) {
            if (endTime > openedAt) {
                val durationMs = endTime - openedAt
                if (durationMs >= 2000L) {
                    val durationSec = durationMs / 1000L
                    val interactions = interactionCounts[pkg] ?: 1
                    val appLabel = resolveAppLabel(pm, pkg)
                    val isSocial = resolveIsSocial(appLabel, pkg, categories)

                    records.add(
                        AppUsageRecord(
                            packageName = pkg,
                            appName = appLabel,
                            startTime = openedAt,
                            endTime = endTime,
                            durationSeconds = durationSec,
                            isSocial = isSocial,
                            interactionCount = interactions,
                            detectedVia = "USAGE_STATS"
                        )
                    )
                }
            }
        }

        // Also query daily aggregate stats as a complementary source if events were sparse
        if (records.isEmpty()) {
            val dailyStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            for (stat in dailyStats) {
                val pkg = stat.packageName
                if (isInternalSystemPackage(pkg)) continue
                val totalTimeSec = stat.totalTimeInForeground / 1000L
                if (totalTimeSec >= 5L && stat.lastTimeUsed >= startTime) {
                    val appLabel = resolveAppLabel(pm, pkg)
                    val isSocial = resolveIsSocial(appLabel, pkg, categories)
                    records.add(
                        AppUsageRecord(
                            packageName = pkg,
                            appName = appLabel,
                            startTime = stat.firstTimeStamp.coerceAtLeast(startTime),
                            endTime = stat.lastTimeUsed,
                            durationSeconds = totalTimeSec,
                            isSocial = isSocial,
                            interactionCount = (totalTimeSec / 4L).toInt().coerceAtLeast(1),
                            detectedVia = "USAGE_STATS_DAILY"
                        )
                    )
                }
            }
        }

        return records.sortedByDescending { it.startTime }
    }

    /**
     * Converts AppUsageRecords into discrete InteractionEvents to hydrate Room and update
     * the Recharts hourly charts with real third-party app activity.
     */
    fun convertRecordsToInteractionEvents(records: List<AppUsageRecord>): List<InteractionEvent> {
        val events = mutableListOf<InteractionEvent>()

        for (record in records) {
            // Log opening event
            events.add(
                InteractionEvent(
                    timestamp = record.startTime,
                    type = "APP_OPEN",
                    appName = record.appName,
                    category = if (record.isSocial) "SOCIAL" else "OTHER"
                )
            )

            // Simulate touch frequency distribution across the duration of the usage session
            // (e.g. 1 touch every 3 seconds for social apps, or 1 every 6 seconds for utilities)
            val intervalMs = if (record.isSocial) 2500L else 5000L
            val simulatedTaps = (record.durationSeconds * 1000L / intervalMs).toInt().coerceIn(1, 120)

            var t = record.startTime + 500L
            for (i in 0 until simulatedTaps) {
                if (t >= record.endTime) break
                events.add(
                    InteractionEvent(
                        timestamp = t,
                        type = "TOUCH",
                        appName = record.appName,
                        category = if (record.isSocial) "SOCIAL" else "OTHER"
                    )
                )
                t += intervalMs
            }
        }

        return events
    }

    /**
     * Identifies the current active foreground app right now.
     */
    fun queryCurrentForegroundApp(context: Context): Pair<String, String>? {
        if (!hasUsageAccessPermission(context)) return null

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(now - 60_000L, now)
        val event = UsageEvents.Event()

        var latestPkg: String? = null
        var latestTime = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                val pkg = event.packageName
                if (pkg != null && !isInternalSystemPackage(pkg) && event.timeStamp > latestTime) {
                    latestPkg = pkg
                    latestTime = event.timeStamp
                }
            }
        }

        return latestPkg?.let { pkg ->
            val label = resolveAppLabel(context.packageManager, pkg)
            Pair(pkg, label)
        }
    }

    private fun resolveAppLabel(pm: PackageManager, packageName: String): String {
        return try {
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            // Clean fallback
            val parts = packageName.split(".")
            val name = parts.lastOrNull() ?: packageName
            name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    private fun resolveIsSocial(appLabel: String, pkg: String, categories: List<AppCategory>): Boolean {
        // 1. Check database categories
        val dbCategory = categories.find {
            it.appName.equals(appLabel, ignoreCase = true) || pkg.contains(it.appName, ignoreCase = true)
        }
        if (dbCategory != null) return dbCategory.isSocial

        // 2. Keyword heuristic
        val lower = (appLabel + " " + pkg).lowercase()
        val socialKeywords = listOf(
            "whatsapp", "instagram", "tiktok", "facebook", "twitter", "telegram",
            "discord", "reddit", "snapchat", "threads", "social", "messenger", "chat"
        )
        return socialKeywords.any { lower.contains(it) }
    }

    private fun isInternalSystemPackage(pkg: String): Boolean {
        return pkg.startsWith("com.android.systemui") ||
                pkg.startsWith("com.google.android.inputmethod") ||
                pkg.startsWith("com.android.inputmethod") ||
                pkg.startsWith("com.android.keyguard") ||
                pkg == "android"
    }
}
