package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.ClassAndWorldCatalog
import com.example.domain.model.EnemyEncounter
import com.example.domain.model.HeroClass
import com.example.domain.model.HeroSkin
import com.example.domain.model.SecretClassId
import com.example.domain.model.SubclassInfo
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
fun ClassesScreen(
    uiState: AeldrinUiState,
    onSelectHeroClass: (HeroClass) -> Unit,
    onSelectHeroSkin: (HeroSkin) -> Unit,
    onEquipSubclass: (SubclassInfo) -> Unit,
    onStartSecretTrialFight: (EnemyEncounter) -> Unit,
    onOpenStarPuzzle: () -> Unit,
    onOpenStealthTrial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val save = uiState.save
    val currentClass = uiState.stats.heroClass
    val unlockedSecrets = save.unlockedSecretIds()

    val standardForClass = ClassAndWorldCatalog.subclasses.filter {
        !it.isSecret && it.parentClass == currentClass
    }
    val allSecrets = ClassAndWorldCatalog.subclasses.filter { it.isSecret }

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
            // Crest Header Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DeepSlate)
                        .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_grimoire_crest),
                        contentDescription = "Emblema das 5 Classes de Aeldrin",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, GildedAmber.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "5 Classes & 30 Subclasses",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GildedAmber
                        )
                        Text(
                            text = "21 evoluções padrão + 9 subclasses secretas desbloqueadas por feitos e provações.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Desbloqueadas: ${unlockedSecrets.size}/9 Secretas",
                            style = MaterialTheme.typography.labelMedium,
                            color = AstralViolet
                        )
                    }
                }
            }

            // 5 Classic Classes Selector
            item {
                Text(
                    text = "Classes Clássicas",
                    style = MaterialTheme.typography.titleMedium,
                    color = GildedAmber
                )
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HeroClass.entries) { heroClass ->
                        val selected = heroClass == currentClass
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectHeroClass(heroClass) },
                            label = {
                                Text(heroClass.displayName, style = MaterialTheme.typography.labelLarge)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = heroClass.accentColor.copy(alpha = 0.25f),
                                selectedLabelColor = heroClass.accentColor
                            ),
                            modifier = Modifier.testTag("select_class_${heroClass.name}")
                        )
                    }
                }
            }

            // 3D Character Skin Selector
            item {
                Text(
                    text = "Skins 3D do Personagem",
                    style = MaterialTheme.typography.titleMedium,
                    color = AstralViolet
                )
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HeroSkin.entries) { skin ->
                        val selected = skin == uiState.stats.activeSkin
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectHeroSkin(skin) },
                            label = {
                                Text(skin.displayName, style = MaterialTheme.typography.labelSmall)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = skin.accentGlow.copy(alpha = 0.24f),
                                selectedLabelColor = skin.accentGlow
                            ),
                            modifier = Modifier.testTag("classes_skin_${skin.id}")
                        )
                    }
                }
            }

            // Selected Class Summary
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElevatedCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, currentClass.accentColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${currentClass.displayName} — ${currentClass.title}",
                            style = MaterialTheme.typography.titleLarge,
                            color = currentClass.accentColor
                        )
                        Text(
                            text = currentClass.archetypeSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Standard Subclasses for Selected Class
            item {
                Text(
                    text = "Subclasses Padrão de ${currentClass.displayName} (${standardForClass.size}/21)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(standardForClass, key = { it.id }) { sub ->
                val isEquipped = save.activeSubclassId == sub.id
                val canEquip = save.level >= sub.requiredLevel
                SubclassCard(
                    subclass = sub,
                    isEquipped = isEquipped,
                    isUnlocked = canEquip,
                    progressNote = if (canEquip) "Disponível no Nível ${save.level}" else "Requer Nível ${sub.requiredLevel}",
                    onEquip = { onEquipSubclass(sub) },
                    trialActionLabel = null,
                    onTrialAction = null
                )
            }

            // All 9 Secret Subclasses
            item {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = ParadoxMagenta)
                    Text(
                        text = "As 9 Subclasses Secretas de Aeldrin",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ParadoxMagenta
                    )
                }
            }

            items(allSecrets, key = { it.id }) { secret ->
                val isUnlocked = secret.id in unlockedSecrets
                val isEquipped = save.activeSubclassId == secret.id

                val progressText = when (secret.id) {
                    SecretClassId.TIRANO_DE_SANGUE.id ->
                        "Progresso: ${save.lowHpWinCount}/1 vitória sobrevivendo em HP crítico (<8%)"
                    SecretClassId.QUEBRADOR_DE_CEUS.id ->
                        "Parries Perfeitos: ${save.perfectParriesCount}/10 (Ative Espada de Madeira no Mundo)"
                    SecretClassId.ARQUIMAGO_CAOS.id ->
                        "Sobrecargas de Mana sobrevividas: ${save.manaOverloadsSurvived}/3"
                    SecretClassId.ESCRIBA_ESTRELAS.id ->
                        "5 Lunetas: ${if (save.lunetasAlignedDone) "Alinhadas" else "Pendente"} + Guardião Astral"
                    SecretClassId.CAMINHANTE_VEU.id ->
                        "Infiltração de Nyx: ${if (save.nyxStealthDone) "Concluída" else "Pendente"}"
                    SecretClassId.CEIFADOR_CARMESIM.id ->
                        "Turnos com Veneno + Sangramento: ${save.dualDotStacksCount}/10 ou Sombra das Catacumbas"
                    SecretClassId.CACADOR_DRAGOES.id ->
                        "Finalize Draco/Sera enquanto Ponto Fraco Aéreo estiver aberto"
                    SecretClassId.ARQUEIRO_FANTASMA.id ->
                        "Ative Eclipse Lunar e vença no Santuário Submerso"
                    SecretClassId.APOSTOLO_CREPUSCULO.id ->
                        "Reduza Arauto Solenis ou Malakor <35% HP e use Purificar Alma"
                    else -> ""
                }

                val (trialLabel, trialAction: (() -> Unit)?) = when (secret.id) {
                    SecretClassId.ESCRIBA_ESTRELAS.id -> "Alinhar 5 Lunetas" to onOpenStarPuzzle
                    SecretClassId.CAMINHANTE_VEU.id -> "Infiltrar em Nyx" to onOpenStealthTrial
                    SecretClassId.CEIFADOR_CARMESIM.id -> "Duelar Sombra" to {
                        onStartSecretTrialFight(ClassAndWorldCatalog.encounters.first { it.id == "trial_sombra_catacumbas" })
                    }
                    SecretClassId.CACADOR_DRAGOES.id -> "Caçar Draco" to {
                        onStartSecretTrialFight(ClassAndWorldCatalog.encounters.first { it.id == "trial_draco_tempestades" })
                    }
                    SecretClassId.ARQUEIRO_FANTASMA.id -> "Santuário Submerso" to {
                        onStartSecretTrialFight(ClassAndWorldCatalog.encounters.first { it.id == "trial_espectro_submerso" })
                    }
                    SecretClassId.APOSTOLO_CREPUSCULO.id -> "Salvar Arauto" to {
                        onStartSecretTrialFight(ClassAndWorldCatalog.encounters.first { it.id == "sub_arauto_solenis" })
                    }
                    SecretClassId.QUEBRADOR_DE_CEUS.id -> "Treinar Parries" to {
                        onStartSecretTrialFight(ClassAndWorldCatalog.encounters.first { it.id == "sub_kael7" })
                    }
                    SecretClassId.TIRANO_DE_SANGUE.id -> "Provação Kael-Dorn" to {
                        onStartSecretTrialFight(ClassAndWorldCatalog.encounters.first { it.id == "sub_kael7" })
                    }
                    SecretClassId.ARQUIMAGO_CAOS.id -> "Fenda de Mirath" to {
                        onStartSecretTrialFight(ClassAndWorldCatalog.encounters.first { it.id == "sub_relojoeiro" })
                    }
                    else -> null to null
                }

                SubclassCard(
                    subclass = secret,
                    isEquipped = isEquipped,
                    isUnlocked = isUnlocked,
                    progressNote = progressText,
                    onEquip = { onEquipSubclass(secret) },
                    trialActionLabel = if (!isUnlocked) trialLabel else null,
                    onTrialAction = if (!isUnlocked) trialAction else null
                )
            }
        }
    }
}

@Composable
private fun SubclassCard(
    subclass: SubclassInfo,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    progressNote: String,
    onEquip: () -> Unit,
    trialActionLabel: String?,
    onTrialAction: (() -> Unit)?
) {
    val borderColor = when {
        isEquipped -> SoulEmerald
        subclass.isSecret && isUnlocked -> GildedAmber
        subclass.isSecret -> ParadoxMagenta.copy(alpha = 0.45f)
        else -> CardBorder
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = subclass.parentClass.accentColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = subclass.parentClass.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = subclass.parentClass.accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (subclass.isSecret) {
                            Text(
                                text = "SECRETA · ${subclass.trialRegion}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParadoxMagenta,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = subclass.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isEquipped) SoulEmerald else MaterialTheme.colorScheme.onSurface
                    )
                }

                when {
                    isEquipped -> Icon(Icons.Filled.CheckCircle, contentDescription = "Equipada", tint = SoulEmerald)
                    !isUnlocked -> Icon(Icons.Filled.Lock, contentDescription = "Bloqueada", tint = TextSecondary)
                }
            }

            Text(
                text = subclass.passiveDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = GildedAmber
            )
            Text(
                text = subclass.unlockCondition,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = progressNote,
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnlocked) SoulEmerald else AstralViolet
            )
            Text(
                text = "Bônus: +${subclass.hpBonus} HP · +${subclass.manaBonus} Mana · +${subclass.atkBonus} ATK · +${subclass.defBonus} DEF · +${subclass.critBonus}% Crit",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isUnlocked && !isEquipped) {
                    Button(
                        onClick = onEquip,
                        colors = ButtonDefaults.buttonColors(containerColor = GildedAmber, contentColor = VoidObsidian),
                        modifier = Modifier.testTag("equip_subclass_${subclass.id}")
                    ) {
                        Text("Equipar Subclasse")
                    }
                }
                if (trialActionLabel != null && onTrialAction != null) {
                    OutlinedButton(
                        onClick = onTrialAction,
                        modifier = Modifier.testTag("trial_button_${subclass.id}")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text(trialActionLabel)
                    }
                }
            }
        }
    }
}
