package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.AstralViolet
import com.example.ui.theme.CrimsonBlood
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.ParadoxMagenta
import com.example.ui.theme.SoulEmerald

enum class CombatPlayMode {
    OPEN_WORLD,
    BOSS_ARENA
}

enum class WorldZoneId(
    val title: String,
    val subtitle: String,
    val isSafeZone: Boolean,
    val accentColor: Color,
    val skyColors: List<Color>,
    val groundBase: Color,
    val groundAlt: Color
) {
    VILA_CENTRAL(
        title = "Vila Central",
        subtitle = "Zona Segura · Cristal, Fogueira & Forja",
        isSafeZone = true,
        accentColor = GildedAmber,
        skyColors = listOf(Color(0xFF1A192A), Color(0xFF11131E), Color(0xFF0A0B10)),
        groundBase = Color(0xFF23222C),
        groundAlt = Color(0xFF2C2A35)
    ),
    FLORESTA_SOMBRIA(
        title = "Floresta Sombria",
        subtitle = "Bosque Corrompido · Acampamentos",
        isSafeZone = false,
        accentColor = ParadoxMagenta,
        skyColors = listOf(Color(0xFF140D24), Color(0xFF0E131D), Color(0xFF090A10)),
        groundBase = Color(0xFF141E1C),
        groundAlt = Color(0xFF1B2724)
    ),
    RUINAS_PILARES(
        title = "Ruínas dos Pilares",
        subtitle = "Vestígios Arcanos · Guardiões",
        isSafeZone = false,
        accentColor = ArcaneCyan,
        skyColors = listOf(Color(0xFF0B1C2B), Color(0xFF101524), Color(0xFF090A10)),
        groundBase = Color(0xFF182230),
        groundAlt = Color(0xFF1E2B3B)
    ),
    PENHASCOS_CINZAS(
        title = "Penhascos de Cinzas",
        subtitle = "Encostas da Cicatriz · Perigo Alto",
        isSafeZone = false,
        accentColor = CrimsonBlood,
        skyColors = listOf(Color(0xFF240E16), Color(0xFF140E18), Color(0xFF090A10)),
        groundBase = Color(0xFF23181C),
        groundAlt = Color(0xFF2D1F24)
    )
}

enum class WorldMonsterArchetype {
    CORRUPTO_LACRAIA,
    GUERREIRO_SOMBRA,
    XAMA_VAZIO
}

enum class WorldEntityState {
    PATROLLING,
    CHASING,
    ATTACKING,
    RETURNING,
    DEAD
}

data class WorldCampBlueprint(
    val id: String,
    val name: String,
    val zoneId: WorldZoneId,
    val centerX: Float,
    val centerZ: Float,
    val leaderName: String,
    val minionCount: Int,
    val xpReward: Int,
    val shardReward: Int
)

data class WorldProp(
    val id: String,
    val kind: WorldPropKind,
    val x: Float,
    val z: Float,
    val scale: Float = 1f,
    val yaw: Float = 0f,
    val accentColor: Color = AstralViolet
)

enum class WorldPropKind {
    VILLAGE_CRYSTAL,
    VILLAGE_CAMPFIRE,
    VILLAGE_FORGE,
    VILLAGE_GATE_POST,
    DARK_TREE,
    RUINED_PILLAR,
    ASH_SPIRE,
    CAMP_FIRE_PURPLE,
    CAMP_TENT,
    CAMP_TOTEM
}

data class WorldMonsterEntity(
    val id: String,
    val campId: String?,
    val name: String,
    val archetype: WorldMonsterArchetype,
    val isElite: Boolean,
    val isInvasionMob: Boolean,
    val homeX: Float,
    val homeZ: Float,
    val x: Float,
    val z: Float,
    val yaw: Float,
    val hp: Int,
    val maxHp: Int,
    val atk: Int,
    val state: WorldEntityState = WorldEntityState.PATROLLING,
    val patrolAngle: Float = 0f,
    val walkPhase: Float = 0f,
    val attackCooldown: Float = 0f,
    val aoeCharge: Float = 0f,
    val aoeTargetX: Float = 0f,
    val aoeTargetZ: Float = 0f,
    val respawnTimer: Float = 0f
)

data class WorldCampRuntime(
    val blueprint: WorldCampBlueprint,
    val chestUnlocked: Boolean = false,
    val chestClaimed: Boolean = false,
    val respawnTimer: Float = 0f
)

data class VillageInvasionState(
    val active: Boolean = false,
    val wave: Int = 0,
    val totalWaves: Int = 2,
    val crystalHp: Int = 240,
    val crystalMaxHp: Int = 240,
    val nextInvasionTimer: Float = 45f,
    val statusBanner: String? = null
)

data class VillageNpc(
    val id: String,
    val name: String,
    val baseX: Float,
    val baseZ: Float,
    val orbitRadius: Float,
    val speed: Float,
    val cloakColor: Color
)
