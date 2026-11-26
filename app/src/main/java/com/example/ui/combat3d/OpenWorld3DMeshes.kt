package com.example.ui.combat3d

import androidx.compose.ui.graphics.Color
import com.example.data.catalog.OpenWorldCatalog
import com.example.domain.model.VillageInvasionState
import com.example.domain.model.VillageNpc
import com.example.domain.model.WorldCampRuntime
import com.example.domain.model.WorldEntityState
import com.example.domain.model.WorldMonsterArchetype
import com.example.domain.model.WorldMonsterEntity
import com.example.domain.model.WorldProp
import com.example.domain.model.WorldPropKind
import com.example.domain.model.WorldZoneId
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.AstralViolet
import com.example.ui.theme.CrimsonBlood
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.ParadoxMagenta
import com.example.ui.theme.SoulEmerald
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.sin

object OpenWorld3DMeshes {

    private const val TILE_SIZE = 2.6f
    private const val CULL_RADIUS = 18.5f

    fun buildHeightmapTerrainChunk(
        playerX: Float,
        playerZ: Float,
        time: Float,
        out: MutableList<Face3D>
    ) {
        val baseGx = floor(playerX / TILE_SIZE).toInt()
        val baseGz = floor(playerZ / TILE_SIZE).toInt()

        for (dx in -4..4) {
            for (dz in -4..4) {
                val gx = baseGx + dx
                val gz = baseGz + dz
                val x0 = gx * TILE_SIZE
                val z0 = gz * TILE_SIZE
                val x1 = x0 + TILE_SIZE
                val z1 = z0 + TILE_SIZE
                val cx = (x0 + x1) * 0.5f
                val cz = (z0 + z1) * 0.5f

                // Skip far corners for circular culling
                if (hypot(cx - playerX, cz - playerZ) > 15.5f) continue

                val y00 = OpenWorldCatalog.terrainHeightAt(x0, z0)
                val y10 = OpenWorldCatalog.terrainHeightAt(x1, z0)
                val y11 = OpenWorldCatalog.terrainHeightAt(x1, z1)
                val y01 = OpenWorldCatalog.terrainHeightAt(x0, z1)

                val zone = OpenWorldCatalog.zoneAt(cx, cz)
                val isAlt = (gx + gz) and 1 == 0
                val baseColor = if (isAlt) zone.groundBase else zone.groundAlt
                val avgY = (y00 + y10 + y11 + y01) * 0.25f
                val elevShade = (1f + avgY * 0.14f).coerceIn(0.74f, 1.28f)
                val tileColor = Color(
                    red = (baseColor.red * elevShade).coerceIn(0f, 1f),
                    green = (baseColor.green * elevShade).coerceIn(0f, 1f),
                    blue = (baseColor.blue * elevShade).coerceIn(0f, 1f),
                    alpha = 1f
                )

                val outline = zone.accentColor.copy(alpha = 0.18f)
                out += Face3D(
                    vertices = listOf(
                        Vec3(x0, y00, z0),
                        Vec3(x1, y10, z0),
                        Vec3(x1, y11, z1),
                        Vec3(x0, y01, z1)
                    ),
                    color = tileColor,
                    emissive = true,
                    outlineColor = outline
                )
            }
        }

        // Village Safe Boundary Rune Ring at radius 10.5m if nearby
        if (hypot(playerX, playerZ) < 22f) {
            Arena3DMath.addOrbitRing(
                out = out,
                center = Vec3(0f, 0.05f, 0f),
                radius = OpenWorldCatalog.VILLAGE_RADIUS,
                thickness = 0.09f,
                segments = 28,
                color = GildedAmber.copy(alpha = 0.45f),
                rotX = 0f,
                rotY = time * 0.06f
            )
        }
    }

    fun buildNearbyProps(
        props: List<WorldProp>,
        playerX: Float,
        playerZ: Float,
        invasion: VillageInvasionState,
        time: Float,
        out: MutableList<Face3D>
    ) {
        val pulse = 0.7f + 0.3f * sin(time * 4.2f)
        for (prop in props) {
            if (hypot(prop.x - playerX, prop.z - playerZ) > CULL_RADIUS) continue
            val groundY = OpenWorldCatalog.terrainHeightAt(prop.x, prop.z)
            val base = Vec3(prop.x, groundY, prop.z)

            when (prop.kind) {
                WorldPropKind.VILLAGE_CRYSTAL -> {
                    val cColor = if (invasion.active) CrimsonBlood else ArcaneCyan
                    Arena3DMath.addBox(out, base + Vec3(0f, 0.35f, 0f), Vec3(0.75f, 0.35f, 0.75f), Color(0xFF282B3A), outline = GildedAmber)
                    Arena3DMath.addOctahedron(out, base + Vec3(0f, 1.85f + sin(time * 2.5f) * 0.12f, 0f), 0.52f, 0.95f, cColor.copy(alpha = 0.9f), rotY = time * 0.9f)
                    Arena3DMath.addOrbitRing(out, base + Vec3(0f, 1.8f, 0f), 1.15f, 0.05f, 16, GildedAmber.copy(alpha = pulse), rotX = 0.75f, rotY = -time * 1.2f)
                }
                WorldPropKind.VILLAGE_CAMPFIRE -> {
                    Arena3DMath.addFloorDisk(out, base + Vec3(0f, 0.03f, 0f), 0.9f, 10, Color(0x55FF8822), GildedAmber.copy(alpha = 0.5f))
                    Arena3DMath.addBox(out, base + Vec3(0f, 0.12f, 0f), Vec3(0.45f, 0.08f, 0.12f), Color(0xFF4A3022), rotY = 0.5f)
                    Arena3DMath.addBox(out, base + Vec3(0f, 0.12f, 0f), Vec3(0.45f, 0.08f, 0.12f), Color(0xFF4A3022), rotY = -0.6f)
                    Arena3DMath.addOctahedron(out, base + Vec3(0f, 0.5f + sin(time * 7f) * 0.06f, 0f), 0.24f, 0.42f, Color(0xFFFFA533).copy(alpha = pulse), rotY = time * 4f)
                }
                WorldPropKind.VILLAGE_FORGE -> {
                    Arena3DMath.addBox(out, base + Vec3(0f, 0.55f, 0f), Vec3(0.75f, 0.55f, 0.55f), Color(0xFF34303A), outline = CrimsonBlood)
                    Arena3DMath.addBox(out, base + Vec3(0f, 0.5f, 0.56f), Vec3(0.35f, 0.25f, 0.03f), Color(0xFFFF5E2B).copy(alpha = pulse), emissive = true)
                    Arena3DMath.addBox(out, base + Vec3(0.9f, 0.42f, 0f), Vec3(0.28f, 0.42f, 0.2f), Color(0xFF5A6272), outline = GildedAmber)
                }
                WorldPropKind.VILLAGE_GATE_POST -> {
                    Arena3DMath.addBox(out, base + Vec3(0f, 1.15f, 0f), Vec3(0.26f, 1.15f, 0.26f), Color(0xFF4C3526), outline = GildedAmber.copy(alpha = 0.45f))
                    Arena3DMath.addOctahedron(out, base + Vec3(0f, 2.55f, 0f), 0.16f, 0.24f, GildedAmber, rotY = time * 1.4f)
                }
                WorldPropKind.DARK_TREE -> {
                    val s = prop.scale
                    Arena3DMath.addBox(out, base + Vec3(0f, 1.15f * s, 0f), Vec3(0.2f * s, 1.15f * s, 0.2f * s), Color(0xFF281D29), outline = ParadoxMagenta.copy(alpha = 0.35f))
                    Arena3DMath.addPyramid(out, base + Vec3(0f, 1.85f * s, 0f), 0.95f * s, 1.65f * s, Color(0xFF192428).copy(alpha = 0.92f), rotY = prop.yaw, outline = ParadoxMagenta.copy(alpha = 0.4f))
                    Arena3DMath.addPyramid(out, base + Vec3(0f, 2.85f * s, 0f), 0.68f * s, 1.35f * s, Color(0xFF231833).copy(alpha = 0.88f), rotY = prop.yaw + 0.4f, outline = SoulEmerald.copy(alpha = 0.35f))
                }
                WorldPropKind.RUINED_PILLAR -> {
                    val s = prop.scale
                    Arena3DMath.addBox(out, base + Vec3(0f, 1.35f * s, 0f), Vec3(0.32f * s, 1.35f * s, 0.32f * s), Color(0xFF2B3B4F), rotY = prop.yaw, outline = ArcaneCyan.copy(alpha = 0.6f))
                    Arena3DMath.addBox(out, base + Vec3(0f, 1.5f * s, 0.33f * s), Vec3(0.18f * s, 0.06f, 0.02f), ArcaneCyan.copy(alpha = pulse), rotY = prop.yaw, emissive = true)
                }
                WorldPropKind.ASH_SPIRE -> {
                    val s = prop.scale
                    Arena3DMath.addPyramid(out, base, 0.72f * s, 2.65f * s, Color(0xFF26161C), rotY = prop.yaw, outline = CrimsonBlood.copy(alpha = 0.55f))
                }
                WorldPropKind.CAMP_FIRE_PURPLE -> {
                    Arena3DMath.addFloorDisk(out, base + Vec3(0f, 0.03f, 0f), 0.8f, 10, Color(0x44B84DFF), ParadoxMagenta.copy(alpha = 0.55f))
                    Arena3DMath.addOctahedron(out, base + Vec3(0f, 0.52f + sin(time * 6f) * 0.08f, 0f), 0.25f, 0.48f, Color(0xFFB84DFF).copy(alpha = pulse), rotY = time * 3.8f)
                }
                WorldPropKind.CAMP_TENT -> {
                    Arena3DMath.addPyramid(out, base, 1.15f, 1.55f, Color(0xFF261A2E).copy(alpha = 0.88f), rotY = prop.yaw, outline = prop.accentColor.copy(alpha = 0.55f))
                }
                WorldPropKind.CAMP_TOTEM -> {
                    Arena3DMath.addBox(out, base + Vec3(0f, 0.95f, 0f), Vec3(0.22f, 0.95f, 0.22f), Color(0xFF23202C), outline = prop.accentColor)
                    Arena3DMath.addOctahedron(out, base + Vec3(0f, 2.15f, 0f), 0.22f, 0.3f, prop.accentColor.copy(alpha = pulse), rotY = time * 2.4f)
                }
            }
        }
    }

    fun buildCampChests(
        camps: List<WorldCampRuntime>,
        playerX: Float,
        playerZ: Float,
        time: Float,
        out: MutableList<Face3D>
    ) {
        for (camp in camps) {
            val cx = camp.blueprint.centerX
            val cz = camp.blueprint.centerZ + 1.45f
            if (hypot(cx - playerX, cz - playerZ) > CULL_RADIUS) continue
            val cy = OpenWorldCatalog.terrainHeightAt(cx, cz)
            val pos = Vec3(cx, cy, cz)

            when {
                camp.chestClaimed -> {
                    Arena3DMath.addBox(out, pos + Vec3(0f, 0.22f, 0f), Vec3(0.42f, 0.22f, 0.28f), Color(0xFF2F2824), outline = Color(0xFF5E534A))
                }
                camp.chestUnlocked -> {
                    Arena3DMath.addFloorDisk(out, pos + Vec3(0f, 0.03f, 0f), 0.85f, 12, SoulEmerald.copy(alpha = 0.25f), GildedAmber)
                    Arena3DMath.addBox(out, pos + Vec3(0f, 0.28f, 0f), Vec3(0.44f, 0.28f, 0.3f), Color(0xFF6B4828), outline = GildedAmber)
                    Arena3DMath.addOctahedron(out, pos + Vec3(0f, 0.92f + sin(time * 4f) * 0.08f, 0f), 0.18f, 0.24f, GildedAmber, rotY = time * 3f)
                }
                else -> {
                    // Sealed chest with purple corruption ring
                    Arena3DMath.addBox(out, pos + Vec3(0f, 0.26f, 0f), Vec3(0.42f, 0.26f, 0.28f), Color(0xFF261F2E), outline = ParadoxMagenta)
                    Arena3DMath.addOrbitRing(out, pos + Vec3(0f, 0.32f, 0f), 0.68f, 0.04f, 12, ParadoxMagenta.copy(alpha = 0.7f), rotX = 0f, rotY = -time * 1.5f)
                }
            }
        }
    }

    fun buildVillageNpcs(
        npcs: List<VillageNpc>,
        playerX: Float,
        playerZ: Float,
        time: Float,
        out: MutableList<Face3D>
    ) {
        if (hypot(playerX, playerZ) > 21f) return
        for ((idx, npc) in npcs.withIndex()) {
            val angle = time * npc.speed + idx * 2.1f
            val nx = npc.baseX + cos(angle) * npc.orbitRadius
            val nz = npc.baseZ + sin(angle) * npc.orbitRadius
            val ny = OpenWorldCatalog.terrainHeightAt(nx, nz)
            val yaw = angle + PI.toFloat() * 0.5f
            val base = Vec3(nx, ny, nz)

            Arena3DMath.addFloorDisk(out, Vec3(nx, ny + 0.02f, nz), 0.32f, 8, Color(0x44000000), npc.cloakColor.copy(alpha = 0.35f))
            Arena3DMath.addBox(out, base + Vec3(0f, 0.62f, 0f), Vec3(0.2f, 0.36f, 0.14f), npc.cloakColor.copy(alpha = 0.85f), rotY = yaw)
            Arena3DMath.addBox(out, base + Vec3(0f, 1.15f, 0f), Vec3(0.14f, 0.14f, 0.14f), Color(0xFF302E3D), rotY = yaw, outline = npc.cloakColor)
        }
    }

    fun buildWorldMonsters(
        monsters: List<WorldMonsterEntity>,
        playerX: Float,
        playerZ: Float,
        time: Float,
        out: MutableList<Face3D>
    ) {
        val pulseAlpha = (0.68f + 0.32f * sin(time * 4.5f)).coerceIn(0f, 1f)
        for (mob in monsters) {
            if (mob.state == WorldEntityState.DEAD) continue
            if (hypot(mob.x - playerX, mob.z - playerZ) > CULL_RADIUS) continue

            val groundY = OpenWorldCatalog.terrainHeightAt(mob.x, mob.z)
            val isMoving = mob.state != WorldEntityState.ATTACKING
            val bob = if (isMoving) abs(sin(mob.walkPhase)) * 0.06f else sin(time * 3f) * 0.04f
            val base = Vec3(mob.x, groundY + bob, mob.z)
            fun place(local: Vec3): Vec3 = local.rotateY(mob.yaw) + base

            val scale = if (mob.isElite) 1.15f else 0.78f
            val fissureColor = if (mob.isInvasionMob) CrimsonBlood else Color(0xFFB84DFF)
            val bodyColor = if (mob.isElite) Color(0xFF1C1824) else Color(0xFF28232F)

            // Shadow
            Arena3DMath.addFloorDisk(out, Vec3(mob.x, groundY + 0.02f, mob.z), 0.48f * scale, 10, Color(0x66000000), fissureColor.copy(alpha = 0.4f))

            // Floor AoE danger circle if monster is winding up a heavy strike
            if (mob.aoeCharge > 0.05f) {
                val ay = OpenWorldCatalog.terrainHeightAt(mob.aoeTargetX, mob.aoeTargetZ)
                val aoeRadius = if (mob.isElite) 1.65f else 1.15f
                Arena3DMath.addOrbitRing(out, Vec3(mob.aoeTargetX, ay + 0.04f, mob.aoeTargetZ), aoeRadius, 0.06f, 16, CrimsonBlood, rotX = 0f, rotY = 0f)
                Arena3DMath.addFloorDisk(out, Vec3(mob.aoeTargetX, ay + 0.03f, mob.aoeTargetZ), aoeRadius * mob.aoeCharge.coerceIn(0.08f, 1f), 14, CrimsonBlood.copy(alpha = 0.32f))
            }

            when (mob.archetype) {
                WorldMonsterArchetype.CORRUPTO_LACRAIA -> {
                    val sway = sin(mob.walkPhase) * 0.12f
                    Arena3DMath.addBox(out, place(Vec3(0f, 0.34f, 0f)), Vec3(0.28f * scale, 0.22f * scale, 0.42f * scale), bodyColor, rotY = mob.yaw, outline = fissureColor.copy(alpha = 0.55f))
                    Arena3DMath.addBox(out, place(Vec3(sway, 0.36f, -0.38f * scale)), Vec3(0.2f * scale, 0.16f * scale, 0.24f * scale), bodyColor, rotY = mob.yaw, outline = fissureColor.copy(alpha = 0.45f))
                    Arena3DMath.addBox(out, place(Vec3(0f, 0.45f, 0.38f * scale)), Vec3(0.16f * scale, 0.06f, 0.03f), fissureColor.copy(alpha = pulseAlpha), rotY = mob.yaw, emissive = true, neonGlow = true)
                }
                WorldMonsterArchetype.XAMA_VAZIO -> {
                    val floatH = 0.22f + sin(time * 3.5f) * 0.07f
                    Arena3DMath.addBox(out, place(Vec3(0f, 0.78f + floatH, 0f)), Vec3(0.25f * scale, 0.42f * scale, 0.18f * scale), bodyColor, rotY = mob.yaw, outline = ParadoxMagenta)
                    Arena3DMath.addBox(out, place(Vec3(0f, 1.32f + floatH, 0f)), Vec3(0.16f * scale, 0.16f * scale, 0.16f * scale), Color(0xFF14101D), rotY = mob.yaw, outline = fissureColor)
                    Arena3DMath.addBox(out, place(Vec3(0f, 1.34f + floatH, 0.16f * scale)), Vec3(0.1f * scale, 0.04f, 0.02f), fissureColor.copy(alpha = pulseAlpha), rotY = mob.yaw, emissive = true, neonGlow = true)
                    Arena3DMath.addOrbitRing(out, place(Vec3(0f, 0.9f + floatH, 0f)), 0.55f * scale, 0.035f, 12, fissureColor.copy(alpha = 0.65f), rotX = 0.4f, rotY = time * 2.5f)
                }
                WorldMonsterArchetype.GUERREIRO_SOMBRA -> {
                    val legSwing = if (isMoving) sin(mob.walkPhase) * 0.45f else 0f
                    Arena3DMath.addBox(out, place(Vec3(-0.14f * scale, 0.28f * scale, 0f)), Vec3(0.09f * scale, 0.28f * scale, 0.1f * scale), bodyColor, rotY = mob.yaw, rotX = legSwing)
                    Arena3DMath.addBox(out, place(Vec3(0.14f * scale, 0.28f * scale, 0f)), Vec3(0.09f * scale, 0.28f * scale, 0.1f * scale), bodyColor, rotY = mob.yaw, rotX = -legSwing)
                    Arena3DMath.addBox(out, place(Vec3(0f, 0.84f * scale, 0f)), Vec3(0.32f * scale, 0.34f * scale, 0.2f * scale), bodyColor, rotY = mob.yaw, outline = fissureColor.copy(alpha = 0.65f))
                    // Glowing Corruption Fissure + Ragged Alpha Cloth
                    Arena3DMath.addBox(out, place(Vec3(0f, 0.85f * scale, 0.21f * scale)), Vec3(0.2f * scale, 0.035f, 0.02f), fissureColor.copy(alpha = pulseAlpha), rotY = mob.yaw, rotZ = 0.5f, emissive = true, neonGlow = true)
                    Arena3DMath.addBox(out, place(Vec3(0f, 0.56f * scale, -0.21f * scale)), Vec3(0.22f * scale, 0.28f * scale, 0.03f), Color(0xFF1D102A).copy(alpha = 0.72f), rotY = mob.yaw, rotX = -0.18f)
                    Arena3DMath.addBox(out, place(Vec3(0f, 1.35f * scale, 0f)), Vec3(0.18f * scale, 0.18f * scale, 0.18f * scale), bodyColor, rotY = mob.yaw, outline = fissureColor)
                    Arena3DMath.addBox(out, place(Vec3(0f, 1.36f * scale, 0.18f * scale)), Vec3(0.11f * scale, 0.04f, 0.025f), CrimsonBlood.copy(alpha = pulseAlpha), rotY = mob.yaw, emissive = true, neonGlow = true)
                    Arena3DMath.addBox(out, place(Vec3(0.42f * scale, 0.94f * scale, 0.22f * scale)), Vec3(0.06f * scale, 0.52f * scale, 0.04f), fissureColor, rotY = mob.yaw, rotX = 0.35f, emissive = true, neonGlow = true)
                }
            }

            // World-space floating HP bar above damaged monsters
            if (mob.hp < mob.maxHp) {
                val hpFrac = (mob.hp.toFloat() / mob.maxHp.toFloat()).coerceIn(0.05f, 1f)
                val barY = groundY + (if (mob.isElite) 2.05f else 1.45f)
                Arena3DMath.addBox(out, Vec3(mob.x, barY, mob.z), Vec3(0.45f, 0.05f, 0.02f), Color(0xFF141418), rotY = 0f, emissive = true)
                Arena3DMath.addBox(out, Vec3(mob.x, barY, mob.z + 0.01f), Vec3(0.42f * hpFrac, 0.04f, 0.02f), CrimsonBlood, rotY = 0f, emissive = true)
            }
        }
    }
}
