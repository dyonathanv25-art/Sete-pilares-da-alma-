package com.example.ui.combat3d

import androidx.compose.ui.graphics.Color
import com.example.domain.model.HeroClass
import com.example.domain.model.HeroSkin
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.AstralViolet
import com.example.ui.theme.CrimsonBlood
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.ParadoxMagenta
import com.example.ui.theme.SoulEmerald
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Builds high-detail sculpted 3D silhouettes, layered armor plates, capes, wings, crowns,
 * floating spirit blades, and EmissiveGlowShader neon runes for every HeroSkin.
 */
object Hero3DSkinBuilder {

    fun buildHero3D(
        heroClass: HeroClass,
        skin: HeroSkin,
        pos: Vec3,
        facingYaw: Float,
        walkPhase: Float,
        isMoving: Boolean,
        isDashing: Boolean,
        attackProgress: Float,
        time: Float,
        shieldActive: Boolean,
        out: MutableList<Face3D>
    ) {
        val primary = if (skin == HeroSkin.CLASS_DEFAULT) heroClass.accentColor else skin.primaryColor
        val secondary = if (skin == HeroSkin.CLASS_DEFAULT) Color(0xFF202536) else skin.secondaryColor
        val glow = if (skin == HeroSkin.CLASS_DEFAULT) heroClass.accentColor else skin.accentGlow
        val pulse = 0.68f + 0.32f * sin(time * 3.8f)
        val runeGlow = glow.copy(alpha = (0.76f + 0.24f * sin(time * 4.4f)).coerceIn(0f, 1f))

        val metalTrim = when (skin) {
            HeroSkin.CELESTIAL_SWORDSAINT -> GildedAmber
            HeroSkin.BLOOD_TYRANT -> Color(0xFF2B131A)
            HeroSkin.ECLIPSE_PHANTOM -> Color(0xFFA8E8FF)
            HeroSkin.ASTRAL_SCRIBER, HeroSkin.TWILIGHT_HERETIC -> GildedAmber
            HeroSkin.PRIMORDIAL_ARCHMAGE -> Color(0xFF9F7AEA)
            else -> Color(0xFF495162)
        }
        val leatherTone = Color(0xFF4A3326)
        val silverHair = when (skin) {
            HeroSkin.CELESTIAL_SWORDSAINT -> Color(0xFFFFF5D6)
            HeroSkin.BLOOD_TYRANT -> Color(0xFF2E1219)
            else -> Color(0xFFDCE2EE)
        }

        val bob = if (isMoving) abs(sin(walkPhase)) * 0.08f else sin(time * 2.8f) * 0.03f
        val base = pos + Vec3(0f, bob, 0f)
        val legSwing = if (isMoving) sin(walkPhase) * 0.56f else 0f
        val armSwing = if (isMoving) -sin(walkPhase) * 0.44f else 0f
        val leanX = if (isDashing) 0.28f else if (isMoving) 0.11f else 0f

        fun place(local: Vec3): Vec3 = local.rotateX(leanX).rotateY(facingYaw) + base

        // 1. Sacred Ground Shadow + Skin Floor Aura
        Arena3DMath.addFloorDisk(
            out = out,
            center = Vec3(pos.x, 0.02f, pos.z),
            radius = 0.48f,
            segments = 14,
            color = Color(0x66000000),
            outline = glow.copy(alpha = 0.45f * pulse)
        )
        if (skin == HeroSkin.BLOOD_TYRANT || skin == HeroSkin.PRIMORDIAL_ARCHMAGE || skin == HeroSkin.CELESTIAL_SWORDSAINT) {
            Arena3DMath.addOrbitRing(
                out = out,
                center = Vec3(pos.x, 0.03f, pos.z),
                radius = 0.56f,
                thickness = 0.025f,
                segments = 16,
                color = runeGlow.copy(alpha = 0.55f),
                rotX = 0f,
                rotY = time * 0.9f,
                neonGlow = true
            )
        }

        // 2. Two-Segment Articulated Legs (Thighs + Knee Guards + Tapered Greaves/Boots)
        for (side in listOf(-1f, 1f)) {
            val swing = if (side < 0f) legSwing else -legSwing
            val zOff = sin(swing) * 0.11f
            // Thigh
            Arena3DMath.addTaperedBox(
                out = out,
                center = place(Vec3(side * 0.135f, 0.40f, zOff * 0.6f)),
                halfSize = Vec3(0.085f, 0.15f, 0.09f),
                topScaleX = 1.15f,
                topScaleZ = 1.12f,
                color = secondary,
                rotY = facingYaw,
                rotX = leanX + swing * 0.85f,
                outline = metalTrim.copy(alpha = 0.55f)
            )
            // Armored Knee Plate
            Arena3DMath.addBox(
                out = out,
                center = place(Vec3(side * 0.135f, 0.28f, 0.07f + zOff)),
                halfSize = Vec3(0.065f, 0.055f, 0.04f),
                color = metalTrim,
                rotY = facingYaw,
                rotX = leanX + swing,
                outline = runeGlow.copy(alpha = 0.65f)
            )
            // Lower Greave / Boot
            Arena3DMath.addTaperedBox(
                out = out,
                center = place(Vec3(side * 0.135f, 0.14f, zOff * 1.15f)),
                halfSize = Vec3(0.088f, 0.14f, 0.105f),
                topScaleX = 0.84f,
                topScaleZ = 0.82f,
                color = if (skin == HeroSkin.CELESTIAL_SWORDSAINT) primary else secondary,
                rotY = facingYaw,
                rotX = leanX + swing,
                outline = runeGlow.copy(alpha = 0.55f)
            )
        }

        // 3. Sculpted Heroic Torso (Broad Shoulders, Tapered Waist, Front Tabard & Belt)
        val isHeavy = heroClass == HeroClass.GUERREIRO || skin == HeroSkin.KAEL_IRON || skin == HeroSkin.BLOOD_TYRANT
        val torsoW = if (isHeavy) 0.27f else 0.22f
        val chestTopScale = if (isHeavy) 1.38f else 1.28f

        Arena3DMath.addTaperedBox(
            out = out,
            center = place(Vec3(0f, 0.84f, 0f)),
            halfSize = Vec3(torsoW, 0.29f, 0.155f),
            topScaleX = chestTopScale,
            topScaleZ = 1.18f,
            color = primary,
            rotY = facingYaw,
            rotX = leanX,
            outline = metalTrim
        )

        // Sculpted Breastplate Crest / Chest Inlay
        Arena3DMath.addTaperedBox(
            out = out,
            center = place(Vec3(0f, 0.92f, 0.165f)),
            halfSize = Vec3(torsoW * 0.62f, 0.16f, 0.03f),
            topScaleX = 1.22f,
            topScaleZ = 0.9f,
            color = if (skin == HeroSkin.TWILIGHT_HERETIC) GildedAmber else metalTrim,
            rotY = facingYaw,
            rotX = leanX,
            outline = runeGlow.copy(alpha = 0.7f)
        )

        // Glowing Chest Core / Rune Emblem
        if (skin == HeroSkin.BLOOD_TYRANT || skin == HeroSkin.CELESTIAL_SWORDSAINT || skin == HeroSkin.PRIMORDIAL_ARCHMAGE || skin == HeroSkin.ECLIPSE_PHANTOM) {
            Arena3DMath.addOctahedron(
                out = out,
                center = place(Vec3(0f, 0.94f, 0.21f)),
                radius = 0.065f,
                height = 0.095f,
                color = runeGlow,
                rotY = facingYaw + time * 1.4f,
                emissive = true,
                neonGlow = true
            )
        } else {
            Arena3DMath.addBox(
                out = out,
                center = place(Vec3(0f, 0.92f, 0.20f)),
                halfSize = Vec3(0.055f, 0.075f, 0.015f),
                color = runeGlow,
                rotY = facingYaw,
                rotX = leanX,
                emissive = true,
                neonGlow = true
            )
        }

        // Cross-Strap Bandolier + Belt + Glowing Buckle + Flowing Front Tabard
        Arena3DMath.addBox(
            out = out,
            center = place(Vec3(0f, 0.87f, 0.19f)),
            halfSize = Vec3(0.042f, 0.28f, 0.018f),
            color = leatherTone,
            rotY = facingYaw,
            rotX = leanX,
            rotZ = 0.56f
        )
        Arena3DMath.addBox(
            out = out,
            center = place(Vec3(0f, 0.57f, 0f)),
            halfSize = Vec3(torsoW + 0.03f, 0.055f, 0.185f),
            color = leatherTone,
            rotY = facingYaw,
            rotX = leanX,
            outline = metalTrim
        )
        Arena3DMath.addBox(
            out = out,
            center = place(Vec3(0f, 0.57f, 0.195f)),
            halfSize = Vec3(0.058f, 0.055f, 0.022f),
            color = runeGlow,
            rotY = facingYaw,
            rotX = leanX,
            emissive = true,
            neonGlow = true
        )

        // Flowing Front Tabard / Skirt Panel (between legs)
        val tabardSway = if (isMoving) 0.18f + sin(walkPhase * 2f) * 0.08f else sin(time * 2.2f) * 0.04f
        Arena3DMath.addTaperedBox(
            out = out,
            center = place(Vec3(0f, 0.40f, 0.16f)),
            halfSize = Vec3(0.115f, 0.17f, 0.018f),
            topScaleX = 0.72f,
            topScaleZ = 1f,
            color = secondary.copy(alpha = 0.92f),
            rotY = facingYaw,
            rotX = leanX + tabardSway,
            outline = runeGlow.copy(alpha = 0.6f)
        )
        // Neon Tabard Hem Sigil
        Arena3DMath.addBox(
            out = out,
            center = place(Vec3(0f, 0.26f, 0.18f)),
            halfSize = Vec3(0.08f, 0.018f, 0.015f),
            color = runeGlow,
            rotY = facingYaw,
            rotX = leanX + tabardSway,
            emissive = true,
            neonGlow = true
        )

        // 4. Layered Double Pauldrons + Gauntlets + Forearm Bracers
        for (side in listOf(-1f, 1f)) {
            val shoulderColor = when {
                skin == HeroSkin.TWILIGHT_HERETIC && side < 0f -> GildedAmber
                skin == HeroSkin.TWILIGHT_HERETIC && side > 0f -> AstralViolet
                else -> metalTrim
            }
            val shoulderX = torsoW * chestTopScale + 0.06f
            // Upper Sculpted Pauldron
            Arena3DMath.addTaperedBox(
                out = out,
                center = place(Vec3(side * shoulderX, 1.07f, 0f)),
                halfSize = Vec3(0.115f, 0.095f, 0.145f),
                topScaleX = 0.78f,
                topScaleZ = 0.85f,
                color = shoulderColor,
                rotY = facingYaw,
                rotX = leanX,
                rotZ = -side * 0.22f,
                outline = runeGlow
            )
            // Lower Pauldron Rim
            Arena3DMath.addBox(
                out = out,
                center = place(Vec3(side * (shoulderX + 0.02f), 0.98f, 0f)),
                halfSize = Vec3(0.095f, 0.045f, 0.125f),
                color = primary,
                rotY = facingYaw,
                rotX = leanX,
                rotZ = -side * 0.18f,
                outline = runeGlow.copy(alpha = 0.5f)
            )
            // Arm + Rune Gauntlet
            val armRot = if (side > 0f && attackProgress > 0f) -sin(attackProgress * PI.toFloat()) * 0.85f else side * armSwing * 0.5f
            Arena3DMath.addBox(
                out = out,
                center = place(Vec3(side * (shoulderX + 0.03f), 0.80f, 0.04f)),
                halfSize = Vec3(0.06f, 0.12f, 0.065f),
                color = secondary,
                rotY = facingYaw,
                rotX = leanX + armRot
            )
            Arena3DMath.addTaperedBox(
                out = out,
                center = place(Vec3(side * (shoulderX + 0.04f), 0.67f, 0.11f)),
                halfSize = Vec3(0.068f, 0.075f, 0.075f),
                topScaleX = 0.88f,
                topScaleZ = 0.88f,
                color = shoulderColor,
                rotY = facingYaw,
                rotX = leanX + armRot,
                outline = runeGlow
            )

            // Blood Tyrant / Kael Iron Shoulder Spikes
            if (skin == HeroSkin.BLOOD_TYRANT || skin == HeroSkin.KAEL_IRON) {
                Arena3DMath.addPyramid(
                    out = out,
                    baseCenter = place(Vec3(side * (shoulderX + 0.04f), 1.16f, 0f)),
                    radius = 0.055f,
                    height = 0.22f,
                    color = if (skin == HeroSkin.BLOOD_TYRANT) runeGlow else metalTrim,
                    rotY = facingYaw,
                    rotZ = -side * 0.45f,
                    emissive = skin == HeroSkin.BLOOD_TYRANT,
                    outline = runeGlow,
                    neonGlow = skin == HeroSkin.BLOOD_TYRANT
                )
            }
        }

        // High Royal Collar / Scarf around neck
        Arena3DMath.addTaperedBox(
            out = out,
            center = place(Vec3(0f, 1.18f, -0.05f)),
            halfSize = Vec3(0.19f, 0.075f, 0.14f),
            topScaleX = 1.16f,
            topScaleZ = 1.08f,
            color = secondary,
            rotY = facingYaw,
            rotX = leanX,
            outline = runeGlow.copy(alpha = 0.7f)
        )

        // 5. Back Silhouette: Wings / Spirit Swords / Mandalas / Multi-Panel Flowing Cape (-Z)
        buildBackOrnaments(
            skin = skin,
            secondary = secondary,
            primary = primary,
            runeGlow = runeGlow,
            isMoving = isMoving,
            walkPhase = walkPhase,
            time = time,
            facingYaw = facingYaw,
            leanX = leanX,
            place = ::place,
            out = out
        )

        // 6. Detailed Headgear (Hoods, Visored Helms, Dragon Horns, Crowns, Halos, Silver Hair)
        buildHeadgear(
            heroClass = heroClass,
            skin = skin,
            primary = primary,
            secondary = secondary,
            metalTrim = metalTrim,
            silverHair = silverHair,
            runeGlow = runeGlow,
            time = time,
            facingYaw = facingYaw,
            leanX = leanX,
            place = ::place,
            out = out
        )

        // 7. Class Weapon with Skin-Infused Material & Swing Animation
        buildClassWeapon(
            heroClass = heroClass,
            skin = skin,
            runeGlow = runeGlow,
            metalTrim = metalTrim,
            armSwing = armSwing,
            attackProgress = attackProgress,
            time = time,
            facingYaw = facingYaw,
            leanX = leanX,
            place = ::place,
            out = out
        )

        if (shieldActive) {
            Arena3DMath.addOrbitRing(
                out = out,
                center = base + Vec3(0f, 0.88f, 0f),
                radius = 0.78f,
                thickness = 0.045f,
                segments = 18,
                color = ArcaneCyan.copy(alpha = 0.78f),
                rotX = 0.35f,
                rotY = -time * 1.9f,
                neonGlow = true
            )
        }
    }

    private fun buildBackOrnaments(
        skin: HeroSkin,
        secondary: Color,
        primary: Color,
        runeGlow: Color,
        isMoving: Boolean,
        walkPhase: Float,
        time: Float,
        facingYaw: Float,
        leanX: Float,
        place: (Vec3) -> Vec3,
        out: MutableList<Face3D>
    ) {
        val capeFlow = if (isMoving) 0.36f + sin(walkPhase * 2f) * 0.1f else 0.14f + sin(time * 2.4f) * 0.05f

        when (skin) {
            HeroSkin.TWILIGHT_HERETIC -> {
                val flap = sin(time * 4.5f) * 0.24f
                // 4 Articulated Wings (Upper + Lower Amber Left, Upper + Lower Violet Right)
                Arena3DMath.addTaperedBox(
                    out = out,
                    center = place(Vec3(-0.46f, 1.24f, -0.25f)),
                    halfSize = Vec3(0.36f, 0.14f, 0.03f),
                    topScaleX = 1.28f,
                    color = GildedAmber.copy(alpha = 0.92f),
                    rotY = facingYaw - 0.38f + flap,
                    rotZ = 0.28f,
                    emissive = true,
                    outline = Color.White.copy(alpha = 0.55f),
                    neonGlow = true
                )
                Arena3DMath.addTaperedBox(
                    out = out,
                    center = place(Vec3(-0.36f, 0.94f, -0.24f)),
                    halfSize = Vec3(0.26f, 0.10f, 0.025f),
                    topScaleX = 1.22f,
                    color = GildedAmber.copy(alpha = 0.80f),
                    rotY = facingYaw - 0.45f + flap * 0.8f,
                    rotZ = 0.12f,
                    emissive = true
                )
                Arena3DMath.addTaperedBox(
                    out = out,
                    center = place(Vec3(0.46f, 1.24f, -0.25f)),
                    halfSize = Vec3(0.36f, 0.14f, 0.03f),
                    topScaleX = 1.28f,
                    color = AstralViolet.copy(alpha = 0.92f),
                    rotY = facingYaw + 0.38f - flap,
                    rotZ = -0.28f,
                    emissive = true,
                    outline = ParadoxMagenta.copy(alpha = 0.7f),
                    neonGlow = true
                )
                Arena3DMath.addTaperedBox(
                    out = out,
                    center = place(Vec3(0.36f, 0.94f, -0.24f)),
                    halfSize = Vec3(0.26f, 0.10f, 0.025f),
                    topScaleX = 1.22f,
                    color = AstralViolet.copy(alpha = 0.80f),
                    rotY = facingYaw + 0.45f - flap * 0.8f,
                    rotZ = -0.12f,
                    emissive = true
                )
                Arena3DMath.addOrbitRing(out, place(Vec3(0f, 1.66f, 0f)), 0.30f, 0.026f, 14, SoulEmerald, rotX = 0.22f, rotY = time * 1.8f, neonGlow = true)
            }

            HeroSkin.CELESTIAL_SWORDSAINT -> {
                // Royal Ivory & Gold Cape
                Arena3DMath.addTaperedBox(
                    out = out,
                    center = place(Vec3(0f, 0.72f, -0.24f)),
                    halfSize = Vec3(0.24f, 0.44f, 0.028f),
                    topScaleX = 0.72f,
                    color = primary.copy(alpha = 0.92f),
                    rotY = facingYaw,
                    rotX = -capeFlow,
                    outline = GildedAmber
                )
                // 4 Fanned Floating Spirit Swords behind back!
                val swordAngles = listOf(-0.68f, -0.24f, 0.24f, 0.68f)
                swordAngles.forEachIndexed { idx, tilt ->
                    val hover = sin(time * 3.4f + idx * 0.9f) * 0.045f
                    val sx = sin(tilt) * 0.48f
                    val sy = 1.15f + cos(tilt) * 0.26f + hover
                    val bladeColor = if (idx % 2 == 0) ArcaneCyan else GildedAmber
                    Arena3DMath.addTaperedBox(
                        out = out,
                        center = place(Vec3(sx, sy, -0.34f)),
                        halfSize = Vec3(0.036f, 0.30f, 0.018f),
                        topScaleX = 0.25f,
                        color = bladeColor,
                        rotY = facingYaw,
                        rotZ = -tilt,
                        emissive = true,
                        outline = Color.White.copy(alpha = 0.7f),
                        neonGlow = true
                    )
                    Arena3DMath.addBox(
                        out = out,
                        center = place(Vec3(sx * 0.85f, sy - 0.16f, -0.33f)),
                        halfSize = Vec3(0.075f, 0.018f, 0.022f),
                        color = GildedAmber,
                        rotY = facingYaw,
                        rotZ = -tilt
                    )
                }
            }

            HeroSkin.PRIMORDIAL_ARCHMAGE -> {
                // Twin Counter-Rotating Arcane Mandalas Behind Shoulders
                val backCenter = place(Vec3(0f, 1.04f, -0.32f))
                Arena3DMath.addOrbitRing(out, backCenter, 0.62f, 0.032f, 18, ArcaneCyan, rotX = 1.45f, rotY = facingYaw + time * 1.1f, neonGlow = true)
                Arena3DMath.addOrbitRing(out, backCenter, 0.42f, 0.028f, 14, ParadoxMagenta, rotX = 1.45f, rotY = facingYaw - time * 1.7f, neonGlow = true)
                // Royal Vestment Tails
                Arena3DMath.addTaperedBox(
                    out = out,
                    center = place(Vec3(0f, 0.68f, -0.23f)),
                    halfSize = Vec3(0.23f, 0.42f, 0.026f),
                    topScaleX = 0.72f,
                    color = secondary.copy(alpha = 0.88f),
                    rotY = facingYaw,
                    rotX = -capeFlow,
                    outline = ArcaneCyan.copy(alpha = 0.7f)
                )
            }

            HeroSkin.ASTRAL_SCRIBER -> {
                val backCenter = place(Vec3(0f, 1.08f, -0.24f))
                Arena3DMath.addOrbitRing(out, backCenter, 0.55f, 0.025f, 16, GildedAmber, rotX = 1.1f, rotY = facingYaw + time * 1.4f, neonGlow = true)
                Arena3DMath.addOrbitRing(out, place(Vec3(0f, 1.66f, 0f)), 0.30f, 0.028f, 14, GildedAmber, rotX = 0.22f, rotY = time * 2.1f, neonGlow = true)
                Arena3DMath.addOctahedron(out, place(Vec3(-0.44f, 1.35f + sin(time * 3f) * 0.06f, -0.18f)), 0.07f, 0.12f, AstralViolet, rotY = time * 2.4f, neonGlow = true)
                Arena3DMath.addOctahedron(out, place(Vec3(0.44f, 1.35f + cos(time * 3f) * 0.06f, -0.18f)), 0.07f, 0.12f, GildedAmber, rotY = -time * 2.4f, neonGlow = true)
                addStandardFlowingCape(secondary, runeGlow, capeFlow, facingYaw, leanX, place, out)
            }

            HeroSkin.PEREGRINO_ARCANE -> {
                addStandardFlowingCape(secondary, runeGlow, capeFlow, facingYaw, leanX, place, out)
                Arena3DMath.addOrbitRing(out, place(Vec3(0f, 1.05f, -0.12f)), 0.56f, 0.024f, 16, runeGlow.copy(alpha = 0.72f), rotX = 0.75f, rotY = time * 1.9f, neonGlow = true)
            }

            HeroSkin.ECLIPSE_PHANTOM -> {
                addStandardFlowingCape(primary.copy(alpha = 0.55f), ArcaneCyan, capeFlow, facingYaw, leanX, place, out)
                val orbitAngle = time * 2.2f
                Arena3DMath.addOctahedron(
                    out = out,
                    center = place(Vec3(cos(orbitAngle) * 0.48f, 1.24f + sin(time * 3f) * 0.07f, sin(orbitAngle) * 0.48f - 0.1f)),
                    radius = 0.08f,
                    height = 0.15f,
                    color = ArcaneCyan,
                    rotY = time * 2.6f,
                    neonGlow = true
                )
                Arena3DMath.addOctahedron(
                    out = out,
                    center = place(Vec3(-cos(orbitAngle) * 0.48f, 1.10f - sin(time * 3f) * 0.07f, -sin(orbitAngle) * 0.48f - 0.1f)),
                    radius = 0.065f,
                    height = 0.12f,
                    color = Color(0xFFD5F7FF),
                    rotY = -time * 2.6f,
                    neonGlow = true
                )
            }

            HeroSkin.NYX_FACELESS -> {
                // Crossed Shadow Daggers strapped to back + Twin Flowing Assassin Scarves
                Arena3DMath.addTaperedBox(out, place(Vec3(0f, 0.96f, -0.22f)), Vec3(0.032f, 0.34f, 0.015f), topScaleX = 0.3f, color = runeGlow, rotY = facingYaw, rotZ = 0.65f, emissive = true, neonGlow = true)
                Arena3DMath.addTaperedBox(out, place(Vec3(0f, 0.96f, -0.22f)), Vec3(0.032f, 0.34f, 0.015f), topScaleX = 0.3f, color = runeGlow, rotY = facingYaw, rotZ = -0.65f, emissive = true, neonGlow = true)
                Arena3DMath.addTaperedBox(out, place(Vec3(-0.14f, 0.64f, -0.26f)), Vec3(0.08f, 0.38f, 0.018f), topScaleX = 0.65f, color = secondary.copy(alpha = 0.72f), rotY = facingYaw, rotX = -capeFlow - 0.1f, outline = runeGlow.copy(alpha = 0.55f))
                Arena3DMath.addTaperedBox(out, place(Vec3(0.14f, 0.64f, -0.26f)), Vec3(0.08f, 0.38f, 0.018f), topScaleX = 0.65f, color = secondary.copy(alpha = 0.72f), rotY = facingYaw, rotX = -capeFlow - 0.16f, outline = runeGlow.copy(alpha = 0.55f))
            }

            else -> {
                addStandardFlowingCape(secondary, runeGlow, capeFlow, facingYaw, leanX, place, out)
            }
        }
    }

    private fun addStandardFlowingCape(
        capeColor: Color,
        runeGlow: Color,
        capeFlow: Float,
        facingYaw: Float,
        leanX: Float,
        place: (Vec3) -> Vec3,
        out: MutableList<Face3D>
    ) {
        Arena3DMath.addTaperedBox(
            out = out,
            center = place(Vec3(0f, 0.76f, -0.23f)),
            halfSize = Vec3(0.24f, 0.38f, 0.028f),
            topScaleX = 0.72f,
            color = capeColor.copy(alpha = capeColor.alpha.coerceAtMost(0.92f)),
            rotY = facingYaw,
            rotX = leanX - capeFlow,
            outline = runeGlow.copy(alpha = 0.48f)
        )
        // Tattered Alpha-Blended Lower Cloak Tails
        Arena3DMath.addTaperedBox(
            out = out,
            center = place(Vec3(-0.14f, 0.40f, -0.28f)),
            halfSize = Vec3(0.075f, 0.18f, 0.018f),
            topScaleX = 1.2f,
            color = capeColor.copy(alpha = 0.58f),
            rotY = facingYaw,
            rotX = leanX - capeFlow - 0.10f
        )
        Arena3DMath.addTaperedBox(
            out = out,
            center = place(Vec3(0.14f, 0.40f, -0.28f)),
            halfSize = Vec3(0.075f, 0.18f, 0.018f),
            topScaleX = 1.2f,
            color = capeColor.copy(alpha = 0.58f),
            rotY = facingYaw,
            rotX = leanX - capeFlow - 0.14f
        )
    }

    private fun buildHeadgear(
        heroClass: HeroClass,
        skin: HeroSkin,
        primary: Color,
        secondary: Color,
        metalTrim: Color,
        silverHair: Color,
        runeGlow: Color,
        time: Float,
        facingYaw: Float,
        leanX: Float,
        place: (Vec3) -> Vec3,
        out: MutableList<Face3D>
    ) {
        val isHooded = skin == HeroSkin.PEREGRINO_ARCANE ||
            skin == HeroSkin.NYX_FACELESS ||
            skin == HeroSkin.ASTRAL_SCRIBER ||
            skin == HeroSkin.PRIMORDIAL_ARCHMAGE ||
            (skin == HeroSkin.CLASS_DEFAULT && (heroClass == HeroClass.MAGO || heroClass == HeroClass.LADINO))

        if (isHooded) {
            // Sculpted Hood + Deep Shadowed Recess + Neon Eyes + Silver Hair Locks
            Arena3DMath.addTaperedBox(
                out = out,
                center = place(Vec3(0f, 1.38f, -0.02f)),
                halfSize = Vec3(0.185f, 0.185f, 0.19f),
                topScaleX = 0.86f,
                topScaleZ = 0.84f,
                color = secondary,
                rotY = facingYaw,
                rotX = leanX,
                outline = runeGlow.copy(alpha = 0.72f)
            )
            // Pointed hood peak
            Arena3DMath.addPyramid(
                out = out,
                baseCenter = place(Vec3(0f, 1.54f, -0.04f)),
                radius = 0.13f,
                height = 0.15f,
                color = secondary,
                rotY = facingYaw,
                rotX = -0.35f,
                outline = runeGlow.copy(alpha = 0.5f)
            )
            Arena3DMath.addBox(
                out = out,
                center = place(Vec3(0f, 1.35f, 0.125f)),
                halfSize = Vec3(0.125f, 0.115f, 0.055f),
                color = Color(0xFF06060A),
                rotY = facingYaw,
                rotX = leanX,
                emissive = true
            )
            // Neon Eyes
            Arena3DMath.addBox(out, place(Vec3(-0.055f, 1.37f, 0.18f)), Vec3(0.028f, 0.017f, 0.014f), runeGlow, rotY = facingYaw, emissive = true, neonGlow = true)
            Arena3DMath.addBox(out, place(Vec3(0.055f, 1.37f, 0.18f)), Vec3(0.028f, 0.017f, 0.014f), runeGlow, rotY = facingYaw, emissive = true, neonGlow = true)

            if (skin == HeroSkin.NYX_FACELESS) {
                // Glowing Assassin Half-Mask
                Arena3DMath.addTaperedBox(out, place(Vec3(0f, 1.29f, 0.165f)), Vec3(0.105f, 0.055f, 0.03f), topScaleX = 1.1f, color = Color(0xFF14131E), rotY = facingYaw, outline = runeGlow, neonGlow = true)
            }
            if (skin == HeroSkin.PRIMORDIAL_ARCHMAGE) {
                // Floating Triple Crystal Diadem above hood
                val hover = sin(time * 3.2f) * 0.03f
                Arena3DMath.addOctahedron(out, place(Vec3(0f, 1.72f + hover, 0.08f)), 0.06f, 0.13f, ArcaneCyan, rotY = time * 2f, neonGlow = true)
                Arena3DMath.addOctahedron(out, place(Vec3(-0.14f, 1.66f + hover, 0.06f)), 0.045f, 0.09f, ParadoxMagenta, rotY = -time * 2f, neonGlow = true)
                Arena3DMath.addOctahedron(out, place(Vec3(0.14f, 1.66f + hover, 0.06f)), 0.045f, 0.09f, ParadoxMagenta, rotY = -time * 2f, neonGlow = true)
            }

            // Silver Hair Strands
            Arena3DMath.addBox(out, place(Vec3(-0.145f, 1.24f, 0.075f)), Vec3(0.03f, 0.11f, 0.038f), silverHair, rotY = facingYaw, rotZ = 0.12f)
            Arena3DMath.addBox(out, place(Vec3(0.145f, 1.24f, 0.075f)), Vec3(0.03f, 0.11f, 0.038f), silverHair, rotY = facingYaw, rotZ = -0.12f)
        } else {
            // Sculpted Knight / Swordsaint / Dragon Helm
            Arena3DMath.addTaperedBox(
                out = out,
                center = place(Vec3(0f, 1.37f, 0f)),
                halfSize = Vec3(0.165f, 0.165f, 0.17f),
                topScaleX = 0.90f,
                topScaleZ = 0.90f,
                color = primary,
                rotY = facingYaw,
                rotX = leanX,
                outline = runeGlow
            )
            // Glowing Visor Slit
            Arena3DMath.addBox(
                out = out,
                center = place(Vec3(0f, 1.38f, 0.168f)),
                halfSize = Vec3(0.11f, 0.034f, 0.025f),
                color = runeGlow,
                rotY = facingYaw,
                rotX = leanX,
                emissive = true,
                neonGlow = true
            )
            // Helm Cheek / Brow Crest
            Arena3DMath.addBox(
                out = out,
                center = place(Vec3(0f, 1.49f, 0.04f)),
                halfSize = Vec3(0.045f, 0.055f, 0.16f),
                color = metalTrim,
                rotY = facingYaw,
                rotX = leanX,
                outline = runeGlow.copy(alpha = 0.6f)
            )

            when (skin) {
                HeroSkin.KAEL_IRON -> {
                    Arena3DMath.addPyramid(out, place(Vec3(-0.13f, 1.52f, 0f)), 0.055f, 0.24f, GildedAmber, rotY = facingYaw, rotZ = 0.28f, outline = CrimsonBlood)
                    Arena3DMath.addPyramid(out, place(Vec3(0.13f, 1.52f, 0f)), 0.055f, 0.24f, GildedAmber, rotY = facingYaw, rotZ = -0.28f, outline = CrimsonBlood)
                }
                HeroSkin.BLOOD_TYRANT -> {
                    // Large Swept Crimson Dragon Horns
                    Arena3DMath.addPyramid(out, place(Vec3(-0.14f, 1.50f, -0.02f)), 0.065f, 0.34f, runeGlow, rotY = facingYaw, rotX = -0.35f, rotZ = 0.45f, emissive = true, neonGlow = true)
                    Arena3DMath.addPyramid(out, place(Vec3(0.14f, 1.50f, -0.02f)), 0.065f, 0.34f, runeGlow, rotY = facingYaw, rotX = -0.35f, rotZ = -0.45f, emissive = true, neonGlow = true)
                }
                HeroSkin.CELESTIAL_SWORDSAINT -> {
                    // Imperial Laurel Crown + Floating Solar Halo
                    Arena3DMath.addOrbitRing(out, place(Vec3(0f, 1.58f, 0f)), 0.24f, 0.025f, 14, GildedAmber, rotX = 0.15f, rotY = time * 1.4f, neonGlow = true)
                    Arena3DMath.addOctahedron(out, place(Vec3(0f, 1.63f, 0.16f)), 0.045f, 0.09f, ArcaneCyan, rotY = time * 2.2f, neonGlow = true)
                }
                HeroSkin.ECLIPSE_PHANTOM -> {
                    // Lunar Crescent Horns
                    Arena3DMath.addPyramid(out, place(Vec3(-0.11f, 1.53f, 0.08f)), 0.038f, 0.19f, ArcaneCyan, rotY = facingYaw, rotZ = 0.22f, emissive = true, neonGlow = true)
                    Arena3DMath.addPyramid(out, place(Vec3(0.11f, 1.53f, 0.08f)), 0.038f, 0.19f, ArcaneCyan, rotY = facingYaw, rotZ = -0.22f, emissive = true, neonGlow = true)
                }
                else -> {
                    Arena3DMath.addBox(out, place(Vec3(0f, 1.52f, -0.04f)), Vec3(0.15f, 0.045f, 0.15f), silverHair, rotY = facingYaw)
                }
            }
        }
    }

    private fun buildClassWeapon(
        heroClass: HeroClass,
        skin: HeroSkin,
        runeGlow: Color,
        metalTrim: Color,
        armSwing: Float,
        attackProgress: Float,
        time: Float,
        facingYaw: Float,
        leanX: Float,
        place: (Vec3) -> Vec3,
        out: MutableList<Face3D>
    ) {
        val slashAngle = if (attackProgress > 0f) sin(attackProgress * PI.toFloat()) * 1.45f else armSwing
        val bladeSteel = when (skin) {
            HeroSkin.BLOOD_TYRANT -> Color(0xFF2B1218)
            HeroSkin.CELESTIAL_SWORDSAINT -> Color(0xFFF6FAFF)
            else -> Color(0xFFE0E6F2)
        }

        when (heroClass) {
            HeroClass.GUERREIRO -> {
                // Sculpted Runic Greatsword + Crossguard + Pommel Crystal
                Arena3DMath.addTaperedBox(
                    out = out,
                    center = place(Vec3(0.44f, 1.12f, 0.28f)),
                    halfSize = Vec3(0.068f, 0.56f, 0.032f),
                    topScaleX = 0.35f,
                    color = bladeSteel,
                    rotY = facingYaw,
                    rotX = leanX + 0.35f + slashAngle,
                    emissive = true,
                    outline = runeGlow,
                    neonGlow = skin == HeroSkin.CELESTIAL_SWORDSAINT || skin == HeroSkin.BLOOD_TYRANT
                )
                Arena3DMath.addBox(out, place(Vec3(0.42f, 0.78f, 0.20f)), Vec3(0.17f, 0.045f, 0.055f), metalTrim, rotY = facingYaw, rotX = leanX + slashAngle * 0.4f, outline = runeGlow)
            }
            HeroClass.MAGO -> {
                // Ornate Catalyst Staff with Orbiting Crystal Ring
                Arena3DMath.addBox(out, place(Vec3(0.42f, 0.94f, 0.22f)), Vec3(0.038f, 0.68f, 0.038f), metalTrim, rotY = facingYaw, rotX = leanX + slashAngle * 0.6f, outline = runeGlow.copy(alpha = 0.5f))
                val crystalPos = place(Vec3(0.42f, 1.74f + sin(time * 4f) * 0.05f, 0.26f))
                Arena3DMath.addOctahedron(out, crystalPos, 0.13f, 0.19f, runeGlow, rotY = time * 2.8f, neonGlow = true)
                Arena3DMath.addOrbitRing(out, crystalPos, 0.21f, 0.018f, 10, GildedAmber, rotX = 0.8f, rotY = -time * 2.4f, neonGlow = true)
            }
            HeroClass.LADINO -> {
                // Dual Tapered Neon Daggers
                Arena3DMath.addTaperedBox(out, place(Vec3(0.40f, 0.88f, 0.28f)), Vec3(0.04f, 0.34f, 0.024f), topScaleX = 0.28f, color = runeGlow, rotY = facingYaw, rotX = 0.82f + slashAngle, emissive = true, neonGlow = true)
                Arena3DMath.addTaperedBox(out, place(Vec3(-0.40f, 0.88f, 0.28f)), Vec3(0.04f, 0.34f, 0.024f), topScaleX = 0.28f, color = runeGlow, rotY = facingYaw, rotX = 0.82f - slashAngle * 0.5f, emissive = true, neonGlow = true)
            }
            HeroClass.ARQUEIRO -> {
                // Recurve Arcane Bow + Glowing Energy Arrow
                Arena3DMath.addTaperedBox(out, place(Vec3(0.38f, 1.18f, 0.34f)), Vec3(0.038f, 0.26f, 0.045f), topScaleX = 0.6f, color = metalTrim, rotY = facingYaw, rotX = -0.28f, outline = runeGlow)
                Arena3DMath.addTaperedBox(out, place(Vec3(0.38f, 0.86f, 0.34f)), Vec3(0.038f, 0.26f, 0.045f), topScaleX = 0.6f, color = metalTrim, rotY = facingYaw, rotX = 0.28f, outline = runeGlow)
                Arena3DMath.addBox(out, place(Vec3(0.38f, 1.02f, 0.48f)), Vec3(0.024f, 0.024f, 0.36f), runeGlow, rotY = facingYaw, emissive = true, neonGlow = true)
            }
            HeroClass.CLERIGO -> {
                // Sanctified Scepter + Radiant Seal Prism
                Arena3DMath.addBox(out, place(Vec3(0.42f, 0.92f, 0.22f)), Vec3(0.048f, 0.50f, 0.048f), GildedAmber, rotY = facingYaw, rotX = slashAngle * 0.7f, outline = runeGlow.copy(alpha = 0.5f))
                Arena3DMath.addOctahedron(out, place(Vec3(0.42f, 1.52f, 0.25f)), 0.15f, 0.16f, SoulEmerald, rotY = time * 2f, neonGlow = true)
                Arena3DMath.addOrbitRing(out, place(Vec3(0.42f, 1.52f, 0.25f)), 0.24f, 0.02f, 10, runeGlow, rotX = 1.2f, rotY = time * 1.6f, neonGlow = true)
            }
        }
    }
}
