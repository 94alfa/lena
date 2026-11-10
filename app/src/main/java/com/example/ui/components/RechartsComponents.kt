package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DopamineSpike
import com.example.data.repository.AppFrequency
import com.example.data.repository.HourlyFrequency
import com.example.ui.theme.LocalExtraColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gráfico Horário 24h Minimalista:
 * - Eixos Y e X legíveis com algarismos tabulares (12sp)
 * - Máximo 3 cores: Teal (Total), Cinza-azulado #94A3B8 (Social), Âmbar (Picos)
 * - Toque em qualquer hora exibe o detalhe sem poluir o ecrã
 */
@Composable
fun RechartsDailyFrequencyChart(
    hourlyFrequencies: List<HourlyFrequency>,
    selectedHour: Int?,
    onSelectHour: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalExtraColors.current
    val primaryColor = extra.chartPrimary
    val secondaryColor = extra.chartSecondary
    val warningColor = extra.chartWarning

    val maxCount = remember(hourlyFrequencies) {
        val rawMax = hourlyFrequencies.maxOfOrNull { it.totalCount } ?: 0
        when {
            rawMax <= 20 -> 20
            rawMax <= 50 -> 50
            else -> ((rawMax + 19) / 20) * 20
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Atividade por hora",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChartLegendItem(color = primaryColor, label = "Total")
                    ChartLegendItem(color = secondaryColor, label = "Social")
                    ChartLegendItem(color = warningColor, label = "Pico")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Eixo Y com escala numérica legível
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 24.dp, end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$maxCount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${maxCount / 2}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Área do Gráfico + Eixo X
                Column(modifier = Modifier.weight(1f)) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val widthPx = constraints.maxWidth.toFloat()
                        val heightPx = constraints.maxHeight.toFloat()
                        val topPad = 8.dp.value * 2f
                        val bottomPad = 8.dp.value * 2f
                        val chartH = (heightPx - topPad - bottomPad).coerceAtLeast(1f)

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onSelectHour(null)
                                }
                        ) {
                            val gridColor = secondaryColor.copy(alpha = 0.22f)

                            // 3 linhas horizontais de referência (Topo, Meio, Base)
                            for (i in 0..2) {
                                val y = topPad + (chartH / 2f) * i
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(widthPx, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = if (i < 2) PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) else null
                                )
                            }

                            val stepWidth = widthPx / 24f
                            val areaPath = Path()
                            val linePath = Path()

                            hourlyFrequencies.forEachIndexed { index, freq ->
                                val x = index * stepWidth + stepWidth / 2f
                                val normalized = (freq.totalCount.toFloat() / maxCount).coerceIn(0f, 1f)
                                val y = topPad + chartH * (1f - normalized)

                                if (index == 0) {
                                    areaPath.moveTo(x, y)
                                    linePath.moveTo(x, y)
                                } else {
                                    val prevX = (index - 1) * stepWidth + stepWidth / 2f
                                    val prevNormalized = (hourlyFrequencies[index - 1].totalCount.toFloat() / maxCount).coerceIn(0f, 1f)
                                    val prevY = topPad + chartH * (1f - prevNormalized)
                                    val midX = (prevX + x) / 2f
                                    areaPath.cubicTo(midX, prevY, midX, y, x, y)
                                    linePath.cubicTo(midX, prevY, midX, y, x, y)
                                }
                            }

                            if (hourlyFrequencies.isNotEmpty()) {
                                val lastX = (hourlyFrequencies.size - 1) * stepWidth + stepWidth / 2f
                                val firstX = stepWidth / 2f
                                val bottomY = topPad + chartH

                                areaPath.lineTo(lastX, bottomY)
                                areaPath.lineTo(firstX, bottomY)
                                areaPath.close()

                                drawPath(
                                    path = areaPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.16f),
                                            primaryColor.copy(alpha = 0.0f)
                                        ),
                                        startY = topPad,
                                        endY = bottomY
                                    )
                                )

                                drawPath(
                                    path = linePath,
                                    color = primaryColor,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            hourlyFrequencies.forEachIndexed { index, freq ->
                                val barWidth = stepWidth * 0.42f
                                val x = index * stepWidth + (stepWidth - barWidth) / 2f

                                if (freq.socialCount > 0) {
                                    val normalizedSocial = (freq.socialCount.toFloat() / maxCount).coerceIn(0f, 1f)
                                    val barHeight = chartH * normalizedSocial
                                    val y = topPad + chartH - barHeight

                                    drawRoundRect(
                                        color = secondaryColor.copy(alpha = 0.65f),
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                    )
                                }

                                if (freq.hasSpike) {
                                    val centerX = index * stepWidth + stepWidth / 2f
                                    val normalized = (freq.totalCount.toFloat() / maxCount).coerceIn(0f, 1f)
                                    val dotY = topPad + chartH * (1f - normalized)

                                    drawCircle(
                                        color = warningColor.copy(alpha = 0.22f),
                                        radius = 8.dp.toPx(),
                                        center = Offset(centerX, dotY)
                                    )
                                    drawCircle(
                                        color = warningColor,
                                        radius = 4.dp.toPx(),
                                        center = Offset(centerX, dotY)
                                    )
                                }

                                if (selectedHour == freq.hour) {
                                    val centerX = index * stepWidth + stepWidth / 2f
                                    drawLine(
                                        color = primaryColor.copy(alpha = 0.5f),
                                        start = Offset(centerX, topPad),
                                        end = Offset(centerX, topPad + chartH),
                                        strokeWidth = 1.5.dp.toPx()
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxSize()) {
                            for (hour in 0..23) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onSelectHour(if (selectedHour == hour) null else hour)
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Eixo X
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("00h", "06h", "12h", "18h", "23h").forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedHour != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedHour?.let { hour ->
                    val data = hourlyFrequencies.find { it.hour == hour }
                    if (data != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:00 • %d toques (%d sociais)", hour, data.totalCount, data.socialCount),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (data.hasSpike) {
                                    Text(
                                        text = "Pico rápido",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        color = extra.warning
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Cartão Unificado e Minimalista de Uso por Aplicação
 */
@Composable
fun AppFrequencyBreakdown(
    appFrequencies: List<AppFrequency>,
    modifier: Modifier = Modifier,
    headerAction: (@Composable () -> Unit)? = null
) {
    val extra = LocalExtraColors.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aplicações mais usadas",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (headerAction != null) {
                    headerAction()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (appFrequencies.isEmpty()) {
                Text(
                    text = "Sem interações registadas hoje.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                val totalInteractions = appFrequencies.sumOf { it.count }.coerceAtLeast(1)

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    appFrequencies.take(4).forEach { app ->
                        val percentage = (app.count.toFloat() / totalInteractions * 100f).toInt()

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "${app.count} ($percentage%)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 14.sp,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { app.count.toFloat() / totalInteractions },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp),
                                color = if (app.isSocial) extra.chartSecondary else extra.chartPrimary,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Registo Minimalista de Picos Rápidos
 */
@Composable
fun DopamineSpikesFeed(
    allSpikes: List<DopamineSpike>,
    modifier: Modifier = Modifier
) {
    val extra = LocalExtraColors.current

    if (allSpikes.isEmpty()) return

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Picos de interação rápida",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${allSpikes.size} hoje",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = extra.warning
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allSpikes.take(3).forEach { spike ->
                    val formattedTime = timeFormat.format(Date(spike.timestamp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(extra.warning, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${spike.appName} • ${spike.touchCount} toques em ${spike.durationSeconds}s",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
