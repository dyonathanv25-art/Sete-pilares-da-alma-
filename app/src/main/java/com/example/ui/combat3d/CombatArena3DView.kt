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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import com.example.data.catalog.ClassAndWorldCatalog
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
import com.example.ui.viewmodel.CombatAnimEvent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CombatArena3DView(
    uiState: AeldrinUiState,
    onSelectSkin: (HeroSkin) -> Unit,
    modifier: Modifier = Modifier
) {
    val combat = uiState.combat
    val encounter = combat.encounter
    val stats = uiState.stats
    val region = remember(encounter.regionId) {
        ClassAndWorldCatalog.regions.find { it.id == encounter.regionId }
    }

    var camYaw by remember { mutableFloatStateOf(0.08f) }
    var camPitch by remember { mutableFloatStateOf(-0.24f) }
    var elapsedSec by remember { mutableFloatStateOf(0f) }
    var vfxTimer by remember { mutableFloatStateOf(0f) }
    var activeVfx by remember { mutableStateOf(CombatAnimEvent.IDLE) }
    var showSkinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(combat.lastAnimSeq) {
        if (combat.lastAnimSeq > 0L) {
            activeVfx = combat.lastAnimEvent
            vfxTimer = 0.75f
        }
    }

    LaunchedEffect(Unit) {
        var prevNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (prevNanos != 0L) {
                    val dt = ((now - prevNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                    elapsedSec += dt
                    if (vfxTimer > 0f) {
                        vfxTimer = (vfxTimer - dt).coerceAtLeast(0f)
                        if (vfxTimer <= 0f) activeVfx = CombatAnimEvent.IDLE
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
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
                    camYaw = (camYaw + dragAmount.x * 0.006f).coerceIn(-0.95f, 0.95f)
                    camPitch = (camPitch - dragAmount.y * 0.005f).coerceIn(-0.62f, 0.15f)
                }
            }
            .testTag("combat_arena_3d_viewport")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val camDist = 6.2f

            // Ambient region particles in sky
            val particleColor = region?.accentColor ?: AstralViolet
            for (i in 0 until 14) {
                val px = ((i * 73f + elapsedSec * (14f + i % 5 * 4f)) % w)
                val py = ((i * 41f - elapsedSec * 12f).mod(h * 0.72f))
                drawCircle(
                    color = particleColor.copy(alpha = 0.28f),
                    radius = 2.2f + (i % 3),
                    center = Offset(px, py)
                )
            }

            val vfxProgress = if (vfxTimer > 0f) (1f - (vfxTimer / 0.75f)) else 0f
            val heroLunge = when (activeVfx) {
                CombatAnimEvent.HERO_SLASH, CombatAnimEvent.HERO_MAGIC, CombatAnimEvent.HERO_PARADOX ->
                    sin(vfxProgress * PI.toFloat()) * 1.15f
                else -> 0f
            }
            val enemyLunge = if (vfxTimer > 0f && activeVfx != CombatAnimEvent.IDLE && combat.enemyFreezeTurns == 0) {
                sin(maxOf(0f, (vfxProgress - 0.45f) * 1.8f) * PI.toFloat()) * 0.6f
            } else 0f

            val faces = ArrayList<Face3D>(140)
            Arena3DMeshes.buildEnvironment(encounter.regionId, elapsedSec, faces)
            Arena3DMeshes.buildHero3D(
                heroClass = stats.heroClass,
                skin = stats.activeSkin,
                time = elapsedSec,
                lungeOffset = heroLunge,
                shieldActive = combat.playerShield > 0 || activeVfx == CombatAnimEvent.HERO_PARRY,
                out = faces
            )
            Arena3DMeshes.buildEnemy3D(
                encounter = encounter,
                time = elapsedSec,
                lungeOffset = enemyLunge,
                aerialOpen = combat.enemyAerialWeakPointOpen,
                isFrozen = combat.enemyFreezeTurns > 0,
                out = faces
            )

            // 3D VFX Geometry
            if (vfxTimer > 0f) {
                when (activeVfx) {
                    CombatAnimEvent.HERO_PARRY -> {
                        Arena3DMath.addOrbitRing(
                            faces,
                            Vec3(-0.7f, 1.0f, 0f),
                            0.85f + vfxProgress * 0.45f,
                            0.08f,
                            18,
                            GildedAmber,
                            rotX = 1.4f,
                            rotY = elapsedSec * 4f
                        )
                    }
                    CombatAnimEvent.HERO_PURIFY -> {
                        Arena3DMath.addBox(
                            faces,
                            Vec3(1.5f, 1.4f, 0.15f),
                            Vec3(0.45f * (1f - vfxProgress * 0.4f), 1.8f, 0.45f * (1f - vfxProgress * 0.4f)),
                            SoulEmerald.copy(alpha = 0.55f),
                            rotY = elapsedSec * 3f,
                            emissive = true
                        )
                    }
                    CombatAnimEvent.HERO_PARADOX -> {
                        Arena3DMath.addOrbitRing(
                            faces,
                            Vec3(0.2f, 1.1f, 0f),
                            1.2f + vfxProgress * 1.2f,
                            0.07f,
                            20,
                            ParadoxMagenta,
                            rotX = 0.9f,
                            rotY = elapsedSec * 5f
                        )
                        Arena3DMath.addOrbitRing(
                            faces,
                            Vec3(1.5f, 1.1f, 0.15f),
                            0.9f + (1f - vfxProgress) * 0.7f,
                            0.07f,
                            16,
                            ArcaneCyan,
                            rotX = -0.8f,
                            rotY = -elapsedSec * 4f
                        )
                    }
                    CombatAnimEvent.HERO_MAGIC, CombatAnimEvent.HERO_SLASH -> {
                        val projX = -0.9f + vfxProgress * 2.4f
                        Arena3DMath.addOctahedron(
                            faces,
                            Vec3(projX, 1.1f, 0f),
                            0.24f,
                            0.24f,
                            stats.activeSkin.accentGlow,
                            rotY = elapsedSec * 8f
                        )
                    }
                    else -> Unit
                }
            }

            val projected = Arena3DMath.projectAndSortFaces(
                faces = faces,
                camYaw = camYaw,
                camPitch = camPitch,
                camDistance = camDist,
                width = w,
                height = h
            )
            drawProjectedFaces(projected)

            // Impact Slash / Shockwave Overlay on Boss 3D anchor
            if (vfxTimer > 0f && combat.lastDamageDealt > 0) {
                val bossScreen = Arena3DMath.projectPoint(
                    Vec3(1.5f, 1.2f, 0.15f),
                    camYaw, camPitch, camDist, w, h
                )?.first
                if (bossScreen != null) {
                    val ringRadius = 18f + vfxProgress * 75f
                    drawCircle(
                        color = stats.activeSkin.accentGlow.copy(alpha = (1f - vfxProgress) * 0.85f),
                        radius = ringRadius,
                        center = bossScreen,
                        style = Stroke(width = 4f)
                    )
                }
            }
        }

        // Top Overlay: Region Ambientação + Camera/Skin Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = VoidObsidian.copy(alpha = 0.72f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(
                        text = "${region?.name ?: "Arena 3D"} · ${encounter.tier.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = region?.accentColor ?: GildedAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Arraste para girar câmera 3D",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Filled.Checkroom,
                            contentDescription = "Escolher Skin 3D",
                            tint = stats.activeSkin.accentGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stats.activeSkin.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(
                    onClick = {
                        camYaw = 0.08f
                        camPitch = -0.24f
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(VoidObsidian.copy(alpha = 0.75f))
                        .testTag("reset_camera_3d_button")
                ) {
                    Icon(
                        Icons.Filled.Cameraswitch,
                        contentDescription = "Resetar câmera 3D",
                        tint = GildedAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom Overlay: Floating Damage / Combat Banner
        if (vfxTimer > 0f && (combat.lastDamageDealt > 0 || combat.lastHealOrShield > 0)) {
            Surface(
                color = VoidObsidian.copy(alpha = 0.82f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .border(1.dp, GildedAmber.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (combat.lastDamageDealt > 0) {
                        Text(
                            text = "-${combat.lastDamageDealt} Dano",
                            style = MaterialTheme.typography.labelLarge,
                            color = CrimsonBlood,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (combat.lastHealOrShield > 0) {
                        Text(
                            text = "+${combat.lastHealOrShield} Vida/Escudo",
                            style = MaterialTheme.typography.labelLarge,
                            color = SoulEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showSkinDialog) {
        HeroSkinPickerDialog(
            currentSkin = stats.activeSkin,
            onSelectSkin = { skin ->
                onSelectSkin(skin)
                showSkinDialog = false
            },
            onDismiss = { showSkinDialog = false }
        )
    }
}

@Composable
fun HeroSkinPickerDialog(
    currentSkin: HeroSkin,
    onSelectSkin: (HeroSkin) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSlate,
        title = {
            Text("Skins 3D do Personagem", style = MaterialTheme.typography.headlineSmall, color = GildedAmber)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroSkin.entries.forEach { skin ->
                    val selected = skin == currentSkin
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) skin.accentGlow.copy(alpha = 0.16f) else ElevatedCard)
                            .border(
                                1.dp,
                                if (selected) skin.accentGlow else CardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectSkin(skin) }
                            .padding(10.dp)
                            .testTag("select_skin_${skin.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(skin.primaryColor)
                                .border(2.dp, skin.accentGlow, CircleShape)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = skin.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selected) skin.accentGlow else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = skin.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
