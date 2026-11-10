package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppCategory
import com.example.data.database.AppDatabase
import com.example.data.database.AppUsageRecord
import com.example.data.database.DopamineSpike
import com.example.data.database.HistoricalDay
import com.example.data.database.InteractionEvent
import com.example.data.repository.AppFrequency
import com.example.data.repository.DailyMetrics
import com.example.data.repository.HourlyFrequency
import com.example.data.repository.InteractionRepository
import com.example.data.usagestats.UsageStatsTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isSocial: Boolean
)

data class EssentialTask(
    val id: String,
    val title: String,
    val category: String, // "EMAIL" or "MESSAGING"
    val isDone: Boolean = false,
    val appTarget: String? = null
)

class DopaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InteractionRepository
    private val prefs = application.getSharedPreferences("dopatap_prefs", Context.MODE_PRIVATE)

    // Raw database state flows
    val allEvents: StateFlow<List<InteractionEvent>>
    val allCategories: StateFlow<List<AppCategory>>
    val historicalDays: StateFlow<List<HistoricalDay>>
    val allSpikes: StateFlow<List<DopamineSpike>>

    // Third-party app usage records from UsageStatsManager persisted in Room
    val allUsageRecords: StateFlow<List<AppUsageRecord>>
    val recentUsageRecords: StateFlow<List<AppUsageRecord>>

    // Device installed launcher apps state
    private val _installedAppInfos = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>>

    // Combined metric state for the current session
    val dailyMetrics: StateFlow<DailyMetrics>

    // Hourly interaction frequencies for Recharts visualization
    val hourlyFrequencies: StateFlow<List<HourlyFrequency>>

    // Per-application interaction distribution
    val appFrequencies: StateFlow<List<AppFrequency>>

    // Permissions: Accessibility Service & UsageStats
    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive = _isServiceActive.asStateFlow()

    private val _isUsageAccessGranted = MutableStateFlow(false)
    val isUsageAccessGranted = _isUsageAccessGranted.asStateFlow()

    // Real-time active foreground app detected via UsageStatsManager
    private val _activeForegroundApp = MutableStateFlow<Pair<String, String>?>(null)
    val activeForegroundApp = _activeForegroundApp.asStateFlow()

    // Onboarding status
    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean("has_completed_onboarding", false))
    val hasCompletedOnboarding = _hasCompletedOnboarding.asStateFlow()

    // Navigation Screen State (Starts with Splash)
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen = _currentScreen.asStateFlow()

    // Selected hour for interactive inspection on Recharts graph
    private val _selectedHour = MutableStateFlow<Int?>(null)
    val selectedHour = _selectedHour.asStateFlow()

    // Break from Phone (Digital Detox / Leave Phone)
    private val _breakDurationMinutes = MutableStateFlow(25)
    val breakDurationMinutes = _breakDurationMinutes.asStateFlow()

    private val _breakSecondsRemaining = MutableStateFlow(25 * 60)
    val breakSecondsRemaining = _breakSecondsRemaining.asStateFlow()

    private val _isBreakActive = MutableStateFlow(false)
    val isBreakActive = _isBreakActive.asStateFlow()

    private var breakJob: Job? = null
    private var realTimeUsageMonitorJob: Job? = null

    // Essential Focus Tasks (Emails & Important Messaging only)
    private val _essentialTasks = MutableStateFlow(
        listOf(
            EssentialTask("1", "Responder e-mails urgentes de trabalho", "EMAIL", isDone = false, appTarget = "Gmail"),
            EssentialTask("2", "Escrever mensagem de confirmação para a família", "MESSAGING", isDone = false, appTarget = "WhatsApp"),
            EssentialTask("3", "Checar SMS de autenticação bancária / códigos", "MESSAGING", isDone = false, appTarget = "Mensagens")
        )
    )
    val essentialTasks = _essentialTasks.asStateFlow()

    // Temporary input state for adding custom apps
    private val _customAppName = MutableStateFlow("")
    val customAppName = _customAppName.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = InteractionRepository(database.interactionDao())

        // Initialize state flows
        allEvents = repository.allEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allCategories = repository.allCategories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        historicalDays = repository.historicalDays.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSpikes = repository.allSpikes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allUsageRecords = repository.allUsageRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        recentUsageRecords = repository.recentUsageRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize installedApps combining app info from PackageManager and database category status
        installedApps = combine(_installedAppInfos, allCategories) { appInfos, categories ->
            appInfos.map { (packageName, appName) ->
                val existingCategory = categories.find { it.appName.equals(appName, ignoreCase = true) }
                val isSocial = existingCategory?.isSocial ?: false
                InstalledApp(packageName, appName, isSocial)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Load installed applications asynchronously
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val pm = context.packageManager
                
                val installedAppsList = pm.getInstalledApplications(0)
                var filteredList = installedAppsList.mapNotNull { appInfo ->
                    val packageName = appInfo.packageName
                    val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                    
                    val isInternalSystem = packageName.startsWith("android") ||
                            packageName.startsWith("com.android.internal") ||
                            packageName.startsWith("com.android.providers") ||
                            packageName.startsWith("com.android.systemui") ||
                            packageName.startsWith("com.android.inputmethod") ||
                            packageName.startsWith("com.android.keyguard") ||
                            packageName.startsWith("com.google.android.ext") ||
                            packageName.startsWith("com.google.android.gms")
                    
                    if (!isSystem && !isInternalSystem) {
                        val appLabel = appInfo.loadLabel(pm).toString()
                        if (packageName.isNotEmpty() && appLabel.isNotEmpty()) {
                            Pair(packageName, appLabel)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }.distinctBy { it.first }

                if (filteredList.isEmpty()) {
                    filteredList = listOf(
                        Pair("com.whatsapp", "WhatsApp"),
                        Pair("com.instagram.android", "Instagram"),
                        Pair("com.zhiliaoapp.musically", "TikTok"),
                        Pair("com.facebook.katana", "Facebook"),
                        Pair("com.android.chrome", "Chrome"),
                        Pair("com.google.android.youtube", "YouTube")
                    )
                }
                
                val sortedList = filteredList.sortedBy { it.second.lowercase() }
                _installedAppInfos.value = sortedList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Compute metrics reactively
        dailyMetrics = allEvents.combine(allCategories) { events, _ ->
            repository.calculateMetrics(events)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyMetrics(0, 0, 0.0f, 0, 0.0f)
        )

        // Hourly interaction frequencies combined with detected dopamine spikes
        hourlyFrequencies = combine(allEvents, allSpikes) { events, spikes ->
            repository.calculateHourlyFrequencies(events, spikes)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.calculateHourlyFrequencies(emptyList(), emptyList())
        )

        // Per-app distribution
        appFrequencies = allEvents.combine(allCategories) { events, _ ->
            repository.calculateAppFrequencies(events)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Pre-populate baseline study and sample data
        viewModelScope.launch {
            repository.initializeBaselineDataIfNeeded()
        }

        updateServiceStatus()
        startRealTimeUsageMonitoring()
    }

    /**
     * Transition from Splash screen after brief presentation
     */
    fun onSplashFinished() {
        if (_hasCompletedOnboarding.value) {
            _currentScreen.value = Screen.DASHBOARD
        } else {
            _currentScreen.value = Screen.ONBOARDING
        }
    }

    /**
     * Completes first-time onboarding and saves to SharedPreferences
     */
    fun completeOnboarding() {
        prefs.edit().putBoolean("has_completed_onboarding", true).apply()
        _hasCompletedOnboarding.value = true
        _currentScreen.value = Screen.DASHBOARD
    }

    /**
     * Restarts the onboarding guide on demand
     */
    fun openOnboardingGuide() {
        _currentScreen.value = Screen.ONBOARDING
    }

    /**
     * Checks if accessibility service and UsageStats access are enabled
     */
    fun updateServiceStatus() {
        val context = getApplication<Application>().applicationContext
        val serviceName = "${context.packageName}/com.example.service.TapAccessibilityService"
        var isEnabled = false

        try {
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            )
            if (accessibilityEnabled == 1) {
                val settingValue = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                if (settingValue != null) {
                    val splitter = TextUtils.SimpleStringSplitter(':')
                    splitter.setString(settingValue)
                    while (splitter.hasNext()) {
                        val accessService = splitter.next()
                        if (accessService.equals(serviceName, ignoreCase = true)) {
                            isEnabled = true
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _isServiceActive.value = isEnabled

        // Verify UsageStatsManager permission
        val hasUsage = UsageStatsTracker.hasUsageAccessPermission(context)
        _isUsageAccessGranted.value = hasUsage
        if (hasUsage) {
            syncUsageStats()
        }
    }

    /**
     * Opens the Usage Access Settings screen for the user
     */
    fun requestUsageAccess() {
        UsageStatsTracker.openUsageAccessSettings(getApplication<Application>().applicationContext)
    }

    /**
     * Syncs UsageStats from Android system for the current day into the Room database.
     */
    fun syncUsageStats() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            if (!UsageStatsTracker.hasUsageAccessPermission(context)) return@launch

            val now = System.currentTimeMillis()
            val startOfDay = now - 24 * 3600 * 1000L // Last 24 hours
            val categories = allCategories.value

            val records = UsageStatsTracker.queryUsageSessions(context, startOfDay, now, categories)
            if (records.isNotEmpty()) {
                repository.insertUsageRecords(records)

                // Convert records into interaction events for Recharts graph hydration
                val interactionEvents = UsageStatsTracker.convertRecordsToInteractionEvents(records)
                for (event in interactionEvents) {
                    repository.insertEvent(event)
                }

                // Detect dopamine spikes from newly loaded events
                val allEv = repository.allEvents
            }
        }
    }

    /**
     * Starts continuous real-time foreground app monitoring (polls every 6 seconds)
     */
    private fun startRealTimeUsageMonitoring() {
        realTimeUsageMonitorJob?.cancel()
        realTimeUsageMonitorJob = viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            while (isActive) {
                if (UsageStatsTracker.hasUsageAccessPermission(context)) {
                    val activeApp = UsageStatsTracker.queryCurrentForegroundApp(context)
                    _activeForegroundApp.value = activeApp

                    // If an active app is detected in foreground, log a touch interaction
                    if (activeApp != null && activeApp.first != context.packageName) {
                        val categories = allCategories.value
                        val cat = categories.find { it.appName.equals(activeApp.second, ignoreCase = true) }
                        val isSocial = cat?.isSocial ?: (activeApp.second.contains("Instagram", true) ||
                                activeApp.second.contains("TikTok", true) ||
                                activeApp.second.contains("WhatsApp", true))

                        repository.insertEvent(
                            InteractionEvent(
                                timestamp = System.currentTimeMillis(),
                                type = "TOUCH",
                                appName = activeApp.second,
                                category = if (isSocial) "SOCIAL" else "OTHER"
                            )
                        )
                    }
                }
                delay(6000L)
            }
        }
    }

    /**
     * Simulates third-party app usage session (for testing without device settings)
     */
    fun simulateThirdPartyAppUsage(
        packageName: String,
        appName: String,
        durationSeconds: Long,
        isSocial: Boolean
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val start = now - (durationSeconds * 1000L)

            val record = AppUsageRecord(
                packageName = packageName,
                appName = appName,
                startTime = start,
                endTime = now,
                durationSeconds = durationSeconds,
                isSocial = isSocial,
                interactionCount = (durationSeconds / 3).toInt().coerceAtLeast(1),
                detectedVia = "USAGE_STATS_SIMULATED"
            )
            repository.insertUsageRecord(record)

            // Also record corresponding interaction events and spike if applicable
            val events = UsageStatsTracker.convertRecordsToInteractionEvents(listOf(record))
            for (e in events) {
                repository.insertEvent(e)
            }

            if (isSocial && durationSeconds >= 120) {
                val spike = DopamineSpike(
                    timestamp = start,
                    appName = appName,
                    touchCount = (durationSeconds / 2).toInt().coerceIn(15, 60),
                    durationSeconds = durationSeconds.toInt(),
                    avgIntervalMs = 230,
                    intensity = 0.85f,
                    triggerType = "Sessão Contínua de Rede Social ($appName)"
                )
                repository.insertSpike(spike)
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectHour(hour: Int?) {
        _selectedHour.value = if (_selectedHour.value == hour) null else hour
    }

    // Phone Break Timer (Digital Detox)
    fun startPhoneBreak(minutes: Int) {
        _breakDurationMinutes.value = minutes
        _breakSecondsRemaining.value = minutes * 60
        _isBreakActive.value = true
        _currentScreen.value = Screen.PHONE_BREAK

        breakJob?.cancel()
        breakJob = viewModelScope.launch {
            while (_breakSecondsRemaining.value > 0 && _isBreakActive.value) {
                delay(1000L)
                _breakSecondsRemaining.value = _breakSecondsRemaining.value - 1
            }
            _isBreakActive.value = false
        }
    }

    fun startBreakTimer() {
        _isBreakActive.value = true
        breakJob?.cancel()
        breakJob = viewModelScope.launch {
            while (_breakSecondsRemaining.value > 0 && _isBreakActive.value) {
                delay(1000L)
                _breakSecondsRemaining.value = _breakSecondsRemaining.value - 1
            }
            _isBreakActive.value = false
        }
    }

    fun pauseBreakTimer() {
        _isBreakActive.value = false
        breakJob?.cancel()
    }

    fun stopBreakTimer() {
        cancelPhoneBreak()
    }

    fun cancelPhoneBreak() {
        breakJob?.cancel()
        _isBreakActive.value = false
        _currentScreen.value = Screen.DASHBOARD
    }

    fun setAppCategory(packageName: String, appName: String, isSocial: Boolean) {
        viewModelScope.launch {
            repository.insertCategory(AppCategory(appName, isSocial))
        }
    }

    // Essential Tasks Toggle
    fun toggleEssentialTask(taskId: String) {
        _essentialTasks.value = _essentialTasks.value.map { task ->
            if (task.id == taskId) task.copy(isDone = !task.isDone) else task
        }
    }

    fun addEssentialTask(title: String, category: String) {
        if (title.isNotBlank()) {
            val newTask = EssentialTask(
                id = System.currentTimeMillis().toString(),
                title = title.trim(),
                category = category,
                isDone = false
            )
            _essentialTasks.value = _essentialTasks.value + newTask
        }
    }

    /**
     * Records a touch event contextually
     */
    fun recordTouch(appName: String) {
        viewModelScope.launch {
            val categories = allCategories.value
            val appCategory = categories.find { it.appName.equals(appName, ignoreCase = true) }
            val isSocial = appCategory?.isSocial ?: false
            val categoryString = if (isSocial) "SOCIAL" else "OTHER"

            repository.insertEvent(
                InteractionEvent(
                    type = "TOUCH",
                    appName = appName,
                    category = categoryString
                )
            )
        }
    }

    /**
     * Simulates a concentrated dopamine spike burst (e.g. 24 fast touches in TikTok or Instagram)
     */
    fun simulateDopamineSpike(appName: String) {
        viewModelScope.launch {
            val categories = allCategories.value
            val appCategory = categories.find { it.appName.equals(appName, ignoreCase = true) }
            val isSocial = appCategory?.isSocial ?: true
            val now = System.currentTimeMillis()

            val burstCount = 24
            val avgInterval = 210L
            val events = mutableListOf<InteractionEvent>()

            for (i in 0 until burstCount) {
                events.add(
                    InteractionEvent(
                        timestamp = now - (burstCount - i) * avgInterval,
                        type = "TOUCH",
                        appName = appName,
                        category = if (isSocial) "SOCIAL" else "OTHER"
                    )
                )
            }

            for (e in events) {
                repository.insertEvent(e)
            }

            val spike = DopamineSpike(
                timestamp = now - (burstCount * avgInterval),
                appName = appName,
                touchCount = burstCount,
                durationSeconds = ((burstCount * avgInterval) / 1000).toInt().coerceAtLeast(1),
                avgIntervalMs = avgInterval.toInt(),
                intensity = if (isSocial) 0.88f else 0.65f,
                triggerType = if (isSocial) "Rolagem Compulsiva / Reels" else "Burst de Interação Rápida"
            )
            repository.insertSpike(spike)
        }
    }

    /**
     * Simulates a mindful focus session (e.g. 12 spaced touches in Chrome)
     */
    fun simulateFocusSession(appName: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val count = 12
            val interval = 1200L

            for (i in 0 until count) {
                repository.insertEvent(
                    InteractionEvent(
                        timestamp = now - (count - i) * interval,
                        type = "TOUCH",
                        appName = appName,
                        category = "OTHER"
                    )
                )
            }
        }
    }

    /**
     * Reloads sample 24h interaction data with dopamine spikes
     */
    fun loadSampleDay() {
        viewModelScope.launch {
            repository.populateSampleDayData()
        }
    }

    /**
     * Toggles whether an application name is labeled as social
     */
    fun toggleAppSocial(appName: String, currentIsSocial: Boolean) {
        viewModelScope.launch {
            repository.insertCategory(AppCategory(appName, !currentIsSocial))
        }
    }

    /**
     * Saves the current session's calculated proportion as a custom data point on the scientific graph
     */
    fun saveSessionToHistory(label: String) {
        val metrics = dailyMetrics.value
        if (metrics.totalTaps == 0) return

        viewModelScope.launch {
            val predictedDopamine = (-3.2f * metrics.socialProportion + 0.8f + (Math.random().toFloat() * 0.2f - 0.1f))
                .coerceIn(-2.0f, 2.0f)

            repository.insertHistoricalDay(
                HistoricalDay(
                    dateString = label,
                    socialProportion = metrics.socialProportion,
                    dopaminePutamen = predictedDopamine,
                    totalTouches = metrics.totalTaps,
                    interactionVigorMs = metrics.vigorMs,
                    isUserRecorded = true
                )
            )
        }
    }

    /**
     * Erases current raw interaction events, spikes, and usage records
     */
    fun clearCurrentSession() {
        viewModelScope.launch {
            repository.clearAllEvents()
            repository.clearAllSpikes()
            repository.clearAllUsageRecords()
        }
    }

    /**
     * Resets database to standard baselines
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            repository.clearAllEvents()
            repository.clearAllSpikes()
            repository.clearAllUsageRecords()
            repository.clearUserHistoricalDays()
            repository.initializeBaselineDataIfNeeded()
        }
    }

    // Theme and Appearance Settings
    private val _themePreference = MutableStateFlow(prefs.getString("theme_preference", "SYSTEM") ?: "SYSTEM")
    val themePreference = _themePreference.asStateFlow()

    fun setThemePreference(pref: String) {
        _themePreference.value = pref
        prefs.edit().putString("theme_preference", pref).apply()
    }

    // Gentle Notifications Settings
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    // Reset All Data
    fun clearAllData() {
        resetToDefaults()
    }
}

enum class Screen {
    SPLASH,
    ONBOARDING,
    DASHBOARD,
    SCIENTIFIC_PLOT,
    CATEGORIES,
    SIMULATOR,
    SETTINGS,
    PHONE_BREAK,
    ESSENTIAL_FOCUS
}
