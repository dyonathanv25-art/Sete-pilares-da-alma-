package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.ClassAndWorldCatalog
import com.example.domain.model.EnemyEncounter
import com.example.domain.model.EnemyTier
import com.example.domain.model.RegionInfo
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
fun WorldLoreScreen(
    uiState: AeldrinUiState,
    onEnterOpenWorld: () -> Unit,
    onLaunchEncounter: (EnemyEncounter) -> Unit,
    onToggleLightArmor: () -> Unit,
    onToggleLunarEclipse: () -> Unit,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var loreExpanded by remember { mutableStateOf(false) }
    val save = uiState.save
    val stats = uiState.stats
    val defeated = save.defeatedBossIds()
    val purified = save.purifiedBossIds()

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
            // Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_aeldrin_world),
                        contentDescription = "Paisagem dos Sete Pilares da Alma em Aeldrin",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        VoidObsidian.copy(alpha = 0.72f),
                                        VoidObsidian.copy(alpha = 0.96f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "AELDRIN · PROTÓTIPO OFFLINE",
                            style = MaterialTheme.typography.labelMedium,
                            color = GildedAmber
                        )
                        Text(
                            text = "Os Sete Pilares da Alma",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "5 Classes · 30 Subclasses · 300 Habilidades · 11+ Chefes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Enter Open-World 3D CTA
            item {
                Button(
                    onClick = onEnterOpenWorld,
                    colors = ButtonDefaults.buttonColors(containerColor = SoulEmerald, contentColor = VoidObsidian),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("enter_open_world_button")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Explorar Mundo Aberto 3D (Zonas, Acampamentos & Vila)", fontWeight = FontWeight.Bold)
                }
            }

            // Viajante Marcado Status Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${save.heroName} · Nível ${save.level}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = GildedAmber
                                )
                                Text(
                                    text = "${stats.heroClass.displayName} — ${stats.activeSubclass.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = stats.heroClass.accentColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = onResetProgress,
                                modifier = Modifier.testTag("reset_save_button")
                            ) {
                                Icon(
                                    Icons.Filled.RestartAlt,
                                    contentDescription = "Reiniciar progresso do protótipo",
                                    tint = TextSecondary
                                )
                            }
                        }

                        // XP bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "XP ${save.xp}/${stats.xpToNextLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Fragmentos: ${save.shards} · Secretas: ${save.unlockedSecretIds().size}/9",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AstralViolet
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (save.xp.toFloat() / stats.xpToNextLevel.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = GildedAmber,
                                trackColor = ElevatedCard
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatPill("HP", "${stats.maxHp}", SoulEmerald)
                            StatPill("Mana", "${stats.maxMana}", AstralViolet)
                            StatPill("ATK", "${stats.atk}", CrimsonBlood)
                            StatPill("DEF", "${stats.def}", GildedAmber)
                            StatPill("Crit", "${stats.critPercent}%", ParadoxMagenta)
                        }

                        // Challenge Toggles for Secret Classes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = save.lightArmorChallengeEnabled,
                                onClick = onToggleLightArmor,
                                label = { Text("Espada de Madeira", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GildedAmber.copy(alpha = 0.22f),
                                    selectedLabelColor = GildedAmber
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("toggle_light_armor_chip")
                            )
                            FilterChip(
                                selected = save.lunarEclipseModeEnabled,
                                onClick = onToggleLunarEclipse,
                                label = { Text("Eclipse Lunar", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AstralViolet.copy(alpha = 0.25f),
                                    selectedLabelColor = AstralViolet
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("toggle_lunar_eclipse_chip")
                            )
                        }
                    }
                }
            }

            // Main Lore Expandable Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElevatedCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { loreExpanded = !loreExpanded }
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ClassAndWorldCatalog.mainLoreHeadline,
                                style = MaterialTheme.typography.titleMedium,
                                color = GildedAmber,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (loreExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Expandir história de Aeldrin",
                                tint = GildedAmber
                            )
                        }
                        AnimatedVisibility(visible = loreExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = ClassAndWorldCatalog.mainLoreBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                ClassAndWorldCatalog.factions.forEach { (faction, desc) ->
                                    Text(
                                        text = "• $faction: $desc",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Regions & Boss Encounters
            items(ClassAndWorldCatalog.regions, key = { it.id }) { region ->
                val regionEncounters = ClassAndWorldCatalog.encounters.filter { it.regionId == region.id }
                RegionEncounterCard(
                    region = region,
                    encounters = regionEncounters,
                    defeatedIds = defeated,
                    purifiedIds = purified,
                    onLaunchEncounter = onLaunchEncounter
                )
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ElevatedCard)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RegionEncounterCard(
    region: RegionInfo,
    encounters: List<EnemyEncounter>,
    defeatedIds: Set<String>,
    purifiedIds: Set<String>,
    onLaunchEncounter: (EnemyEncounter) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, region.accentColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = region.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = region.accentColor
                    )
                    Text(
                        text = region.subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    color = region.accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = region.factionFocus,
                        style = MaterialTheme.typography.labelSmall,
                        color = region.accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = region.loreSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            encounters.forEach { encounter ->
                val isDefeated = encounter.id in defeatedIds
                val isPurified = encounter.id in purifiedIds
                val tierColor = when (encounter.tier) {
                    EnemyTier.SUB_BOSS -> GildedAmber
                    EnemyTier.MAIN_BOSS -> CrimsonBlood
                    EnemyTier.SECRET_TRIAL -> ParadoxMagenta
                    EnemyTier.FINAL_RAID -> AstralViolet
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElevatedCard)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
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
                                text = encounter.tier.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = tierColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "· Nv.${encounter.recommendedLevel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            if (isDefeated) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Derrotado",
                                    tint = if (isPurified) SoulEmerald else GildedAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = encounter.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = encounter.mechanicHint,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                    Button(
                        onClick = { onLaunchEncounter(encounter) },
                        colors = ButtonDefaults.buttonColors(containerColor = tierColor),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("fight_${encounter.id}")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Lutar", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
