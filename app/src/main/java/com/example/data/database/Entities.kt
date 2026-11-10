package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interaction_events")
data class InteractionEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "TOUCH", "SCREEN_ON", "SCREEN_OFF"
    val appName: String? = null,
    val category: String? = null // "SOCIAL", "OTHER" (null for screen events)
)

@Entity(tableName = "app_categories")
data class AppCategory(
    @PrimaryKey val appName: String,
    val isSocial: Boolean
)

@Entity(tableName = "historical_days")
data class HistoricalDay(
    @PrimaryKey val dateString: String,
    val socialProportion: Float, // 0.0 to 1.0 (e.g., 0.45 = 45%)
    val dopaminePutamen: Float,  // Standardized dopamine synthesis capacity in putamen (e.g., -2.0 to 2.0)
    val totalTouches: Int,
    val interactionVigorMs: Int, // 25th percentile in ms
    val isUserRecorded: Boolean = false
)

@Entity(tableName = "dopamine_spikes")
data class DopamineSpike(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val appName: String,
    val touchCount: Int,
    val durationSeconds: Int,
    val avgIntervalMs: Int,
    val intensity: Float, // 0.0f to 1.0f
    val triggerType: String // e.g. "Rapid Social Scroll", "Burst Messaging", "Compulsive Feed Refresh"
)

@Entity(tableName = "daily_summaries")
data class DailySummary(
    @PrimaryKey val dateString: String,
    val totalInteractions: Int,
    val socialInteractions: Int,
    val peakHour: Int,
    val dopamineSpikeCount: Int,
    val highestSpikeIntensity: Float
)

@Entity(tableName = "app_usage_records")
data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val isSocial: Boolean,
    val interactionCount: Int = 0,
    val detectedVia: String = "USAGE_STATS"
)
