package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.catalog.ClassAndWorldCatalog
import com.example.data.catalog.SkillCatalog
import com.example.domain.model.Skill
import com.example.domain.model.SkillCategory
import com.example.ui.theme.AstralViolet
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepSlate
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.SoulEmerald
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AeldrinUiState

@Composable
fun GrimoireScreen(
    uiState: AeldrinUiState,
    onEquipSkillToSlot: (Skill, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SkillCategory?>(null) }
    var skillToEquip by remember { mutableStateOf<Skill?>(null) }

    val save = uiState.save
    val equippedIds = remember(save.equippedSkillIdsCsv) { save.equippedSkillIds().toSet() }
    val unlockedSecrets = remember(save.unlockedSecretClassIdsCsv) { save.unlockedSecretIds() }

    val filteredSkills by remember(searchQuery, selectedCategory) {
        derivedStateOf {
            SkillCatalog.allSkills.filter { skill ->
                val matchesCat = selectedCategory == null || skill.category == selectedCategory
                val q = searchQuery.trim()
                val matchesQuery = q.isEmpty() ||
                    skill.name.contains(q, ignoreCase = true) ||
                    skill.code.contains(q, ignoreCase = true) ||
                    skill.description.contains(q, ignoreCase = true) ||
                    skill.evolutionForms.any { it.contains(q, ignoreCase = true) }
                matchesCat && matchesQuery
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
        ) {
            // Header + Loadout Strip + Search + Category Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepSlate)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Grimório das 300 Habilidades",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GildedAmber
                    )
                    Text(
                        text = "${filteredSkills.size}/300",
                        style = MaterialTheme.typography.labelMedium,
                        color = AstralViolet
                    )
                }

                // Active 5-Slot Preview
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(uiState.equippedSkills, key = { it.id }) { eq ->
                        Surface(
                            color = ElevatedCard,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, eq.category.accentColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "#${eq.code} ${eq.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = eq.category.accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nome, #001–300, efeito...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grimoire_search_input")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("Todas (300)", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GildedAmber.copy(alpha = 0.22f),
                                selectedLabelColor = GildedAmber
                            )
                        )
                    }
                    items(SkillCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                            label = {
                                Text("${cat.label} (${cat.rangeLabel})", style = MaterialTheme.typography.labelSmall)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cat.accentColor.copy(alpha = 0.22f),
                                selectedLabelColor = cat.accentColor
                            ),
                            modifier = Modifier.testTag("filter_category_${cat.name}")
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSkills, key = { it.id }) { skill ->
                    val isLockedSecret = skill.requiredSubclassId != null && skill.requiredSubclassId !in unlockedSecrets
                    val isEquipped = skill.id in equippedIds
                    val subclassReqName = skill.requiredSubclassId?.let { reqId ->
                        ClassAndWorldCatalog.subclasses.find { it.id == reqId }?.name
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DeepSlate)
                            .border(
                                1.dp,
                                if (isEquipped) SoulEmerald else skill.category.accentColor.copy(alpha = 0.35f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(enabled = !isLockedSecret) { skillToEquip = skill }
                            .padding(12.dp)
                            .testTag("grimoire_skill_${skill.id}"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "#${skill.code}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = skill.category.accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = skill.category.accentColor.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = skill.category.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = skill.category.accentColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                skill.ownerClass?.let { cls ->
                                    Text(
                                        text = cls.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cls.accentColor
                                    )
                                }
                            }

                            when {
                                isEquipped -> Icon(Icons.Filled.CheckCircle, contentDescription = "Equipada", tint = SoulEmerald, modifier = Modifier.size(20.dp))
                                isLockedSecret -> Icon(Icons.Filled.Lock, contentDescription = "Bloqueada", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                else -> Icon(Icons.Filled.AddCircle, contentDescription = "Equipar habilidade", tint = GildedAmber, modifier = Modifier.size(20.dp))
                            }
                        }

                        Text(
                            text = skill.displayNameForLevel(save.level, uiState.stats.activeSubclass.name),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = skill.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        if (skill.evolutionForms.size == 3) {
                            Text(
                                text = "Evolução: ${skill.evolutionForms.joinToString(" → ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoulEmerald
                            )
                        }
                        if (subclassReqName != null) {
                            Text(
                                text = if (isLockedSecret) "Requer Classe Secreta: $subclassReqName" else "Arte de $subclassReqName",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isLockedSecret) skill.category.accentColor else SoulEmerald
                            )
                        }
                        Text(
                            text = "Custo: ${skill.manaCost} Mana · Recarga: ${skill.cooldownTurns}t · Multiplicador: ${skill.powerMultiplier}x",
                            style = MaterialTheme.typography.labelSmall,
                            color = AstralViolet
                        )
                    }
                }
            }
        }
    }

    skillToEquip?.let { selected ->
        AlertDialog(
            onDismissRequest = { skillToEquip = null },
            containerColor = DeepSlate,
            title = {
                Text("Equipar #${selected.code} ${selected.name}", style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Escolha em qual dos 5 slots de combate deseja colocar esta habilidade:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    (0 until 5).forEach { slotIdx ->
                        val currentInSlot = uiState.equippedSkills.getOrNull(slotIdx)
                        val isSame = currentInSlot?.id == selected.id
                        if (isSame) {
                            Button(
                                onClick = {
                                    onEquipSkillToSlot(selected, slotIdx)
                                    skillToEquip = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoulEmerald),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Slot ${slotIdx + 1}: #${currentInSlot?.code} ${currentInSlot?.name}")
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    onEquipSkillToSlot(selected, slotIdx)
                                    skillToEquip = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("equip_slot_$slotIdx")
                            ) {
                                Text("Slot ${slotIdx + 1}: ${currentInSlot?.let { "#${it.code} ${it.name}" } ?: "Vazio"}")
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { skillToEquip = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
