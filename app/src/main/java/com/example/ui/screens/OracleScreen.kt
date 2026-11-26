package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai.OracleUiState
import com.example.ui.theme.AstralViolet
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonBlood
import com.example.ui.theme.DeepSlate
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.ParadoxMagenta
import com.example.ui.theme.SoulEmerald
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidObsidian
import com.example.ui.viewmodel.AeldrinUiState

@Composable
fun OracleScreen(
    gameState: AeldrinUiState,
    oracleState: OracleUiState,
    onAskStream: (String) -> Unit,
    onGenerateComboPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var promptInput by remember { mutableStateOf("") }

    val quickPrompts = remember(gameState.combat.encounter.name, gameState.stats.heroClass) {
        listOf(
            "Como vencer ${gameState.combat.encounter.name} com ${gameState.stats.heroClass.displayName}?",
            "Qual classe secreta combina melhor com meu estilo?",
            "Explique a história de ${gameState.combat.encounter.regionId} e suas facções",
            "Como usar habilidades Paradoxais (291–300) contra O Arquiteto do Vazio?"
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepSlate),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AstralViolet.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AstralViolet)
                            Text(
                                text = "Oráculo dos Sete Pilares (Gemini)",
                                style = MaterialTheme.typography.headlineSmall,
                                color = GildedAmber
                            )
                        }
                        Text(
                            text = "Estrategista em tempo real conectado ao seu personagem (${gameState.stats.heroClass.displayName} · ${gameState.stats.activeSubclass.name}) e ao chefe ${gameState.combat.encounter.name}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Button(
                            onClick = onGenerateComboPlan,
                            enabled = oracleState !is OracleUiState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = AstralViolet, contentColor = VoidObsidian),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("oracle_generate_combo_button")
                        ) {
                            Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Gerar Plano Tático Estruturado (JSON)")
                        }
                    }
                }
            }

            // Quick Prompt Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickPrompts) { suggestion ->
                        AssistChip(
                            onClick = {
                                promptInput = suggestion
                                onAskStream(suggestion)
                            },
                            label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Prompt Input
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        placeholder = { Text("Pergunte sobre combos, chefes, lore ou classes secretas...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("oracle_prompt_input")
                    )
                    IconButton(
                        onClick = {
                            onAskStream(promptInput)
                            promptInput = ""
                        },
                        enabled = promptInput.isNotBlank() && oracleState !is OracleUiState.Loading,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GildedAmber)
                            .testTag("oracle_send_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar pergunta ao Oráculo",
                            tint = VoidObsidian
                        )
                    }
                }
            }

            // Response Area
            item {
                when (oracleState) {
                    OracleUiState.Idle -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ElevatedCard),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                text = "Toque em uma sugestão acima, envie uma pergunta ou gere um plano tático estruturado para ${gameState.combat.encounter.name}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    OracleUiState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(ElevatedCard)
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = AstralViolet,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                text = "Consultando os Sete Pilares via Gemini...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AstralViolet
                            )
                        }
                    }

                    is OracleUiState.Streaming -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepSlate),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AstralViolet.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Transmitindo resposta...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AstralViolet
                                )
                                Text(
                                    text = oracleState.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    is OracleUiState.SuccessText -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepSlate),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GildedAmber.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Visão do Oráculo",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GildedAmber
                                )
                                Text(
                                    text = oracleState.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    is OracleUiState.SuccessCombo -> {
                        val plan = oracleState.plan
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepSlate),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SoulEmerald.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = plan.comboTitle,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = SoulEmerald
                                )
                                plan.synergySteps.forEachIndexed { idx, step ->
                                    Text(
                                        text = "${idx + 1}. $step",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Sinergia Paradoxal: ${plan.paradoxTip}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ParadoxMagenta,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Rumo à Classe Secreta: ${plan.secretTrialHint}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GildedAmber
                                )
                            }
                        }
                    }

                    is OracleUiState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CrimsonBlood.copy(alpha = 0.14f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CrimsonBlood, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = CrimsonBlood)
                                    Text(
                                        text = "Erro do Oráculo Gemini",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CrimsonBlood
                                    )
                                }
                                Text(
                                    text = oracleState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                OutlinedButton(onClick = onGenerateComboPlan) {
                                    Text("Tentar Novamente")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
