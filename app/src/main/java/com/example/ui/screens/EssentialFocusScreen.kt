package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DopaCard
import com.example.ui.components.DopaDisclaimerFooter
import com.example.ui.components.DopaPrimaryButton
import com.example.ui.components.DopaScreenHeader
import com.example.ui.components.DopaSecondaryButton
import com.example.ui.theme.LocalExtraColors
import com.example.ui.viewmodel.DopaViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun EssentialFocusScreen(viewModel: DopaViewModel) {
    val tasks by viewModel.essentialTasks.collectAsState()
    val context = LocalContext.current
    val extra = LocalExtraColors.current

    var newTaskTitle by remember { mutableStateOf("") }
    var showAddTask by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateTo(Screen.DASHBOARD)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar ao painel",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Foco Essencial",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Resolva apenas o indispensável e guarde o telefone",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Card explicativo de tranquilidade
        item {
            DopaCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mail,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Responda a e-mails urgentes ou mensagens da família de forma intencional, sem se perder no ciclo de rolagem infinita.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Lista de Tarefas Essenciais
        items(tasks, key = { it.id }) { task ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (task.isDone) extra.success.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleEssentialTask(task.id) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = if (task.isDone) "Concluído" else "Marcar como concluído",
                                tint = if (task.isDone) extra.success else MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 15.sp,
                                    fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.Medium
                                ),
                                color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                            )
                            if (task.appTarget != null) {
                                Text(
                                    text = "Aplicativo sugerido: ${task.appTarget}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (task.appTarget != null && !task.isDone) {
                        IconButton(
                            onClick = {
                                val intent = context.packageManager.getLaunchIntentForPackage(
                                    if (task.category == "EMAIL") "com.google.android.gm" else "com.whatsapp"
                                )
                                if (intent != null) {
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Abrir aplicativo diretamente",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Adicionar Nova Tarefa
        item {
            if (showAddTask) {
                DopaCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            placeholder = { Text("Ex: Responder cliente sobre reunião", fontSize = 14.sp) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DopaSecondaryButton(
                                text = "Cancelar",
                                onClick = { showAddTask = false },
                                modifier = Modifier.weight(1f)
                            )
                            DopaPrimaryButton(
                                text = "Salvar Tarefa",
                                onClick = {
                                    if (newTaskTitle.isNotBlank()) {
                                        viewModel.addEssentialTask(newTaskTitle, "MESSAGING")
                                        newTaskTitle = ""
                                        showAddTask = false
                                    }
                                },
                                enabled = newTaskTitle.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                DopaSecondaryButton(
                    text = "Adicionar Tarefa Importante",
                    icon = Icons.Filled.Add,
                    onClick = { showAddTask = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_add_essential_task"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            DopaDisclaimerFooter()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
