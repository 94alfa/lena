package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionDao {

    @Query("SELECT * FROM interaction_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<InteractionEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: InteractionEvent)

    @Query("DELETE FROM interaction_events")
    suspend fun clearAllEvents()

    // App categories
    @Query("SELECT * FROM app_categories ORDER BY appName ASC")
    fun getAllCategoriesFlow(): Flow<List<AppCategory>>

    @Query("SELECT * FROM app_categories")
    suspend fun getAllCategories(): List<AppCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<AppCategory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: AppCategory)

    // Historical days (the study plot data points)
    @Query("SELECT * FROM historical_days ORDER BY dateString ASC")
    fun getHistoricalDays(): Flow<List<HistoricalDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricalDays(days: List<HistoricalDay>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricalDay(day: HistoricalDay)

    @Query("DELETE FROM historical_days WHERE isUserRecorded = 1")
    suspend fun clearUserHistoricalDays()

    // Dopamine spikes
    @Query("SELECT * FROM dopamine_spikes ORDER BY timestamp DESC")
    fun getAllSpikes(): Flow<List<DopamineSpike>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpike(spike: DopamineSpike)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpikes(spikes: List<DopamineSpike>)

    @Query("DELETE FROM dopamine_spikes")
    suspend fun clearAllSpikes()

    // Daily summaries
    @Query("SELECT * FROM daily_summaries ORDER BY dateString DESC")
    fun getAllSummaries(): Flow<List<DailySummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: DailySummary)

    @Query("DELETE FROM daily_summaries")
    suspend fun clearAllSummaries()

    // Third-party App Usage Records from UsageStatsManager
    @Query("SELECT * FROM app_usage_records ORDER BY startTime DESC")
    fun getAllUsageRecords(): Flow<List<AppUsageRecord>>

    @Query("SELECT * FROM app_usage_records ORDER BY startTime DESC LIMIT :limit")
    fun getRecentUsageRecords(limit: Int): Flow<List<AppUsageRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecord(record: AppUsageRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecords(records: List<AppUsageRecord>)

    @Query("DELETE FROM app_usage_records")
    suspend fun clearAllUsageRecords()

    @Query("SELECT MAX(endTime) FROM app_usage_records")
    suspend fun getLatestUsageEndTime(): Long?
}
