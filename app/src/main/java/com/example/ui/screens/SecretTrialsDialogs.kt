package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AstralViolet
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonBlood
import com.example.ui.theme.DeepSlate
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.ParadoxMagenta
import com.example.ui.theme.SoulEmerald
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.StarPuzzleState
import com.example.ui.viewmodel.StealthTrialState

@Composable
fun StarAlignmentDialog(
    state: StarPuzzleState,
    onRotateLuneta: (Int) -> Unit,
    onStartGuardianFight: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!state.isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSlate,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AstralViolet)
                Text("Provação das 5 Lunetas (Aethel)", style = MaterialTheme.typography.headlineSmall)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Gire cada uma das 5 lunetas antigas até que todas apontem para o Zênite (0°). Após alinhar os astros, enfrente o Guardião Astral!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    state.lunetaAngles.forEachIndexed { index, angleStep ->
                        val isAligned = angleStep == 0
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .minimumInteractiveComponentSize()
                                    .clip(CircleShape)
                                    .background(if (isAligned) SoulEmerald.copy(alpha = 0.2f) else ElevatedCard)
                                    .border(
                                        width = 2.dp,
                                        color = if (isAligned) SoulEmerald else CardBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { onRotateLuneta(index) }
                                    .testTag("luneta_button_$index")
                            ) {
                                Text(
                                    text = "▲",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (isAligned) SoulEmerald else GildedAmber,
                                    modifier = Modifier.rotate(angleStep * 90f)
                                )
                            }
                            Text(
                                text = "L${index + 1}: ${angleStep * 90}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAligned) SoulEmerald else TextSecondary
                            )
                        }
                    }
                }
                if (state.solved) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SoulEmerald.copy(alpha = 0.14f))
                            .padding(10.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SoulEmerald)
                        Text(
                            text = "Constelação alinhada! O Guardião Astral despertou.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoulEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (state.solved) {
                Button(
                    onClick = onStartGuardianFight,
                    colors = ButtonDefaults.buttonColors(containerColor = AstralViolet),
                    modifier = Modifier.testTag("launch_astral_guardian_button")
                ) {
                    Text("Enfrentar Guardião Astral")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun NyxStealthTrialDialog(
    state: StealthTrialState,
    onChooseAction: (silentApproach: Boolean) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!state.isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSlate,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.VisibilityOff, contentDescription = null, tint = ParadoxMagenta)
                Text("Infiltração em Nyx (Sem-Rosto)", style = MaterialTheme.typography.headlineSmall)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = state.lastActionNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Setor ${state.step}/5", style = MaterialTheme.typography.labelMedium, color = GildedAmber)
                        Text(
                            "Suspeita: ${state.suspicion}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.suspicion >= 70) CrimsonBlood else TextSecondary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { state.suspicion / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (state.suspicion >= 70) CrimsonBlood else ParadoxMagenta,
                        trackColor = ElevatedCard
                    )
                }

                if (state.completed) {
                    Text(
                        text = "Sucesso! Você provou seu anonimato e desbloqueou Caminhante do Véu (Sem-Rosto).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoulEmerald,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                } else if (!state.failed) {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { onChooseAction(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stealth_shadow_step_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ParadoxMagenta)
                    ) {
                        Text("Esgueirar pelas Sombras (Baixa Suspeita)")
                    }
                    OutlinedButton(
                        onClick = { onChooseAction(false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stealth_distraction_button")
                    ) {
                        Text("Distração Sonora sem Matar Guardas")
                    }
                }
            }
        },
        confirmButton = {
            if (state.failed) {
                Button(onClick = onRetry, modifier = Modifier.testTag("stealth_retry_button")) {
                    Text("Tentar Novamente")
                }
            } else if (state.completed) {
                Button(onClick = onDismiss) {
                    Text("Concluir")
                }
            }
        },
        dismissButton = {
            if (!state.completed) {
                TextButton(onClick = onDismiss) {
                    Text("Sair")
                }
            }
        }
    )
}
