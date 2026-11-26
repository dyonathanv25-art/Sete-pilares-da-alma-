package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.AstralViolet
import com.example.ui.theme.CrimsonBlood
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.ParadoxMagenta
import com.example.ui.theme.SoulEmerald
import com.example.ui.theme.TextSecondary

enum class HeroClass(
    val displayName: String,
    val title: String,
    val baseHp: Int,
    val baseMana: Int,
    val baseAtk: Int,
    val baseDef: Int,
    val baseCritPercent: Int,
    val accentColor: Color,
    val archetypeSummary: String
) {
    GUERREIRO(
        displayName = "Guerreiro",
        title = "Baluarte da Cicatriz",
        baseHp = 340,
        baseMana = 90,
        baseAtk = 34,
        baseDef = 24,
        baseCritPercent = 12,
        accentColor = CrimsonBlood,
        archetypeSummary = "Mestre de aço, postura de parry, resistência extrema e golpes sísmicos."
    ),
    MAGO(
        displayName = "Mago",
        title = "Tecelão dos Pilares",
        baseHp = 230,
        baseMana = 190,
        baseAtk = 44,
        baseDef = 12,
        baseCritPercent = 18,
        accentColor = AstralViolet,
        archetypeSummary = "Domina fendas temporais, sobrecargas arcanas e padrões estelares."
    ),
    LADINO(
        displayName = "Ladino",
        title = "Sombra sem Nome",
        baseHp = 260,
        baseMana = 125,
        baseAtk = 40,
        baseDef = 15,
        baseCritPercent = 28,
        accentColor = ParadoxMagenta,
        archetypeSummary = "Especialista em infiltração, veneno, sangramento e execuções críticas."
    ),
    ARQUEIRO(
        displayName = "Arqueiro",
        title = "Olho do Horizonte",
        baseHp = 250,
        baseMana = 135,
        baseAtk = 41,
        baseDef = 14,
        baseCritPercent = 24,
        accentColor = SoulEmerald,
        archetypeSummary = "Abate colossos em pontos fracos aéreos e domina o tiro espectral."
    ),
    CLERIGO(
        displayName = "Clérigo",
        title = "Portador do Selo",
        baseHp = 295,
        baseMana = 165,
        baseAtk = 32,
        baseDef = 20,
        baseCritPercent = 14,
        accentColor = GildedAmber,
        archetypeSummary = "Equilibra luz purificadora, barreiras sagradas e rituais do crepúsculo."
    )
}

enum class HeroSkin(
    val id: String,
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentGlow: Color
) {
    CLASS_DEFAULT(
        id = "CLASS_DEFAULT",
        displayName = "Armadura da Classe",
        description = "Visual clássico adaptado à sua classe e arma principal.",
        primaryColor = Color(0xFF4A5578),
        secondaryColor = Color(0xFF23283B),
        accentGlow = GildedAmber
    ),
    KAEL_IRON(
        id = "KAEL_IRON",
        displayName = "Guarda de Ferro",
        description = "Placas pesadas de Kael-Dorn com manto carmesim.",
        primaryColor = Color(0xFF8C96A8),
        secondaryColor = Color(0xFF3E1E26),
        accentGlow = CrimsonBlood
    ),
    ASTRAL_SCRIBER(
        id = "ASTRAL_SCRIBER",
        displayName = "Manto Astral",
        description = "Vestes estelares de Mirath com auréola orbital dourada.",
        primaryColor = Color(0xFF3A2D6E),
        secondaryColor = Color(0xFF191438),
        accentGlow = AstralViolet
    ),
    NYX_FACELESS(
        id = "NYX_FACELESS",
        displayName = "Sem-Rosto de Nyx",
        description = "Capuz sombrio da Guilda dos Nomes Apagados.",
        primaryColor = Color(0xFF1F2233),
        secondaryColor = Color(0xFF11121C),
        accentGlow = ParadoxMagenta
    ),
    ECLIPSE_PHANTOM(
        id = "ECLIPSE_PHANTOM",
        displayName = "Espectro Lunar",
        description = "Silhueta espectral prateada do Santuário Submerso.",
        primaryColor = Color(0xFF60B8D4),
        secondaryColor = Color(0xFF1B3547),
        accentGlow = ArcaneCyan
    ),
    TWILIGHT_HERETIC(
        id = "TWILIGHT_HERETIC",
        displayName = "Crepúsculo Herege",
        description = "Asas duplas de Solenis fundindo luz dourada e vazio.",
        primaryColor = Color(0xFFE5B85C),
        secondaryColor = Color(0xFF4C2B73),
        accentGlow = SoulEmerald
    )
}

enum class SecretClassId(val id: String) {
    TIRANO_DE_SANGUE("secret_tirano_sangue"),
    QUEBRADOR_DE_CEUS("secret_quebrador_ceus"),
    ARQUIMAGO_CAOS("secret_arquimago_caos"),
    ESCRIBA_ESTRELAS("secret_escriba_estrelas"),
    CAMINHANTE_VEU("secret_caminhante_veu"),
    CEIFADOR_CARMESIM("secret_ceifador_carmesim"),
    CACADOR_DRAGOES("secret_cacador_dragoes"),
    ARQUEIRO_FANTASMA("secret_arqueiro_fantasma"),
    APOSTOLO_CREPUSCULO("secret_apostolo_crepusculo")
}

data class SubclassInfo(
    val id: String,
    val name: String,
    val parentClass: HeroClass,
    val isSecret: Boolean,
    val requiredLevel: Int,
    val trialRegion: String,
    val unlockCondition: String,
    val passiveDescription: String,
    val hpBonus: Int,
    val manaBonus: Int,
    val atkBonus: Int,
    val defBonus: Int,
    val critBonus: Int
)

enum class SkillCategory(
    val label: String,
    val rangeLabel: String,
    val accentColor: Color
) {
    COMUM("Comum", "001–050", TextSecondary),
    RARA("Rara", "051–100", ArcaneCyan),
    LENDARIA("Lendária", "101–150", GildedAmber),
    MITICA("Mítica", "151–200", AstralViolet),
    SECRETA("Secreta", "201–250", CrimsonBlood),
    EVOLUTIVA("Evolutiva", "251–290", SoulEmerald),
    PARADOXAL("Quebra-Jogo", "291–300", ParadoxMagenta)
}

enum class SkillEffectType {
    DAMAGE,
    HEAL,
    SHIELD,
    DOT_POISON,
    DOT_BLEED,
    DUAL_DOT,
    PARRY_COUNTER,
    PURIFY,
    MANA_SURGE,
    PARADOX_INVERSION,
    PARADOX_SWAP_HP,
    PARADOX_RESET_CD,
    PARADOX_SWAP_STATS,
    PARADOX_UNDO_DEATH,
    PARADOX_TIME_FREEZE,
    PARADOX_PIERCE_SEAL,
    PARADOX_ECHO,
    PARADOX_TWILIGHT_ECLIPSE,
    PARADOX_DECREE
}

data class Skill(
    val id: Int,
    val name: String,
    val category: SkillCategory,
    val ownerClass: HeroClass?,
    val requiredSubclassId: String? = null,
    val manaCost: Int,
    val cooldownTurns: Int,
    val powerMultiplier: Float,
    val effectType: SkillEffectType,
    val description: String,
    val evolutionForms: List<String> = emptyList()
) {
    val code: String get() = id.toString().padStart(3, '0')

    fun displayNameForLevel(level: Int, subclassName: String?): String {
        if (evolutionForms.size == 3) {
            return when {
                level >= 15 -> "${evolutionForms[2]} (Forma III)"
                level >= 7 -> "${evolutionForms[1]} (Forma II)"
                else -> "${evolutionForms[0]} (Forma I)"
            }
        }
        return if (category == SkillCategory.EVOLUTIVA && subclassName != null && level >= 10) {
            "$name · $subclassName"
        } else {
            name
        }
    }
}

data class RegionInfo(
    val id: String,
    val name: String,
    val subtitle: String,
    val factionFocus: String,
    val loreSummary: String,
    val accentColor: Color
)

enum class EnemyTier(val label: String) {
    SUB_BOSS("Subchefe"),
    MAIN_BOSS("Chefe Principal"),
    SECRET_TRIAL("Provação Secreta"),
    FINAL_RAID("Raid Final")
}

data class EnemyEncounter(
    val id: String,
    val name: String,
    val title: String,
    val regionId: String,
    val tier: EnemyTier,
    val recommendedLevel: Int,
    val maxHp: Int,
    val attackPower: Int,
    val defense: Int,
    val xpReward: Int,
    val shardReward: Int,
    val isCursed: Boolean = false,
    val hasAerialWeakPoint: Boolean = false,
    val loreDescription: String,
    val mechanicHint: String,
    val secretRewardId: String? = null
)
