package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import com.example.data.database.AppDatabase
import com.example.data.database.AppCategory
import com.example.data.database.InteractionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TapAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var currentForegroundApp = "lena"

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val type = when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> "SCREEN_ON"
                Intent.ACTION_SCREEN_OFF -> "SCREEN_OFF"
                else -> return
            }
            logEvent(type, null)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Register receiver for screen on/off tracking
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val pkg = event.packageName?.toString() ?: return
                currentForegroundApp = mapPackageToAppName(pkg)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                val pkg = event.packageName?.toString() ?: return
                val appName = mapPackageToAppName(pkg)
                logEvent("TOUCH", appName)
            }
        }
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun logEvent(type: String, appName: String?) {
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val dao = db.interactionDao()

                val categoryString = if (appName != null) {
                    val categories = dao.getAllCategories()
                    val existingCategory = categories.find { it.appName.equals(appName, ignoreCase = true) }

                    val isSocial = if (existingCategory == null) {
                        // Dynamically discover and register a new app seen on the device
                        val isGuessedSocial = mapAppNameOrPackageToSocial(appName)
                        dao.insertCategory(AppCategory(appName, isGuessedSocial))
                        isGuessedSocial
                    } else {
                        existingCategory.isSocial
                    }
                    if (isSocial) "SOCIAL" else "OTHER"
                } else {
                    null
                }

                dao.insertEvent(
                    InteractionEvent(
                        type = type,
                        appName = appName,
                        category = categoryString
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun mapAppNameOrPackageToSocial(appName: String): Boolean {
        val lowerAppName = appName.lowercase()
        val socialKeywords = listOf(
            "chat", "social", "messenger", "twitter", "facebook", "instagram", "threads",
            "whatsapp", "telegram", "discord", "tiktok", "snapchat", "reddit", "linkedin",
            "tinder", "grindr", "bumble", "pinterest", "skype", "viber", "wechat", "line"
        )
        return socialKeywords.any { lowerAppName.contains(it) }
    }

    private fun mapPackageToAppName(pkg: String): String {
        // 1. Predefined standard mappings for popular packages
        val standardName = when {
            pkg.contains("whatsapp", ignoreCase = true) -> "WhatsApp"
            pkg.contains("instagram", ignoreCase = true) -> "Instagram"
            pkg.contains("tiktok", ignoreCase = true) -> "TikTok"
            pkg.contains("facebook", ignoreCase = true) -> "Facebook"
            pkg.contains("twitter", ignoreCase = true) || pkg.contains("x.android", ignoreCase = true) -> "X / Twitter"
            pkg.contains("telegram", ignoreCase = true) -> "Telegram"
            pkg.contains("discord", ignoreCase = true) -> "Discord"
            pkg.contains("reddit", ignoreCase = true) -> "Reddit"
            pkg.contains("linkedin", ignoreCase = true) -> "LinkedIn"
            pkg.contains("snapchat", ignoreCase = true) -> "Snapchat"
            pkg.contains("android.chrome", ignoreCase = true) -> "Chrome"
            pkg.contains("android.gm", ignoreCase = true) -> "Gmail"
            pkg.contains("spotify", ignoreCase = true) -> "Spotify"
            pkg.contains("calculator", ignoreCase = true) -> "Calculator"
            pkg.contains("camera", ignoreCase = true) -> "Câmera"
            pkg.contains("dialer", ignoreCase = true) || pkg.contains("phone", ignoreCase = true) -> "Telefone"
            pkg.contains("dopatap", ignoreCase = true) || pkg.contains("lena", ignoreCase = true) -> "lena"
            else -> null
        }
        if (standardName != null) return standardName

        // 2. Dynamic lookup via Android PackageManager for high-fidelity app name retrieval
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            // 3. Robust clean fallback parsing if app name can't be resolved dynamically
            val parts = pkg.split(".")
            val rawName = parts.lastOrNull() ?: pkg
            rawName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
