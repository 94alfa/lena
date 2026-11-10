package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppCategory
import com.example.data.database.AppUsageRecord
import com.example.data.database.HistoricalDay
import com.example.ui.components.AppFrequencyBreakdown
import com.example.ui.components.DopaAmberNoticeCard
import com.example.ui.components.DopaCard
import com.example.ui.components.DopaDisclaimerFooter
import com.example.ui.components.DopaMetricCard
import com.example.ui.components.DopaPrimaryButton
import com.example.ui.components.DopaSecondaryButton
import com.example.ui.components.DopaTextButton
import com.example.ui.components.DopamineSpikesFeed
import com.example.ui.components.RechartsDailyFrequencyChart
import com.example.ui.theme.LocalExtraColors
import com.example.ui.viewmodel.DopaViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MainDashboard(viewModel: DopaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isServiceActive by viewModel.isServiceActive.collectAsState()
    val isUsageAccessGranted by viewModel.isUsageAccessGranted.collectAsState()
    val activeForegroundApp by viewModel.activeForegroundApp.collectAsState()
    val recentUsageRecords by viewModel.recentUsageRecords.collectAsState()

    val metrics by viewModel.dailyMetrics.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val historicalDays by viewModel.historicalDays.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val hourlyFrequencies by viewModel.hourlyFrequencies.collectAsState()
    val allSpikes by viewModel.allSpikes.collectAsState()
    val appFrequencies by viewModel.appFrequencies.collectAsState()
    val selectedHour by viewModel.selectedHour.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val extra = LocalExtraColors.current
    val hasSpikes = allSpikes.isNotEmpty()

    val showNavigationMenu = currentScreen == Screen.DASHBOARD ||
            currentScreen == Screen.SCIENTIFIC_PLOT ||
            currentScreen == Screen.CATEGORIES ||
            currentScreen == Screen.SIMULATOR ||
            currentScreen == Screen.SETTINGS

    if (drawerState.isOpen) {
        BackHandler {
            scope.launch { drawerState.close() }
        }
    } else if (currentScreen != Screen.DASHBOARD && currentScreen != Screen.SPLASH && currentScreen != Screen.ONBOARDING) {
        BackHandler {
            viewModel.navigateTo(Screen.DASHBOARD)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showNavigationMenu,
        drawerContent = {
            if (showNavigationMenu) {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    drawerContentColor = MaterialTheme.colorScheme.onSurface,
                    drawerTonalElevation = 0.dp,
                    drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                    modifier = Modifier
                        .width(288.dp)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Cabeçalho Minimalista da Cortina
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val primaryColor = MaterialTheme.colorScheme.primary
                                    Canvas(modifier = Modifier.size(24.dp)) {
                                        drawArc(
                                            color = primaryColor,
                                            startAngle = 40f,
                                            sweepAngle = 276f,
                                            useCenter = false,
                                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        drawCircle(
                                            color = primaryColor,
                                            radius = 3.dp.toPx(),
                                            center = Offset(size.width / 2f, size.height / 2f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "lena",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 22.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(
                                    onClick = { scope.launch { drawerState.close() } },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Fechar menu",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))

                            val itemColors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedContainerColor = Color.Transparent,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface
                            )

                            NavigationDrawerItem(
                                label = { Text("Início", style = MaterialTheme.typography.labelLarge) },
                                selected = currentScreen == Screen.DASHBOARD,
                                onClick = {
                                    viewModel.navigateTo(Screen.DASHBOARD)
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_dashboard")
                            )

                            NavigationDrawerItem(
                                label = { Text("Estudo", style = MaterialTheme.typography.labelLarge) },
                                selected = currentScreen == Screen.SCIENTIFIC_PLOT,
                                onClick = {
                                    viewModel.navigateTo(Screen.SCIENTIFIC_PLOT)
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Filled.Analytics, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_scientific_plot")
                            )

                            NavigationDrawerItem(
                                label = { Text("Aplicações", style = MaterialTheme.typography.labelLarge) },
                                selected = currentScreen == Screen.CATEGORIES,
                                onClick = {
                                    viewModel.navigateTo(Screen.CATEGORIES)
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Filled.Category, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_categories")
                            )

                            NavigationDrawerItem(
                                label = { Text("Simulador", style = MaterialTheme.typography.labelLarge) },
                                selected = currentScreen == Screen.SIMULATOR,
                                onClick = {
                                    viewModel.navigateTo(Screen.SIMULATOR)
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Filled.TouchApp, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_simulator")
                            )

                            NavigationDrawerItem(
                                label = { Text("Ajustes", style = MaterialTheme.typography.labelLarge) },
                                selected = currentScreen == Screen.SETTINGS,
                                onClick = {
                                    viewModel.navigateTo(Screen.SETTINGS)
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = itemColors,
                                modifier = Modifier.testTag("nav_settings")
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(12.dp))

                            NavigationDrawerItem(
                                label = { Text("Pausa de 25 min", style = MaterialTheme.typography.labelLarge) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    viewModel.startPhoneBreak(25)
                                },
                                icon = { Icon(Icons.Filled.HourglassTop, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = itemColors
                            )

                            NavigationDrawerItem(
                                label = { Text("Foco essencial", style = MaterialTheme.typography.labelLarge) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    viewModel.navigateTo(Screen.ESSENTIAL_FOCUS)
                                },
                                icon = { Icon(Icons.Filled.Mail, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = itemColors
                            )
                        }

                        Text(
                            text = "Consciência & Equilíbrio Digital",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showNavigationMenu) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("btn_open_curtain_menu")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Abrir menu",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                val screenTitle = when (currentScreen) {
                                    Screen.DASHBOARD -> "lena"
                                    Screen.SCIENTIFIC_PLOT -> "Estudo"
                                    Screen.CATEGORIES -> "Aplicações"
                                    Screen.SIMULATOR -> "Simulador"
                                    Screen.SETTINGS -> "Ajustes"
                                    else -> "lena"
                                }

                                Text(
                                    text = screenTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 20.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            // Indicador discreto de estado
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (hasSpikes) extra.warning else extra.success,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (hasSpikes) "Uso acelerado" else "Ritmo calmo",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (showNavigationMenu) innerPadding else PaddingValues(0.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    Screen.SPLASH -> SplashScreen(viewModel)
                    Screen.ONBOARDING -> OnboardingScreen(viewModel)
                    Screen.PHONE_BREAK -> PhoneBreakScreen(viewModel)
                    Screen.ESSENTIAL_FOCUS -> EssentialFocusScreen(viewModel)
                    Screen.DASHBOARD -> DashboardContentScreen(
                        viewModel = viewModel,
                        hourlyFrequencies = hourlyFrequencies,
                        allSpikes = allSpikes,
                        appFrequencies = appFrequencies,
                        recentUsageRecords = recentUsageRecords,
                        metrics = metrics,
                        selectedHour = selectedHour,
                        isServiceActive = isServiceActive,
                        isUsageAccessGranted = isUsageAccessGranted,
                        activeForegroundApp = activeForegroundApp
                    )
                    Screen.SCIENTIFIC_PLOT -> ScientificPlotScreen(
                        viewModel = viewModel,
                        historicalDays = historicalDays,
                        currentMetrics = metrics,
                        isServiceActive = isServiceActive
                    )
                    Screen.CATEGORIES -> CategoriesScreen(viewModel, installedApps)
                    Screen.SIMULATOR -> SimulatorScreen(viewModel, metrics, categories)
                    Screen.SETTINGS -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

/**
 * Ecrã Início Minimalista:
 * 1. Estado do dia
 * 2. Recomendação de pausa (1 ação principal por ecrã)
 * 3. 3 Métricas essenciais (número grande + rótulo 14sp)
 * 4. Gráfico horário 24h com eixos legíveis
 * 5. Resumo de aplicações
 */
@Composable
fun DashboardContentScreen(
    viewModel: DopaViewModel,
    hourlyFrequencies: List<com.example.data.repository.HourlyFrequency>,
    allSpikes: List<com.example.data.database.DopamineSpike>,
    appFrequencies: List<com.example.data.repository.AppFrequency>,
    recentUsageRecords: List<AppUsageRecord>,
    metrics: com.example.data.repository.DailyMetrics,
    selectedHour: Int?,
    isServiceActive: Boolean,
    isUsageAccessGranted: Boolean,
    activeForegroundApp: Pair<String, String>?
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val spikeCount = allSpikes.size
    val hasSpikes = spikeCount > 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Estado do Dia
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (hasSpikes) "Hoje: uso um pouco acelerado" else "Hoje: ritmo calmo e equilibrado",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    lineHeight = 34.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 2. Cartão Âmbar de Recomendação de Pausa (Único botão primário do ecrã)
        item {
            DopaAmberNoticeCard(
                title = "Que tal uma pausa de 25 minutos?",
                description = if (hasSpikes)
                    "Os teus toques estão mais rápidos que o habitual."
                else
                    "O teu ritmo está estável. Uma pausa programada ajuda a preservar a clareza mental.",
                actionText = "Iniciar pausa de 25 min",
                onActionClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.startPhoneBreak(25)
                },
                secondaryActionText = "Ver apenas e-mails e mensagens essenciais",
                onSecondaryActionClick = {
                    viewModel.navigateTo(Screen.ESSENTIAL_FOCUS)
                }
            )
        }

        // 3. Métricas Essenciais (Número grande + rótulo 14sp. Nada mais.)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DopaMetricCard(
                    value = "${metrics.totalTaps}",
                    label = "Toques",
                    modifier = Modifier.weight(1f)
                )

                val peakHour = remember(hourlyFrequencies) {
                    hourlyFrequencies.maxByOrNull { it.totalCount }?.hour ?: 0
                }
                DopaMetricCard(
                    value = String.format(Locale.getDefault(), "%02dh", peakHour),
                    label = "Hora de pico",
                    modifier = Modifier.weight(1f)
                )

                DopaMetricCard(
                    value = "$spikeCount",
                    label = "Picos rápidos",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Gráfico Horário 24h
        item {
            RechartsDailyFrequencyChart(
                hourlyFrequencies = hourlyFrequencies,
                selectedHour = selectedHour,
                onSelectHour = { hour ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.selectHour(hour)
                }
            )
        }

        // 5. Aplicações mais usadas + Sincronização UsageStats integrada
        item {
            AppFrequencyBreakdown(
                appFrequencies = appFrequencies,
                headerAction = {
                    if (!isUsageAccessGranted) {
                        DopaTextButton(
                            text = "Ativar acesso",
                            onClick = {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            },
                            testTag = "btn_grant_usagestats"
                        )
                    } else {
                        DopaTextButton(
                            text = "Atualizar",
                            onClick = { viewModel.syncUsageStats() },
                            testTag = "btn_sync_usagestats"
                        )
                    }
                }
            )
        }

        // 6. Picos rápidos (exibido apenas quando existem picos registados)
        if (allSpikes.isNotEmpty()) {
            item {
                DopamineSpikesFeed(allSpikes = allSpikes)
            }
        }

        // 7. Rodapé Ético
        item {
            DopaDisclaimerFooter()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Ecrã 2: Estudo Comportamental Minimalista
 */
@Composable
fun ScientificPlotScreen(
    viewModel: DopaViewModel,
    historicalDays: List<HistoricalDay>,
    currentMetrics: com.example.data.repository.DailyMetrics,
    isServiceActive: Boolean
) {
    var dayLabel by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            FocusCadenceBarometer(
                socialProportion = currentMetrics.socialProportion
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DopaMetricCard(
                    value = "${currentMetrics.totalTaps}",
                    label = "Toques hoje",
                    modifier = Modifier.weight(1f)
                )

                DopaMetricCard(
                    value = String.format(Locale.getDefault(), "%.0f%%", currentMetrics.socialProportion * 100),
                    label = "Proporção social",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            ScientificScatterPlot(
                historicalDays = historicalDays,
                currentSocialProportion = currentMetrics.socialProportion,
                currentTouches = currentMetrics.totalTaps
            )
        }

        item {
            DopaCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Guardar registo do dia",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dayLabel,
                        onValueChange = { dayLabel = it },
                        placeholder = { Text("Ex: Segunda-feira", fontSize = 15.sp) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_day_label")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DopaSecondaryButton(
                            text = "Limpar",
                            onClick = {
                                viewModel.clearCurrentSession()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_clear_session"
                        )

                        DopaPrimaryButton(
                            text = "Guardar dia",
                            onClick = {
                                if (dayLabel.trim().isNotEmpty() && currentMetrics.totalTaps > 0) {
                                    viewModel.saveSessionToHistory(dayLabel.trim())
                                    dayLabel = ""
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            enabled = dayLabel.trim().isNotEmpty() && currentMetrics.totalTaps > 0,
                            modifier = Modifier.weight(1.4f),
                            testTag = "btn_save_session"
                        )
                    }
                }
            }
        }

        item {
            DopaDisclaimerFooter()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Índice de Foco Estimado (Display 48sp tnum + escala limpa)
 */
@Composable
fun FocusCadenceBarometer(
    socialProportion: Float
) {
    val extra = LocalExtraColors.current
    val focusPercent = (100 - (socialProportion * 100).toInt()).coerceIn(0, 100)

    DopaCard {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Índice de foco estimado",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$focusPercent%",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 48.sp,
                    lineHeight = 56.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(focusPercent / 100f)
                        .height(8.dp)
                        .background(
                            if (focusPercent >= 50) MaterialTheme.colorScheme.primary else extra.warning,
                            RoundedCornerShape(4.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "0% Disperso",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "100% Foco estável",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Gráfico de Dispersão com Eixos X e Y Legíveis
 */
@Composable
fun ScientificScatterPlot(
    historicalDays: List<HistoricalDay>,
    currentSocialProportion: Float,
    currentTouches: Int
) {
    val extra = LocalExtraColors.current

    DopaCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Proporção social vs. foco",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Histórico de sessões e posição de hoje",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Eixo Y
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 22.dp, end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Alto", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Médio", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Baixo", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val gridColor = extra.chartSecondary.copy(alpha = 0.25f)
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, h / 2f),
                                end = Offset(w, h / 2f),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )

                            // Linha de tendência
                            drawLine(
                                color = extra.chartSecondary.copy(alpha = 0.5f),
                                start = Offset(w * 0.08f, h * 0.2f),
                                end = Offset(w * 0.92f, h * 0.85f),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )

                            // Dias anteriores
                            historicalDays.forEach { day ->
                                val x = (day.socialProportion.coerceIn(0f, 1f) * w)
                                val normalizedScore = ((day.dopaminePutamen + 2f) / 4f).coerceIn(0f, 1f)
                                val y = h * (1f - normalizedScore)

                                drawCircle(
                                    color = extra.chartSecondary,
                                    radius = 4.5.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }

                            // Hoje (Teal)
                            if (currentTouches > 0) {
                                val liveX = (currentSocialProportion.coerceIn(0f, 1f) * w)
                                val liveScore = (-3.0f * currentSocialProportion + 0.8f).coerceIn(-2.0f, 2.0f)
                                val normalizedLive = ((liveScore + 2f) / 4f).coerceIn(0f, 1f)
                                val liveY = h * (1f - normalizedLive)

                                drawCircle(
                                    color = extra.chartPrimary.copy(alpha = 0.22f),
                                    radius = 11.dp.toPx(),
                                    center = Offset(liveX, liveY)
                                )
                                drawCircle(
                                    color = extra.chartPrimary,
                                    radius = 5.5.dp.toPx(),
                                    center = Offset(liveX, liveY)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0% social", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("50%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("100% social", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * Ecrã 3: Aplicações Instaladas
 */
@Composable
fun CategoriesScreen(viewModel: DopaViewModel, installedApps: List<com.example.ui.viewmodel.InstalledApp>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Selecione as aplicações de redes sociais para monitorizar o ritmo de toques.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (installedApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A carregar aplicações instaladas...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(installedApps, key = { it.packageName }) { app ->
                DopaCard(
                    padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            AppIcon(
                                packageName = app.packageName,
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (app.isSocial) "Rede social" else "Utilitário",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = if (app.isSocial) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = app.isSocial,
                            onCheckedChange = { isChecked ->
                                viewModel.setAppCategory(app.packageName, app.appName, isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("switch_${app.packageName}")
                        )
                    }
                }
            }
        }

        item {
            DopaDisclaimerFooter()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Ecrã 4: Simulador de Eventos Minimalista
 */
@Composable
fun SimulatorScreen(
    viewModel: DopaViewModel,
    metrics: com.example.data.repository.DailyMetrics,
    categories: List<AppCategory>
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DopaMetricCard(
                    value = "${metrics.totalTaps}",
                    label = "Toques",
                    modifier = Modifier.weight(1f)
                )
                DopaMetricCard(
                    value = "${metrics.socialTaps}",
                    label = "Toques sociais",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            DopaCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Testar padrão de uso",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gere interações simuladas para pré-visualizar o comportamento dos gráficos e alertas de pausa.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    DopaPrimaryButton(
                        text = "Simular pico rápido (Instagram)",
                        icon = Icons.Filled.Bolt,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.simulateDopamineSpike("Instagram")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_simulate_instagram_spike"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DopaSecondaryButton(
                            text = "Instagram 15m",
                            onClick = {
                                viewModel.simulateThirdPartyAppUsage(
                                    packageName = "com.instagram.android",
                                    appName = "Instagram",
                                    durationSeconds = 900,
                                    isSocial = true
                                )
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_sim_ig_15m"
                        )

                        DopaSecondaryButton(
                            text = "WhatsApp 5m",
                            onClick = {
                                viewModel.simulateThirdPartyAppUsage(
                                    packageName = "com.whatsapp",
                                    appName = "WhatsApp",
                                    durationSeconds = 300,
                                    isSocial = false
                                )
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_sim_wa_5m"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DopaSecondaryButton(
                            text = "Sincronizar",
                            icon = Icons.Filled.Sync,
                            onClick = { viewModel.syncUsageStats() },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_sim_sync"
                        )

                        DopaSecondaryButton(
                            text = "Limpar hoje",
                            icon = Icons.Filled.Refresh,
                            onClick = { viewModel.clearCurrentSession() },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_sim_clear"
                        )
                    }
                }
            }
        }

        item {
            DopaDisclaimerFooter()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Carregador Nativo de Ícone da Aplicação via PackageManager
 */
@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            android.widget.ImageView(ctx).apply {
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            }
        },
        modifier = modifier,
        update = { imageView ->
            try {
                val pm = context.packageManager
                val icon = pm.getApplicationIcon(packageName)
                imageView.setImageDrawable(icon)
            } catch (e: Exception) {
                try {
                    imageView.setImageDrawable(context.packageManager.defaultActivityIcon)
                } catch (ex: Exception) {
                    imageView.setImageDrawable(null)
                }
            }
        }
    )
}
