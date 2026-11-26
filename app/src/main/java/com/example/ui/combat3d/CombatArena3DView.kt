package com.example.ui.combat3d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.catalog.ClassAndWorldCatalog
import com.example.domain.model.HeroClass
import com.example.domain.model.HeroSkin
import com.example.ui.combat3d.Arena3DMath.drawProjectedFaces
import com.example.ui.theme.ArcaneCyan
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
import com.example.ui.viewmodel.BattleOutcome
import com.example.ui.viewmodel.CombatAnimEvent
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private data class Projectile3D(
    val start: Vec3,
    val end: Vec3,
    val progress: Float,
    val color: Color
)

@Composable
fun CombatArena3DView(
    uiState: AeldrinUiState,
    onSelectSkin: (HeroSkin) -> Unit,
    onAttackInRange: () -> Unit,
    onAttackOutOfRange: () -> Unit,
    onBossStrikeResolved: (playerInDangerZone: Boolean, playerParried: Boolean, playerDodged: Boolean) -> Unit,
    fillAvailableHeight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val combat = uiState.combat
    val encounter = combat.encounter
    val stats = uiState.stats
    val region = remember(encounter.regionId) {
        ClassAndWorldCatalog.regions.find { it.id == encounter.regionId }
    }

    val latestCombat by rememberUpdatedState(combat)
    val latestClass by rememberUpdatedState(stats.heroClass)
    val latestOnBossStrike by rememberUpdatedState(onBossStrikeResolved)

    // Camera & Viewport State
    var isExpanded by remember { mutableStateOf(false) }
    var lockOnEnabled by remember { mutableStateOf(true) }
    var camYaw by remember { mutableFloatStateOf(0f) }
    var camPitch by remember { mutableFloatStateOf(-0.34f) }
    var camTargetX by remember { mutableFloatStateOf(0f) }
    var camTargetZ by remember { mutableFloatStateOf(-1.8f) }

    // Hero Real-Time 3D State
    var heroX by remember(encounter.id) { mutableFloatStateOf(0f) }
    var heroZ by remember(encounter.id) { mutableFloatStateOf(-2.0f) }
    var heroYaw by remember(encounter.id) { mutableFloatStateOf(0f) }
    var heroWalkPhase by remember { mutableFloatStateOf(0f) }
    var heroAttackTimer by remember { mutableFloatStateOf(0f) }
    var dashTimer by remember { mutableFloatStateOf(0f) }
    var dashCooldown by remember { mutableFloatStateOf(0f) }
    var parryWindowTimer by remember { mutableFloatStateOf(0f) }

    // Joystick input (-1..1)
    var joyX by remember { mutableFloatStateOf(0f) }
    var joyY by remember { mutableFloatStateOf(0f) }

    // Boss Real-Time 3D AI State
    var enemyX by remember(encounter.id) { mutableFloatStateOf(0f) }
    var enemyZ by remember(encounter.id) { mutableFloatStateOf(2.1f) }
    var enemyYaw by remember(encounter.id) { mutableFloatStateOf(PI.toFloat()) }
    var enemyWalkPhase by remember { mutableFloatStateOf(0f) }
    var enemyMoving by remember { mutableStateOf(false) }
    var bossCooldownTimer by remember(encounter.id) { mutableFloatStateOf(2.2f) }
    var aoeActive by remember(encounter.id) { mutableStateOf(false) }
    var aoeCenterX by remember(encounter.id) { mutableFloatStateOf(0f) }
    var aoeCenterZ by remember(encounter.id) { mutableFloatStateOf(0f) }
    var aoeCharge by remember(encounter.id) { mutableFloatStateOf(0f) }
    val aoeRadius = 1.9f

    // VFX & Projectiles
    var elapsedSec by remember { mutableFloatStateOf(0f) }
    var vfxTimer by remember { mutableFloatStateOf(0f) }
    var activeVfx by remember { mutableStateOf(CombatAnimEvent.IDLE) }
    var projectile by remember { mutableStateOf<Projectile3D?>(null) }
    var showSkinDialog by remember { mutableStateOf(false) }

    // React to skill / attack triggers from ViewModel
    LaunchedEffect(combat.lastAnimSeq) {
        if (combat.lastAnimSeq > 0L) {
            activeVfx = combat.lastAnimEvent
            vfxTimer = 0.65f
            heroAttackTimer = 0.35f
            // Auto-face boss when casting a skill
            heroYaw = atan2(enemyX - heroX, enemyZ - heroZ)
            if (combat.lastAnimEvent == CombatAnimEvent.HERO_MAGIC ||
                combat.lastAnimEvent == CombatAnimEvent.HERO_PARADOX
            ) {
                projectile = Projectile3D(
                    start = Vec3(heroX, 1.05f, heroZ),
                    end = Vec3(enemyX, 1.15f, enemyZ),
                    progress = 0f,
                    color = stats.activeSkin.accentGlow
                )
            }
        }
    }

    // 60 FPS Real-Time Movement, Camera Follow & Boss AI Loop
    LaunchedEffect(encounter.id) {
        var prevNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (prevNanos != 0L) {
                    val dt = ((now - prevNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                    elapsedSec += dt

                    // Timers
                    if (vfxTimer > 0f) {
                        vfxTimer = (vfxTimer - dt).coerceAtLeast(0f)
                        if (vfxTimer <= 0f) activeVfx = CombatAnimEvent.IDLE
                    }
                    if (heroAttackTimer > 0f) heroAttackTimer = (heroAttackTimer - dt).coerceAtLeast(0f)
                    if (dashTimer > 0f) dashTimer = (dashTimer - dt).coerceAtLeast(0f)
                    if (dashCooldown > 0f) dashCooldown = (dashCooldown - dt).coerceAtLeast(0f)
                    if (parryWindowTimer > 0f) parryWindowTimer = (parryWindowTimer - dt).coerceAtLeast(0f)

                    projectile = projectile?.let { p ->
                        val nextP = p.progress + dt * 3.8f
                        if (nextP >= 1f) null else p.copy(progress = nextP)
                    }

                    // 1. Hero 360-degree Movement relative to camera Yaw
                    val joyMag = hypot(joyX, joyY)
                    if (joyMag > 0.08f && latestCombat.outcome == BattleOutcome.ONGOING) {
                        val fwdX = -sin(camYaw)
                        val fwdZ = cos(camYaw)
                        val rightX = cos(camYaw)
                        val rightZ = sin(camYaw)

                        // Joystick Y up is negative on screen -> forward
                        val moveX = rightX * joyX + fwdX * (-joyY)
                        val moveZ = rightZ * joyX + fwdZ * (-joyY)
                        val moveLen = hypot(moveX, moveZ).coerceAtLeast(0.001f)
                        val nx = moveX / moveLen
                        val nz = moveZ / moveLen

                        val baseSpeed = if (dashTimer > 0f) 7.4f else 3.2f
                        val step = baseSpeed * minOf(1f, joyMag) * dt
                        var nextX = heroX + nx * step
                        var nextZ = heroZ + nz * step

                        // Keep inside circular 3D arena
                        val r = hypot(nextX, nextZ)
                        val maxR = Arena3DMeshes.ARENA_RADIUS - 0.45f
                        if (r > maxR) {
                            nextX = nextX / r * maxR
                            nextZ = nextZ / r * maxR
                        }
                        heroX = nextX
                        heroZ = nextZ
                        heroYaw = atan2(nx, nz)
                        heroWalkPhase += dt * (if (dashTimer > 0f) 18f else 11f)
                    } else if (dashTimer > 0f && latestCombat.outcome == BattleOutcome.ONGOING) {
                        // Dash forward in facing direction even if stick released
                        val step = 7.4f * dt
                        var nextX = heroX + sin(heroYaw) * step
                        var nextZ = heroZ + cos(heroYaw) * step
                        val r = hypot(nextX, nextZ)
                        val maxR = Arena3DMeshes.ARENA_RADIUS - 0.45f
                        if (r > maxR) {
                            nextX = nextX / r * maxR
                            nextZ = nextZ / r * maxR
                        }
                        heroX = nextX
                        heroZ = nextZ
                        heroWalkPhase += dt * 16f
                    }

                    // Smooth 3rd-person camera follow
                    val followLerp = (dt * 7f).coerceAtMost(1f)
                    camTargetX += (heroX - camTargetX) * followLerp
                    camTargetZ += (heroZ - camTargetZ) * followLerp

                    if (lockOnEnabled && hypot(joyX, joyY) < 0.08f) {
                        // Gently orient camera toward boss when idle if lock-on enabled
                        val desiredYaw = atan2(-(enemyX - heroX), enemyZ - heroZ)
                        val diff = atan2(sin(desiredYaw - camYaw), cos(desiredYaw - camYaw))
                        camYaw += diff * (dt * 1.4f)
                    }

                    // 2. Real-Time 3D Boss AI & AoE Telegraphs
                    if (latestCombat.outcome == BattleOutcome.ONGOING && latestCombat.enemyFreezeTurns == 0) {
                        val toHeroX = heroX - enemyX
                        val toHeroZ = heroZ - enemyZ
                        val dist = hypot(toHeroX, toHeroZ).coerceAtLeast(0.001f)
                        enemyYaw = atan2(toHeroX, toHeroZ)

                        if (!aoeActive) {
                            if (dist > 1.85f) {
                                enemyMoving = true
                                val bossSpeed = 1.25f
                                enemyX += (toHeroX / dist) * bossSpeed * dt
                                enemyZ += (toHeroZ / dist) * bossSpeed * dt
                                enemyWalkPhase += dt * 6f
                            } else {
                                enemyMoving = false
                            }
                            bossCooldownTimer -= dt
                            if (bossCooldownTimer <= 0f) {
                                aoeActive = true
                                aoeCenterX = heroX
                                aoeCenterZ = heroZ
                                aoeCharge = 0f
                            }
                        } else {
                            enemyMoving = false
                            aoeCharge += dt / 1.45f
                            if (aoeCharge >= 1f) {
                                val distFromAoe = hypot(heroX - aoeCenterX, heroZ - aoeCenterZ)
                                val inZone = distFromAoe <= aoeRadius
                                val dodged = dashTimer > 0f
                                val parried = parryWindowTimer > 0f
                                if (parried && inZone) {
                                    activeVfx = CombatAnimEvent.HERO_PARRY
                                    vfxTimer = 0.65f
                                }
                                latestOnBossStrike(inZone, parried, dodged)
                                aoeActive = false
                                aoeCharge = 0f
                                bossCooldownTimer = 2.8f
                            }
                        }
                    } else {
                        enemyMoving = false
                        if (latestCombat.enemyFreezeTurns > 0) aoeActive = false
                    }
                }
                prevNanos = now
            }
        }
    }

    val skyColors = remember(encounter.regionId) {
        when (encounter.regionId) {
            "kael_dorn" -> listOf(Color(0xFF1F0C12), Color(0xFF130B14), VoidObsidian)
            "mirath" -> listOf(Color(0xFF0A1D2B), Color(0xFF0E1626), VoidObsidian)
            "nyx" -> listOf(Color(0xFF1B0D26), Color(0xFF100A19), VoidObsidian)
            "aethel" -> listOf(Color(0xFF0C2422), Color(0xFF0D1724), VoidObsidian)
            "solenis" -> listOf(Color(0xFF261D10), Color(0xFF14111E), VoidObsidian)
            else -> listOf(Color(0xFF1D1030), Color(0xFF100B1C), VoidObsidian)
        }
    }

    val isRanged = stats.heroClass == HeroClass.MAGO || stats.heroClass == HeroClass.ARQUEIRO
    val distToBoss = hypot(enemyX - heroX, enemyZ - heroZ)
    val inAttackRange = if (isRanged) distToBoss <= 7.5f else distToBoss <= 3.6f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (fillAvailableHeight) Modifier.fillMaxSize()
                else Modifier.height(if (isExpanded) 490.dp else 350.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(skyColors))
            .border(
                1.5.dp,
                if (combat.enemyAerialWeakPointOpen) SoulEmerald else CardBorder,
                RoundedCornerShape(18.dp)
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    lockOnEnabled = false
                    camYaw += dragAmount.x * 0.007f
                    camPitch = (camPitch - dragAmount.y * 0.005f).coerceIn(-0.68f, -0.08f)
                }
            }
            .testTag("combat_arena_3d_viewport")
    ) {
        // 3D Perspective Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val camDist = if (isExpanded) 5.8f else 6.1f
            val camTarget = Vec3(camTargetX, 0.9f, camTargetZ)

            // Ambient region particles
            val particleColor = region?.accentColor ?: AstralViolet
            for (i in 0 until 14) {
                val px = ((i * 79f + elapsedSec * (12f + i % 4 * 5f)) % w)
                val py = ((i * 47f - elapsedSec * 14f).mod(h * 0.7f))
                drawCircle(
                    color = particleColor.copy(alpha = 0.25f),
                    radius = 2f + (i % 3),
                    center = Offset(px, py)
                )
            }

            val faces = ArrayList<Face3D>(220)
            Arena3DMeshes.buildEnvironment(encounter.regionId, elapsedSec, faces)

            val atkProg = if (heroAttackTimer > 0f) 1f - (heroAttackTimer / 0.35f) else 0f
            Arena3DMeshes.buildHero3D(
                heroClass = stats.heroClass,
                skin = stats.activeSkin,
                pos = Vec3(heroX, 0f, heroZ),
                facingYaw = heroYaw,
                walkPhase = heroWalkPhase,
                isMoving = hypot(joyX, joyY) > 0.08f,
                isDashing = dashTimer > 0f,
                attackProgress = atkProg,
                time = elapsedSec,
                shieldActive = combat.playerShield > 0 || parryWindowTimer > 0f,
                out = faces
            )

            Arena3DMeshes.buildEnemy3D(
                encounter = encounter,
                pos = Vec3(enemyX, 0f, enemyZ),
                facingYaw = enemyYaw,
                walkPhase = enemyWalkPhase,
                isMoving = enemyMoving,
                aerialOpen = combat.enemyAerialWeakPointOpen,
                isFrozen = combat.enemyFreezeTurns > 0,
                aoeCenter = if (aoeActive) Vec3(aoeCenterX, 0f, aoeCenterZ) else null,
                aoeRadius = aoeRadius,
                aoeProgress = aoeCharge,
                time = elapsedSec,
                out = faces
            )

            // Active 3D Projectiles & Combat VFX
            projectile?.let { proj ->
                val curPos = Vec3(
                    proj.start.x + (proj.end.x - proj.start.x) * proj.progress,
                    proj.start.y + (proj.end.y - proj.start.y) * proj.progress,
                    proj.start.z + (proj.end.z - proj.start.z) * proj.progress
                )
                Arena3DMath.addOctahedron(faces, curPos, 0.22f, 0.22f, proj.color, rotY = elapsedSec * 10f)
            }

            if (parryWindowTimer > 0f) {
                Arena3DMath.addOrbitRing(
                    faces,
                    Vec3(heroX, 0.95f, heroZ),
                    0.95f,
                    0.07f,
                    16,
                    GildedAmber,
                    rotX = 1.35f,
                    rotY = elapsedSec * 5f
                )
            }

            if (vfxTimer > 0f) {
                val p = 1f - (vfxTimer / 0.65f)
                when (activeVfx) {
                    CombatAnimEvent.HERO_PURIFY -> {
                        Arena3DMath.addBox(
                            faces,
                            Vec3(enemyX, 1.4f, enemyZ),
                            Vec3(0.42f * (1f - p * 0.3f), 1.8f, 0.42f * (1f - p * 0.3f)),
                            SoulEmerald.copy(alpha = 0.5f),
                            rotY = elapsedSec * 3f,
                            emissive = true
                        )
                    }
                    CombatAnimEvent.HERO_PARADOX -> {
                        Arena3DMath.addOrbitRing(faces, Vec3(heroX, 1.0f, heroZ), 1.1f + p, 0.06f, 18, ParadoxMagenta, rotX = 0.8f, rotY = elapsedSec * 5f)
                        Arena3DMath.addOrbitRing(faces, Vec3(enemyX, 1.1f, enemyZ), 1.0f + (1f - p) * 0.6f, 0.06f, 16, ArcaneCyan, rotX = -0.8f, rotY = -elapsedSec * 4f)
                    }
                    else -> Unit
                }
            }

            val projected = Arena3DMath.projectAndSortFaces(
                faces = faces,
                camTarget = camTarget,
                camYaw = camYaw,
                camPitch = camPitch,
                camDistance = camDist,
                width = w,
                height = h
            )
            drawProjectedFaces(projected, elapsedSec)

            if (vfxTimer > 0f && combat.lastDamageDealt > 0) {
                val p = 1f - (vfxTimer / 0.65f)
                Arena3DMath.projectPoint(Vec3(enemyX, 1.2f, enemyZ), camTarget, camYaw, camPitch, camDist, w, h)?.first?.let { center ->
                    drawCircle(
                        color = stats.activeSkin.accentGlow.copy(alpha = (1f - p) * 0.85f),
                        radius = 18f + p * 68f,
                        center = center,
                        style = Stroke(width = 4f)
                    )
                }
            }
        }

        // Top HUD: Region, Distance indicator, Skin, Lock-on, Expand 3D
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = VoidObsidian.copy(alpha = 0.78f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(
                        text = "${region?.name ?: "Aeldrin"} · Dist: ${"%.1f".format(distToBoss)}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (inAttackRange) SoulEmerald else GildedAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (aoeActive) "PERIGO! Saia do círculo vermelho!" else "Arraste para girar a câmera 3D",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (aoeActive) CrimsonBlood else TextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = VoidObsidian.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .border(1.dp, stats.activeSkin.accentGlow.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .clickable { showSkinDialog = true }
                        .testTag("open_skin_picker_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Checkroom, contentDescription = "Skins 3D", tint = stats.activeSkin.accentGlow, modifier = Modifier.size(15.dp))
                        Text("Skin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                IconButton(
                    onClick = { lockOnEnabled = !lockOnEnabled },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (lockOnEnabled) GildedAmber.copy(alpha = 0.28f) else VoidObsidian.copy(alpha = 0.75f))
                        .testTag("lock_on_camera_button")
                ) {
                    Icon(
                        Icons.Filled.GpsFixed,
                        contentDescription = "Travar mira no chefe",
                        tint = if (lockOnEnabled) GildedAmber else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        camYaw = 0f
                        camPitch = -0.34f
                        lockOnEnabled = true
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(VoidObsidian.copy(alpha = 0.75f))
                        .testTag("reset_camera_3d_button")
                ) {
                    Icon(Icons.Filled.Cameraswitch, contentDescription = "Resetar câmera 3D", tint = GildedAmber, modifier = Modifier.size(18.dp))
                }

                if (!fillAvailableHeight) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(VoidObsidian.copy(alpha = 0.75f))
                            .testTag("expand_3d_button")
                    ) {
                        Icon(
                            if (isExpanded) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                            contentDescription = "Expandir Arena 3D",
                            tint = AstralViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Bottom-Left: Genshin-Style 360° Virtual Analog Joystick
        val joyBaseRadiusPx = 115f
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .size(116.dp)
                .clip(CircleShape)
                .background(VoidObsidian.copy(alpha = 0.52f))
                .border(1.5.dp, GildedAmber.copy(alpha = 0.5f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val dx = (offset.x - cx) / joyBaseRadiusPx
                            val dy = (offset.y - cy) / joyBaseRadiusPx
                            val len = hypot(dx, dy).coerceAtLeast(1f)
                            joyX = (dx / len).coerceIn(-1f, 1f)
                            joyY = (dy / len).coerceIn(-1f, 1f)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val dx = (change.position.x - cx) / joyBaseRadiusPx
                            val dy = (change.position.y - cy) / joyBaseRadiusPx
                            val mag = hypot(dx, dy)
                            if (mag > 1f) {
                                joyX = dx / mag
                                joyY = dy / mag
                            } else {
                                joyX = dx
                                joyY = dy
                            }
                        },
                        onDragEnd = {
                            joyX = 0f
                            joyY = 0f
                        },
                        onDragCancel = {
                            joyX = 0f
                            joyY = 0f
                        }
                    )
                }
                .testTag("virtual_joystick_3d")
        ) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset((joyX * 32.dp.toPx()).roundToInt(), (joyY * 32.dp.toPx()).roundToInt())
                    }
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GildedAmber.copy(alpha = 0.85f))
                    .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            )
        }

        // Bottom-Right: Genshin-Style Action Cluster (Atacar, Esquiva/Dash, Parry)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Parry Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (parryWindowTimer > 0f) GildedAmber else DeepSlate.copy(alpha = 0.88f))
                        .border(1.5.dp, GildedAmber, CircleShape)
                        .clickable(enabled = combat.outcome == BattleOutcome.ONGOING) {
                            parryWindowTimer = 0.65f
                            heroYaw = atan2(enemyX - heroX, enemyZ - heroZ)
                        }
                        .testTag("arena_parry_3d_button")
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = "Parry 3D",
                        tint = if (parryWindowTimer > 0f) VoidObsidian else GildedAmber,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text("Parry", style = MaterialTheme.typography.labelSmall, color = GildedAmber)
            }

            // Esquiva / Dash Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val canDash = dashCooldown <= 0f && combat.outcome == BattleOutcome.ONGOING
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (dashTimer > 0f) ArcaneCyan else DeepSlate.copy(alpha = 0.88f))
                        .border(1.5.dp, if (canDash) ArcaneCyan else TextSecondary, CircleShape)
                        .clickable(enabled = canDash) {
                            dashTimer = 0.42f
                            dashCooldown = 1.15f
                        }
                        .testTag("arena_dash_3d_button")
                ) {
                    Icon(
                        Icons.Filled.DirectionsRun,
                        contentDescription = "Esquiva / Dash",
                        tint = if (dashTimer > 0f) VoidObsidian else ArcaneCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text("Esquiva", style = MaterialTheme.typography.labelSmall, color = ArcaneCyan)
            }

            // Main Attack Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(if (inAttackRange) CrimsonBlood else ElevatedCard.copy(alpha = 0.88f))
                        .border(2.dp, GildedAmber, CircleShape)
                        .clickable(enabled = combat.outcome == BattleOutcome.ONGOING) {
                            heroYaw = atan2(enemyX - heroX, enemyZ - heroZ)
                            heroAttackTimer = 0.35f
                            if (isRanged) {
                                projectile = Projectile3D(
                                    start = Vec3(heroX, 1.05f, heroZ),
                                    end = Vec3(enemyX, 1.15f, enemyZ),
                                    progress = 0f,
                                    color = stats.activeSkin.accentGlow
                                )
                            } else if (distToBoss in 1.6f..3.6f) {
                                // Genshin-style melee gap-closing step toward boss!
                                val dx = enemyX - heroX
                                val dz = enemyZ - heroZ
                                val d = sqrt(dx * dx + dz * dz).coerceAtLeast(0.01f)
                                val step = (d - 1.4f).coerceAtLeast(0f)
                                heroX += dx / d * step
                                heroZ += dz / d * step
                            }
                            if (inAttackRange) {
                                onAttackInRange()
                            } else {
                                onAttackOutOfRange()
                            }
                        }
                        .testTag("arena_attack_3d_button")
                ) {
                    Icon(
                        Icons.Filled.SportsKabaddi,
                        contentDescription = "Atacar 3D",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text("Atacar", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showSkinDialog) {
        HeroSkinPickerDialog(
            currentSkin = stats.activeSkin,
            heroClass = stats.heroClass,
            onSelectSkin = { skin ->
                onSelectSkin(skin)
                showSkinDialog = false
            },
            onDismiss = { showSkinDialog = false }
        )
    }
}
