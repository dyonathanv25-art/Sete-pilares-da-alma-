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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.catalog.OpenWorldCatalog
import com.example.domain.model.HeroClass
import com.example.domain.model.HeroSkin
import com.example.domain.model.VillageInvasionState
import com.example.domain.model.WorldCampRuntime
import com.example.domain.model.WorldEntityState
import com.example.domain.model.WorldMonsterEntity
import com.example.domain.model.WorldZoneId
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private data class WorldProjectile3D(
    val start: Vec3,
    val end: Vec3,
    val progress: Float,
    val color: Color
)

@Composable
fun OpenWorld3DView(
    uiState: AeldrinUiState,
    onSelectSkin: (HeroSkin) -> Unit,
    onPlayerDamaged: (damage: Int, attackerName: String) -> Unit,
    onMonsterDefeated: (name: String, xpReward: Int, shardReward: Int) -> Unit,
    onClaimCampChest: (campName: String, xpReward: Int, shardReward: Int) -> Unit,
    onRestAtVillage: () -> Unit,
    onUpgradeAtForge: () -> Unit,
    onInvasionResolved: (victory: Boolean) -> Unit,
    onWorldLog: (String, Boolean) -> Unit,
    fillAvailableHeight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val stats = uiState.stats
    val combat = uiState.combat
    val save = uiState.save

    val latestStats by rememberUpdatedState(stats)
    val latestCombat by rememberUpdatedState(combat)
    val latestLevel by rememberUpdatedState(save.level)

    var isExpanded by remember { mutableStateOf(false) }
    var showSkinDialog by remember { mutableStateOf(false) }
    var camYaw by remember { mutableFloatStateOf(0f) }
    var camPitch by remember { mutableFloatStateOf(-0.36f) }

    // Hero Open-World Position & Combat State
    var heroX by remember { mutableFloatStateOf(0f) }
    var heroZ by remember { mutableFloatStateOf(-4.2f) }
    var heroY by remember { mutableFloatStateOf(0f) }
    var heroYaw by remember { mutableFloatStateOf(0f) }
    var camTargetX by remember { mutableFloatStateOf(0f) }
    var camTargetY by remember { mutableFloatStateOf(0.9f) }
    var camTargetZ by remember { mutableFloatStateOf(-4.2f) }
    var heroWalkPhase by remember { mutableFloatStateOf(0f) }
    var heroAttackTimer by remember { mutableFloatStateOf(0f) }
    var dashTimer by remember { mutableFloatStateOf(0f) }
    var dashCooldown by remember { mutableFloatStateOf(0f) }
    var parryTimer by remember { mutableFloatStateOf(0f) }

    var joyX by remember { mutableFloatStateOf(0f) }
    var joyY by remember { mutableFloatStateOf(0f) }

    // Open World Simulation Entities
    val staticProps = remember { OpenWorldCatalog.buildStaticProps() }
    var camps by remember { mutableStateOf(OpenWorldCatalog.spawnInitialCamps()) }
    var monsters by remember { mutableStateOf(OpenWorldCatalog.spawnAllWorldMonsters(save.level)) }
    var invasion by remember { mutableStateOf(VillageInvasionState()) }
    var localFrameTick by remember { mutableIntStateOf(0) }

    var elapsedSec by remember { mutableFloatStateOf(0f) }
    var projectile by remember { mutableStateOf<WorldProjectile3D?>(null) }
    var aoeBlastTimer by remember { mutableFloatStateOf(0f) }
    var aoeBlastColor by remember { mutableStateOf(AstralViolet) }

    // Respawn hero safely at Vila Central if HP drops to 0
    LaunchedEffect(combat.playerHp) {
        if (combat.playerHp <= 0) {
            heroX = 0f
            heroZ = -3.8f
            heroY = OpenWorldCatalog.terrainHeightAt(heroX, heroZ)
            onRestAtVillage()
            onWorldLog("Você recuou para a Vila Central e foi restaurado pela Fogueira!", true)
        }
    }

    // Cast equipped Grimoire skills directly onto nearby Open-World monsters!
    LaunchedEffect(combat.lastAnimSeq) {
        if (combat.lastAnimSeq > 0L &&
            (combat.lastAnimEvent == CombatAnimEvent.HERO_MAGIC ||
                combat.lastAnimEvent == CombatAnimEvent.HERO_PARADOX ||
                combat.lastAnimEvent == CombatAnimEvent.HERO_PURIFY)
        ) {
            aoeBlastTimer = 0.6f
            aoeBlastColor = if (combat.lastAnimEvent == CombatAnimEvent.HERO_PARADOX) ParadoxMagenta else stats.activeSkin.accentGlow
            heroAttackTimer = 0.35f

            val skillDmg = (stats.atk * 1.65f).roundToInt().coerceAtLeast(24)
            var hitCount = 0
            val updated = monsters.map { m ->
                if (m.state != WorldEntityState.DEAD && hypot(m.x - heroX, m.z - heroZ) <= 8.0f) {
                    hitCount++
                    val newHp = (m.hp - skillDmg).coerceAtLeast(0)
                    if (newHp == 0) {
                        onMonsterDefeated(m.name, if (m.isElite) 35 else 16, if (m.isElite) 14 else 6)
                        m.copy(hp = 0, state = WorldEntityState.DEAD, aoeCharge = 0f)
                    } else {
                        m.copy(hp = newHp, state = WorldEntityState.CHASING)
                    }
                } else {
                    m
                }
            }
            monsters = updated
            if (hitCount > 0) {
                onWorldLog("Habilidade arcana atingiu $hitCount inimigo(s) no mapa por $skillDmg de dano!", true)
            }
        }
    }

    fun triggerInvasionStart() {
        if (invasion.active) return
        val wave1 = OpenWorldCatalog.spawnInvasionWave(1, latestLevel)
        monsters = monsters.filterNot { it.isInvasionMob } + wave1
        invasion = VillageInvasionState(
            active = true,
            wave = 1,
            totalWaves = 2,
            crystalHp = 240,
            crystalMaxHp = 240,
            nextInvasionTimer = 50f,
            statusBanner = "ALERTA! A Vila Central está sob ataque! (Onda 1/2)"
        )
        onWorldLog("INVASÃO! Uma horda sombria marcha contra o Cristal da Vila!", true)
    }

    // 60 FPS Open-World Game Loop (Heightmap Movement, Camera Follow, Camp AI & Village Invasion)
    LaunchedEffect(Unit) {
        var prevNanos = 0L
        var aiAccum = 0f
        while (true) {
            withFrameNanos { now ->
                if (prevNanos != 0L) {
                    val dt = ((now - prevNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                    elapsedSec += dt
                    aiAccum += dt

                    if (heroAttackTimer > 0f) heroAttackTimer = (heroAttackTimer - dt).coerceAtLeast(0f)
                    if (dashTimer > 0f) dashTimer = (dashTimer - dt).coerceAtLeast(0f)
                    if (dashCooldown > 0f) dashCooldown = (dashCooldown - dt).coerceAtLeast(0f)
                    if (parryTimer > 0f) parryTimer = (parryTimer - dt).coerceAtLeast(0f)
                    if (aoeBlastTimer > 0f) aoeBlastTimer = (aoeBlastTimer - dt).coerceAtLeast(0f)

                    projectile = projectile?.let { p ->
                        val np = p.progress + dt * 4.2f
                        if (np >= 1f) null else p.copy(progress = np)
                    }

                    // 1. 360° Free Movement over Heightmap
                    val joyMag = hypot(joyX, joyY)
                    val bound = OpenWorldCatalog.WORLD_HALF_EXTENT - 1.5f
                    if (joyMag > 0.08f) {
                        val fwdX = -sin(camYaw)
                        val fwdZ = cos(camYaw)
                        val rightX = cos(camYaw)
                        val rightZ = sin(camYaw)
                        val moveX = rightX * joyX + fwdX * (-joyY)
                        val moveZ = rightZ * joyX + fwdZ * (-joyY)
                        val mLen = hypot(moveX, moveZ).coerceAtLeast(0.001f)
                        val nx = moveX / mLen
                        val nz = moveZ / mLen
                        val speed = if (dashTimer > 0f) 9.5f else 4.4f
                        val step = speed * minOf(1f, joyMag) * dt
                        heroX = (heroX + nx * step).coerceIn(-bound, bound)
                        heroZ = (heroZ + nz * step).coerceIn(-bound, bound)
                        heroYaw = atan2(nx, nz)
                        heroWalkPhase += dt * (if (dashTimer > 0f) 19f else 12f)
                    } else if (dashTimer > 0f) {
                        val step = 9.5f * dt
                        heroX = (heroX + sin(heroYaw) * step).coerceIn(-bound, bound)
                        heroZ = (heroZ + cos(heroYaw) * step).coerceIn(-bound, bound)
                        heroWalkPhase += dt * 16f
                    }

                    // Heightmap Y sample
                    val targetGroundY = OpenWorldCatalog.terrainHeightAt(heroX, heroZ)
                    heroY += (targetGroundY - heroY) * (dt * 14f).coerceAtMost(1f)

                    // Smooth 3rd-person camera tracking
                    val lerp = (dt * 8f).coerceAtMost(1f)
                    camTargetX += (heroX - camTargetX) * lerp
                    camTargetY += ((heroY + 0.9f) - camTargetY) * lerp
                    camTargetZ += (heroZ - camTargetZ) * lerp

                    // 2. Monster AI, Camp Leashing, Chest Unlock & Invasion Waves (stepped cleanly every ~50ms for light CPU)
                    if (aiAccum >= 0.05f) {
                        val adt = aiAccum
                        aiAccum = 0f
                        localFrameTick++

                        val inSafeVillage = hypot(heroX, heroZ) <= OpenWorldCatalog.VILLAGE_RADIUS
                        val nextMobs = monsters.map { mob ->
                            if (mob.state == WorldEntityState.DEAD) return@map mob

                            val distHero = hypot(heroX - mob.x, heroZ - mob.z)
                            val distHome = hypot(mob.x - mob.homeX, mob.z - mob.homeZ)

                            if (mob.isInvasionMob) {
                                // Invasion mob targets hero if close, otherwise marches toward Crystal (0, 0)
                                val targetX = if (distHero <= 5.5f) heroX else 0f
                                val targetZ = if (distHero <= 5.5f) heroZ else 0f
                                val dx = targetX - mob.x
                                val dz = targetZ - mob.z
                                val dTarget = hypot(dx, dz).coerceAtLeast(0.001f)
                                val newYaw = atan2(dx, dz)

                                if (mob.aoeCharge > 0f) {
                                    val nc = mob.aoeCharge + adt / 1.35f
                                    if (nc >= 1f) {
                                        if (distHero <= 1.7f && dashTimer <= 0f) {
                                            if (parryTimer > 0f) {
                                                onWorldLog("Parry contra invasor ${mob.name}!", false)
                                            } else {
                                                onPlayerDamaged(mob.atk, mob.name)
                                            }
                                        } else if (hypot(mob.x, mob.z) <= 2.6f) {
                                            val newCryHp = (invasion.crystalHp - mob.atk).coerceAtLeast(0)
                                            invasion = invasion.copy(crystalHp = newCryHp)
                                        }
                                        mob.copy(aoeCharge = 0f, attackCooldown = 2.0f, yaw = newYaw, state = WorldEntityState.CHASING)
                                    } else {
                                        mob.copy(aoeCharge = nc, yaw = newYaw, state = WorldEntityState.ATTACKING)
                                    }
                                } else if (dTarget <= 1.85f) {
                                    val cd = (mob.attackCooldown - adt).coerceAtLeast(0f)
                                    if (cd <= 0f) {
                                        mob.copy(
                                            aoeCharge = 0.06f,
                                            aoeTargetX = if (distHero <= 5.5f) heroX else 0f,
                                            aoeTargetZ = if (distHero <= 5.5f) heroZ else 0f,
                                            yaw = newYaw,
                                            state = WorldEntityState.ATTACKING
                                        )
                                    } else {
                                        mob.copy(attackCooldown = cd, yaw = newYaw, state = WorldEntityState.ATTACKING)
                                    }
                                } else {
                                    val spd = if (mob.isElite) 2.25f else 2.55f
                                    mob.copy(
                                        x = mob.x + (dx / dTarget) * spd * adt,
                                        z = mob.z + (dz / dTarget) * spd * adt,
                                        yaw = newYaw,
                                        walkPhase = mob.walkPhase + adt * 9f,
                                        attackCooldown = (mob.attackCooldown - adt).coerceAtLeast(0f),
                                        state = WorldEntityState.CHASING
                                    )
                                }
                            } else {
                                // Camp Monster AI: Patrol -> Detect (11.5m) -> Chase/Attack -> Leash Return (19m)
                                when (mob.state) {
                                    WorldEntityState.PATROLLING -> {
                                        if (distHero <= 11.5f && !inSafeVillage) {
                                            mob.copy(state = WorldEntityState.CHASING)
                                        } else {
                                            val na = mob.patrolAngle + adt * 0.48f
                                            val r = if (mob.isElite) 1.2f else 2.8f
                                            val tx = mob.homeX + cos(na) * r
                                            val tz = mob.homeZ + sin(na) * r
                                            val dx = tx - mob.x
                                            val dz = tz - mob.z
                                            val d = hypot(dx, dz).coerceAtLeast(0.01f)
                                            mob.copy(
                                                x = mob.x + (dx / d) * 1.1f * adt,
                                                z = mob.z + (dz / d) * 1.1f * adt,
                                                yaw = atan2(dx, dz),
                                                patrolAngle = na,
                                                walkPhase = mob.walkPhase + adt * 5f
                                            )
                                        }
                                    }
                                    WorldEntityState.RETURNING -> {
                                        val dx = mob.homeX - mob.x
                                        val dz = mob.homeZ - mob.z
                                        val d = hypot(dx, dz).coerceAtLeast(0.001f)
                                        if (d < 2.2f) {
                                            mob.copy(hp = mob.maxHp, aoeCharge = 0f, state = WorldEntityState.PATROLLING)
                                        } else {
                                            mob.copy(
                                                x = mob.x + (dx / d) * 3.4f * adt,
                                                z = mob.z + (dz / d) * 3.4f * adt,
                                                yaw = atan2(dx, dz),
                                                hp = mob.maxHp,
                                                aoeCharge = 0f,
                                                walkPhase = mob.walkPhase + adt * 10f
                                            )
                                        }
                                    }
                                    WorldEntityState.CHASING, WorldEntityState.ATTACKING -> {
                                        if (distHome > 18.5f || inSafeVillage) {
                                            mob.copy(state = WorldEntityState.RETURNING, aoeCharge = 0f)
                                        } else {
                                            val dx = heroX - mob.x
                                            val dz = heroZ - mob.z
                                            val d = hypot(dx, dz).coerceAtLeast(0.001f)
                                            val newYaw = atan2(dx, dz)

                                            if (mob.aoeCharge > 0f) {
                                                val nc = mob.aoeCharge + adt / 1.35f
                                                if (nc >= 1f) {
                                                    val hitRadius = if (mob.isElite) 1.65f else 1.2f
                                                    val inZone = hypot(heroX - mob.aoeTargetX, heroZ - mob.aoeTargetZ) <= hitRadius
                                                    if (inZone && dashTimer <= 0f) {
                                                        if (parryTimer > 0f) {
                                                            val counter = (latestStats.atk * 1.2f).roundToInt()
                                                            val remHp = (mob.hp - counter).coerceAtLeast(0)
                                                            onWorldLog("Parry perfeito em ${mob.name}! Contra-ataque de $counter!", true)
                                                            if (remHp == 0) {
                                                                onMonsterDefeated(mob.name, if (mob.isElite) 35 else 16, if (mob.isElite) 14 else 6)
                                                                return@map mob.copy(hp = 0, aoeCharge = 0f, state = WorldEntityState.DEAD)
                                                            }
                                                            return@map mob.copy(hp = remHp, aoeCharge = 0f, attackCooldown = 2.0f, state = WorldEntityState.CHASING)
                                                        } else {
                                                            onPlayerDamaged(mob.atk, mob.name)
                                                        }
                                                    }
                                                    mob.copy(aoeCharge = 0f, attackCooldown = 2.1f, yaw = newYaw, state = WorldEntityState.CHASING)
                                                } else {
                                                    mob.copy(aoeCharge = nc, yaw = newYaw, state = WorldEntityState.ATTACKING)
                                                }
                                            } else if (d <= 1.85f) {
                                                val cd = (mob.attackCooldown - adt).coerceAtLeast(0f)
                                                if (cd <= 0f) {
                                                    mob.copy(
                                                        aoeCharge = 0.06f,
                                                        aoeTargetX = heroX,
                                                        aoeTargetZ = heroZ,
                                                        yaw = newYaw,
                                                        state = WorldEntityState.ATTACKING
                                                    )
                                                } else {
                                                    mob.copy(attackCooldown = cd, yaw = newYaw, state = WorldEntityState.ATTACKING)
                                                }
                                            } else {
                                                val spd = if (mob.isElite) 2.5f else 2.85f
                                                mob.copy(
                                                    x = mob.x + (dx / d) * spd * adt,
                                                    z = mob.z + (dz / d) * spd * adt,
                                                    yaw = newYaw,
                                                    walkPhase = mob.walkPhase + adt * 9f,
                                                    attackCooldown = (mob.attackCooldown - adt).coerceAtLeast(0f),
                                                    state = WorldEntityState.CHASING
                                                )
                                            }
                                        }
                                    }
                                    WorldEntityState.DEAD -> mob
                                }
                            }
                        }

                        // Check Camp Cleared -> Unlock Chest & Handle Respawns
                        var updatedMobs = nextMobs
                        val updatedCamps = camps.map { camp ->
                            val campMobs = updatedMobs.filter { it.campId == camp.blueprint.id }
                            val anyAlive = campMobs.any { it.state != WorldEntityState.DEAD }
                            if (!anyAlive && !camp.chestUnlocked && !camp.chestClaimed) {
                                onWorldLog("${camp.blueprint.name} limpo! Baú Selado desbloqueado!", true)
                                camp.copy(chestUnlocked = true, respawnTimer = 35f)
                            } else if (!anyAlive && (camp.chestClaimed || camp.chestUnlocked)) {
                                val nextTimer = camp.respawnTimer - adt
                                if (nextTimer <= 0f) {
                                    // Respawn camp monsters
                                    val fresh = OpenWorldCatalog.spawnCampMonsters(camp.blueprint, latestLevel)
                                    updatedMobs = updatedMobs.filterNot { it.campId == camp.blueprint.id } + fresh
                                    WorldCampRuntime(blueprint = camp.blueprint)
                                } else {
                                    camp.copy(respawnTimer = nextTimer)
                                }
                            } else {
                                camp
                            }
                        }
                        camps = updatedCamps
                        monsters = updatedMobs

                        // Check Village Invasion Waves & Victory/Defeat
                        if (invasion.active) {
                            if (invasion.crystalHp <= 0) {
                                monsters = monsters.filterNot { it.isInvasionMob }
                                invasion = VillageInvasionState(active = false, nextInvasionTimer = 55f, statusBanner = null)
                                onInvasionResolved(false)
                            } else {
                                val invAlive = monsters.any { it.isInvasionMob && it.state != WorldEntityState.DEAD }
                                if (!invAlive) {
                                    if (invasion.wave < invasion.totalWaves) {
                                        val nextWave = invasion.wave + 1
                                        monsters = monsters.filterNot { it.isInvasionMob } + OpenWorldCatalog.spawnInvasionWave(nextWave, latestLevel)
                                        invasion = invasion.copy(
                                            wave = nextWave,
                                            statusBanner = "ALERTA! Onda $nextWave/${invasion.totalWaves} marchando sobre a Vila!"
                                        )
                                        onWorldLog("Onda $nextWave da invasão se aproxima da Vila Central!", true)
                                    } else {
                                        monsters = monsters.filterNot { it.isInvasionMob }
                                        invasion = VillageInvasionState(active = false, nextInvasionTimer = 60f, statusBanner = null)
                                        onInvasionResolved(true)
                                    }
                                }
                            }
                        } else {
                            val nextInv = invasion.nextInvasionTimer - adt
                            if (nextInv <= 0f) {
                                triggerInvasionStart()
                            } else {
                                invasion = invasion.copy(nextInvasionTimer = nextInv)
                            }
                        }
                    }
                }
                prevNanos = now
            }
        }
    }

    val currentZone = OpenWorldCatalog.zoneAt(heroX, heroZ)
    val isRanged = stats.heroClass == HeroClass.MAGO || stats.heroClass == HeroClass.ARQUEIRO
    val attackRange = if (isRanged) 8.5f else 3.4f

    // Nearby Interactive World Targets
    val nearbyChest = camps.firstOrNull { c ->
        c.chestUnlocked && !c.chestClaimed &&
            hypot(heroX - c.blueprint.centerX, heroZ - (c.blueprint.centerZ + 1.45f)) <= 2.9f
    }
    val nearVillageCampfire = hypot(heroX - OpenWorldCatalog.VILLAGE_CAMPFIRE_X, heroZ - OpenWorldCatalog.VILLAGE_CAMPFIRE_Z) <= 3.0f
    val nearVillageForge = hypot(heroX - OpenWorldCatalog.VILLAGE_FORGE_X, heroZ - OpenWorldCatalog.VILLAGE_FORGE_Z) <= 3.0f
    val nearestEnemy = monsters.filter { it.state != WorldEntityState.DEAD }
        .minByOrNull { hypot(it.x - heroX, it.z - heroZ) }
    val nearestEnemyDist = nearestEnemy?.let { hypot(it.x - heroX, it.z - heroZ) } ?: 99f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (fillAvailableHeight) Modifier.fillMaxSize()
                else Modifier.height(if (isExpanded) 520.dp else 390.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(currentZone.skyColors))
            .border(1.5.dp, if (invasion.active) CrimsonBlood else CardBorder, RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    camYaw += dragAmount.x * 0.007f
                    camPitch = (camPitch - dragAmount.y * 0.005f).coerceIn(-0.68f, -0.10f)
                }
            }
            .testTag("open_world_3d_viewport")
    ) {
        // 3D Open-World Perspective Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val camDist = if (isExpanded) 6.4f else 6.8f
            val camTarget = Vec3(camTargetX, camTargetY, camTargetZ)

            // Ambient Zone Particles
            for (i in 0 until 14) {
                val px = ((i * 83f + elapsedSec * (11f + i % 4 * 5f)) % w)
                val py = ((i * 51f - elapsedSec * 13f).mod(h * 0.68f))
                drawCircle(
                    color = currentZone.accentColor.copy(alpha = 0.28f),
                    radius = 2f + (i % 3),
                    center = Offset(px, py)
                )
            }

            val faces = ArrayList<Face3D>(340)
            OpenWorld3DMeshes.buildHeightmapTerrainChunk(heroX, heroZ, elapsedSec, faces)
            OpenWorld3DMeshes.buildNearbyProps(staticProps, heroX, heroZ, invasion, elapsedSec, faces)
            OpenWorld3DMeshes.buildCampChests(camps, heroX, heroZ, elapsedSec, faces)
            OpenWorld3DMeshes.buildVillageNpcs(OpenWorldCatalog.villageNpcs, heroX, heroZ, elapsedSec, faces)
            OpenWorld3DMeshes.buildWorldMonsters(monsters, heroX, heroZ, elapsedSec, faces)

            val atkProg = if (heroAttackTimer > 0f) 1f - (heroAttackTimer / 0.35f) else 0f
            Arena3DMeshes.buildHero3D(
                heroClass = stats.heroClass,
                skin = stats.activeSkin,
                pos = Vec3(heroX, heroY, heroZ),
                facingYaw = heroYaw,
                walkPhase = heroWalkPhase,
                isMoving = hypot(joyX, joyY) > 0.08f,
                isDashing = dashTimer > 0f,
                attackProgress = atkProg,
                time = elapsedSec,
                shieldActive = combat.playerShield > 0 || parryTimer > 0f,
                out = faces
            )

            projectile?.let { p ->
                val cur = Vec3(
                    p.start.x + (p.end.x - p.start.x) * p.progress,
                    p.start.y + (p.end.y - p.start.y) * p.progress,
                    p.start.z + (p.end.z - p.start.z) * p.progress
                )
                Arena3DMath.addOctahedron(faces, cur, 0.22f, 0.22f, p.color, rotY = elapsedSec * 12f)
            }

            if (parryTimer > 0f) {
                Arena3DMath.addOrbitRing(faces, Vec3(heroX, heroY + 0.95f, heroZ), 0.95f, 0.06f, 16, GildedAmber, rotX = 1.35f, rotY = elapsedSec * 5f)
            }
            if (aoeBlastTimer > 0f) {
                val p = 1f - (aoeBlastTimer / 0.6f)
                Arena3DMath.addOrbitRing(faces, Vec3(heroX, heroY + 0.6f, heroZ), 1.0f + p * 3.8f, 0.09f, 20, aoeBlastColor, rotX = 0f, rotY = elapsedSec * 4f)
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
        }

        // Top HUD: Zone Info + Invasion Banner + Minimap Radar + Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Zone Badge
                Surface(
                    color = VoidObsidian.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.border(1.dp, currentZone.accentColor.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = currentZone.title.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = currentZone.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentZone.subtitle} · Alt ${"%.1f".format(heroY)}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // Right Cluster: Skin, Invasion Trigger, Camera Reset, Expand, Minimap
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Surface(
                                color = VoidObsidian.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .border(1.dp, stats.activeSkin.accentGlow.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                    .clickable { showSkinDialog = true }
                                    .testTag("open_world_skin_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.Checkroom, contentDescription = "Skin 3D", tint = stats.activeSkin.accentGlow, modifier = Modifier.size(14.dp))
                                    Text("Skin", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }

                            if (!invasion.active) {
                                Surface(
                                    color = CrimsonBlood.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .border(1.dp, CrimsonBlood.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                        .clickable { triggerInvasionStart() }
                                        .testTag("trigger_invasion_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Filled.Warning, contentDescription = "Iniciar Invasão", tint = CrimsonBlood, modifier = Modifier.size(14.dp))
                                        Text("Invasão", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    camYaw = 0f
                                    camPitch = -0.36f
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(VoidObsidian.copy(alpha = 0.8f))
                            ) {
                                Icon(Icons.Filled.Cameraswitch, contentDescription = "Resetar câmera", tint = GildedAmber, modifier = Modifier.size(16.dp))
                            }

                            if (!fillAvailableHeight) {
                                IconButton(
                                    onClick = { isExpanded = !isExpanded },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(VoidObsidian.copy(alpha = 0.8f))
                                ) {
                                    Icon(
                                        if (isExpanded) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                        contentDescription = "Expandir Mapa 3D",
                                        tint = AstralViolet,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Circular Open-World Minimap Radar
                    OpenWorldMinimap(
                        heroX = heroX,
                        heroZ = heroZ,
                        heroYaw = heroYaw,
                        camps = camps,
                        monsters = monsters,
                        invasionActive = invasion.active,
                        tick = localFrameTick
                    )
                }
            }

            // Village Invasion Alert Banner & Crystal HP
            if (invasion.active) {
                Surface(
                    color = CrimsonBlood.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CrimsonBlood, RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = invasion.statusBanner ?: "VILA SOB ATAQUE!",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cristal ${invasion.crystalHp}/${invasion.crystalMaxHp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArcaneCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { (invasion.crystalHp.toFloat() / invasion.crystalMaxHp.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ArcaneCyan,
                            trackColor = VoidObsidian
                        )
                    }
                }
            }
        }

        // Contextual Interact Prompt (Chest / Campfire / Forge)
        if (nearbyChest != null || nearVillageCampfire || nearVillageForge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
            ) {
                when {
                    nearbyChest != null -> {
                        Button(
                            onClick = {
                                val target = nearbyChest
                                camps = camps.map {
                                    if (it.blueprint.id == target.blueprint.id) it.copy(chestClaimed = true, respawnTimer = 30f)
                                    else it
                                }
                                onClaimCampChest(target.blueprint.name, target.blueprint.xpReward, target.blueprint.shardReward)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GildedAmber, contentColor = VoidObsidian),
                            modifier = Modifier.testTag("open_world_claim_chest_button")
                        ) {
                            Icon(Icons.Filled.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Abrir Baú (+${nearbyChest.blueprint.xpReward} XP)", fontWeight = FontWeight.Bold)
                        }
                    }
                    nearVillageCampfire -> {
                        Button(
                            onClick = {
                                onRestAtVillage()
                                // Resting at campfire also respawns cleared camps for farming!
                                val anyCleared = camps.any { it.chestClaimed }
                                if (anyCleared) {
                                    camps = OpenWorldCatalog.spawnInitialCamps()
                                    monsters = OpenWorldCatalog.spawnAllWorldMonsters(save.level) + monsters.filter { it.isInvasionMob }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoulEmerald, contentColor = VoidObsidian),
                            modifier = Modifier.testTag("open_world_rest_campfire_button")
                        ) {
                            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Descansar & Curar", fontWeight = FontWeight.Bold)
                        }
                    }
                    nearVillageForge -> {
                        Button(
                            onClick = onUpgradeAtForge,
                            colors = ButtonDefaults.buttonColors(containerColor = ArcaneCyan, contentColor = VoidObsidian),
                            modifier = Modifier.testTag("open_world_forge_button")
                        ) {
                            Icon(Icons.Filled.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Forja (30 Frag · +XP)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Bottom-Left: 360° Virtual Analog Joystick
        val joyBaseRadiusPx = 110f
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .size(112.dp)
                .clip(CircleShape)
                .background(VoidObsidian.copy(alpha = 0.54f))
                .border(1.5.dp, currentZone.accentColor.copy(alpha = 0.55f), CircleShape)
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
                .testTag("open_world_joystick")
        ) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset((joyX * 30.dp.toPx()).roundToInt(), (joyY * 30.dp.toPx()).roundToInt())
                    }
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(currentZone.accentColor.copy(alpha = 0.88f))
                    .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            )
        }

        // Bottom-Right: Action Cluster (Parry, Esquiva, Atacar)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (parryTimer > 0f) GildedAmber else DeepSlate.copy(alpha = 0.88f))
                        .border(1.5.dp, GildedAmber, CircleShape)
                        .clickable { parryTimer = 0.65f }
                        .testTag("open_world_parry_button")
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = "Parry", tint = if (parryTimer > 0f) VoidObsidian else GildedAmber, modifier = Modifier.size(20.dp))
                }
                Text("Parry", style = MaterialTheme.typography.labelSmall, color = GildedAmber)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val canDash = dashCooldown <= 0f
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (dashTimer > 0f) ArcaneCyan else DeepSlate.copy(alpha = 0.88f))
                        .border(1.5.dp, if (canDash) ArcaneCyan else TextSecondary, CircleShape)
                        .clickable(enabled = canDash) {
                            dashTimer = 0.42f
                            dashCooldown = 1.05f
                        }
                        .testTag("open_world_dash_button")
                ) {
                    Icon(Icons.Filled.DirectionsRun, contentDescription = "Esquiva", tint = if (dashTimer > 0f) VoidObsidian else ArcaneCyan, modifier = Modifier.size(22.dp))
                }
                Text("Esquiva", style = MaterialTheme.typography.labelSmall, color = ArcaneCyan)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val hasTarget = nearestEnemy != null && nearestEnemyDist <= attackRange
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (hasTarget) CrimsonBlood else ElevatedCard.copy(alpha = 0.88f))
                        .border(2.dp, GildedAmber, CircleShape)
                        .clickable {
                            heroAttackTimer = 0.35f
                            if (nearestEnemy != null && nearestEnemyDist <= attackRange) {
                                heroYaw = atan2(nearestEnemy.x - heroX, nearestEnemy.z - heroZ)
                                if (isRanged) {
                                    val sy = OpenWorldCatalog.terrainHeightAt(nearestEnemy.x, nearestEnemy.z) + 1.0f
                                    projectile = WorldProjectile3D(
                                        start = Vec3(heroX, heroY + 1.05f, heroZ),
                                        end = Vec3(nearestEnemy.x, sy, nearestEnemy.z),
                                        progress = 0f,
                                        color = stats.activeSkin.accentGlow
                                    )
                                }
                                val isCrit = Random.nextInt(100) < stats.critPercent
                                val dmg = ((stats.atk * (if (isCrit) 1.55f else 1.0f)).roundToInt()).coerceAtLeast(15)
                                val hitCenterX = nearestEnemy.x
                                val hitCenterZ = nearestEnemy.z
                                var hits = 0
                                monsters = monsters.map { m ->
                                    if (m.state != WorldEntityState.DEAD &&
                                        (m.id == nearestEnemy.id || hypot(m.x - hitCenterX, m.z - hitCenterZ) <= 2.1f)
                                    ) {
                                        hits++
                                        val nhp = (m.hp - dmg).coerceAtLeast(0)
                                        if (nhp == 0) {
                                            onMonsterDefeated(m.name, if (m.isElite) 35 else 16, if (m.isElite) 14 else 6)
                                            m.copy(hp = 0, aoeCharge = 0f, state = WorldEntityState.DEAD)
                                        } else {
                                            m.copy(hp = nhp, state = WorldEntityState.CHASING)
                                        }
                                    } else {
                                        m
                                    }
                                }
                                if (hits > 0) {
                                    onWorldLog(
                                        "${if (isCrit) "CRÍTICO! " else ""}Golpe atingiu $hits inimigo(s) por $dmg de dano.",
                                        isCrit
                                    )
                                }
                            }
                        }
                        .testTag("open_world_attack_button")
                ) {
                    Icon(Icons.Filled.SportsKabaddi, contentDescription = "Atacar", tint = Color.White, modifier = Modifier.size(28.dp))
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

@Composable
private fun OpenWorldMinimap(
    heroX: Float,
    heroZ: Float,
    heroYaw: Float,
    camps: List<WorldCampRuntime>,
    monsters: List<WorldMonsterEntity>,
    invasionActive: Boolean,
    tick: Int
) {
    @Suppress("UNUSED_VARIABLE")
    val refreshTrigger = tick
    Canvas(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(VoidObsidian.copy(alpha = 0.84f))
            .border(1.5.dp, if (invasionActive) CrimsonBlood else GildedAmber.copy(alpha = 0.65f), CircleShape)
    ) {
        val r = size.minDimension / 2f
        val scale = r / OpenWorldCatalog.WORLD_HALF_EXTENT
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Vila Central circle
        drawCircle(
            color = ArcaneCyan.copy(alpha = 0.25f),
            radius = OpenWorldCatalog.VILLAGE_RADIUS * scale,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )

        // Camps
        for (c in camps) {
            val px = cx + c.blueprint.centerX * scale
            val py = cy - c.blueprint.centerZ * scale
            val col = when {
                c.chestUnlocked && !c.chestClaimed -> GildedAmber
                c.chestClaimed -> TextSecondary
                else -> ParadoxMagenta
            }
            drawCircle(color = col, radius = 4.2f, center = Offset(px, py))
        }

        // Invasion Mobs
        for (m in monsters) {
            if (m.isInvasionMob && m.state != WorldEntityState.DEAD) {
                val mx = cx + m.x * scale
                val my = cy - m.z * scale
                drawCircle(color = CrimsonBlood, radius = 3.2f, center = Offset(mx, my))
            }
        }

        // Player Dot & Facing Direction
        val hx = cx + heroX * scale
        val hy = cy - heroZ * scale
        drawCircle(color = SoulEmerald, radius = 4.5f, center = Offset(hx, hy))
        drawLine(
            color = Color.White,
            start = Offset(hx, hy),
            end = Offset(hx + sin(heroYaw) * 8f, hy - cos(heroYaw) * 8f),
            strokeWidth = 2.5f
        )
    }
}
