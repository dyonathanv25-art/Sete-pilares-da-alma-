package com.example.ui.combat3d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.HeroClass
import com.example.domain.model.HeroSkin
import com.example.ui.combat3d.Arena3DMath.drawProjectedFaces
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepSlate
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidObsidian

@Composable
fun HeroSkinPickerDialog(
    currentSkin: HeroSkin,
    onSelectSkin: (HeroSkin) -> Unit,
    onDismiss: () -> Unit,
    heroClass: HeroClass = HeroClass.GUERREIRO
) {
    var previewSkin by remember(currentSkin) { mutableStateOf(currentSkin) }
    var manualYawOffset by remember { mutableFloatStateOf(0f) }
    var animTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastNs = 0L
        while (true) {
            withFrameNanos { nowNs ->
                if (lastNs != 0L) {
                    val dt = ((nowNs - lastNs) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    animTime += dt
                }
                lastNs = nowNs
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = DeepSlate,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 740.dp)
                .heightIn(max = 400.dp)
                .border(1.5.dp, previewSkin.accentGlow.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Arsenal de Skins 3D",
                            style = MaterialTheme.typography.titleLarge,
                            color = GildedAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Arraste o modelo para girar em 360° e escolha seu visual",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_skin_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Fechar skins",
                            tint = Color.White
                        )
                    }
                }

                // Split Pane: Live 3D Rotating Preview (Left) + Scrollable Skin List (Right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left: Live 3D Preview Card
                    Column(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        previewSkin.accentGlow.copy(alpha = 0.22f),
                                        VoidObsidian
                                    )
                                )
                            )
                            .border(1.dp, previewSkin.accentGlow.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures { _, dragAmount ->
                                        manualYawOffset += dragAmount * 0.015f
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val faces = ArrayList<Face3D>(140)
                                Arena3DMath.addOrbitRing(
                                    out = faces,
                                    center = Vec3(0f, 0.02f, 0f),
                                    radius = 0.72f,
                                    thickness = 0.03f,
                                    segments = 20,
                                    color = previewSkin.accentGlow.copy(alpha = 0.65f),
                                    rotX = 0f,
                                    rotY = animTime * 0.5f,
                                    neonGlow = true
                                )
                                Arena3DMeshes.buildHero3D(
                                    heroClass = heroClass,
                                    skin = previewSkin,
                                    pos = Vec3(0f, 0f, 0f),
                                    facingYaw = animTime * 0.85f + manualYawOffset,
                                    walkPhase = 0f,
                                    isMoving = false,
                                    isDashing = false,
                                    attackProgress = 0f,
                                    time = animTime,
                                    shieldActive = false,
                                    out = faces
                                )
                                val projected = Arena3DMath.projectAndSortFaces(
                                    faces = faces,
                                    camTarget = Vec3(0f, 0.92f, 0f),
                                    camYaw = 0f,
                                    camPitch = 0.22f,
                                    camDistance = 3.25f,
                                    width = size.width,
                                    height = size.height
                                )
                                drawProjectedFaces(projected, animTime)
                            }

                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VoidObsidian.copy(alpha = 0.65f))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = previewSkin.accentGlow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "360°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = previewSkin.accentGlow
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = previewSkin.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = previewSkin.accentGlow,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = previewSkin.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = { onSelectSkin(previewSkin) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = previewSkin.accentGlow,
                                contentColor = VoidObsidian
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("confirm_skin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (previewSkin == currentSkin) "Manter Skin" else "Equipar Skin",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }

                    // Right: Scrollable Skin List
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(HeroSkin.entries, key = { it.id }) { skin ->
                            val isPreviewed = skin == previewSkin
                            val isEquipped = skin == currentSkin
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isPreviewed -> skin.accentGlow.copy(alpha = 0.18f)
                                            else -> ElevatedCard
                                        }
                                    )
                                    .border(
                                        width = if (isPreviewed) 1.5.dp else 1.dp,
                                        color = if (isPreviewed) skin.accentGlow else CardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { previewSkin = skin }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("select_skin_${skin.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Dual-tone palette orb with emissive rim
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(skin.primaryColor, skin.secondaryColor)
                                            )
                                        )
                                        .border(2.dp, skin.accentGlow, CircleShape)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = skin.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isPreviewed) skin.accentGlow else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isEquipped) {
                                            Text(
                                                text = "ATIVA",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GildedAmber,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(GildedAmber.copy(alpha = 0.16f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = skin.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
