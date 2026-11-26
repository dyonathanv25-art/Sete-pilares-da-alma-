package com.example.ui.combat3d

import androidx.compose.ui.graphics.Color
import com.example.domain.model.EnemyEncounter
import com.example.domain.model.HeroClass
import com.example.domain.model.HeroSkin
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.AstralViolet
import com.example.ui.theme.CrimsonBlood
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.ParadoxMagenta
import com.example.ui.theme.SoulEmerald
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Arena3DMeshes {

    const val ARENA_RADIUS = 5.4f

    fun buildEnvironment(regionId: String, time: Float, out: MutableList<Face3D>) {
        val tileDark: Color
        val tileLight: Color
        val lineColor: Color
        val pillarColor: Color
        val pillarGlow: Color

        when (regionId) {
            "kael_dorn" -> {
                tileDark = Color(0xFF1A1418)
                tileLight = Color(0xFF251B20)
                lineColor = CrimsonBlood.copy(alpha = 0.42f)
                pillarColor = Color(0xFF3A353D)
                pillarGlow = CrimsonBlood
            }
            "mirath" -> {
                tileDark = Color(0xFF0E1D29)
                tileLight = Color(0xFF15293A)
                lineColor = ArcaneCyan.copy(alpha = 0.42f)
                pillarColor = Color(0xFF23394D)
                pillarGlow = ArcaneCyan
            }
            "nyx" -> {
                tileDark = Color(0xFF130F1F)
                tileLight = Color(0xFF1D162E)
                lineColor = ParadoxMagenta.copy(alpha = 0.38f)
                pillarColor = Color(0xFF272138)
                pillarGlow = ParadoxMagenta
            }
            "aethel" -> {
                tileDark = Color(0xFF10201F)
                tileLight = Color(0xFF182F2C)
                lineColor = SoulEmerald.copy(alpha = 0.42f)
                pillarColor = Color(0xFF2A4240)
                pillarGlow = SoulEmerald
            }
            "solenis" -> {
                tileDark = Color(0xFF1F1A14)
                tileLight = Color(0xFF2C241B)
                lineColor = GildedAmber.copy(alpha = 0.42f)
                pillarColor = Color(0xFF42372B)
                pillarGlow = GildedAmber
            }
            else -> {
                tileDark = Color(0xFF151024)
                tileLight = Color(0xFF1F1733)
                lineColor = AstralViolet.copy(alpha = 0.48f)
                pillarColor = Color(0xFF2B2340)
                pillarGlow = AstralViolet
            }
        }

        // Walkable 3D tiled arena floor (5x5 tiles covering -5.5..+5.5 so walking feels fast & spatial!)
        val tileSize = 2.2f
        val halfTile = tileSize * 0.49f
        for (gx in -2..2) {
            for (gz in -2..2) {
                val isAlt = (gx + gz) % 2 == 0
                Arena3DMath.addQuadXZ(
                    out = out,
                    center = Vec3(gx * tileSize, 0f, gz * tileSize),
                    halfW = halfTile,
                    halfD = halfTile,
                    color = if (isAlt) tileDark else tileLight,
                    emissive = true,
                    outline = lineColor.copy(alpha = 0.25f)
                )
            }
        }

        // Outer Sacred Boundary Rune Ring
        Arena3DMath.addOrbitRing(
            out = out,
            center = Vec3(0f, 0.03f, 0f),
            radius = ARENA_RADIUS,
            thickness = 0.08f,
            segments = 24,
            color = lineColor,
            rotX = 0f,
            rotY = time * 0.12f
        )

        // 360-degree Region Landmarks around the perimeter
        when (regionId) {
            "cicatriz" -> {
                for (i in 0 until 7) {
                    val angle = (i * 2f * PI.toFloat() / 7f) + time * 0.08f
                    val px = cos(angle) * 6.4f
                    val pz = sin(angle) * 6.4f
                    val floatY = 1.15f + sin(time * 1.6f + i) * 0.18f
                    Arena3DMath.addBox(out, Vec3(px, floatY, pz), Vec3(0.24f, 1.05f, 0.24f), pillarColor, rotY = angle, outline = pillarGlow.copy(alpha = 0.55f))
                    Arena3DMath.addOctahedron(out, Vec3(px, floatY + 1.35f, pz), 0.22f, 0.34f, pillarGlow, rotY = time + i)
                }
            }
            "mirath" -> {
                for (i in 0 until 6) {
                    val angle = i * (2f * PI.toFloat() / 6f)
                    val px = cos(angle) * 6.2f
                    val pz = sin(angle) * 6.2f
                    Arena3DMath.addBox(out, Vec3(px, 1.4f, pz), Vec3(0.28f, 1.4f, 0.28f), pillarColor, rotY = angle, outline = ArcaneCyan)
                }
                Arena3DMath.addOrbitRing(out, Vec3(0f, 3.2f, 0f), 3.6f, 0.09f, 20, ArcaneCyan.copy(alpha = 0.45f), rotX = 1.1f, rotY = time * 0.45f)
                Arena3DMath.addOrbitRing(out, Vec3(0f, 3.2f, 0f), 2.6f, 0.07f, 18, GildedAmber.copy(alpha = 0.5f), rotX = -0.9f, rotY = -time * 0.6f)
            }
            "aethel" -> {
                for (i in 0 until 6) {
                    val angle = i * (2f * PI.toFloat() / 6f)
                    val px = cos(angle) * 6.5f
                    val pz = sin(angle) * 6.5f
                    Arena3DMath.addPyramid(out, Vec3(px, 0f, pz), 0.78f, 2.5f, pillarColor, rotY = angle, outline = SoulEmerald.copy(alpha = 0.5f))
                }
                Arena3DMath.addOrbitRing(out, Vec3(0f, 3.6f, 0f), 3.0f, 0.06f, 18, GildedAmber.copy(alpha = 0.5f), rotX = 1.25f, rotY = time * 0.4f)
            }
            "solenis" -> {
                for (i in 0 until 6) {
                    val angle = i * (2f * PI.toFloat() / 6f)
                    val px = cos(angle) * 6.2f
                    val pz = sin(angle) * 6.2f
                    val isGold = i % 2 == 0
                    Arena3DMath.addBox(
                        out,
                        Vec3(px, 1.4f, pz),
                        Vec3(0.28f, 1.4f, 0.28f),
                        if (isGold) GildedAmber.copy(alpha = 0.65f) else Color(0xFF3A2258),
                        rotY = angle,
                        outline = if (isGold) GildedAmber else AstralViolet
                    )
                }
            }
            else -> {
                // Kael-Dorn / Nyx Perimeter Bastions & Braziers
                for (i in 0 until 6) {
                    val angle = i * (2f * PI.toFloat() / 6f)
                    val px = cos(angle) * 6.2f
                    val pz = sin(angle) * 6.2f
                    Arena3DMath.addBox(out, Vec3(px, 1.15f, pz), Vec3(0.32f, 1.15f, 0.32f), pillarColor, rotY = angle, outline = pillarGlow.copy(alpha = 0.5f))
                    Arena3DMath.addOctahedron(out, Vec3(px, 2.65f + sin(time * 2.2f + i) * 0.08f, pz), 0.22f, 0.3f, pillarGlow, rotY = time + i)
                }
            }
        }
    }

    /**
     * Local character model faces +Z, rotated in world space by `facingYaw`.
     */
    fun buildHero3D(
        heroClass: HeroClass,
        skin: HeroSkin,
        pos: Vec3,
        facingYaw: Float,
        walkPhase: Float,
        isMoving: Boolean,
        isDashing: Boolean,
        attackProgress: Float, // 0 = idle, 0..1 = strike swing
        time: Float,
        shieldActive: Boolean,
        out: MutableList<Face3D>
    ) {
        Hero3DSkinBuilder.buildHero3D(
            heroClass = heroClass,
            skin = skin,
            pos = pos,
            facingYaw = facingYaw,
            walkPhase = walkPhase,
            isMoving = isMoving,
            isDashing = isDashing,
            attackProgress = attackProgress,
            time = time,
            shieldActive = shieldActive,
            out = out
        )
    }

    /**
     * Local enemy model faces +Z, rotated by `facingYaw`, with real-time floor AoE telegraph ring,
     * dark-charred carapace, pulsing neon corruption fissures, and alpha-blended ragged shadow cloth.
     */
    fun buildEnemy3D(
        encounter: EnemyEncounter,
        pos: Vec3,
        facingYaw: Float,
        walkPhase: Float,
        isMoving: Boolean,
        aerialOpen: Boolean,
        isFrozen: Boolean,
        aoeCenter: Vec3?,
        aoeRadius: Float,
        aoeProgress: Float, // 0..1
        time: Float,
        out: MutableList<Face3D>
    ) {
        val bob = if (isFrozen) 0f else if (isMoving) kotlin.math.abs(sin(walkPhase)) * 0.07f else sin(time * 2.5f) * 0.06f
        val base = pos + Vec3(0f, bob, 0f)
        fun place(local: Vec3): Vec3 = local.rotateY(facingYaw) + base

        val pulseAlpha = (0.68f + 0.32f * sin(time * 4.5f)).coerceIn(0f, 1f)
        val (bodyColor, trimColor, coreGlow) = when {
            isFrozen -> Triple(Color(0xFF428EA8), ArcaneCyan, Color.White)
            encounter.id == "boss_arquiteto" -> Triple(Color(0xFF14111E), GildedAmber, Color(0xFFB84DFF))
            encounter.id.contains("draco") || encounter.id == "boss_sera" -> Triple(Color(0xFF1E3330), SoulEmerald, GildedAmber)
            encounter.id.contains("hidra") -> Triple(Color(0xFF36191D), CrimsonBlood, Color(0xFFFF6633))
            encounter.id.contains("nyx") || encounter.id == "boss_zero" || encounter.id.contains("sombra") -> Triple(Color(0xFF161520), ParadoxMagenta, CrimsonBlood)
            encounter.id.contains("relojoeiro") || encounter.id == "boss_lyraen" -> Triple(Color(0xFF1A293A), ArcaneCyan, Color(0xFFB84DFF))
            encounter.isCursed -> Triple(Color(0xFF281833), GildedAmber, ParadoxMagenta)
            else -> Triple(Color(0xFF2B2832), CrimsonBlood, Color(0xFFB84DFF))
        }
        val fissureGlow = coreGlow.copy(alpha = pulseAlpha)
        val raggedCloth = Color(0xFF1D102A).copy(alpha = 0.72f)

        // Boss Floor Shadow
        Arena3DMath.addFloorDisk(out, Vec3(pos.x, 0.02f, pos.z), 0.72f, 12, Color(0x77000000), trimColor.copy(alpha = 0.45f))

        // Active Floor AoE Danger Telegraph (Genshin-style red circle!)
        if (aoeCenter != null && aoeRadius > 0f && aoeProgress > 0f) {
            Arena3DMath.addOrbitRing(
                out = out,
                center = Vec3(aoeCenter.x, 0.04f, aoeCenter.z),
                radius = aoeRadius,
                thickness = 0.08f,
                segments = 20,
                color = CrimsonBlood,
                rotX = 0f,
                rotY = 0f
            )
            Arena3DMath.addFloorDisk(
                out = out,
                center = Vec3(aoeCenter.x, 0.03f, aoeCenter.z),
                radius = aoeRadius * aoeProgress.coerceIn(0.05f, 1f),
                segments = 18,
                color = CrimsonBlood.copy(alpha = 0.34f),
                outline = GildedAmber.copy(alpha = 0.7f)
            )
        }

        when {
            encounter.id == "boss_arquiteto" -> {
                Arena3DMath.addOctahedron(out, place(Vec3(0f, 1.45f, 0f)), 0.65f, 1.1f, bodyColor, rotY = time * 0.7f, emissive = false, outline = fissureGlow)
                Arena3DMath.addOctahedron(out, place(Vec3(0f, 1.45f, 0f)), 0.28f, 0.48f, fissureGlow, rotY = -time * 1.6f, emissive = true)
                Arena3DMath.addOrbitRing(out, place(Vec3(0f, 1.45f, 0f)), 1.1f, 0.06f, 18, trimColor, rotX = 0.75f, rotY = time * 0.9f)
                Arena3DMath.addOrbitRing(out, place(Vec3(0f, 1.45f, 0f)), 0.95f, 0.05f, 16, fissureGlow, rotX = -0.8f, rotY = -time * 1.1f)
            }
            encounter.id.contains("draco") || encounter.id == "boss_sera" -> {
                val wingFlap = sin(time * 4.8f) * 0.35f
                val hoverY = 0.48f + sin(time * 3f) * 0.12f
                Arena3DMath.addBox(out, place(Vec3(0f, 0.95f + hoverY, 0f)), Vec3(0.44f, 0.36f, 0.42f), bodyColor, rotY = facingYaw, outline = trimColor)
                Arena3DMath.addBox(out, place(Vec3(0f, 0.95f + hoverY, 0.41f)), Vec3(0.2f, 0.06f, 0.03f), fissureGlow, rotY = facingYaw, emissive = true)
                Arena3DMath.addBox(out, place(Vec3(0f, 1.35f + hoverY, 0.46f)), Vec3(0.22f, 0.18f, 0.3f), bodyColor, rotY = facingYaw, outline = trimColor)
                Arena3DMath.addBox(out, place(Vec3(0f, 1.38f + hoverY, 0.72f)), Vec3(0.12f, 0.05f, 0.04f), CrimsonBlood, rotY = facingYaw, emissive = true)
                Arena3DMath.addBox(out, place(Vec3(-0.68f, 1.25f + hoverY, -0.1f)), Vec3(0.68f, 0.26f, 0.05f), trimColor.copy(alpha = 0.85f), rotY = facingYaw + 0.3f, rotZ = -0.3f - wingFlap, outline = fissureGlow)
                Arena3DMath.addBox(out, place(Vec3(0.68f, 1.25f + hoverY, -0.1f)), Vec3(0.68f, 0.26f, 0.05f), trimColor.copy(alpha = 0.85f), rotY = facingYaw - 0.3f, rotZ = 0.3f + wingFlap, outline = fissureGlow)
                if (aerialOpen) {
                    Arena3DMath.addOctahedron(out, place(Vec3(0f, 0.9f + hoverY, 0.48f)), 0.24f, 0.32f, SoulEmerald, rotY = time * 3.2f)
                }
            }
            encounter.id.contains("hidra") -> {
                Arena3DMath.addBox(out, place(Vec3(0f, 0.65f, 0f)), Vec3(0.54f, 0.5f, 0.46f), bodyColor, rotY = facingYaw, outline = trimColor)
                Arena3DMath.addBox(out, place(Vec3(0f, 0.65f, 0.46f)), Vec3(0.28f, 0.07f, 0.025f), fissureGlow, rotY = facingYaw, rotZ = 0.35f, emissive = true)
                for (headIdx in -1..1) {
                    val sway = sin(time * 3.5f + headIdx * 1.4f) * 0.15f
                    val neckCenter = place(Vec3(headIdx * 0.36f + sway, 1.35f, 0.24f))
                    Arena3DMath.addBox(out, neckCenter, Vec3(0.11f, 0.42f, 0.11f), bodyColor, rotY = facingYaw, rotZ = headIdx * 0.22f, outline = trimColor)
                    Arena3DMath.addPyramid(out, neckCenter + Vec3(0f, 0.4f, 0f), 0.16f, 0.26f, fissureGlow, rotY = facingYaw, emissive = true)
                }
            }
            else -> {
                val legSwing = if (isMoving) sin(walkPhase) * 0.45f else 0f
                Arena3DMath.addBox(out, place(Vec3(-0.18f, 0.35f, 0f)), Vec3(0.12f, 0.35f, 0.12f), bodyColor, rotY = facingYaw, rotX = legSwing, outline = fissureGlow.copy(alpha = 0.45f))
                Arena3DMath.addBox(out, place(Vec3(0.18f, 0.35f, 0f)), Vec3(0.12f, 0.35f, 0.12f), bodyColor, rotY = facingYaw, rotX = -legSwing, outline = fissureGlow.copy(alpha = 0.45f))
                // Corrupted Armored Torso + Glowing Corruption Fissures
                Arena3DMath.addBox(out, place(Vec3(0f, 1.0f, 0f)), Vec3(0.42f, 0.42f, 0.26f), bodyColor, rotY = facingYaw, outline = trimColor)
                Arena3DMath.addBox(out, place(Vec3(0f, 1.02f, 0.265f)), Vec3(0.24f, 0.04f, 0.02f), fissureGlow, rotY = facingYaw, rotZ = 0.55f, emissive = true, neonGlow = true)
                Arena3DMath.addBox(out, place(Vec3(0f, 0.96f, 0.265f)), Vec3(0.18f, 0.035f, 0.02f), CrimsonBlood.copy(alpha = pulseAlpha), rotY = facingYaw, rotZ = -0.45f, emissive = true, neonGlow = true)
                // Ragged Alpha-Blended Shadow Cloth Strips
                Arena3DMath.addBox(out, place(Vec3(-0.22f, 0.65f, -0.26f)), Vec3(0.12f, 0.32f, 0.03f), raggedCloth, rotY = facingYaw, rotX = -0.18f)
                Arena3DMath.addBox(out, place(Vec3(0.22f, 0.65f, -0.26f)), Vec3(0.12f, 0.32f, 0.03f), raggedCloth, rotY = facingYaw, rotX = -0.22f)
                // Helmet / Head & Intense Glowing Eyes
                Arena3DMath.addBox(out, place(Vec3(0f, 1.62f, 0f)), Vec3(0.22f, 0.22f, 0.22f), bodyColor, rotY = facingYaw, outline = trimColor)
                Arena3DMath.addBox(out, place(Vec3(0f, 1.64f, 0.21f)), Vec3(0.13f, 0.05f, 0.03f), fissureGlow, rotY = facingYaw, emissive = true, neonGlow = true)

                if (encounter.id.contains("relojoeiro") || encounter.id == "boss_lyraen") {
                    Arena3DMath.addOrbitRing(out, place(Vec3(0f, 1.15f, 0f)), 0.82f, 0.05f, 14, ArcaneCyan, rotX = 0.6f, rotY = time * 1.4f)
                } else if (encounter.isCursed) {
                    val flap = sin(time * 3f) * 0.2f
                    Arena3DMath.addBox(out, place(Vec3(-0.55f, 1.25f, -0.22f)), Vec3(0.48f, 0.24f, 0.05f), GildedAmber.copy(alpha = 0.85f), rotY = facingYaw, rotZ = 0.3f + flap, emissive = true)
                    Arena3DMath.addBox(out, place(Vec3(0.55f, 1.25f, -0.22f)), Vec3(0.48f, 0.24f, 0.05f), ParadoxMagenta.copy(alpha = 0.85f), rotY = facingYaw, rotZ = -0.3f - flap, emissive = true)
                } else {
                    Arena3DMath.addBox(out, place(Vec3(0.5f, 1.12f, 0.28f)), Vec3(0.09f, 0.72f, 0.06f), trimColor, rotY = facingYaw, rotX = 0.3f, emissive = true)
                }
            }
        }
    }
}
