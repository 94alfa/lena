package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppCategory
import com.example.data.database.AppDatabase
import com.example.data.database.DopamineSpike
import com.example.data.database.InteractionEvent
import com.example.data.repository.InteractionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: InteractionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = InteractionRepository(db.interactionDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("lena", appName)
    }

    @Test
    fun `test Room schema with interaction events and dopamine spikes`() = runBlocking {
        val event = InteractionEvent(
            type = "TOUCH",
            appName = "TikTok",
            category = "SOCIAL"
        )
        repository.insertEvent(event)

        val events = repository.allEvents.first()
        assertEquals(1, events.size)
        assertEquals("TikTok", events[0].appName)

        val spike = DopamineSpike(
            appName = "TikTok",
            touchCount = 25,
            durationSeconds = 6,
            avgIntervalMs = 210,
            intensity = 0.88f,
            triggerType = "Rolagem Compulsiva / Reels"
        )
        repository.insertSpike(spike)

        val spikes = repository.allSpikes.first()
        assertEquals(1, spikes.size)
        assertEquals(25, spikes[0].touchCount)
        assertTrue(spikes[0].intensity > 0.8f)
    }

    @Test
    fun `test hourly frequency calculation and spike detection`() = runBlocking {
        val now = System.currentTimeMillis()
        val events = mutableListOf<InteractionEvent>()
        for (i in 0 until 15) {
            events.add(
                InteractionEvent(
                    timestamp = now - (15 - i) * 200L,
                    type = "TOUCH",
                    appName = "Instagram",
                    category = "SOCIAL"
                )
            )
        }

        val detectedSpikes = repository.detectDopamineSpikes(events)
        assertTrue("Expected at least 1 dopamine spike detected", detectedSpikes.isNotEmpty())

        val hourly = repository.calculateHourlyFrequencies(events, detectedSpikes)
        assertEquals(24, hourly.size)
        val activeHour = hourly.find { it.totalCount > 0 }
        assertNotNull(activeHour)
        assertEquals("Instagram", activeHour?.topApp)
        assertTrue(activeHour?.hasSpike == true)
    }

    @Test
    fun `test Room schema with AppUsageRecord persistence`() = runBlocking {
        val now = System.currentTimeMillis()
        val record = com.example.data.database.AppUsageRecord(
            packageName = "com.instagram.android",
            appName = "Instagram",
            startTime = now - 900_000L,
            endTime = now,
            durationSeconds = 900,
            isSocial = true,
            interactionCount = 84,
            detectedVia = "USAGE_EVENTS"
        )
        repository.insertUsageRecord(record)

        val records = repository.allUsageRecords.first()
        assertEquals(1, records.size)
        assertEquals("com.instagram.android", records[0].packageName)
        assertEquals("Instagram", records[0].appName)
        assertEquals(900L, records[0].durationSeconds)
        assertTrue(records[0].isSocial)
    }
}
