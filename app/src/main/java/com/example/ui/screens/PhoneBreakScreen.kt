package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DopaDisclaimerFooter
import com.example.ui.components.DopaPrimaryButton
import com.example.ui.components.DopaSecondaryButton
import com.example.ui.theme.LocalExtraColors
import com.example.ui.viewmodel.DopaViewModel
import com.example.ui.viewmodel.Screen
import java.util.Locale

@Composable
fun PhoneBreakScreen(viewModel: DopaViewModel) {
    val secondsRemaining by viewModel.breakSecondsRemaining.collectAsState()
    val isBreakActive by viewModel.isBreakActive.collectAsState()
    val extra = LocalExtraColors.current

    BackHandler {
        viewModel.navigateTo(Screen.DASHBOARD)
    }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    // Respiração Guiada: Animação suave de 4s inspiração e 4s expiração
    val breathTransition = rememberInfiniteTransition(label = "breath_pulse")
    val breathScale by breathTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Barra Superior: Título e Botão de Fechar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pausa Consciente",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Fechar pausa",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Anel de Respiração Central e Contador
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val baseRadius = size.width / 2.3f

                    // Halo de expansão e contração pulmonar
                    drawCircle(
                        color = primaryContainer.copy(alpha = 0.45f),
                        radius = baseRadius * breathScale,
                        center = center
                    )

                    // Borda externa suave
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.3f),
                        radius = baseRadius * 0.95f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Borda principal sólida
                    drawCircle(
                        color = primaryColor,
                        radius = baseRadius * 0.85f,
                        center = center,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 48.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (breathScale > 1.0f) "Expire suavemente..." else "Inspire com calma...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Pouse o celular sobre a mesa.",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Permita aos olhos descansarem da luz da tela. 25 minutos longe de notificações restauram sua capacidade de foco e serenidade.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Ações Inferiores
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isBreakActive) {
                DopaSecondaryButton(
                    text = "Pausar Temporizador",
                    icon = Icons.Filled.Pause,
                    onClick = { viewModel.pauseBreakTimer() },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                DopaPrimaryButton(
                    text = "Iniciar Pausa de 25 min",
                    icon = Icons.Filled.PlayArrow,
                    onClick = { viewModel.startBreakTimer() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            DopaSecondaryButton(
                text = "Encerrar e Voltar",
                onClick = {
                    viewModel.stopBreakTimer()
                    viewModel.navigateTo(Screen.DASHBOARD)
                },
                modifier = Modifier.fillMaxWidth()
            )

            DopaDisclaimerFooter()
        }
    }
}
