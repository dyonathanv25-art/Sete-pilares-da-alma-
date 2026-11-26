package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
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
import com.example.domain.model.CombatPlayMode
import com.example.domain.model.EnemyEncounter
import com.example.domain.model.EnemyTier
import com.example.domain.model.HeroSkin
import com.example.domain.model.Skill
import com.example.ui.combat3d.CombatArena3DView
import com.example.ui.combat3d.OpenWorld3DView
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
    onSelectPlayMode: (CombatPlayMode) -> Unit,
    onSelectEncounter: (EnemyEncounter) -> Unit,
    onSelectSkin: (HeroSkin) -> Unit,
    onBasicAttack: () -> Unit,
    onAttackOutOfRange: () -> Unit,
    onBossStrikeResolved: (Boolean, Boolean, Boolean) -> Unit,
    onManaOverload: () -> Unit,
    onPurifySoul: () -> Unit,
    onUseSkill: (Skill) -> Unit,
    onRetryFight: () -> Unit,
    onOpenWorldPlayerDamaged: (Int, String) -> Unit,
    onOpenWorldMonsterDefeated: (String, Int, Int) -> Unit,
    onOpenWorldClaimChest: (String, Int, Int) -> Unit,
    onOpenWorldRestVillage: () -> Unit,
    onOpenWorldUpgradeForge: () -> Unit,
    onOpenWorldInvasionResolved: (Boolean) -> Unit,
    onOpenWorldLog: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOpenWorld = uiState.combatPlayMode == CombatPlayMode.OPEN_WORLD
    val combat = uiState.combat
    val encounter = combat.encounter
    val isPurifyReady = encounter.isCursed &&
        combat.outcome == BattleOutcome.ONGOING &&
        (combat.enemyHp.toFloat() / combat.enemyMaxHp.toFloat()) <= 0.35f

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            // Genshin-style Landscape Split Layout: Left = Full-height 3D World/Arena, Right = Live HUD, Skills & Log
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.62f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CombatModeHeaderRow(
                        isOpenWorld = isOpenWorld,
                        onSelectPlayMode = onSelectPlayMode
                    )
                    if (!isOpenWorld) {
                        BossSwitcherRow(
                            selectedId = encounter.id,
                            onSelectEncounter = onSelectEncounter
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (isOpenWorld) {
                            OpenWorld3DView(
                                uiState = uiState,
                                onSelectSkin = onSelectSkin,
                                onPlayerDamaged = onOpenWorldPlayerDamaged,
                                onMonsterDefeated = onOpenWorldMonsterDefeated,
                                onClaimCampChest = onOpenWorldClaimChest,
                                onRestAtVillage = onOpenWorldRestVillage,
                                onUpgradeAtForge = onOpenWorldUpgradeForge,
                                onInvasionResolved = onOpenWorldInvasionResolved,
                                onWorldLog = onOpenWorldLog,
                                fillAvailableHeight = true
                            )
                        } else {
                            CombatArena3DView(
                                uiState = uiState,
                                onSelectSkin = onSelectSkin,
                                onAttackInRange = onBasicAttack,
                                onAttackOutOfRange = onAttackOutOfRange,
                                onBossStrikeResolved = onBossStrikeResolved,
                                fillAvailableHeight = true
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    combatHudItems(
                        uiState = uiState,
                        isOpenWorld = isOpenWorld,
                        isPurifyReady = isPurifyReady,
                        onManaOverload = onManaOverload,
                        onPurifySoul = onPurifySoul,
                        onUseSkill = onUseSkill,
                        onRetryFight = onRetryFight
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CombatModeHeaderRow(
                        isOpenWorld = isOpenWorld,
                        onSelectPlayMode = onSelectPlayMode
                    )
                }

                if (isOpenWorld) {
                    item {
                        OpenWorld3DView(
                            uiState = uiState,
                            onSelectSkin = onSelectSkin,
                            onPlayerDamaged = onOpenWorldPlayerDamaged,
                            onMonsterDefeated = onOpenWorldMonsterDefeated,
                            onClaimCampChest = onOpenWorldClaimChest,
                            onRestAtVillage = onOpenWorldRestVillage,
                            onUpgradeAtForge = onOpenWorldUpgradeForge,
                            onInvasionResolved = onOpenWorldInvasionResolved,
                            onWorldLog = onOpenWorldLog
                        )
                    }
                } else {
                    item {
                        BossSwitcherRow(
                            selectedId = encounter.id,
                            onSelectEncounter = onSelectEncounter
                        )
                    }
                    item {
                        CombatArena3DView(
                            uiState = uiState,
                            onSelectSkin = onSelectSkin,
                            onAttackInRange = onBasicAttack,
                            onAttackOutOfRange = onAttackOutOfRange,
                            onBossStrikeResolved = onBossStrikeResolved
                        )
                    }
                }

                combatHudItems(
                    uiState = uiState,
                    isOpenWorld = isOpenWorld,
                    isPurifyReady = isPurifyReady,
                    onManaOverload = onManaOverload,
                    onPurifySoul = onPurifySoul,
                    onUseSkill = onUseSkill,
                    onRetryFight = onRetryFight
                )
            }
        }
    }
}

@Composable
private fun CombatModeHeaderRow(
    isOpenWorld: Boolean,
    onSelectPlayMode: (CombatPlayMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = isOpenWorld,
            onClick = { onSelectPlayMode(CombatPlayMode.OPEN_WORLD) },
            label = { Text("Mundo Aberto 3D", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = SoulEmerald.copy(alpha = 0.22f),
                selectedLabelColor = SoulEmerald
            ),
            modifier = Modifier.weight(1f).testTag("mode_open_world_chip")
        )
        FilterChip(
            selected = !isOpenWorld,
            onClick = { onSelectPlayMode(CombatPlayMode.BOSS_ARENA) },
            label = { Text("Arena de Chefes", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CrimsonBlood.copy(alpha = 0.22f),
                selectedLabelColor = GildedAmber
            ),
            modifier = Modifier.weight(1f).testTag("mode_boss_arena_chip")
        )
    }
}

@Composable
private fun BossSwitcherRow(
    selectedId: String,
    onSelectEncounter: (EnemyEncounter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ClassAndWorldCatalog.encounters, key = { it.id }) { boss ->
            val selected = boss.id == selectedId
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

@OptIn(ExperimentalLayoutApi::class)
private fun LazyListScope.combatHudItems(
    uiState: AeldrinUiState,
    isOpenWorld: Boolean,
    isPurifyReady: Boolean,
    onManaOverload: () -> Unit,
    onPurifySoul: () -> Unit,
    onUseSkill: (Skill) -> Unit,
    onRetryFight: () -> Unit
) {
    val combat = uiState.combat
    val encounter = combat.encounter
    val save = uiState.save
    val stats = uiState.stats

    if (!isOpenWorld) {
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
                        modifier = Modifier.matchParentSize()
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
                    modifier = Modifier.padding(14.dp),
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
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = encounter.title,
                                style = MaterialTheme.typography.bodySmall,
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
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CrimsonBlood,
                        trackColor = ElevatedCard
                    )

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
                            text = "${combat.enemyIntent.label} (${combat.enemyIntentDamage})",
                            color = intentColor
                        )
                        if (combat.enemyPoisonTurns > 0) StatusBadge("Veneno (${combat.enemyPoisonTurns}t)", SoulEmerald)
                        if (combat.enemyBleedTurns > 0) StatusBadge("Sangramento (${combat.enemyBleedTurns}t)", CrimsonBlood)
                        if (combat.enemyFreezeTurns > 0) StatusBadge("Congelado (${combat.enemyFreezeTurns}t)", ArcaneCyan)
                        if (encounter.isCursed) {
                            StatusBadge(
                                text = if (isPurifyReady) "PURIFICAR PRONTO!" else "Amaldiçoado (<35%)",
                                color = if (isPurifyReady) SoulEmerald else ParadoxMagenta
                            )
                        }
                        if (combat.enemyAerialWeakPointOpen) {
                            StatusBadge("Ponto Fraco EXPOSTO (+35%)", SoulEmerald)
                        }
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
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stats.heroClass.displayName} · Nv.${save.level}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GildedAmber
                    )
                    Text(
                        text = "${save.shards} Frag",
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcaneCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "HP ${combat.playerHp}/${combat.playerMaxHp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoulEmerald
                        )
                        LinearProgressIndicator(
                            progress = { (combat.playerHp.toFloat() / combat.playerMaxHp.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (combat.playerHp < combat.playerMaxHp * 0.25f) CrimsonBlood else SoulEmerald,
                            trackColor = ElevatedCard
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Mana ${combat.playerMana}/${combat.playerMaxMana}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AstralViolet
                        )
                        LinearProgressIndicator(
                            progress = { (combat.playerMana.toFloat() / combat.playerMaxMana.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AstralViolet,
                            trackColor = ElevatedCard
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (combat.playerShield > 0) StatusBadge("Escudo ${combat.playerShield}", ArcaneCyan)
                    if (combat.inversionTurns > 0) StatusBadge("Inversão (${combat.inversionTurns}t)", ParadoxMagenta)
                    if (combat.undoDeathActive) StatusBadge("Desfazer Morte", GildedAmber)
                    if (combat.overloadBuffActive) StatusBadge("Sobrecarga +40%", AstralViolet)
                    if (save.lightArmorChallengeEnabled) StatusBadge("Espada Madeira (${save.perfectParriesCount}/10)", GildedAmber)
                }
            }
        }
    }

    if (!isOpenWorld && combat.outcome != BattleOutcome.ONGOING) {
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
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (combat.outcome) {
                            BattleOutcome.VICTORY_PURIFIED -> "ALMA PURIFICADA! +${encounter.xpReward} XP"
                            BattleOutcome.VICTORY_SLAIN -> "VITÓRIA! +${encounter.xpReward} XP · +${encounter.shardReward} Frag"
                            BattleOutcome.DEFEAT -> "DERROTA EM COMBATE"
                            BattleOutcome.ONGOING -> ""
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isWin) SoulEmerald else CrimsonBlood
                    )
                    combat.newlyUnlockedSecret?.let { unlocked ->
                        Text(
                            text = "SECRETA DESBLOQUEADA: ${unlocked.name}!",
                            style = MaterialTheme.typography.labelLarge,
                            color = GildedAmber
                        )
                    }
                    Button(
                        onClick = onRetryFight,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isWin) GildedAmber else CrimsonBlood),
                        modifier = Modifier.testTag("retry_combat_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Lutar Novamente")
                    }
                }
            }
        }
    }

    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onManaOverload,
                enabled = combat.outcome == BattleOutcome.ONGOING,
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("overload_button")
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = AstralViolet, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Sobrecarga (+55 Mana, +40% Poder)", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            if (!isOpenWorld && encounter.isCursed && combat.outcome == BattleOutcome.ONGOING) {
                Button(
                    onClick = onPurifySoul,
                    enabled = isPurifyReady,
                    colors = ButtonDefaults.buttonColors(containerColor = SoulEmerald, contentColor = VoidObsidian),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("purify_soul_button")
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(
                        if (isPurifyReady) "Purificar Alma (Salvar Chefe)" else "Purificar (<35% HP)",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    item {
        Text(
            text = "Habilidades Equipadas (5 Slots)",
            style = MaterialTheme.typography.titleSmall,
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
                .clip(RoundedCornerShape(12.dp))
                .background(if (enabled) ElevatedCard else DeepSlate)
                .border(
                    1.dp,
                    if (enabled) skill.category.accentColor.copy(alpha = 0.65f) else CardBorder,
                    RoundedCornerShape(12.dp)
                )
                .clickable(enabled = enabled) { onUseSkill(skill) }
                .padding(10.dp)
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
                    style = MaterialTheme.typography.titleSmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.size(8.dp))
            if (cd > 0) {
                Surface(color = CrimsonBlood.copy(alpha = 0.25f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "CD ${cd}t",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonBlood,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Surface(color = AstralViolet.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = if (skill.manaCost == 0) "0 M" else "${skill.manaCost} M",
                        style = MaterialTheme.typography.labelSmall,
                        color = AstralViolet,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    item {
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepSlate),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.AutoFixHigh, contentDescription = null, tint = GildedAmber, modifier = Modifier.size(15.dp))
                    Text("Registro de Combate", style = MaterialTheme.typography.titleSmall, color = GildedAmber)
                }
                combat.logs.asReversed().take(5).forEach { entry ->
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
                            style = MaterialTheme.typography.bodySmall,
                            color = if (entry.isHighlight) GildedAmber else MaterialTheme.colorScheme.onSurface
                        )
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
