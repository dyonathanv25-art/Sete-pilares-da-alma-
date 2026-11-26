package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.ClassAndWorldCatalog
import com.example.domain.model.EnemyEncounter
import com.example.domain.model.EnemyTier
import com.example.domain.model.Skill
import com.example.ui.theme.ArcaneCyan
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
import com.example.ui.viewmodel.BattleOutcome
import com.example.ui.viewmodel.EnemyIntentType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CombatArenaScreen(
    uiState: AeldrinUiState,
    onSelectEncounter: (EnemyEncounter) -> Unit,
    onBasicAttack: () -> Unit,
    onParryStance: () -> Unit,
    onManaOverload: () -> Unit,
    onPurifySoul: () -> Unit,
    onUseSkill: (Skill) -> Unit,
    onRetryFight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val combat = uiState.combat
    val encounter = combat.encounter
    val save = uiState.save
    val stats = uiState.stats
    val isPurifyReady = encounter.isCursed &&
        combat.outcome == BattleOutcome.ONGOING &&
        (combat.enemyHp.toFloat() / combat.enemyMaxHp.toFloat()) <= 0.35f

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Boss Switcher Strip
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ClassAndWorldCatalog.encounters, key = { it.id }) { boss ->
                        val selected = boss.id == encounter.id
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectEncounter(boss) },
                            label = { Text(boss.name, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GildedAmber.copy(alpha = 0.22f),
                                selectedLabelColor = GildedAmber
                            )
                        )
                    }
                }
            }

            // Boss Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            1.5.dp,
                            if (combat.enemyAerialWeakPointOpen) SoulEmerald else CardBorder,
                            RoundedCornerShape(18.dp)
                        )
                ) {
                    if (encounter.tier == EnemyTier.FINAL_RAID || encounter.tier == EnemyTier.MAIN_BOSS) {
                        Image(
                            painter = painterResource(id = R.drawable.img_boss_architect),
                            contentDescription = "Arte de combate do chefe ${encounter.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            VoidObsidian.copy(alpha = 0.58f),
                                            VoidObsidian.copy(alpha = 0.92f)
                                        )
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(DeepSlate)
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${encounter.tier.label.uppercase()} · TURNO ${combat.turn}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GildedAmber
                                )
                                Text(
                                    text = encounter.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = encounter.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "${combat.enemyHp}/${combat.enemyMaxHp}",
                                style = MaterialTheme.typography.titleMedium,
                                color = CrimsonBlood,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (combat.enemyHp.toFloat() / combat.enemyMaxHp.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = CrimsonBlood,
                            trackColor = ElevatedCard
                        )

                        // Boss Status Pills + Intent Banner
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val intentColor = when (combat.enemyIntent) {
                                EnemyIntentType.HEAVY_TELEGRAPHED -> GildedAmber
                                EnemyIntentType.AERIAL_DIVE -> SoulEmerald
                                EnemyIntentType.CURSE_PULSE -> ParadoxMagenta
                                EnemyIntentType.FROZEN -> ArcaneCyan
                                else -> CrimsonBlood
                            }
                            StatusBadge(
                                text = "Intenção: ${combat.enemyIntent.label} (${combat.enemyIntentDamage} Dano)",
                                color = intentColor
                            )
                            if (combat.enemyPoisonTurns > 0) {
                                StatusBadge("Veneno (${combat.enemyPoisonTurns}t)", SoulEmerald)
                            }
                            if (combat.enemyBleedTurns > 0) {
                                StatusBadge("Sangramento (${combat.enemyBleedTurns}t)", CrimsonBlood)
                            }
                            if (combat.enemyFreezeTurns > 0) {
                                StatusBadge("Congelado (${combat.enemyFreezeTurns}t)", ArcaneCyan)
                            }
                            if (encounter.isCursed) {
                                StatusBadge(
                                    text = if (isPurifyReady) "Amaldiçoado — PURIFICAR PRONTO!" else "Amaldiçoado (<35% Purificar)",
                                    color = if (isPurifyReady) SoulEmerald else ParadoxMagenta
                                )
                            }
                            if (combat.enemyAerialWeakPointOpen) {
                                StatusBadge("Ponto Fraco Aéreo EXPOSTO (+35% Dano)", SoulEmerald)
                            }
                        }
                    }
                }
            }

            // Player Status Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stats.heroClass.displayName} · ${stats.activeSubclass.name}",
                                style = MaterialTheme.typography.titleMedium,
                                color = GildedAmber
                            )
                            if (combat.playerShield > 0) {
                                StatusBadge("Escudo ${combat.playerShield}", ArcaneCyan)
                            }
                        }

                        // HP & Mana
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "HP ${combat.playerHp}/${combat.playerMaxHp}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SoulEmerald
                                )
                                LinearProgressIndicator(
                                    progress = { (combat.playerHp.toFloat() / combat.playerMaxHp.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (combat.playerHp < combat.playerMaxHp * 0.25f) CrimsonBlood else SoulEmerald,
                                    trackColor = ElevatedCard
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Mana ${combat.playerMana}/${combat.playerMaxMana}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AstralViolet
                                )
                                LinearProgressIndicator(
                                    progress = { (combat.playerMana.toFloat() / combat.playerMaxMana.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = AstralViolet,
                                    trackColor = ElevatedCard
                                )
                            }
                        }

                        // Active Buffs / Secret Progress
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (combat.inversionTurns > 0) {
                                StatusBadge("Inversão Causal (${combat.inversionTurns}t)", ParadoxMagenta)
                            }
                            if (combat.undoDeathActive) {
                                StatusBadge("Desfazer Morte Armado", GildedAmber)
                            }
                            if (combat.overloadBuffActive) {
                                StatusBadge("Sobrecarga +40% Dano", AstralViolet)
                            }
                            if (save.lightArmorChallengeEnabled) {
                                StatusBadge("Espada de Madeira (${save.perfectParriesCount}/10 Parries)", GildedAmber)
                            }
                            if (save.lunarEclipseModeEnabled) {
                                StatusBadge("Eclipse Lunar (Sem Encantamentos)", AstralViolet)
                            }
                        }
                    }
                }
            }

            // Outcome Banner if Finished
            if (combat.outcome != BattleOutcome.ONGOING) {
                item {
                    val isWin = combat.outcome != BattleOutcome.DEFEAT
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWin) SoulEmerald.copy(alpha = 0.14f) else CrimsonBlood.copy(alpha = 0.16f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, if (isWin) SoulEmerald else CrimsonBlood, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when (combat.outcome) {
                                    BattleOutcome.VICTORY_PURIFIED -> "ALMA PURIFICADA! +${encounter.xpReward} XP"
                                    BattleOutcome.VICTORY_SLAIN -> "VITÓRIA! +${encounter.xpReward} XP · +${encounter.shardReward} Fragmentos"
                                    BattleOutcome.DEFEAT -> "DERROTA EM COMBATE"
                                    BattleOutcome.ONGOING -> ""
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isWin) SoulEmerald else CrimsonBlood
                            )
                            combat.newlyUnlockedSecret?.let { unlocked ->
                                Text(
                                    text = "SUBCLASSE SECRETA DESBLOQUEADA: ${unlocked.name}!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GildedAmber
                                )
                            }
                            Button(
                                onClick = onRetryFight,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isWin) GildedAmber else CrimsonBlood
                                ),
                                modifier = Modifier.testTag("retry_combat_button")
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(6.dp))
                                Text("Lutar Novamente")
                            }
                        }
                    }
                }
            }

            // Core Tactical Actions (Attack / Parry / Overload / Purify)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onBasicAttack,
                            enabled = combat.outcome == BattleOutcome.ONGOING,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonBlood),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("basic_attack_button")
                        ) {
                            Text("Atacar", maxLines = 1)
                        }
                        Button(
                            onClick = onParryStance,
                            enabled = combat.outcome == BattleOutcome.ONGOING,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (combat.enemyIntent == EnemyIntentType.HEAVY_TELEGRAPHED || combat.enemyIntent == EnemyIntentType.AERIAL_DIVE) {
                                    GildedAmber
                                } else {
                                    ElevatedCard
                                },
                                contentColor = if (combat.enemyIntent == EnemyIntentType.HEAVY_TELEGRAPHED || combat.enemyIntent == EnemyIntentType.AERIAL_DIVE) {
                                    VoidObsidian
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("parry_button")
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(4.dp))
                            Text("Parry", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onManaOverload,
                            enabled = combat.outcome == BattleOutcome.ONGOING,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("overload_button")
                        ) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = AstralViolet, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(3.dp))
                            Text("Sobrecarga", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    if (encounter.isCursed && combat.outcome == BattleOutcome.ONGOING) {
                        Button(
                            onClick = onPurifySoul,
                            enabled = isPurifyReady,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoulEmerald,
                                contentColor = VoidObsidian
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("purify_soul_button")
                        ) {
                            Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(
                                if (isPurifyReady) "Purificar Alma (Salvar Chefe & Desbloquear Herege)"
                                else "Purificar Alma (Disponível abaixo de 35% HP)"
                            )
                        }
                    }
                }
            }

            // 5 Equipped Skills Grid
            item {
                Text(
                    text = "Habilidades Equipadas (5 Slots)",
                    style = MaterialTheme.typography.titleMedium,
                    color = GildedAmber
                )
            }

            items(uiState.equippedSkills, key = { it.id }) { skill ->
                val cd = combat.skillCooldowns[skill.id] ?: 0
                val enabled = combat.outcome == BattleOutcome.ONGOING && cd == 0
                val displayName = skill.displayNameForLevel(save.level, stats.activeSubclass.name)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (enabled) ElevatedCard else DeepSlate)
                        .border(
                            1.dp,
                            if (enabled) skill.category.accentColor.copy(alpha = 0.65f) else CardBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable(enabled = enabled) { onUseSkill(skill) }
                        .padding(12.dp)
                        .testTag("combat_skill_${skill.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "#${skill.code}",
                                style = MaterialTheme.typography.labelSmall,
                                color = skill.category.accentColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = skill.category.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = skill.category.accentColor
                            )
                        }
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (enabled) MaterialTheme.colorScheme.onSurface else TextSecondary
                        )
                        Text(
                            text = skill.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        if (cd > 0) {
                            Surface(
                                color = CrimsonBlood.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "CD ${cd}t",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CrimsonBlood,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = AstralViolet.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (skill.manaCost == 0) "0 Mana" else "${skill.manaCost} Mana",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AstralViolet,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Combat Log
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepSlate),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.AutoFixHigh, contentDescription = null, tint = GildedAmber, modifier = Modifier.size(16.dp))
                            Text("Registro Tático de Combate", style = MaterialTheme.typography.titleMedium, color = GildedAmber)
                        }
                        combat.logs.asReversed().take(6).forEach { entry ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "[T${entry.turn}]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = entry.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (entry.isHighlight) GildedAmber else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}
