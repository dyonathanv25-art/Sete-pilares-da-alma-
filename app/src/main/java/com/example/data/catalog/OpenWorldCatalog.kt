package com.example.data.catalog

import com.example.domain.model.VillageNpc
import com.example.domain.model.WorldCampBlueprint
import com.example.domain.model.WorldCampRuntime
import com.example.domain.model.WorldEntityState
import com.example.domain.model.WorldMonsterArchetype
import com.example.domain.model.WorldMonsterEntity
import com.example.domain.model.WorldProp
import com.example.domain.model.WorldPropKind
import com.example.domain.model.WorldZoneId
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.CrimsonBlood
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.ParadoxMagenta
import com.example.ui.theme.SoulEmerald
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

object OpenWorldCatalog {

    const val WORLD_HALF_EXTENT = 38f
    const val VILLAGE_RADIUS = 10.5f
    const val VILLAGE_CAMPFIRE_X = -3.4f
    const val VILLAGE_CAMPFIRE_Z = 2.6f
    const val VILLAGE_FORGE_X = 3.8f
    const val VILLAGE_FORGE_Z = 2.4f

    val camps: List<WorldCampBlueprint> = listOf(
        WorldCampBlueprint(
            id = "camp_clareira",
            name = "Acampamento da Clareira Sombria",
            zoneId = WorldZoneId.FLORESTA_SOMBRIA,
            centerX = -18.5f,
            centerZ = -6.0f,
            leaderName = "Guerreiro da Sombra",
            minionCount = 2,
            xpReward = 75,
            shardReward = 35
        ),
        WorldCampBlueprint(
            id = "camp_espinhos",
            name = "Acampamento dos Espinhos",
            zoneId = WorldZoneId.FLORESTA_SOMBRIA,
            centerX = -21.0f,
            centerZ = 17.5f,
            leaderName = "Xamã do Vazio",
            minionCount = 3,
            xpReward = 95,
            shardReward = 45
        ),
        WorldCampBlueprint(
            id = "camp_pilares",
            name = "Acampamento dos Pilares Caídos",
            zoneId = WorldZoneId.RUINAS_PILARES,
            centerX = 11.5f,
            centerZ = 21.5f,
            leaderName = "Sentinela Corrompido",
            minionCount = 3,
            xpReward = 110,
            shardReward = 55
        ),
        WorldCampBlueprint(
            id = "camp_penhascos",
            name = "Acampamento da Fenda de Cinzas",
            zoneId = WorldZoneId.PENHASCOS_CINZAS,
            centerX = 21.0f,
            centerZ = -13.5f,
            leaderName = "Carrasco da Cicatriz",
            minionCount = 3,
            xpReward = 130,
            shardReward = 65
        )
    )

    val villageNpcs: List<VillageNpc> = listOf(
        VillageNpc("npc_ferreiro", "Ferreiro Dorn", 3.2f, 1.4f, 1.4f, 0.55f, GildedAmber),
        VillageNpc("npc_curandeira", "Irmã Lyra", -2.8f, 1.8f, 1.6f, 0.45f, SoulEmerald),
        VillageNpc("npc_vigia", "Vigia Kaelen", 0.5f, -5.2f, 2.2f, 0.65f, ArcaneCyan)
    )

    /**
     * Smooth mathematical heightmap Y(x, z):
     * - Flat inside Vila Central (r <= 9.5m) and near camp centers so structures & chests sit evenly.
     * - Rolling hills, trails, and elevated Penhascos ridges across the open world.
     */
    fun terrainHeightAt(x: Float, z: Float): Float {
        val distVillage = hypot(x, z)
        val villageBlend = ((distVillage - 8.5f) / 4.5f).coerceIn(0f, 1f)

        var campFlatten = 1f
        for (camp in camps) {
            val d = hypot(x - camp.centerX, z - camp.centerZ)
            val b = ((d - 4.2f) / 3.5f).coerceIn(0f, 1f)
            if (b < campFlatten) campFlatten = b
        }

        val rollingHills = 0.85f * sin(x * 0.14f) * cos(z * 0.14f) +
            0.42f * sin((x + z) * 0.21f)
        val cliffRise = if (x > 10f || z < -11f) {
            (((x - 10f).coerceAtLeast(0f) + (-z - 11f).coerceAtLeast(0f)) * 0.08f).coerceAtMost(2.2f)
        } else {
            0f
        }
        return (rollingHills + cliffRise) * villageBlend * campFlatten
    }

    fun zoneAt(x: Float, z: Float): WorldZoneId {
        if (hypot(x, z) <= VILLAGE_RADIUS) return WorldZoneId.VILA_CENTRAL
        if (x < -10.5f) return WorldZoneId.FLORESTA_SOMBRIA
        if (z > 10.5f) return WorldZoneId.RUINAS_PILARES
        return WorldZoneId.PENHASCOS_CINZAS
    }

    fun buildStaticProps(): List<WorldProp> {
        val list = ArrayList<WorldProp>(72)

        // Vila Central Safe Hub
        list += WorldProp("v_crystal", WorldPropKind.VILLAGE_CRYSTAL, 0f, 0f, scale = 1.25f, accentColor = ArcaneCyan)
        list += WorldProp("v_fire", WorldPropKind.VILLAGE_CAMPFIRE, VILLAGE_CAMPFIRE_X, VILLAGE_CAMPFIRE_Z, accentColor = GildedAmber)
        list += WorldProp("v_forge", WorldPropKind.VILLAGE_FORGE, VILLAGE_FORGE_X, VILLAGE_FORGE_Z, accentColor = CrimsonBlood)

        // 4 Village Gate Posts (North, South, East, West openings)
        val gates = listOf(
            -1.8f to 9.4f, 1.8f to 9.4f,
            -1.8f to -9.4f, 1.8f to -9.4f,
            -9.4f to -1.8f, -9.4f to 1.8f,
            9.4f to -1.8f, 9.4f to 1.8f
        )
        gates.forEachIndexed { idx, (gx, gz) ->
            list += WorldProp("v_gate_$idx", WorldPropKind.VILLAGE_GATE_POST, gx, gz, accentColor = GildedAmber)
        }

        // Monster Camps Props (Campfire + Tent + Totem)
        camps.forEach { camp ->
            list += WorldProp("${camp.id}_fire", WorldPropKind.CAMP_FIRE_PURPLE, camp.centerX, camp.centerZ, accentColor = ParadoxMagenta)
            list += WorldProp("${camp.id}_tent", WorldPropKind.CAMP_TENT, camp.centerX - 2.6f, camp.centerZ + 1.8f, yaw = 0.6f, accentColor = camp.zoneId.accentColor)
            list += WorldProp("${camp.id}_totem", WorldPropKind.CAMP_TOTEM, camp.centerX + 2.4f, camp.centerZ - 1.9f, accentColor = camp.zoneId.accentColor)
        }

        // Floresta Sombria Trees (deterministic grid scatter outside village & camps)
        val treeCoords = listOf(
            -13f to -14f, -15f to 2f, -14f to 9f, -24f to -14f, -26f to 3f,
            -28f to 11f, -31f to -4f, -16f to 25f, -29f to 24f, -32f to -19f,
            -13f to -24f, -24f to -25f, -30f to 16f, -17f to 8f
        )
        treeCoords.forEachIndexed { i, (tx, tz) ->
            list += WorldProp("tree_$i", WorldPropKind.DARK_TREE, tx, tz, scale = 0.9f + (i % 3) * 0.2f, accentColor = ParadoxMagenta)
        }

        // Ruínas dos Sete Pilares
        val pillarCoords = listOf(
            -5f to 16f, 4f to 17f, 17f to 15f, 23f to 24f, 4f to 28f,
            -6f to 26f, 18f to 30f, 28f to 16f, 10f to 13f
        )
        pillarCoords.forEachIndexed { i, (px, pz) ->
            list += WorldProp("ruin_$i", WorldPropKind.RUINED_PILLAR, px, pz, scale = 0.95f + (i % 2) * 0.25f, yaw = i * 0.5f, accentColor = ArcaneCyan)
        }

        // Penhascos de Cinzas Obsidian Spires
        val spireCoords = listOf(
            14f to -4f, 16f to -21f, 26f to -4f, 29f to -22f, 8f to -18f,
            -2f to -22f, 24f to 5f, 31f to -11f, 13f to -29f
        )
        spireCoords.forEachIndexed { i, (sx, sz) ->
            list += WorldProp("spire_$i", WorldPropKind.ASH_SPIRE, sx, sz, scale = 1f + (i % 3) * 0.22f, accentColor = CrimsonBlood)
        }

        return list
    }

    fun spawnInitialCamps(): List<WorldCampRuntime> = camps.map { WorldCampRuntime(blueprint = it) }

    fun spawnCampMonsters(camp: WorldCampBlueprint, heroLevel: Int): List<WorldMonsterEntity> {
        val list = ArrayList<WorldMonsterEntity>(camp.minionCount + 1)
        val scaleHp = 1f + (heroLevel - 1) * 0.14f
        val scaleAtk = 1f + (heroLevel - 1) * 0.1f

        val leaderArch = when (camp.zoneId) {
            WorldZoneId.FLORESTA_SOMBRIA -> if (camp.id.contains("espinhos")) WorldMonsterArchetype.XAMA_VAZIO else WorldMonsterArchetype.GUERREIRO_SOMBRA
            WorldZoneId.RUINAS_PILARES -> WorldMonsterArchetype.GUERREIRO_SOMBRA
            else -> WorldMonsterArchetype.GUERREIRO_SOMBRA
        }

        // 1 Elite Camp Leader
        list += WorldMonsterEntity(
            id = "${camp.id}_leader",
            campId = camp.id,
            name = camp.leaderName,
            archetype = leaderArch,
            isElite = true,
            isInvasionMob = false,
            homeX = camp.centerX,
            homeZ = camp.centerZ,
            x = camp.centerX + 0.9f,
            z = camp.centerZ + 0.9f,
            yaw = 0f,
            hp = (145 * scaleHp).toInt(),
            maxHp = (145 * scaleHp).toInt(),
            atk = (18 * scaleAtk).toInt(),
            state = WorldEntityState.PATROLLING,
            patrolAngle = 0f
        )

        // 2-3 Minions patrolling around campfire
        for (i in 0 until camp.minionCount) {
            val angle = (i.toFloat() / camp.minionCount) * 2f * PI.toFloat()
            val mx = camp.centerX + cos(angle) * 2.5f
            val mz = camp.centerZ + sin(angle) * 2.5f
            val arch = if (i % 2 == 0) WorldMonsterArchetype.CORRUPTO_LACRAIA else WorldMonsterArchetype.XAMA_VAZIO
            list += WorldMonsterEntity(
                id = "${camp.id}_mob_$i",
                campId = camp.id,
                name = if (arch == WorldMonsterArchetype.CORRUPTO_LACRAIA) "Corrupto das Sombras" else "Acólito do Vazio",
                archetype = arch,
                isElite = false,
                isInvasionMob = false,
                homeX = camp.centerX,
                homeZ = camp.centerZ,
                x = mx,
                z = mz,
                yaw = angle + PI.toFloat(),
                hp = (78 * scaleHp).toInt(),
                maxHp = (78 * scaleHp).toInt(),
                atk = (11 * scaleAtk).toInt(),
                state = WorldEntityState.PATROLLING,
                patrolAngle = angle
            )
        }
        return list
    }

    fun spawnAllWorldMonsters(heroLevel: Int): List<WorldMonsterEntity> =
        camps.flatMap { spawnCampMonsters(it, heroLevel) }

    fun spawnInvasionWave(wave: Int, heroLevel: Int): List<WorldMonsterEntity> {
        val originCamp = camps[(wave - 1) % camps.size]
        val dirX = -originCamp.centerX
        val dirZ = -originCamp.centerZ
        val len = hypot(dirX, dirZ).coerceAtLeast(1f)
        val spawnDist = 17.5f
        val baseX = (originCamp.centerX / len) * spawnDist
        val baseZ = (originCamp.centerZ / len) * spawnDist
        val scaleHp = 1f + (heroLevel - 1) * 0.14f
        val count = if (wave == 1) 3 else 4
        val list = ArrayList<WorldMonsterEntity>(count)

        for (i in 0 until count) {
            val spread = (i - count / 2f) * 2.1f
            val perpX = -dirZ / len * spread
            val perpZ = dirX / len * spread
            val isElite = (wave >= 2 && i == 0)
            list += WorldMonsterEntity(
                id = "inv_w${wave}_$i",
                campId = null,
                name = if (isElite) "Comandante Invasor" else "Saqueador Corrompido",
                archetype = if (isElite) WorldMonsterArchetype.GUERREIRO_SOMBRA else WorldMonsterArchetype.CORRUPTO_LACRAIA,
                isElite = isElite,
                isInvasionMob = true,
                homeX = 0f,
                homeZ = 0f,
                x = baseX + perpX,
                z = baseZ + perpZ,
                yaw = 0f,
                hp = ((if (isElite) 160 else 82) * scaleHp).toInt(),
                maxHp = ((if (isElite) 160 else 82) * scaleHp).toInt(),
                atk = if (isElite) 18 else 12,
                state = WorldEntityState.CHASING
            )
        }
        return list
    }
}
