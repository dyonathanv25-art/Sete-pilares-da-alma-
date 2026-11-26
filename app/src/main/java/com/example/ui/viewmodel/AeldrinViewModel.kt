package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.ClassAndWorldCatalog
import com.example.data.catalog.SkillCatalog
import com.example.data.local.AeldrinDatabase
import com.example.data.local.PlayerSaveEntity
import com.example.data.repository.GameRepository
import com.example.domain.model.CombatPlayMode
import com.example.domain.model.EnemyEncounter
import com.example.domain.model.HeroClass
import com.example.domain.model.HeroSkin
import com.example.domain.model.SecretClassId
import com.example.domain.model.Skill
import com.example.domain.model.SkillCategory
import com.example.domain.model.SkillEffectType
import com.example.domain.model.SubclassInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

enum class EnemyIntentType(val label: String) {
    NORMAL_STRIKE("Ataque Direto"),
    HEAVY_TELEGRAPHED("Golpe Anunciado (Parry!)"),
    AERIAL_DIVE("Voo Rasante (Ponto Fraco!)"),
    CURSE_PULSE("Pulso de Maldição"),
    TIME_DISTORTION("Distorção Temporal"),
    FROZEN("Congelado no Loop")
}

enum class BattleOutcome {
    ONGOING,
    VICTORY_SLAIN,
    VICTORY_PURIFIED,
    DEFEAT
}

enum class CombatAnimEvent {
    IDLE,
    HERO_SLASH,
    HERO_MAGIC,
    HERO_PARRY,
    HERO_PURIFY,
    HERO_PARADOX,
    HERO_OVERLOAD
}

data class ComputedHeroStats(
    val heroClass: HeroClass,
    val activeSubclass: SubclassInfo,
    val activeSkin: HeroSkin,
    val maxHp: Int,
    val maxMana: Int,
    val atk: Int,
    val def: Int,
    val critPercent: Int,
    val xpToNextLevel: Int
)

data class CombatLogEntry(
    val id: Long,
    val turn: Int,
    val message: String,
    val isHighlight: Boolean = false
)

data class CombatState(
    val encounter: EnemyEncounter = ClassAndWorldCatalog.encounters.first(),
    val turn: Int = 1,
    val playerHp: Int = 420,
    val playerMaxHp: Int = 420,
    val playerMana: Int = 100,
    val playerMaxMana: Int = 100,
    val playerShield: Int = 0,
    val enemyHp: Int = 260,
    val enemyMaxHp: Int = 260,
    val enemyPoisonTurns: Int = 0,
    val enemyBleedTurns: Int = 0,
    val enemyFreezeTurns: Int = 0,
    val enemyAerialWeakPointOpen: Boolean = false,
    val enemyIntent: EnemyIntentType = EnemyIntentType.NORMAL_STRIKE,
    val enemyIntentDamage: Int = 24,
    val skillCooldowns: Map<Int, Int> = emptyMap(),
    val inversionTurns: Int = 0,
    val undoDeathActive: Boolean = false,
    val overloadBuffActive: Boolean = false,
    val turnsSinceLastHeal: Int = 5,
    val wasBelowFivePercentThisFight: Boolean = false,
    val lastUsedSkillId: Int? = null,
    val spellCastCount: Int = 0,
    val outcome: BattleOutcome = BattleOutcome.ONGOING,
    val newlyUnlockedSecret: SubclassInfo? = null,
    val lastAnimEvent: CombatAnimEvent = CombatAnimEvent.IDLE,
    val lastAnimSeq: Long = 0L,
    val lastDamageDealt: Int = 0,
    val lastHealOrShield: Int = 0,
    val logs: List<CombatLogEntry> = listOf(
        CombatLogEntry(1L, 1, "Sentinela Kael-7 bloqueia os portões de Kael-Dorn. Prepare sua postura!", true)
    )
)

data class StarPuzzleState(
    val isOpen: Boolean = false,
    val lunetaAngles: List<Int> = listOf(1, 2, 3, 1, 2), // Target is 0 for all 5
    val solved: Boolean = false
)

data class StealthTrialState(
    val isOpen: Boolean = false,
    val step: Int = 0, // 0..5
    val suspicion: Int = 0, // 0..100; >= 100 fails
    val lastActionNote: String = "Você entrou nos becos de Nyx sem ser notado. Atravesse 5 setores sem alertar nem matar guardas.",
    val completed: Boolean = false,
    val failed: Boolean = false
)

data class AeldrinUiState(
    val save: PlayerSaveEntity = PlayerSaveEntity(),
    val stats: ComputedHeroStats = ComputedHeroStats(
        heroClass = HeroClass.GUERREIRO,
        activeSubclass = ClassAndWorldCatalog.subclasses.first(),
        activeSkin = HeroSkin.CLASS_DEFAULT,
        maxHp = 420,
        maxMana = 100,
        atk = 40,
        def = 38,
        critPercent = 12,
        xpToNextLevel = 120
    ),
    val equippedSkills: List<Skill> = emptyList(),
    val combatPlayMode: CombatPlayMode = CombatPlayMode.OPEN_WORLD,
    val combat: CombatState = CombatState(),
    val starPuzzle: StarPuzzleState = StarPuzzleState(),
    val stealthTrial: StealthTrialState = StealthTrialState(),
    val bannerNotification: String? = null
)

class AeldrinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository = GameRepository(
        AeldrinDatabase.getInstance(application).playerSaveDao()
    )

    private val combatFlow = MutableStateFlow(CombatState())
    private val combatModeFlow = MutableStateFlow(CombatPlayMode.OPEN_WORLD)
    private val starPuzzleFlow = MutableStateFlow(StarPuzzleState())
    private val stealthTrialFlow = MutableStateFlow(StealthTrialState())
    private val bannerFlow = MutableStateFlow<String?>(null)
    private var logSequence = 2L

    private val dialogPairFlow = combine(starPuzzleFlow, stealthTrialFlow) { star, stealth -> star to stealth }

    val uiState: StateFlow<AeldrinUiState> = combine(
        repository.playerSave,
        combatModeFlow,
        combatFlow,
        dialogPairFlow,
        bannerFlow
    ) { saveOrNull, mode, combat, dialogs, banner ->
        val save = saveOrNull ?: PlayerSaveEntity()
        val stats = computeStats(save)
        val equipped = save.equippedSkillIds().mapNotNull { SkillCatalog.getById(it) }
        AeldrinUiState(
            save = save,
            stats = stats,
            equippedSkills = equipped,
            combatPlayMode = mode,
            combat = combat,
            starPuzzle = dialogs.first,
            stealthTrial = dialogs.second,
            bannerNotification = banner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AeldrinUiState()
    )

    init {
        viewModelScope.launch {
            val initialSave = repository.ensureInitialized()
            val stats = computeStats(initialSave)
            startEncounterInternal(ClassAndWorldCatalog.encounters.first(), stats)
        }
    }

    fun dismissBanner() {
        bannerFlow.value = null
    }

    fun computeStats(save: PlayerSaveEntity): ComputedHeroStats {
        val heroClass = runCatching { HeroClass.valueOf(save.heroClassName) }
            .getOrDefault(HeroClass.GUERREIRO)
        val subclass = ClassAndWorldCatalog.subclasses.find { it.id == save.activeSubclassId }
            ?: ClassAndWorldCatalog.subclasses.first { it.parentClass == heroClass }

        val lvlOffset = max(0, save.level - 1)
        val rawHp = heroClass.baseHp + subclass.hpBonus + lvlOffset * 24
        val rawMana = heroClass.baseMana + subclass.manaBonus + lvlOffset * 9
        var atk = heroClass.baseAtk + subclass.atkBonus + lvlOffset * 4
        var def = heroClass.baseDef + subclass.defBonus + lvlOffset * 2
        var crit = heroClass.baseCritPercent + subclass.critBonus

        if (save.lightArmorChallengeEnabled) {
            def = max(4, def / 2)
            atk += 7
            crit += 10
        }
        if (save.lunarEclipseModeEnabled) {
            crit += 15
        }

        val activeSkin = HeroSkin.entries.find { it.id == save.activeHeroSkinId } ?: HeroSkin.CLASS_DEFAULT

        return ComputedHeroStats(
            heroClass = heroClass,
            activeSubclass = subclass,
            activeSkin = activeSkin,
            maxHp = rawHp,
            maxMana = rawMana,
            atk = atk,
            def = def,
            critPercent = crit.coerceAtMost(85),
            xpToNextLevel = save.level * 120
        )
    }

    fun selectHeroSkin(skin: HeroSkin) {
        viewModelScope.launch {
            repository.updateSave { it.copy(activeHeroSkinId = skin.id) }
            bannerFlow.value = "Skin 3D equipada: ${skin.displayName}"
        }
    }

    fun selectHeroClass(heroClass: HeroClass) {
        viewModelScope.launch {
            val defaultSubclass = ClassAndWorldCatalog.subclasses.first {
                it.parentClass == heroClass && !it.isSecret
            }
            val defaultSkills = SkillCatalog.defaultLoadoutFor(heroClass).joinToString(",")
            var updatedSave = PlayerSaveEntity()
            repository.updateSave { current ->
                updatedSave = current.copy(
                    heroClassName = heroClass.name,
                    activeSubclassId = defaultSubclass.id,
                    equippedSkillIdsCsv = defaultSkills
                )
                updatedSave
            }
            val newStats = computeStats(updatedSave)
            startEncounterInternal(combatFlow.value.encounter, newStats)
            bannerFlow.value = "Classe alterada para ${heroClass.displayName} (${defaultSubclass.name})."
        }
    }

    fun equipSubclass(subclass: SubclassInfo) {
        val save = uiState.value.save
        if (subclass.isSecret && subclass.id !in save.unlockedSecretIds()) {
            bannerFlow.value = "Subclasse secreta bloqueada: ${subclass.unlockCondition}"
            return
        }
        if (!subclass.isSecret && save.level < subclass.requiredLevel) {
            bannerFlow.value = "Requer Nível ${subclass.requiredLevel} para equipar ${subclass.name}."
            return
        }
        viewModelScope.launch {
            var updatedSave = save
            repository.updateSave { current ->
                val newLoadout = if (current.heroClassName != subclass.parentClass.name) {
                    SkillCatalog.defaultLoadoutFor(subclass.parentClass).joinToString(",")
                } else {
                    current.equippedSkillIdsCsv
                }
                updatedSave = current.copy(
                    heroClassName = subclass.parentClass.name,
                    activeSubclassId = subclass.id,
                    equippedSkillIdsCsv = newLoadout
                )
                updatedSave
            }
            val stats = computeStats(updatedSave)
            startEncounterInternal(combatFlow.value.encounter, stats)
            bannerFlow.value = "${subclass.name} equipada!"
        }
    }

    fun equipSkillToSlot(skill: Skill, slotIndex: Int) {
        val save = uiState.value.save
        if (skill.requiredSubclassId != null && skill.requiredSubclassId !in save.unlockedSecretIds()) {
            bannerFlow.value = "Desbloqueie a classe secreta correspondente para equipar ${skill.name}."
            return
        }
        viewModelScope.launch {
            repository.updateSave { current ->
                val list = current.equippedSkillIds().toMutableList()
                while (list.size < 5) list.add(1)
                val existingIdx = list.indexOf(skill.id)
                if (existingIdx in 0..4) {
                    val temp = list[slotIndex]
                    list[slotIndex] = skill.id
                    list[existingIdx] = temp
                } else {
                    list[slotIndex] = skill.id
                }
                current.copy(equippedSkillIdsCsv = list.joinToString(","))
            }
            bannerFlow.value = "#${skill.code} ${skill.name} equipada no Slot ${slotIndex + 1}."
        }
    }

    fun toggleLightArmorChallenge() {
        viewModelScope.launch {
            var updated = uiState.value.save
            repository.updateSave { current ->
                updated = current.copy(lightArmorChallengeEnabled = !current.lightArmorChallengeEnabled)
                updated
            }
            val mode = if (updated.lightArmorChallengeEnabled) "ATIVADO" else "DESATIVADO"
            bannerFlow.value = "Desafio Espada de Madeira / Sem Armadura Pesada $mode."
        }
    }

    fun toggleLunarEclipseMode() {
        viewModelScope.launch {
            var updated = uiState.value.save
            repository.updateSave { current ->
                updated = current.copy(lunarEclipseModeEnabled = !current.lunarEclipseModeEnabled)
                updated
            }
            val mode = if (updated.lunarEclipseModeEnabled) "ATIVADO" else "DESATIVADO"
            bannerFlow.value = "Modo Eclipse Lunar (Sem Encantamentos) $mode."
        }
    }

    fun setCombatPlayMode(mode: CombatPlayMode) {
        combatModeFlow.value = mode
    }

    fun startEncounter(encounter: EnemyEncounter) {
        combatModeFlow.value = CombatPlayMode.BOSS_ARENA
        startEncounterInternal(encounter, uiState.value.stats)
    }

    private fun startEncounterInternal(encounter: EnemyEncounter, stats: ComputedHeroStats) {
        combatFlow.value = CombatState(
            encounter = encounter,
            turn = 1,
            playerHp = stats.maxHp,
            playerMaxHp = stats.maxHp,
            playerMana = stats.maxMana,
            playerMaxMana = stats.maxMana,
            playerShield = 0,
            enemyHp = encounter.maxHp,
            enemyMaxHp = encounter.maxHp,
            enemyIntent = EnemyIntentType.NORMAL_STRIKE,
            enemyIntentDamage = encounter.attackPower,
            logs = listOf(
                CombatLogEntry(
                    id = logSequence++,
                    turn = 1,
                    message = "${encounter.name} (${encounter.tier.label}) entrou na arena! ${encounter.mechanicHint}",
                    isHighlight = true
                )
            )
        )
    }

    fun performBasicAttack() {
        val state = combatFlow.value
        if (state.outcome != BattleOutcome.ONGOING) return
        val stats = uiState.value.stats
        val save = uiState.value.save

        var dmg = calculateDamage(
            baseAtk = stats.atk,
            multiplier = 1.0f,
            enemyDef = state.encounter.defense,
            critPercent = stats.critPercent,
            ignoreDefense = save.lunarEclipseModeEnabled,
            hasAerialBonus = state.enemyAerialWeakPointOpen,
            isOverloaded = state.overloadBuffActive
        )
        if (state.enemyPoisonTurns > 0 && state.enemyBleedTurns > 0 && save.activeSubclassId == SecretClassId.CEIFADOR_CARMESIM.id) {
            dmg = (dmg * 1.4f).roundToInt()
        }

        val logs = mutableListOf<String>()
        logs += "Ataque básico causou $dmg de dano em ${state.encounter.name}."
        val manaRecovered = min(stats.maxMana, state.playerMana + 12)

        advanceCombatAfterPlayerAction(
            state = state.copy(
                playerMana = manaRecovered,
                overloadBuffActive = false,
                turnsSinceLastHeal = state.turnsSinceLastHeal + 1
            ),
            damageDealt = dmg,
            healedAmount = 0,
            shieldGained = 0,
            parriedThisTurn = false,
            purifyAttempted = false,
            usedSkill = null,
            actionLogs = logs,
            skipEnemyTurn = true
        )
    }

    fun performOutOfRangeSwing() {
        val state = combatFlow.value
        if (state.outcome != BattleOutcome.ONGOING) return
        combatFlow.update { cur ->
            cur.copy(
                lastAnimEvent = CombatAnimEvent.HERO_SLASH,
                lastAnimSeq = cur.lastAnimSeq + 1L,
                lastDamageDealt = 0,
                lastHealOrShield = 0,
                logs = (cur.logs + CombatLogEntry(logSequence++, cur.turn, "Alvo fora de alcance! Aproxime-se com o analógico ou use Esquiva.")).takeLast(16)
            )
        }
    }

    fun performParryStance() {
        val state = combatFlow.value
        if (state.outcome != BattleOutcome.ONGOING) return
        val stats = uiState.value.stats
        val isTelegraphed = state.enemyIntent == EnemyIntentType.HEAVY_TELEGRAPHED ||
            state.enemyIntent == EnemyIntentType.AERIAL_DIVE

        val logs = mutableListOf<String>()
        val counterDmg = if (isTelegraphed) {
            logs += "PARRY PERFEITO! Você aparou o golpe anunciado e contra-atacou!"
            (stats.atk * 1.65f).roundToInt()
        } else {
            logs += "Postura defensiva erguida (-55% dano recebido neste turno)."
            (stats.atk * 0.55f).roundToInt()
        }

        if (isTelegraphed) {
            viewModelScope.launch {
                var unlockedNow: SubclassInfo? = null
                repository.updateSave { current ->
                    val newParries = current.perfectParriesCount + 1
                    val secrets = current.unlockedSecretIds().toMutableSet()
                    if (newParries >= 10 && current.lightArmorChallengeEnabled && SecretClassId.QUEBRADOR_DE_CEUS.id !in secrets) {
                        secrets += SecretClassId.QUEBRADOR_DE_CEUS.id
                        unlockedNow = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.QUEBRADOR_DE_CEUS.id }
                    } else if (newParries >= 10 && SecretClassId.QUEBRADOR_DE_CEUS.id !in secrets) {
                        secrets += SecretClassId.QUEBRADOR_DE_CEUS.id
                        unlockedNow = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.QUEBRADOR_DE_CEUS.id }
                    }
                    current.copy(
                        perfectParriesCount = newParries,
                        unlockedSecretClassIdsCsv = secrets.joinToString(",")
                    )
                }
                unlockedNow?.let { sub ->
                    combatFlow.update { it.copy(newlyUnlockedSecret = sub) }
                    bannerFlow.value = "Classe Secreta Desbloqueada: ${sub.name}!"
                }
            }
        }

        advanceCombatAfterPlayerAction(
            state = state.copy(turnsSinceLastHeal = state.turnsSinceLastHeal + 1),
            damageDealt = counterDmg,
            healedAmount = 0,
            shieldGained = if (isTelegraphed) 0 else stats.def,
            parriedThisTurn = true,
            purifyAttempted = false,
            usedSkill = null,
            actionLogs = logs,
            skipEnemyTurn = true
        )
    }

    fun triggerManaOverload() {
        val state = combatFlow.value
        if (state.outcome != BattleOutcome.ONGOING) return
        val recoil = max(12, (state.playerMaxHp * 0.10f).roundToInt())
        val newHp = max(1, state.playerHp - recoil)
        val newMana = min(state.playerMaxMana, state.playerMana + 55)

        viewModelScope.launch {
            var unlockedNow: SubclassInfo? = null
            repository.updateSave { current ->
                val newOverloads = current.manaOverloadsSurvived + 1
                val secrets = current.unlockedSecretIds().toMutableSet()
                if (newOverloads >= 3 && SecretClassId.ARQUIMAGO_CAOS.id !in secrets) {
                    secrets += SecretClassId.ARQUIMAGO_CAOS.id
                    unlockedNow = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.ARQUIMAGO_CAOS.id }
                }
                current.copy(
                    manaOverloadsSurvived = newOverloads,
                    unlockedSecretClassIdsCsv = secrets.joinToString(",")
                )
            }
            unlockedNow?.let { sub ->
                combatFlow.update { it.copy(newlyUnlockedSecret = sub) }
                bannerFlow.value = "Classe Secreta Desbloqueada: ${sub.name}!"
            }
        }

        combatFlow.update { cur ->
            cur.copy(
                playerHp = newHp,
                playerMana = newMana,
                overloadBuffActive = true,
                logs = (cur.logs + CombatLogEntry(
                    id = logSequence++,
                    turn = cur.turn,
                    message = "SOBRECARGA DE MANA! -$recoil HP por recuo, +55 Mana e próximo golpe +40% poder!",
                    isHighlight = true
                )).takeLast(14)
            )
        }
    }

    fun attemptPurifySoul() {
        val state = combatFlow.value
        if (state.outcome != BattleOutcome.ONGOING) return
        if (!state.encounter.isCursed) {
            bannerFlow.value = "Este alvo não está sob maldição purificável."
            return
        }
        val hpRatio = state.enemyHp.toFloat() / state.enemyMaxHp.toFloat()
        if (hpRatio > 0.35f) {
            bannerFlow.value = "Enfraqueça ${state.encounter.name} abaixo de 35% HP antes de purificar!"
            return
        }

        val logs = listOf("Você canalizou Luz e Trevas para purificar ${state.encounter.name} em vez de matá-lo!")
        advanceCombatAfterPlayerAction(
            state = state,
            damageDealt = 0,
            healedAmount = 25,
            shieldGained = 0,
            parriedThisTurn = false,
            purifyAttempted = true,
            usedSkill = null,
            actionLogs = logs,
            skipEnemyTurn = true
        )
    }

    fun useSkill(skill: Skill) {
        val state = combatFlow.value
        if (state.outcome != BattleOutcome.ONGOING) return
        val remainingCd = state.skillCooldowns[skill.id] ?: 0
        if (remainingCd > 0) {
            bannerFlow.value = "${skill.name} em recarga ($remainingCd turnos)."
            return
        }

        val stats = uiState.value.stats
        val save = uiState.value.save

        // If player doesn't have enough mana, allow casting with automatic Mana Overload!
        var overloadedNow = false
        var hpAfterRecoil = state.playerHp
        val manaAfterCast = if (state.playerMana < skill.manaCost) {
            overloadedNow = true
            val recoil = max(10, (state.playerMaxHp * 0.12f).roundToInt())
            hpAfterRecoil = max(1, state.playerHp - recoil)
            0
        } else {
            state.playerMana - skill.manaCost
        }

        if (overloadedNow) {
            viewModelScope.launch {
                var unlockedNow: SubclassInfo? = null
                repository.updateSave { current ->
                    val newCount = current.manaOverloadsSurvived + 1
                    val secrets = current.unlockedSecretIds().toMutableSet()
                    if (newCount >= 3 && SecretClassId.ARQUIMAGO_CAOS.id !in secrets) {
                        secrets += SecretClassId.ARQUIMAGO_CAOS.id
                        unlockedNow = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.ARQUIMAGO_CAOS.id }
                    }
                    current.copy(
                        manaOverloadsSurvived = newCount,
                        unlockedSecretClassIdsCsv = secrets.joinToString(",")
                    )
                }
                unlockedNow?.let { sub ->
                    combatFlow.update { it.copy(newlyUnlockedSecret = sub) }
                    bannerFlow.value = "Classe Secreta Desbloqueada: ${sub.name}!"
                }
            }
        }

        val nextCastCount = state.spellCastCount + 1
        val starGuaranteeCrit = save.activeSubclassId == SecretClassId.ESCRIBA_ESTRELAS.id && nextCastCount % 3 == 0
        val effectiveSkill = if (skill.effectType == SkillEffectType.PARADOX_ECHO && state.lastUsedSkillId != null) {
            SkillCatalog.getById(state.lastUsedSkillId) ?: skill
        } else {
            skill
        }
        val echoBoost = if (skill.effectType == SkillEffectType.PARADOX_ECHO) 1.8f else 1.0f

        val logs = mutableListOf<String>()
        if (overloadedNow) {
            logs += "Mana insuficiente -> Sobrecarga forçada ao lançar ${skill.name}!"
        }

        var dmg = 0
        var heal = 0
        var shield = 0
        var poisonToAdd = 0
        var bleedToAdd = 0
        var parried = false
        var purify = false
        var newInversion = state.inversionTurns
        var newUndoDeath = state.undoDeathActive
        var newFreeze = state.enemyFreezeTurns
        var newEnemyHpOverride: Int? = null
        var newPlayerHpOverride: Int? = null
        var resetCooldowns = false
        var manaBonus = 0

        val mult = effectiveSkill.powerMultiplier * echoBoost *
            (if (skill.category == SkillCategory.EVOLUTIVA) 1.0f + save.level * 0.04f else 1.0f)

        when (effectiveSkill.effectType) {
            SkillEffectType.DAMAGE -> {
                dmg = calculateDamage(
                    stats.atk, mult, state.encounter.defense,
                    if (starGuaranteeCrit) 100 else stats.critPercent,
                    save.lunarEclipseModeEnabled,
                    state.enemyAerialWeakPointOpen,
                    state.overloadBuffActive || overloadedNow
                )
                logs += "${skill.displayNameForLevel(save.level, stats.activeSubclass.name)} causou $dmg de dano!"
            }
            SkillEffectType.HEAL -> {
                heal = (stats.atk * mult * 1.45f).roundToInt()
                if (state.inversionTurns > 0) {
                    dmg += heal
                    logs += "Inversão Causal! Sua cura causou $heal de dano!"
                } else {
                    logs += "${skill.name} restaurou $heal de HP."
                }
            }
            SkillEffectType.SHIELD -> {
                shield = if (save.lunarEclipseModeEnabled) 0 else (stats.def * mult * 1.5f).roundToInt()
                logs += if (save.lunarEclipseModeEnabled) {
                    "Eclipse Lunar ativo: escudos bloqueados!"
                } else {
                    "${skill.name} concedeu +$shield de Escudo."
                }
            }
            SkillEffectType.DOT_POISON -> {
                dmg = calculateDamage(stats.atk, mult * 0.85f, state.encounter.defense, stats.critPercent, save.lunarEclipseModeEnabled, state.enemyAerialWeakPointOpen, state.overloadBuffActive)
                poisonToAdd = 3
                logs += "${skill.name} causou $dmg e aplicou Veneno (3 turnos)."
            }
            SkillEffectType.DOT_BLEED -> {
                dmg = calculateDamage(stats.atk, mult * 0.85f, state.encounter.defense, stats.critPercent, save.lunarEclipseModeEnabled, state.enemyAerialWeakPointOpen, state.overloadBuffActive)
                bleedToAdd = 3
                logs += "${skill.name} causou $dmg e aplicou Sangramento (3 turnos)."
            }
            SkillEffectType.DUAL_DOT -> {
                dmg = calculateDamage(stats.atk, mult * 0.9f, state.encounter.defense, stats.critPercent, save.lunarEclipseModeEnabled, state.enemyAerialWeakPointOpen, state.overloadBuffActive)
                poisonToAdd = 3
                bleedToAdd = 3
                logs += "${skill.name} causou $dmg e aplicou Veneno + Sangramento simultâneos!"
            }
            SkillEffectType.PARRY_COUNTER -> {
                parried = true
                dmg = calculateDamage(stats.atk, mult * 1.25f, state.encounter.defense, stats.critPercent, true, state.enemyAerialWeakPointOpen, state.overloadBuffActive)
                logs += "${skill.name} ergueu contra-ataque e causou $dmg!"
            }
            SkillEffectType.PURIFY -> {
                purify = true
                heal = (stats.atk * 1.1f).roundToInt()
                dmg = calculateDamage(stats.atk, mult * 0.8f, state.encounter.defense, stats.critPercent, false, false, false)
                logs += "${skill.name} purificou debuffs, curou $heal HP e causou $dmg de luz."
            }
            SkillEffectType.MANA_SURGE -> {
                manaBonus = 50
                dmg = calculateDamage(stats.atk, mult * 0.95f, state.encounter.defense, stats.critPercent, save.lunarEclipseModeEnabled, state.enemyAerialWeakPointOpen, true)
                logs += "${skill.name} restaurou +50 Mana e golpeou por $dmg!"
            }
            SkillEffectType.PARADOX_INVERSION -> {
                newInversion = 2
                logs += "PARADOXO #291: Inversão de Causa e Efeito! Dano sofrido vira cura por 2 turnos!"
            }
            SkillEffectType.PARADOX_SWAP_HP -> {
                val pPct = hpAfterRecoil.toFloat() / state.playerMaxHp.toFloat()
                val ePct = state.enemyHp.toFloat() / state.enemyMaxHp.toFloat()
                newPlayerHpOverride = (state.playerMaxHp * ePct).roundToInt().coerceIn(1, state.playerMaxHp)
                val targetEnemyHp = (state.enemyMaxHp * pPct).roundToInt().coerceIn(1, state.enemyMaxHp)
                val maxDelta = (state.enemyMaxHp * 0.45f).roundToInt()
                newEnemyHpOverride = max(targetEnemyHp, state.enemyHp - maxDelta)
                logs += "PARADOXO #292: Troca de % de Vida executada!"
            }
            SkillEffectType.PARADOX_RESET_CD -> {
                resetCooldowns = true
                manaBonus = (state.playerMaxMana * 0.65f).roundToInt()
                logs += "PARADOXO #293: Reset Absoluto zerou recargas e restaurou Mana!"
            }
            SkillEffectType.PARADOX_SWAP_STATS -> {
                dmg = stats.def * 4 + calculateDamage(stats.atk, 1.5f, 0, stats.critPercent, true, state.enemyAerialWeakPointOpen, true)
                logs += "PARADOXO #294: Defesa convertida em ataque verdadeiro: $dmg de dano!"
            }
            SkillEffectType.PARADOX_UNDO_DEATH -> {
                newUndoDeath = true
                shield = 30
                logs += "PARADOXO #295: Âncora Temporal armada — Desfazer Morte ativo!"
            }
            SkillEffectType.PARADOX_TIME_FREEZE -> {
                newFreeze = 2
                dmg = calculateDamage(stats.atk, mult, state.encounter.defense, stats.critPercent, true, state.enemyAerialWeakPointOpen, false)
                logs += "PARADOXO #296: Chefe congelado no loop de 7 minutos por 2 turnos ($dmg dano)!"
            }
            SkillEffectType.PARADOX_PIERCE_SEAL -> {
                dmg = calculateDamage(stats.atk, mult, 0, 100, true, state.enemyAerialWeakPointOpen, true)
                logs += "PARADOXO #297: Selo Quebrado ignorou toda defesa e cravou $dmg de dano verdadeiro!"
            }
            SkillEffectType.PARADOX_ECHO -> {
                dmg = calculateDamage(stats.atk, 2.1f, state.encounter.defense, stats.critPercent, true, state.enemyAerialWeakPointOpen, true)
                logs += "PARADOXO #298: Eco Sem-Rosto replicou poder anterior por $dmg!"
            }
            SkillEffectType.PARADOX_TWILIGHT_ECLIPSE -> {
                heal = (state.playerMaxHp * 0.35f).roundToInt()
                dmg = heal * 2
                logs += "PARADOXO #299: Eclipse do Crepúsculo curou $heal e causou $dmg sombrio-luminoso!"
            }
            SkillEffectType.PARADOX_DECREE -> {
                dmg = calculateDamage(stats.atk + save.level * 5, mult, 0, stats.critPercent, true, state.enemyAerialWeakPointOpen, true)
                logs += "PARADOXO #300: Decreto do Viajante Marcado detonou os Sete Pilares por $dmg!"
            }
        }

        val updatedCooldowns = if (resetCooldowns) {
            emptyMap()
        } else {
            state.skillCooldowns.toMutableMap().apply {
                if (skill.cooldownTurns > 0) put(skill.id, skill.cooldownTurns + 1)
            }
        }

        val updatedState = state.copy(
            playerHp = newPlayerHpOverride ?: hpAfterRecoil,
            playerMana = min(state.playerMaxMana, manaAfterCast + manaBonus),
            enemyHp = newEnemyHpOverride ?: state.enemyHp,
            enemyPoisonTurns = state.enemyPoisonTurns + poisonToAdd,
            enemyBleedTurns = state.enemyBleedTurns + bleedToAdd,
            enemyFreezeTurns = newFreeze,
            skillCooldowns = updatedCooldowns,
            inversionTurns = newInversion,
            undoDeathActive = newUndoDeath,
            overloadBuffActive = false,
            turnsSinceLastHeal = if (heal > 0) 0 else state.turnsSinceLastHeal + 1,
            lastUsedSkillId = if (skill.effectType != SkillEffectType.PARADOX_ECHO) skill.id else state.lastUsedSkillId,
            spellCastCount = nextCastCount
        )

        advanceCombatAfterPlayerAction(
            state = updatedState,
            damageDealt = dmg,
            healedAmount = heal,
            shieldGained = shield,
            parriedThisTurn = parried,
            purifyAttempted = purify,
            usedSkill = skill,
            actionLogs = logs,
            skipEnemyTurn = true
        )
    }

    private fun advanceCombatAfterPlayerAction(
        state: CombatState,
        damageDealt: Int,
        healedAmount: Int,
        shieldGained: Int,
        parriedThisTurn: Boolean,
        purifyAttempted: Boolean,
        usedSkill: Skill?,
        actionLogs: List<String>,
        skipEnemyTurn: Boolean = false
    ) {
        val stats = uiState.value.stats
        val save = uiState.value.save
        val logs = state.logs.toMutableList()
        var currentTurn = state.turn
        var playerHp = min(state.playerMaxHp, state.playerHp + healedAmount)
        var playerShield = state.playerShield + shieldGained
        var enemyHp = max(0, state.enemyHp - damageDealt)
        var poisonTurns = state.enemyPoisonTurns
        var bleedTurns = state.enemyBleedTurns
        var freezeTurns = state.enemyFreezeTurns
        var inversionTurns = state.inversionTurns
        var undoDeath = state.undoDeathActive
        var below5 = state.wasBelowFivePercentThisFight || (playerHp.toFloat() / state.playerMaxHp.toFloat() <= 0.08f)

        val animEvent = when {
            purifyAttempted -> CombatAnimEvent.HERO_PURIFY
            parriedThisTurn -> CombatAnimEvent.HERO_PARRY
            usedSkill?.category == SkillCategory.PARADOXAL -> CombatAnimEvent.HERO_PARADOX
            usedSkill != null -> CombatAnimEvent.HERO_MAGIC
            else -> CombatAnimEvent.HERO_SLASH
        }
        val animSeq = state.lastAnimSeq + 1L

        actionLogs.forEach { msg ->
            logs += CombatLogEntry(logSequence++, currentTurn, msg, isHighlight = damageDealt >= stats.atk * 2 || purifyAttempted)
        }

        // Apply Tirano de Sangue lifesteal passive
        if (save.activeSubclassId == SecretClassId.TIRANO_DE_SANGUE.id && damageDealt > 0 && playerHp < state.playerMaxHp * 0.4f) {
            val drain = max(5, (damageDealt * 0.25f).roundToInt())
            playerHp = min(state.playerMaxHp, playerHp + drain)
            logs += CombatLogEntry(logSequence++, currentTurn, "Tirano de Sangue drenou +$drain HP.")
        }

        // Check Purification Victory!
        if (purifyAttempted && state.encounter.isCursed && enemyHp <= (state.enemyMaxHp * 0.35f)) {
            logs += CombatLogEntry(logSequence++, currentTurn, "ALMA PURIFICADA! ${state.encounter.name} foi salvo das trevas!", isHighlight = true)
            finalizeBattleVictory(
                purified = true,
                finalState = state.copy(
                    playerHp = playerHp,
                    enemyHp = enemyHp,
                    outcome = BattleOutcome.VICTORY_PURIFIED,
                    logs = logs.takeLast(16)
                )
            )
            return
        }

        // Check DoT ticks on Enemy
        var dualDotThisTurn = false
        if (enemyHp > 0 && (poisonTurns > 0 || bleedTurns > 0)) {
            if (poisonTurns > 0 && bleedTurns > 0) dualDotThisTurn = true
            val dotDmg = (if (poisonTurns > 0) 14 else 0) + (if (bleedTurns > 0) 16 else 0)
            enemyHp = max(0, enemyHp - dotDmg)
            if (poisonTurns > 0) poisonTurns--
            if (bleedTurns > 0) bleedTurns--
            logs += CombatLogEntry(logSequence++, currentTurn, "Veneno/Sangramento corroeram $dotDmg HP do chefe.")
        }

        if (dualDotThisTurn) {
            viewModelScope.launch {
                var unlockedNow: SubclassInfo? = null
                repository.updateSave { current ->
                    val count = current.dualDotStacksCount + 1
                    val secrets = current.unlockedSecretIds().toMutableSet()
                    if (count >= 10 && SecretClassId.CEIFADOR_CARMESIM.id !in secrets) {
                        secrets += SecretClassId.CEIFADOR_CARMESIM.id
                        unlockedNow = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.CEIFADOR_CARMESIM.id }
                    }
                    current.copy(
                        dualDotStacksCount = count,
                        unlockedSecretClassIdsCsv = secrets.joinToString(",")
                    )
                }
                unlockedNow?.let { sub ->
                    combatFlow.update { it.copy(newlyUnlockedSecret = sub) }
                    bannerFlow.value = "Classe Secreta Desbloqueada: ${sub.name}!"
                }
            }
        }

        // Enemy Defeated?
        if (enemyHp <= 0) {
            logs += CombatLogEntry(logSequence++, currentTurn, "${state.encounter.name} foi derrotado!", isHighlight = true)
            finalizeBattleVictory(
                purified = false,
                finalState = state.copy(
                    playerHp = playerHp,
                    enemyHp = 0,
                    wasBelowFivePercentThisFight = below5,
                    outcome = BattleOutcome.VICTORY_SLAIN,
                    logs = logs.takeLast(16)
                )
            )
            return
        }

        // Enemy Turn (only if not handled asynchronously by real-time 3D Boss AI)
        if (!skipEnemyTurn) {
            if (freezeTurns > 0) {
                freezeTurns--
                logs += CombatLogEntry(logSequence++, currentTurn, "${state.encounter.name} está congelado na Lacuna Temporal!", isHighlight = true)
            } else {
                var incoming = state.enemyIntentDamage
                if (parriedThisTurn) {
                    incoming = if (state.enemyIntent == EnemyIntentType.HEAVY_TELEGRAPHED || state.enemyIntent == EnemyIntentType.AERIAL_DIVE) {
                        (incoming * 0.15f).roundToInt()
                    } else {
                        (incoming * 0.45f).roundToInt()
                    }
                }
                val mitigated = max(4, incoming - (stats.def / 3))

                if (inversionTurns > 0) {
                    playerHp = min(state.playerMaxHp, playerHp + mitigated)
                    logs += CombatLogEntry(logSequence++, currentTurn, "Inversão Causal converteu $mitigated de dano em CURA!", isHighlight = true)
                } else {
                    var remainingDmg = mitigated
                    if (playerShield > 0) {
                        val absorbed = min(playerShield, remainingDmg)
                        playerShield -= absorbed
                        remainingDmg -= absorbed
                    }
                    playerHp = max(0, playerHp - remainingDmg)
                    logs += CombatLogEntry(logSequence++, currentTurn, "${state.encounter.name} usou ${state.enemyIntent.label} (-$remainingDmg HP).")
                }
            }
        }

        if (playerHp.toFloat() / state.playerMaxHp.toFloat() <= 0.08f) {
            below5 = true
        }

        // Undo Death Check (#295)
        if (playerHp <= 0 && undoDeath) {
            undoDeath = false
            playerHp = (state.playerMaxHp * 0.45f).roundToInt()
            logs += CombatLogEntry(logSequence++, currentTurn, "DESFAZER MORTE ativado! Você retornou da beira da morte com $playerHp HP!", isHighlight = true)
        }

        if (playerHp <= 0) {
            logs += CombatLogEntry(logSequence++, currentTurn, "Você tombou em combate... Reorganize suas habilidades e tente novamente.", isHighlight = true)
            combatFlow.value = state.copy(
                playerHp = 0,
                enemyHp = enemyHp,
                outcome = BattleOutcome.DEFEAT,
                logs = logs.takeLast(16)
            )
            return
        }

        if (inversionTurns > 0) inversionTurns--
        val nextTurn = currentTurn + 1

        // Tick down cooldowns
        val tickedCooldowns = state.skillCooldowns.mapValues { (_, cd) -> max(0, cd - 1) }
            .filterValues { it > 0 }

        // Choose next boss intent
        val (nextIntent, nextDmg, aerialOpen) = rollNextIntent(state.encounter, nextTurn, freezeTurns)

        combatFlow.value = state.copy(
            turn = nextTurn,
            playerHp = playerHp,
            playerMana = min(state.playerMaxMana, state.playerMana + 8),
            playerShield = playerShield,
            enemyHp = enemyHp,
            enemyPoisonTurns = poisonTurns,
            enemyBleedTurns = bleedTurns,
            enemyFreezeTurns = freezeTurns,
            enemyAerialWeakPointOpen = aerialOpen,
            enemyIntent = nextIntent,
            enemyIntentDamage = nextDmg,
            skillCooldowns = tickedCooldowns,
            inversionTurns = inversionTurns,
            undoDeathActive = undoDeath,
            wasBelowFivePercentThisFight = below5,
            lastAnimEvent = animEvent,
            lastAnimSeq = animSeq,
            lastDamageDealt = damageDealt,
            lastHealOrShield = healedAmount + shieldGained,
            logs = logs.takeLast(16)
        )
    }

    fun resolveRealTimeBossStrike(
        playerInDangerZone: Boolean,
        playerParried: Boolean,
        playerDodged: Boolean
    ) {
        val state = combatFlow.value
        if (state.outcome != BattleOutcome.ONGOING) return
        val stats = uiState.value.stats
        val logs = state.logs.toMutableList()
        var playerHp = state.playerHp
        var playerShield = state.playerShield
        var freezeTurns = state.enemyFreezeTurns
        var inversionTurns = state.inversionTurns
        var undoDeath = state.undoDeathActive
        var below5 = state.wasBelowFivePercentThisFight
        var enemyHp = state.enemyHp
        var counterDamage = 0

        if (freezeTurns > 0) {
            freezeTurns--
            logs += CombatLogEntry(logSequence++, state.turn, "${state.encounter.name} está congelado na Lacuna Temporal!", isHighlight = true)
        } else if (playerDodged || !playerInDangerZone) {
            logs += CombatLogEntry(logSequence++, state.turn, "ESQUIVA! Você saiu da área de impacto de ${state.encounter.name}!", isHighlight = true)
        } else if (playerParried) {
            counterDamage = (stats.atk * 1.5f).roundToInt()
            enemyHp = max(0, enemyHp - counterDamage)
            logs += CombatLogEntry(logSequence++, state.turn, "PARRY PERFEITO na arena 3D! Contra-ataque de $counterDamage!", isHighlight = true)
            viewModelScope.launch {
                var unlockedNow: SubclassInfo? = null
                repository.updateSave { current ->
                    val newParries = current.perfectParriesCount + 1
                    val secrets = current.unlockedSecretIds().toMutableSet()
                    if (newParries >= 10 && SecretClassId.QUEBRADOR_DE_CEUS.id !in secrets) {
                        secrets += SecretClassId.QUEBRADOR_DE_CEUS.id
                        unlockedNow = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.QUEBRADOR_DE_CEUS.id }
                    }
                    current.copy(
                        perfectParriesCount = newParries,
                        unlockedSecretClassIdsCsv = secrets.joinToString(",")
                    )
                }
                unlockedNow?.let { sub ->
                    combatFlow.update { it.copy(newlyUnlockedSecret = sub) }
                    bannerFlow.value = "Classe Secreta Desbloqueada: ${sub.name}!"
                }
            }
        } else {
            val mitigated = max(5, state.enemyIntentDamage - (stats.def / 3))
            if (inversionTurns > 0) {
                playerHp = min(state.playerMaxHp, playerHp + mitigated)
                logs += CombatLogEntry(logSequence++, state.turn, "Inversão Causal converteu $mitigated de dano em CURA!", isHighlight = true)
            } else {
                var remainingDmg = mitigated
                if (playerShield > 0) {
                    val absorbed = min(playerShield, remainingDmg)
                    playerShield -= absorbed
                    remainingDmg -= absorbed
                }
                playerHp = max(0, playerHp - remainingDmg)
                logs += CombatLogEntry(logSequence++, state.turn, "${state.encounter.name} acertou ${state.enemyIntent.label} (-$remainingDmg HP).")
            }
        }

        if (playerHp.toFloat() / state.playerMaxHp.toFloat() <= 0.08f) {
            below5 = true
        }
        if (playerHp <= 0 && undoDeath) {
            undoDeath = false
            playerHp = (state.playerMaxHp * 0.45f).roundToInt()
            logs += CombatLogEntry(logSequence++, state.turn, "DESFAZER MORTE ativado! Reviveu com $playerHp HP!", isHighlight = true)
        }
        if (playerHp <= 0) {
            logs += CombatLogEntry(logSequence++, state.turn, "Você tombou na arena 3D... Tente novamente!", isHighlight = true)
            combatFlow.value = state.copy(
                playerHp = 0,
                enemyHp = enemyHp,
                outcome = BattleOutcome.DEFEAT,
                logs = logs.takeLast(16)
            )
            return
        }
        if (enemyHp <= 0) {
            finalizeBattleVictory(
                purified = false,
                finalState = state.copy(
                    playerHp = playerHp,
                    enemyHp = 0,
                    wasBelowFivePercentThisFight = below5,
                    outcome = BattleOutcome.VICTORY_SLAIN,
                    logs = logs.takeLast(16)
                )
            )
            return
        }

        if (inversionTurns > 0) inversionTurns--
        val nextTurn = state.turn + 1
        val tickedCooldowns = state.skillCooldowns.mapValues { (_, cd) -> max(0, cd - 1) }.filterValues { it > 0 }
        val (nextIntent, nextDmg, aerialOpen) = rollNextIntent(state.encounter, nextTurn, freezeTurns)

        combatFlow.value = state.copy(
            turn = nextTurn,
            playerHp = playerHp,
            playerMana = min(state.playerMaxMana, state.playerMana + 6),
            playerShield = playerShield,
            enemyHp = enemyHp,
            enemyFreezeTurns = freezeTurns,
            enemyAerialWeakPointOpen = aerialOpen,
            enemyIntent = nextIntent,
            enemyIntentDamage = nextDmg,
            skillCooldowns = tickedCooldowns,
            inversionTurns = inversionTurns,
            undoDeathActive = undoDeath,
            wasBelowFivePercentThisFight = below5,
            lastDamageDealt = counterDamage,
            logs = logs.takeLast(16)
        )
    }

    private fun rollNextIntent(
        encounter: EnemyEncounter,
        turn: Int,
        freezeTurns: Int
    ): Triple<EnemyIntentType, Int, Boolean> {
        if (freezeTurns > 0) {
            return Triple(EnemyIntentType.FROZEN, 0, false)
        }
        return when {
            encounter.hasAerialWeakPoint && turn % 3 == 0 ->
                Triple(EnemyIntentType.AERIAL_DIVE, (encounter.attackPower * 1.45f).roundToInt(), true)
            encounter.isCursed && turn % 3 == 2 ->
                Triple(EnemyIntentType.CURSE_PULSE, (encounter.attackPower * 1.25f).roundToInt(), false)
            turn % 2 == 0 ->
                Triple(EnemyIntentType.HEAVY_TELEGRAPHED, (encounter.attackPower * 1.55f).roundToInt(), false)
            else ->
                Triple(EnemyIntentType.NORMAL_STRIKE, encounter.attackPower, false)
        }
    }

    private fun finalizeBattleVictory(purified: Boolean, finalState: CombatState) {
        viewModelScope.launch {
            var unlockedSecret: SubclassInfo? = null
            var leveledUpTo: Int? = null
            repository.updateSave { current ->
                val secrets = current.unlockedSecretIds().toMutableSet()
                val defeated = current.defeatedBossIds().toMutableSet()
                val purifiedSet = current.purifiedBossIds().toMutableSet()

                defeated += finalState.encounter.id
                if (purified) purifiedSet += finalState.encounter.id

                // Check Secret Class Triggers on Victory!
                if (purified && SecretClassId.APOSTOLO_CREPUSCULO.id !in secrets) {
                    secrets += SecretClassId.APOSTOLO_CREPUSCULO.id
                    unlockedSecret = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.APOSTOLO_CREPUSCULO.id }
                }
                if (finalState.wasBelowFivePercentThisFight && finalState.turnsSinceLastHeal >= 2 && SecretClassId.TIRANO_DE_SANGUE.id !in secrets) {
                    secrets += SecretClassId.TIRANO_DE_SANGUE.id
                    unlockedSecret = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.TIRANO_DE_SANGUE.id }
                }
                if (finalState.enemyAerialWeakPointOpen && finalState.encounter.hasAerialWeakPoint && SecretClassId.CACADOR_DRAGOES.id !in secrets) {
                    secrets += SecretClassId.CACADOR_DRAGOES.id
                    unlockedSecret = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.CACADOR_DRAGOES.id }
                }
                if (current.lunarEclipseModeEnabled && (finalState.encounter.id == "trial_espectro_submerso" || finalState.encounter.regionId == "mirath") && SecretClassId.ARQUEIRO_FANTASMA.id !in secrets) {
                    secrets += SecretClassId.ARQUEIRO_FANTASMA.id
                    unlockedSecret = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.ARQUEIRO_FANTASMA.id }
                }
                if (finalState.encounter.id == "trial_guardiao_astral" && current.lunetasAlignedDone && SecretClassId.ESCRIBA_ESTRELAS.id !in secrets) {
                    secrets += SecretClassId.ESCRIBA_ESTRELAS.id
                    unlockedSecret = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.ESCRIBA_ESTRELAS.id }
                }
                if (finalState.encounter.id == "trial_sombra_catacumbas" && SecretClassId.CEIFADOR_CARMESIM.id !in secrets) {
                    secrets += SecretClassId.CEIFADOR_CARMESIM.id
                    unlockedSecret = ClassAndWorldCatalog.subclasses.find { it.id == SecretClassId.CEIFADOR_CARMESIM.id }
                }

                // XP & Level-up
                var newXp = current.xp + finalState.encounter.xpReward
                var newLevel = current.level
                while (newXp >= newLevel * 120) {
                    newXp -= newLevel * 120
                    newLevel++
                    leveledUpTo = newLevel
                }

                current.copy(
                    level = newLevel,
                    xp = newXp,
                    shards = current.shards + finalState.encounter.shardReward,
                    unlockedSecretClassIdsCsv = secrets.joinToString(","),
                    defeatedBossIdsCsv = defeated.joinToString(","),
                    purifiedBossIdsCsv = purifiedSet.joinToString(","),
                    lowHpWinCount = if (finalState.wasBelowFivePercentThisFight) current.lowHpWinCount + 1 else current.lowHpWinCount
                )
            }

            combatFlow.value = finalState.copy(newlyUnlockedSecret = unlockedSecret)
            unlockedSecret?.let {
                bannerFlow.value = "SEGREDO REVELADO: ${it.name} desbloqueada!"
            } ?: leveledUpTo?.let { lvl ->
                bannerFlow.value = "Nível $lvl alcançado! Habilidades Evolutivas fortalecidas."
            }
        }
    }

    private fun calculateDamage(
        baseAtk: Int,
        multiplier: Float,
        enemyDef: Int,
        critPercent: Int,
        ignoreDefense: Boolean,
        hasAerialBonus: Boolean,
        isOverloaded: Boolean
    ): Int {
        val effectiveDef = if (ignoreDefense) 0 else enemyDef / 2
        var raw = max(8, (baseAtk * multiplier).roundToInt() - effectiveDef)
        if (Random.nextInt(100) < critPercent) {
            raw = (raw * 1.55f).roundToInt()
        }
        if (hasAerialBonus) {
            raw = (raw * 1.35f).roundToInt()
        }
        if (isOverloaded) {
            raw = (raw * 1.40f).roundToInt()
        }
        return raw
    }

    // --- Interactive Secret Trials ---
    fun openStarPuzzle() {
        starPuzzleFlow.value = StarPuzzleState(
            isOpen = true,
            lunetaAngles = listOf(1, 2, 3, 2, 1),
            solved = false
        )
    }

    fun closeStarPuzzle() {
        starPuzzleFlow.update { it.copy(isOpen = false) }
    }

    fun rotateLuneta(index: Int) {
        starPuzzleFlow.update { cur ->
            val updated = cur.lunetaAngles.toMutableList()
            updated[index] = (updated[index] + 1) % 4
            val isSolved = updated.all { it == 0 }
            if (isSolved) {
                viewModelScope.launch {
                    repository.updateSave { it.copy(lunetasAlignedDone = true) }
                }
            }
            cur.copy(lunetaAngles = updated, solved = isSolved)
        }
    }

    fun launchAstralGuardianAfterPuzzle() {
        closeStarPuzzle()
        val astralBoss = ClassAndWorldCatalog.encounters.first { it.id == "trial_guardiao_astral" }
        startEncounter(astralBoss)
        bannerFlow.value = "5 Lunetas alinhadas! Derrote o Guardião Astral para despertar Escriba das Estrelas."
    }

    fun openStealthTrial() {
        stealthTrialFlow.value = StealthTrialState(isOpen = true)
    }

    fun closeStealthTrial() {
        stealthTrialFlow.update { it.copy(isOpen = false) }
    }

    fun advanceStealthStep(silentApproach: Boolean) {
        stealthTrialFlow.update { cur ->
            if (cur.completed || cur.failed) return@update cur
            val suspicionGain = if (silentApproach) Random.nextInt(8, 20) else Random.nextInt(25, 42)
            val newSuspicion = (cur.suspicion + suspicionGain).coerceAtMost(100)
            val nextStep = cur.step + 1

            when {
                newSuspicion >= 100 -> cur.copy(
                    suspicion = 100,
                    failed = true,
                    lastActionNote = "Um vigia da Guilda ouviu seus passos! Reinicie a infiltração sem ser detectado."
                )
                nextStep >= 5 -> {
                    viewModelScope.launch {
                        repository.updateSave { save ->
                            val secrets = save.unlockedSecretIds().toMutableSet()
                            secrets += SecretClassId.CAMINHANTE_VEU.id
                            save.copy(
                                nyxStealthDone = true,
                                unlockedSecretClassIdsCsv = secrets.joinToString(",")
                            )
                        }
                        bannerFlow.value = "Provação de Nyx concluída! Caminhante do Véu (Sem-Rosto) desbloqueado!"
                    }
                    cur.copy(
                        step = 5,
                        suspicion = newSuspicion,
                        completed = true,
                        lastActionNote = "Você cruzou os 5 setores de Nyx sem deixar vestígios e sem eliminar nenhum guarda!"
                    )
                }
                else -> cur.copy(
                    step = nextStep,
                    suspicion = newSuspicion,
                    lastActionNote = if (silentApproach) {
                        "Setor $nextStep/5 superado nas sombras (+${suspicionGain}% suspeita)."
                    } else {
                        "Você usou distração arriscada no Setor $nextStep/5 (+${suspicionGain}% suspeita)."
                    }
                )
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetSave()
            val fresh = PlayerSaveEntity()
            startEncounterInternal(ClassAndWorldCatalog.encounters.first(), computeStats(fresh))
            bannerFlow.value = "Progresso reiniciado para o início da jornada."
        }
    }

    // --- Open-World Simulation Callbacks ---
    fun appendOpenWorldLog(message: String, isHighlight: Boolean = false) {
        combatFlow.update { cur ->
            cur.copy(
                logs = (cur.logs + CombatLogEntry(logSequence++, cur.turn, message, isHighlight)).takeLast(16)
            )
        }
    }

    fun openWorldPlayerDamaged(damage: Int, attackerName: String) {
        val stats = uiState.value.stats
        val mitigated = max(3, damage - (stats.def / 4))
        combatFlow.update { cur ->
            var remaining = mitigated
            var shield = cur.playerShield
            if (shield > 0) {
                val absorbed = min(shield, remaining)
                shield -= absorbed
                remaining -= absorbed
            }
            val newHp = max(0, cur.playerHp - remaining)
            cur.copy(
                playerHp = newHp,
                playerShield = shield,
                logs = (cur.logs + CombatLogEntry(logSequence++, cur.turn, "$attackerName acertou você (-$remaining HP).")).takeLast(16)
            )
        }
    }

    fun openWorldMonsterDefeated(name: String, xpReward: Int, shardReward: Int) {
        grantOpenWorldRewards(xpReward, shardReward, "$name derrotado! (+$xpReward XP · +$shardReward Frag)")
    }

    fun openWorldClaimCampChest(campName: String, xpReward: Int, shardReward: Int) {
        grantOpenWorldRewards(xpReward, shardReward, "Baú de $campName aberto! +$xpReward XP · +$shardReward Fragmentos!")
        bannerFlow.value = "Baú saqueado em $campName (+$xpReward XP, +$shardReward Fragmentos)!"
    }

    fun openWorldRestAtVillage() {
        val stats = uiState.value.stats
        combatFlow.update { cur ->
            cur.copy(
                playerHp = stats.maxHp,
                playerMaxHp = stats.maxHp,
                playerMana = stats.maxMana,
                playerMaxMana = stats.maxMana,
                skillCooldowns = emptyMap(),
                outcome = BattleOutcome.ONGOING,
                logs = (cur.logs + CombatLogEntry(logSequence++, cur.turn, "Descanso na Fogueira da Vila: HP e Mana restaurados!", true)).takeLast(16)
            )
        }
        bannerFlow.value = "HP e Mana restaurados na Fogueira da Vila Central!"
    }

    fun openWorldUpgradeAtForge() {
        val save = uiState.value.save
        if (save.shards < 30) {
            bannerFlow.value = "Precisa de 30 Fragmentos para forjar na Vila (você tem ${save.shards})."
            return
        }
        grantOpenWorldRewards(xpReward = 90, shardReward = -30, logMsg = "Forja da Vila aprimorou seu equipamento! (-30 Frag, +90 XP)")
        bannerFlow.value = "Equipamento forjado! (+90 XP de maestria)"
    }

    fun openWorldInvasionResolved(victory: Boolean) {
        if (victory) {
            grantOpenWorldRewards(140, 75, "VILA SALVA! Recompensa de defesa: +140 XP · +75 Fragmentos!")
            bannerFlow.value = "Vila Central defendida! +140 XP e +75 Fragmentos!"
        } else {
            appendOpenWorldLog("O Cristal da Vila foi danificado pela horda, mas os guardas reagruparam!", true)
            bannerFlow.value = "A invasão recuou após danificar o Cristal da Vila."
        }
    }

    private fun grantOpenWorldRewards(xpReward: Int, shardReward: Int, logMsg: String) {
        viewModelScope.launch {
            var leveledUpTo: Int? = null
            repository.updateSave { current ->
                var newXp = current.xp + xpReward
                var newLevel = current.level
                while (newXp >= newLevel * 120) {
                    newXp -= newLevel * 120
                    newLevel++
                    leveledUpTo = newLevel
                }
                current.copy(
                    level = newLevel,
                    xp = newXp,
                    shards = max(0, current.shards + shardReward)
                )
            }
            appendOpenWorldLog(logMsg, true)
            leveledUpTo?.let { lvl ->
                bannerFlow.value = "Nível $lvl alcançado no Mundo Aberto!"
            }
        }
    }
}
