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

    fun buildEnvironment(regionId: String, time: Float, out: MutableList<Face3D>) {
        val floorMain: Color
        val floorLine: Color
        val pillarColor: Color
        val pillarGlow: Color

        when (regionId) {
            "kael_dorn" -> {
                floorMain = Color(0xFF1F191C)
                floorLine = CrimsonBlood.copy(alpha = 0.45f)
                pillarColor = Color(0xFF3A353D)
                pillarGlow = CrimsonBlood
            }
            "mirath" -> {
                floorMain = Color(0xFF10202C)
                floorLine = ArcaneCyan.copy(alpha = 0.45f)
                pillarColor = Color(0xFF23394D)
                pillarGlow = ArcaneCyan
            }
            "nyx" -> {
                floorMain = Color(0xFF151221)
                floorLine = ParadoxMagenta.copy(alpha = 0.4f)
                pillarColor = Color(0xFF272138)
                pillarGlow = ParadoxMagenta
            }
            "aethel" -> {
                floorMain = Color(0xFF122322)
                floorLine = SoulEmerald.copy(alpha = 0.45f)
                pillarColor = Color(0xFF2A4240)
                pillarGlow = SoulEmerald
            }
            "solenis" -> {
                floorMain = Color(0xFF211C17)
                floorLine = GildedAmber.copy(alpha = 0.45f)
                pillarColor = Color(0xFF42372B)
                pillarGlow = GildedAmber
            }
            else -> { // cicatriz
                floorMain = Color(0xFF171226)
                floorLine = AstralViolet.copy(alpha = 0.55f)
                pillarColor = Color(0xFF2B2340)
                pillarGlow = AstralViolet
            }
        }

        // 3D Arena Platform Floor
        Arena3DMath.addBox(
            out = out,
            center = Vec3(0f, -0.18f, 0f),
            halfSize = Vec3(4.2f, 0.18f, 2.7f),
            color = floorMain,
            outline = floorLine
        )
        // Perspective Rune Arena Ring
        Arena3DMath.addOrbitRing(
            out = out,
            center = Vec3(0f, 0.02f, 0f),
            radius = 2.1f,
            thickness = 0.05f,
            segments = 16,
            color = floorLine,
            rotX = 0f,
            rotY = time * 0.25f,
            emissive = true
        )

        // Region-specific 3D Landmarks
        when (regionId) {
            "cicatriz" -> {
                // 7 Shattered Soul Pillars orbiting in background
                for (i in 0 until 7) {
                    val angle = (i * 2f * PI.toFloat() / 7f) + time * 0.12f
                    val px = cos(angle) * 3.5f
                    val pz = sin(angle) * 2.2f + 1.2f
                    val floatY = 0.8f + sin(time * 1.5f + i) * 0.14f
                    Arena3DMath.addBox(out, Vec3(px, floatY, pz), Vec3(0.16f, 0.72f, 0.16f), pillarColor, rotY = angle, outline = pillarGlow.copy(alpha = 0.5f))
                    Arena3DMath.addOctahedron(out, Vec3(px, floatY + 0.95f, pz), 0.14f, 0.22f, pillarGlow, rotY = time + i)
                }
            }
            "mirath" -> {
                // Rotating Temporal Clockwork Rings
                Arena3DMath.addOrbitRing(out, Vec3(0f, 1.9f, 1.9f), 1.65f, 0.06f, 18, ArcaneCyan.copy(alpha = 0.55f), rotX = 1.2f, rotY = time * 0.6f)
                Arena3DMath.addOrbitRing(out, Vec3(0f, 1.9f, 1.9f), 1.15f, 0.05f, 16, GildedAmber.copy(alpha = 0.65f), rotX = -0.9f, rotY = -time * 0.8f)
                Arena3DMath.addBox(out, Vec3(-3.0f, 1.0f, 1.6f), Vec3(0.24f, 1.0f, 0.24f), pillarColor, outline = ArcaneCyan)
                Arena3DMath.addBox(out, Vec3(3.0f, 1.0f, 1.6f), Vec3(0.24f, 1.0f, 0.24f), pillarColor, outline = ArcaneCyan)
            }
            "aethel" -> {
                // Sky Peaks & Astrolabe Ring
                Arena3DMath.addPyramid(out, Vec3(-3.1f, 0f, 1.7f), 0.65f, 2.0f, pillarColor, outline = SoulEmerald.copy(alpha = 0.5f))
                Arena3DMath.addPyramid(out, Vec3(3.1f, 0f, 1.7f), 0.65f, 2.2f, pillarColor, outline = SoulEmerald.copy(alpha = 0.5f))
                Arena3DMath.addOrbitRing(out, Vec3(0f, 2.3f, 1.5f), 1.3f, 0.04f, 16, GildedAmber.copy(alpha = 0.6f), rotX = 1.1f, rotY = time * 0.5f)
            }
            "solenis" -> {
                // Split Gold & Shadow Sanctuary Pillars
                Arena3DMath.addBox(out, Vec3(-2.9f, 1.15f, 1.5f), Vec3(0.25f, 1.15f, 0.25f), GildedAmber.copy(alpha = 0.7f), outline = GildedAmber)
                Arena3DMath.addBox(out, Vec3(2.9f, 1.15f, 1.5f), Vec3(0.25f, 1.15f, 0.25f), Color(0xFF3E225C), outline = AstralViolet)
                Arena3DMath.addOrbitRing(out, Vec3(0f, 2.1f, 1.8f), 1.0f, 0.06f, 16, GildedAmber.copy(alpha = 0.55f), rotX = 1.4f, rotY = time * 0.35f)
            }
            else -> {
                // Kael-Dorn / Nyx Twin Bastions + Glowing Braziers
                for (side in listOf(-3.1f, 3.1f)) {
                    Arena3DMath.addBox(out, Vec3(side, 0.95f, 1.5f), Vec3(0.28f, 0.95f, 0.28f), pillarColor, outline = pillarGlow.copy(alpha = 0.5f))
                    Arena3DMath.addOctahedron(out, Vec3(side, 2.15f + sin(time * 2f) * 0.08f, 1.5f), 0.2f, 0.28f, pillarGlow, rotY = time)
                }
            }
        }
    }

    fun buildHero3D(
        heroClass: HeroClass,
        skin: HeroSkin,
        time: Float,
        lungeOffset: Float,
        shieldActive: Boolean,
        out: MutableList<Face3D>
    ) {
        val primary = if (skin == HeroSkin.CLASS_DEFAULT) heroClass.accentColor else skin.primaryColor
        val secondary = if (skin == HeroSkin.CLASS_DEFAULT) Color(0xFF262B3D) else skin.secondaryColor
        val glow = if (skin == HeroSkin.CLASS_DEFAULT) heroClass.accentColor else skin.accentGlow
        val bob = sin(time * 3.2f) * 0.04f
        val base = Vec3(-1.45f + lungeOffset, bob, -0.15f)
        val facingYaw = 0.35f

        // Legs
        Arena3DMath.addBox(out, base + Vec3(-0.14f, 0.3f, 0f), Vec3(0.1f, 0.3f, 0.11f), secondary, rotY = facingYaw)
        Arena3DMath.addBox(out, base + Vec3(0.14f, 0.3f, 0f), Vec3(0.1f, 0.3f, 0.11f), secondary, rotY = facingYaw)

        // Torso Armor / Robe
        val torsoW = if (heroClass == HeroClass.GUERREIRO || skin == HeroSkin.KAEL_IRON) 0.34f else 0.28f
        Arena3DMath.addBox(out, base + Vec3(0f, 0.88f, 0f), Vec3(torsoW, 0.34f, 0.2f), primary, rotY = facingYaw, outline = glow.copy(alpha = 0.5f))

        // Pauldrons
        Arena3DMath.addBox(out, base + Vec3(-torsoW - 0.07f, 1.08f, 0f), Vec3(0.13f, 0.12f, 0.15f), glow, rotY = facingYaw)
        Arena3DMath.addBox(out, base + Vec3(torsoW + 0.07f, 1.08f, 0f), Vec3(0.13f, 0.12f, 0.15f), glow, rotY = facingYaw)

        // Cape / Wings by Skin or Class
        if (skin == HeroSkin.TWILIGHT_HERETIC) {
            val flap = sin(time * 4f) * 0.25f
            Arena3DMath.addBox(out, base + Vec3(-0.45f, 1.15f, 0.28f), Vec3(0.4f, 0.22f, 0.04f), GildedAmber, rotY = -0.5f + flap, rotZ = 0.25f, emissive = true)
            Arena3DMath.addBox(out, base + Vec3(0.15f, 1.15f, 0.34f), Vec3(0.4f, 0.22f, 0.04f), AstralViolet, rotY = 0.5f - flap, rotZ = -0.25f, emissive = true)
        } else {
            Arena3DMath.addBox(out, base + Vec3(-0.14f, 0.78f, 0.25f), Vec3(0.25f, 0.45f, 0.04f), secondary, rotY = facingYaw, rotZ = sin(time * 2.5f) * 0.06f)
        }

        // Head / Helm / Hood
        Arena3DMath.addBox(out, base + Vec3(0f, 1.42f, 0f), Vec3(0.18f, 0.18f, 0.18f), primary, rotY = facingYaw, outline = glow)
        // Visor / Eyes Glow
        Arena3DMath.addBox(out, base + Vec3(0.08f, 1.44f, -0.16f), Vec3(0.11f, 0.04f, 0.03f), glow, rotY = facingYaw, emissive = true)

        // Skin Signature Crown / Halo
        when (skin) {
            HeroSkin.ASTRAL_SCRIBER -> {
                Arena3DMath.addOrbitRing(out, base + Vec3(0f, 1.72f, 0f), 0.34f, 0.03f, 12, GildedAmber, rotX = 0.3f, rotY = time * 1.8f)
            }
            HeroSkin.ECLIPSE_PHANTOM -> {
                Arena3DMath.addOctahedron(out, base + Vec3(-0.42f, 1.35f + sin(time * 3f) * 0.1f, 0f), 0.09f, 0.16f, ArcaneCyan, rotY = time * 2f)
            }
            HeroSkin.KAEL_IRON -> {
                Arena3DMath.addPyramid(out, base + Vec3(-0.14f, 1.6f, 0f), 0.06f, 0.22f, GildedAmber)
                Arena3DMath.addPyramid(out, base + Vec3(0.14f, 1.6f, 0f), 0.06f, 0.22f, GildedAmber)
            }
            else -> Unit
        }

        // Class Weapon 3D
        val swingRot = -0.3f + (lungeOffset * 1.1f)
        when (heroClass) {
            HeroClass.GUERREIRO -> {
                // Greatsword
                Arena3DMath.addBox(out, base + Vec3(0.45f, 1.18f, -0.25f), Vec3(0.07f, 0.62f, 0.04f), Color(0xFFE0E6F2), rotY = facingYaw, rotZ = swingRot, emissive = true, outline = glow)
                Arena3DMath.addBox(out, base + Vec3(0.38f, 0.85f, -0.22f), Vec3(0.18f, 0.05f, 0.07f), GildedAmber, rotY = facingYaw, rotZ = swingRot)
            }
            HeroClass.MAGO -> {
                // Astral Staff + Floating Crystal
                Arena3DMath.addBox(out, base + Vec3(0.42f, 0.95f, -0.2f), Vec3(0.04f, 0.72f, 0.04f), GildedAmber, rotY = facingYaw)
                Arena3DMath.addOctahedron(out, base + Vec3(0.42f, 1.8f + sin(time * 4f) * 0.06f, -0.2f), 0.14f, 0.2f, glow, rotY = time * 2.5f)
            }
            HeroClass.LADINO -> {
                // Twin Curved Daggers
                Arena3DMath.addBox(out, base + Vec3(0.42f, 0.92f, -0.26f), Vec3(0.05f, 0.34f, 0.03f), glow, rotY = facingYaw, rotZ = -0.6f + swingRot, emissive = true)
                Arena3DMath.addBox(out, base + Vec3(-0.36f, 0.92f, -0.26f), Vec3(0.05f, 0.34f, 0.03f), glow, rotY = facingYaw, rotZ = 0.6f, emissive = true)
            }
            HeroClass.ARQUEIRO -> {
                // Greatbow
                Arena3DMath.addBox(out, base + Vec3(0.48f, 1.08f, -0.25f), Vec3(0.05f, 0.56f, 0.06f), GildedAmber, rotY = facingYaw, rotZ = 0.15f)
                Arena3DMath.addBox(out, base + Vec3(0.56f, 1.08f, -0.32f), Vec3(0.32f, 0.03f, 0.03f), glow, rotY = facingYaw, emissive = true)
            }
            HeroClass.CLERIGO -> {
                // Holy Scepter + Floating Seal
                Arena3DMath.addBox(out, base + Vec3(0.42f, 0.96f, -0.2f), Vec3(0.05f, 0.52f, 0.05f), GildedAmber, rotY = facingYaw)
                Arena3DMath.addOctahedron(out, base + Vec3(0.42f, 1.56f, -0.2f), 0.16f, 0.16f, SoulEmerald, rotY = time * 1.8f)
            }
        }

        // Active Shield Dome indicator
        if (shieldActive) {
            Arena3DMath.addOrbitRing(out, base + Vec3(0f, 0.9f, 0f), 0.68f, 0.05f, 14, ArcaneCyan.copy(alpha = 0.65f), rotX = 0.4f, rotY = -time * 1.5f)
        }
    }

    fun buildEnemy3D(
        encounter: EnemyEncounter,
        time: Float,
        lungeOffset: Float,
        aerialOpen: Boolean,
        isFrozen: Boolean,
        out: MutableList<Face3D>
    ) {
        val bob = if (isFrozen) 0f else sin(time * 2.5f) * 0.07f
        val base = Vec3(1.5f - lungeOffset, bob, 0.15f)
        val facingYaw = -0.35f

        val (bodyColor, trimColor, coreGlow) = when {
            isFrozen -> Triple(Color(0xFF428EA8), ArcaneCyan, Color.White)
            encounter.id == "boss_arquiteto" -> Triple(Color(0xFF201638), GildedAmber, AstralViolet)
            encounter.id.contains("draco") || encounter.id == "boss_sera" -> Triple(Color(0xFF24453E), SoulEmerald, GildedAmber)
            encounter.id.contains("hidra") -> Triple(Color(0xFF4D2126), CrimsonBlood, Color(0xFFFF8833))
            encounter.id.contains("nyx") || encounter.id == "boss_zero" || encounter.id.contains("sombra") -> Triple(Color(0xFF1A1926), ParadoxMagenta, CrimsonBlood)
            encounter.id.contains("relojoeiro") || encounter.id == "boss_lyraen" -> Triple(Color(0xFF20344A), ArcaneCyan, GildedAmber)
            encounter.isCursed -> Triple(Color(0xFF362140), GildedAmber, ParadoxMagenta)
            else -> Triple(Color(0xFF484E5E), CrimsonBlood, GildedAmber)
        }

        when {
            encounter.id == "boss_arquiteto" -> {
                // Cosmic Void Monolith Boss + Orbiting Shards + Shattered Gold Chains
                Arena3DMath.addOctahedron(out, base + Vec3(0f, 1.35f, 0f), 0.62f, 1.05f, bodyColor, rotY = time * 0.6f, emissive = false, outline = coreGlow)
                Arena3DMath.addOctahedron(out, base + Vec3(0f, 1.35f, 0f), 0.28f, 0.45f, coreGlow, rotY = -time * 1.5f, emissive = true)
                Arena3DMath.addOrbitRing(out, base + Vec3(0f, 1.35f, 0f), 1.05f, 0.06f, 18, trimColor, rotX = 0.7f, rotY = time * 0.9f)
                Arena3DMath.addOrbitRing(out, base + Vec3(0f, 1.35f, 0f), 0.9f, 0.05f, 16, coreGlow, rotX = -0.8f, rotY = -time * 1.1f)
            }
            encounter.id.contains("draco") || encounter.id == "boss_sera" -> {
                // Winged Colossus / Storm Dragon
                val wingFlap = sin(time * 4.5f) * 0.35f
                val hoverY = 0.45f + sin(time * 3f) * 0.12f
                Arena3DMath.addBox(out, base + Vec3(0f, 0.95f + hoverY, 0f), Vec3(0.42f, 0.36f, 0.35f), bodyColor, rotY = facingYaw, outline = trimColor)
                Arena3DMath.addBox(out, base + Vec3(-0.28f, 1.35f + hoverY, -0.35f), Vec3(0.2f, 0.18f, 0.28f), bodyColor, rotY = facingYaw, outline = trimColor)
                Arena3DMath.addBox(out, base + Vec3(-0.42f, 1.38f + hoverY, -0.58f), Vec3(0.06f, 0.04f, 0.04f), CrimsonBlood, emissive = true)
                // Giant 3D Wings
                Arena3DMath.addBox(out, base + Vec3(-0.25f, 1.25f + hoverY, 0.38f), Vec3(0.75f, 0.28f, 0.05f), trimColor, rotY = -0.4f, rotZ = 0.3f + wingFlap, outline = coreGlow)
                Arena3DMath.addBox(out, base + Vec3(0.55f, 1.25f + hoverY, 0.38f), Vec3(0.75f, 0.28f, 0.05f), trimColor, rotY = 0.4f, rotZ = -0.3f - wingFlap, outline = coreGlow)
                if (aerialOpen) {
                    // Glowing Aerial Weak Point Crystal!
                    Arena3DMath.addOctahedron(out, base + Vec3(0f, 1.0f + hoverY, -0.45f), 0.24f, 0.32f, SoulEmerald, rotY = time * 3f)
                }
            }
            encounter.id.contains("hidra") -> {
                // 3-Headed Ash Hydra
                Arena3DMath.addBox(out, base + Vec3(0f, 0.65f, 0.1f), Vec3(0.52f, 0.5f, 0.42f), bodyColor, rotY = facingYaw, outline = trimColor)
                for (headIdx in -1..1) {
                    val sway = sin(time * 3.5f + headIdx * 1.4f) * 0.15f
                    val neckCenter = base + Vec3(headIdx * 0.34f + sway, 1.35f, -0.18f)
                    Arena3DMath.addBox(out, neckCenter, Vec3(0.11f, 0.42f, 0.11f), bodyColor, rotZ = headIdx * 0.25f, outline = trimColor)
                    Arena3DMath.addPyramid(out, neckCenter + Vec3(0f, 0.4f, -0.12f), 0.16f, 0.26f, coreGlow, rotY = facingYaw, emissive = true)
                }
            }
            else -> {
                // Humanoid Titan / Chrono-Mage / Shadow Assassin / Twilight Seraph
                Arena3DMath.addBox(out, base + Vec3(-0.18f, 0.35f, 0f), Vec3(0.12f, 0.35f, 0.12f), bodyColor, rotY = facingYaw)
                Arena3DMath.addBox(out, base + Vec3(0.18f, 0.35f, 0f), Vec3(0.12f, 0.35f, 0.12f), bodyColor, rotY = facingYaw)
                Arena3DMath.addBox(out, base + Vec3(0f, 1.0f, 0f), Vec3(0.42f, 0.42f, 0.26f), bodyColor, rotY = facingYaw, outline = trimColor)
                Arena3DMath.addBox(out, base + Vec3(0f, 1.62f, 0f), Vec3(0.22f, 0.22f, 0.22f), bodyColor, rotY = facingYaw, outline = trimColor)
                // Glowing Boss Eyes
                Arena3DMath.addBox(out, base + Vec3(-0.08f, 1.64f, -0.21f), Vec3(0.13f, 0.05f, 0.03f), coreGlow, rotY = facingYaw, emissive = true)

                if (encounter.id.contains("relojoeiro") || encounter.id == "boss_lyraen") {
                    Arena3DMath.addOrbitRing(out, base + Vec3(0f, 1.15f, 0f), 0.78f, 0.05f, 14, ArcaneCyan, rotX = 0.6f, rotY = time * 1.4f)
                } else if (encounter.isCursed) {
                    val flap = sin(time * 3f) * 0.2f
                    Arena3DMath.addBox(out, base + Vec3(-0.55f, 1.25f, 0.25f), Vec3(0.48f, 0.24f, 0.05f), GildedAmber, rotZ = 0.3f + flap, emissive = true)
                    Arena3DMath.addBox(out, base + Vec3(0.55f, 1.25f, 0.25f), Vec3(0.48f, 0.24f, 0.05f), ParadoxMagenta, rotZ = -0.3f - flap, emissive = true)
                } else {
                    // Boss Weapon
                    Arena3DMath.addBox(out, base + Vec3(-0.52f, 1.12f, -0.26f), Vec3(0.09f, 0.72f, 0.06f), trimColor, rotY = facingYaw, rotZ = 0.28f, emissive = true)
                }
            }
        }
    }
}
