package com.example.data.repository

import com.example.data.database.AppCategory
import com.example.data.database.AppUsageRecord
import com.example.data.database.DailySummary
import com.example.data.database.DopamineSpike
import com.example.data.database.HistoricalDay
import com.example.data.database.InteractionDao
import com.example.data.database.InteractionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import kotlin.math.sqrt

class InteractionRepository(private val dao: InteractionDao) {

    val allEvents: Flow<List<InteractionEvent>> = dao.getAllEvents()
    val allCategories: Flow<List<AppCategory>> = dao.getAllCategoriesFlow()
    val historicalDays: Flow<List<HistoricalDay>> = dao.getHistoricalDays()
    val allSpikes: Flow<List<DopamineSpike>> = dao.getAllSpikes()
    val allSummaries: Flow<List<DailySummary>> = dao.getAllSummaries()
    val allUsageRecords: Flow<List<AppUsageRecord>> = dao.getAllUsageRecords()
    val recentUsageRecords: Flow<List<AppUsageRecord>> = dao.getRecentUsageRecords(15)

    suspend fun insertUsageRecord(record: AppUsageRecord) {
        dao.insertUsageRecord(record)
    }

    suspend fun insertUsageRecords(records: List<AppUsageRecord>) {
        dao.insertUsageRecords(records)
    }

    suspend fun clearAllUsageRecords() {
        dao.clearAllUsageRecords()
    }

    suspend fun getLatestUsageEndTime(): Long? {
        return dao.getLatestUsageEndTime()
    }

    suspend fun insertEvent(event: InteractionEvent) {
        dao.insertEvent(event)
    }

    suspend fun clearAllEvents() {
        dao.clearAllEvents()
    }

    suspend fun insertCategory(category: AppCategory) {
        dao.insertCategory(category)
    }

    suspend fun insertHistoricalDay(day: HistoricalDay) {
        dao.insertHistoricalDay(day)
    }

    suspend fun clearUserHistoricalDays() {
        dao.clearUserHistoricalDays()
    }

    suspend fun insertSpike(spike: DopamineSpike) {
        dao.insertSpike(spike)
    }

    suspend fun insertSpikes(spikes: List<DopamineSpike>) {
        dao.insertSpikes(spikes)
    }

    suspend fun clearAllSpikes() {
        dao.clearAllSpikes()
    }

    suspend fun insertSummary(summary: DailySummary) {
        dao.insertSummary(summary)
    }

    /**
     * Pre-populates the database with standard applications and research study baseline data.
     */
    suspend fun initializeBaselineDataIfNeeded() {
        val existingCategories = dao.getAllCategories()
        if (existingCategories.isEmpty()) {
            val defaultCategories = listOf(
                AppCategory("WhatsApp", true),
                AppCategory("Instagram", true),
                AppCategory("TikTok", true),
                AppCategory("Facebook", true),
                AppCategory("X / Twitter", true),
                AppCategory("Telegram", true),
                AppCategory("Discord", true),
                AppCategory("Reddit", true),
                AppCategory("LinkedIn", true),
                AppCategory("Snapchat", true),
                AppCategory("Chrome", false),
                AppCategory("Gmail", false),
                AppCategory("Spotify", false),
                AppCategory("Calculator", false),
                AppCategory("lena", false),
                AppCategory("Câmera", false),
                AppCategory("Telefone", false)
            )
            dao.insertCategories(defaultCategories)
        }

        val existingDays = dao.getHistoricalDays().first()
        if (existingDays.isEmpty()) {
            val baselineDays = listOf(
                HistoricalDay("Estudo ID 1", 0.15f, 1.1f, 450, 380),
                HistoricalDay("Estudo ID 2", 0.22f, 0.9f, 600, 350),
                HistoricalDay("Estudo ID 3", 0.30f, 0.6f, 720, 310),
                HistoricalDay("Estudo ID 4", 0.38f, 0.2f, 900, 280),
                HistoricalDay("Estudo ID 5", 0.45f, -0.1f, 1100, 250),
                HistoricalDay("Estudo ID 6", 0.52f, -0.3f, 1350, 220),
                HistoricalDay("Estudo ID 7", 0.60f, -0.6f, 1600, 190),
                HistoricalDay("Estudo ID 8", 0.68f, -1.0f, 1900, 170),
                HistoricalDay("Estudo ID 9", 0.75f, -1.3f, 2300, 150),
                HistoricalDay("Estudo ID 10", 0.28f, 0.7f, 680, 330),
                HistoricalDay("Estudo ID 11", 0.48f, -0.2f, 1150, 240),
                HistoricalDay("Estudo ID 12", 0.65f, -0.8f, 1750, 180),
                HistoricalDay("Estudo ID 13", 0.18f, 1.0f, 510, 370),
                HistoricalDay("Estudo ID 14", 0.55f, -0.5f, 1420, 210),
                HistoricalDay("Estudo ID 15", 0.72f, -1.2f, 2150, 160)
            )
            dao.insertHistoricalDays(baselineDays)
        }

        // Also if no events exist yet, generate an initial realistic sample day
        // so the user immediately experiences the interactive charts upon opening!
        val existingEvents = dao.getAllEvents().first()
        if (existingEvents.isEmpty()) {
            populateSampleDayData()
        }
    }

    /**
     * Helper to compute key metrics from raw events of the current day.
     */
    fun calculateMetrics(events: List<InteractionEvent>): DailyMetrics {
        val tapEvents = events.filter { it.type == "TOUCH" }.sortedBy { it.timestamp }
        val totalTaps = tapEvents.size
        if (totalTaps == 0) {
            return DailyMetrics(0, 0, 0.0f, 0, 0.0f)
        }

        val socialTaps = tapEvents.count { it.category == "SOCIAL" }
        val socialProportion = socialTaps.toFloat() / totalTaps

        val intervals = mutableListOf<Long>()
        for (i in 1 until tapEvents.size) {
            val diff = tapEvents[i].timestamp - tapEvents[i - 1].timestamp
            if (diff in 50..5000) {
                intervals.add(diff)
            }
        }

        val vigorMs = if (intervals.isNotEmpty()) {
            intervals.sort()
            val index = (intervals.size * 0.25).toInt().coerceIn(0, intervals.lastIndex)
            intervals[index].toInt()
        } else {
            0
        }

        val generalUsageNormalized = sqrt(totalTaps.toDouble()).toFloat()

        return DailyMetrics(totalTaps, socialTaps, socialProportion, vigorMs, generalUsageNormalized)
    }

    /**
     * Detects suspected dopamine spikes using high-frequency interaction bursts.
     * In behavioral neuroscience, rapid interactive bursts (<600ms interval, >=10 taps in 60s)
     * indicate high autonomic arousal and dopaminergic feedback loops.
     */
    fun detectDopamineSpikes(events: List<InteractionEvent>): List<DopamineSpike> {
        val tapEvents = events.filter { it.type == "TOUCH" && !it.appName.isNullOrEmpty() }
            .sortedBy { it.timestamp }
        if (tapEvents.size < 8) return emptyList()

        val detectedSpikes = mutableListOf<DopamineSpike>()
        var windowStart = 0

        while (windowStart < tapEvents.size) {
            val startTime = tapEvents[windowStart].timestamp
            val currentApp = tapEvents[windowStart].appName ?: "App"
            val isSocial = tapEvents[windowStart].category == "SOCIAL"

            // Look forward within a 75-second window
            var windowEnd = windowStart
            while (windowEnd < tapEvents.size &&
                (tapEvents[windowEnd].timestamp - startTime) <= 75_000L &&
                tapEvents[windowEnd].appName == currentApp
            ) {
                windowEnd++
            }

            val countInWindow = windowEnd - windowStart
            if (countInWindow >= 8) {
                val durationMs = (tapEvents[windowEnd - 1].timestamp - startTime).coerceAtLeast(1000L)
                val durationSec = (durationMs / 1000L).toInt().coerceAtLeast(1)
                val avgIntervalMs = (durationMs / countInWindow).toInt()

                // Calculate intensity based on tempo (lower interval = higher speed) and app type
                val speedFactor = ((700f - avgIntervalMs.coerceIn(150, 700)) / 550f).coerceIn(0.1f, 1.0f)
                val socialBonus = if (isSocial) 0.2f else 0.0f
                val volumeBonus = (countInWindow / 30f).coerceIn(0f, 0.2f)
                val intensity = (speedFactor * 0.6f + socialBonus + volumeBonus).coerceIn(0.2f, 1.0f)

                val triggerType = when {
                    isSocial && countInWindow >= 20 -> "Rolagem Compulsiva / Reels"
                    isSocial && avgIntervalMs < 350 -> "Disparo Rápido de Mensagens"
                    isSocial -> "Navegação Social Intensa"
                    avgIntervalMs < 300 -> "Burst de Interação em Aplicativo"
                    else -> "Pico de Frequência Rápido"
                }

                detectedSpikes.add(
                    DopamineSpike(
                        timestamp = startTime,
                        appName = currentApp,
                        touchCount = countInWindow,
                        durationSeconds = durationSec,
                        avgIntervalMs = avgIntervalMs,
                        intensity = intensity,
                        triggerType = triggerType
                    )
                )

                // Advance beyond this window to avoid duplicate spike spamming
                windowStart = windowEnd
            } else {
                windowStart++
            }
        }

        return detectedSpikes.sortedByDescending { it.timestamp }
    }

    /**
     * Calculates the hourly interaction frequency (0 to 23 hours) for Recharts-style Compose graphs.
     */
    fun calculateHourlyFrequencies(
        events: List<InteractionEvent>,
        spikes: List<DopamineSpike>
    ): List<HourlyFrequency> {
        val cal = Calendar.getInstance()
        val hourBuckets = Array(24) { hour ->
            HourlyFrequency(
                hour = hour,
                totalCount = 0,
                socialCount = 0,
                otherCount = 0,
                hasSpike = false,
                spikeCount = 0,
                maxSpikeIntensity = 0f,
                topApp = null
            )
        }

        // Group touches by hour of day
        val tapsByHour = mutableMapOf<Int, MutableList<InteractionEvent>>()
        for (event in events) {
            if (event.type == "TOUCH") {
                cal.timeInMillis = event.timestamp
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                tapsByHour.getOrPut(hour) { mutableListOf() }.add(event)
            }
        }

        // Group spikes by hour of day
        val spikesByHour = mutableMapOf<Int, MutableList<DopamineSpike>>()
        for (spike in spikes) {
            cal.timeInMillis = spike.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            spikesByHour.getOrPut(hour) { mutableListOf() }.add(spike)
        }

        for (hour in 0..23) {
            val list = tapsByHour[hour] ?: emptyList()
            val total = list.size
            val social = list.count { it.category == "SOCIAL" }
            val other = total - social

            val hourSpikes = spikesByHour[hour] ?: emptyList()
            val hasSpike = hourSpikes.isNotEmpty()
            val maxSpikeIntensity = hourSpikes.maxOfOrNull { it.intensity } ?: 0f

            val topApp = list.mapNotNull { it.appName }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }?.key

            hourBuckets[hour] = HourlyFrequency(
                hour = hour,
                totalCount = total,
                socialCount = social,
                otherCount = other,
                hasSpike = hasSpike,
                spikeCount = hourSpikes.size,
                maxSpikeIntensity = maxSpikeIntensity,
                topApp = topApp
            )
        }

        return hourBuckets.toList()
    }

    /**
     * Calculates breakdown of interactions per application.
     */
    fun calculateAppFrequencies(events: List<InteractionEvent>): List<AppFrequency> {
        val tapEvents = events.filter { it.type == "TOUCH" && !it.appName.isNullOrEmpty() }
        val total = tapEvents.size
        if (total == 0) return emptyList()

        return tapEvents.groupBy { it.appName!! }
            .map { (appName, appEvents) ->
                val count = appEvents.size
                val isSocial = appEvents.firstOrNull()?.category == "SOCIAL"
                val percentage = count.toFloat() / total
                AppFrequency(appName, count, percentage, isSocial)
            }
            .sortedByDescending { it.count }
    }

    /**
     * Populates a complete 24-hour realistic sample day to immediately visualize
     * interaction frequency curves and suspected dopamine spikes in the Recharts Compose graph.
     */
    suspend fun populateSampleDayData() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val newEvents = mutableListOf<InteractionEvent>()
        val newSpikes = mutableListOf<DopamineSpike>()

        fun addBurst(hour: Int, minute: Int, app: String, isSocial: Boolean, count: Int, intervalMs: Long) {
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            var time = cal.timeInMillis

            for (i in 0 until count) {
                newEvents.add(
                    InteractionEvent(
                        timestamp = time,
                        type = "TOUCH",
                        appName = app,
                        category = if (isSocial) "SOCIAL" else "OTHER"
                    )
                )
                time += intervalMs + (Math.random() * 50).toLong()
            }
        }

        // 07:30 - Morning wake-up check (WhatsApp & Chrome)
        addBurst(7, 30, "WhatsApp", true, 18, 380L)
        addBurst(7, 45, "Chrome", false, 12, 600L)

        // 09:15 - Commute / Early work (Spotify, Telegram)
        addBurst(9, 15, "Spotify", false, 8, 900L)
        addBurst(9, 30, "Telegram", true, 14, 450L)

        // 11:45 - Pre-lunch break (TikTok - Suspected Dopamine Spike #1)
        addBurst(11, 45, "TikTok", true, 32, 220L)

        // 13:10 - Lunch scroll (Instagram - Suspected Dopamine Spike #2)
        addBurst(13, 10, "Instagram", true, 28, 250L)
        addBurst(13, 30, "Chrome", false, 15, 650L)

        // 15:20 - Afternoon fatigue / distraction (Reddit - Suspected Dopamine Spike #3)
        addBurst(15, 20, "Reddit", true, 24, 290L)
        addBurst(16, 10, "Gmail", false, 16, 750L)

        // 18:30 - Evening messages (WhatsApp)
        addBurst(18, 30, "WhatsApp", true, 22, 340L)

        // 20:45 - Prime time relaxation (YouTube / Instagram - Suspected Dopamine Spike #4)
        addBurst(20, 45, "Instagram", true, 35, 210L)

        // 22:15 - Bedtime compulsive scroll (TikTok - Suspected Dopamine Spike #5)
        addBurst(22, 15, "TikTok", true, 42, 190L)

        for (e in newEvents) {
            dao.insertEvent(e)
        }

        val detected = detectDopamineSpikes(newEvents)
        dao.insertSpikes(detected)
    }
}

data class DailyMetrics(
    val totalTaps: Int,
    val socialTaps: Int,
    val socialProportion: Float,
    val vigorMs: Int,
    val generalUsageNormalized: Float
)

data class HourlyFrequency(
    val hour: Int,
    val totalCount: Int,
    val socialCount: Int,
    val otherCount: Int,
    val hasSpike: Boolean,
    val spikeCount: Int,
    val maxSpikeIntensity: Float,
    val topApp: String?
)

data class AppFrequency(
    val appName: String,
    val count: Int,
    val percentage: Float,
    val isSocial: Boolean
)
