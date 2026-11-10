package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DopaCard
import com.example.ui.components.DopaDisclaimerFooter
import com.example.ui.components.DopaPrimaryButton
import com.example.ui.components.DopaSecondaryButton
import com.example.ui.components.DopaTextButton
import com.example.ui.theme.LocalExtraColors
import com.example.ui.viewmodel.DopaViewModel

data class OnboardingStep(
    val stepIndex: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val illustration: @Composable () -> Unit
)

@Composable
fun OnboardingScreen(viewModel: DopaViewModel) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val extra = LocalExtraColors.current

    val steps = remember {
        listOf(
            OnboardingStep(
                stepIndex = 0,
                title = "O Toque Automático",
                subtitle = "Trazendo consciência aos gestos invisíveis",
                description = "Em média, tocamos na tela do celular mais de 2.000 vezes por dia sem prestar atenção. A lena monitora o ritmo desses toques para que você conheça seus hábitos reais de uso.",
                icon = Icons.Filled.TouchApp,
                illustration = {
                    DopaCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TouchApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Ritmo em tempo real",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "Medição suave e respeitosa da cadência de interação",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            ),
            OnboardingStep(
                stepIndex = 1,
                title = "Picos de Interação Rápida",
                subtitle = "Entendendo quando o ritmo acelera",
                description = "Ao rolar feeds infinitos ou alternar apps freneticamente, a frequência de toques sobe de forma expressiva. Identificamos esses momentos como picos rápidos para alertar sobre dispersão.",
                icon = Icons.Filled.Bolt,
                illustration = {
                    Surface(
                        color = extra.warningContainer,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, extra.warning.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(extra.warning.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    tint = extra.warning,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Detecção de aceleração",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = extra.onWarningContainer
                                )
                                Text(
                                    text = "Mais de 10 toques rápidos em segundos indicam impulsividade",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = extra.onWarningContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            ),
            OnboardingStep(
                stepIndex = 2,
                title = "Pausa Gentil de 25 Min",
                subtitle = "Desacelerar em vez de bloquear",
                description = "Em vez de bloquear suas redes à força, a lena convida você a pousar o aparelho por 25 minutos, com temporizador calmo e respiração guiada para restabelecer a clareza mental.",
                icon = Icons.Filled.HourglassTop,
                illustration = {
                    DopaCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.HourglassTop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "25 minutos de pausa",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "Espaço para descansar os olhos e a mente",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            ),
            OnboardingStep(
                stepIndex = 3,
                title = "Modo Foco Essencial",
                subtitle = "Apenas o que é indispensável",
                description = "Precisa apenas responder a um e-mail de trabalho ou checar mensagens importantes da família? O modo essencial direciona para a tarefa sem o risco de se perder em feeds.",
                icon = Icons.Filled.Mail,
                illustration = {
                    DopaCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Mail,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "E-mails e Mensagens",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "Conclua a comunicação prioritária e guarde o celular",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            )
        )
    }

    val currentStep = steps[currentStepIndex]
    val isLastStep = currentStepIndex == steps.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar: Step Indicator & Skip Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEachIndexed { index, _ ->
                    val isSelected = index == currentStepIndex
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (isSelected) 28.dp else 12.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            if (!isLastStep) {
                DopaTextButton(
                    text = "Pular",
                    onClick = { viewModel.completeOnboarding() },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    testTag = "btn_skip_onboarding"
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // Center Content with Smooth Transitions
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "onboarding_step_content",
            modifier = Modifier.weight(1f)
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 26.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = step.subtitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                step.illustration()
            }
        }

        // Bottom Actions & Medical Note
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isLastStep) {
                DopaPrimaryButton(
                    text = "Começar com Consciência",
                    icon = Icons.Filled.Check,
                    onClick = { viewModel.completeOnboarding() },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_finish_onboarding"
                )
            } else {
                DopaPrimaryButton(
                    text = "Continuar",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { currentStepIndex++ },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_next_onboarding"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            DopaDisclaimerFooter()
        }
    }
}
