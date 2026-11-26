package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SportsKabaddi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ClassesScreen
import com.example.ui.screens.CombatArenaScreen
import com.example.ui.screens.GrimoireScreen
import com.example.ui.screens.NyxStealthTrialDialog
import com.example.ui.screens.StarAlignmentDialog
import com.example.ui.screens.WorldLoreScreen
import com.example.ui.theme.AeldrinTheme
import com.example.ui.theme.DeepSlate
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidObsidian
import com.example.ui.viewmodel.AeldrinViewModel

const val ROUTE_WORLD = "world"
const val ROUTE_COMBAT = "combat"
const val ROUTE_GRIMOIRE = "grimoire"
const val ROUTE_CLASSES = "classes"

private data class NavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val AELDRIN_TABS = listOf(
    NavTab(ROUTE_WORLD, "Mundo", Icons.Filled.Explore, Icons.Outlined.Explore),
    NavTab(ROUTE_COMBAT, "Combate", Icons.Filled.SportsKabaddi, Icons.Outlined.SportsKabaddi),
    NavTab(ROUTE_GRIMOIRE, "Grimório", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    NavTab(ROUTE_CLASSES, "Classes", Icons.Filled.Security, Icons.Outlined.Security)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AeldrinTheme {
                AeldrinApp()
            }
        }
    }
}

@Composable
fun AeldrinApp(viewModel: AeldrinViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidObsidian)
    ) {
        val useRail = maxWidth >= 700.dp

        Scaffold(
            containerColor = VoidObsidian,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = DeepSlate) {
                        AELDRIN_TABS.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VoidObsidian,
                                    selectedTextColor = GildedAmber,
                                    indicatorColor = GildedAmber,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(containerColor = DeepSlate) {
                        AELDRIN_TABS.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = VoidObsidian,
                                    selectedTextColor = GildedAmber,
                                    indicatorColor = GildedAmber,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("rail_${tab.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_WORLD,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(ROUTE_WORLD) {
                            WorldLoreScreen(
                                uiState = uiState,
                                onLaunchEncounter = { encounter ->
                                    viewModel.startEncounter(encounter)
                                    navigateToTab(ROUTE_COMBAT)
                                },
                                onToggleLightArmor = viewModel::toggleLightArmorChallenge,
                                onToggleLunarEclipse = viewModel::toggleLunarEclipseMode,
                                onResetProgress = viewModel::resetProgress
                            )
                        }
                        composable(ROUTE_COMBAT) {
                            CombatArenaScreen(
                                uiState = uiState,
                                onSelectEncounter = viewModel::startEncounter,
                                onBasicAttack = viewModel::performBasicAttack,
                                onParryStance = viewModel::performParryStance,
                                onManaOverload = viewModel::triggerManaOverload,
                                onPurifySoul = viewModel::attemptPurifySoul,
                                onUseSkill = viewModel::useSkill,
                                onRetryFight = { viewModel.startEncounter(uiState.combat.encounter) }
                            )
                        }
                        composable(ROUTE_GRIMOIRE) {
                            GrimoireScreen(
                                uiState = uiState,
                                onEquipSkillToSlot = viewModel::equipSkillToSlot
                            )
                        }
                        composable(ROUTE_CLASSES) {
                            ClassesScreen(
                                uiState = uiState,
                                onSelectHeroClass = viewModel::selectHeroClass,
                                onEquipSubclass = viewModel::equipSubclass,
                                onStartSecretTrialFight = { encounter ->
                                    viewModel.startEncounter(encounter)
                                    navigateToTab(ROUTE_COMBAT)
                                },
                                onOpenStarPuzzle = viewModel::openStarPuzzle,
                                onOpenStealthTrial = viewModel::openStealthTrial
                            )
                        }
                    }

                    // Top Notification Banner
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.bannerNotification != null,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        uiState.bannerNotification?.let { message ->
                            Surface(
                                color = ElevatedCard,
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 6.dp,
                                shadowElevation = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GildedAmber,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = viewModel::dismissBanner) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Fechar aviso",
                                            tint = TextSecondary
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

    StarAlignmentDialog(
        state = uiState.starPuzzle,
        onRotateLuneta = viewModel::rotateLuneta,
        onStartGuardianFight = {
            viewModel.launchAstralGuardianAfterPuzzle()
            navigateToTab(ROUTE_COMBAT)
        },
        onDismiss = viewModel::closeStarPuzzle
    )

    NyxStealthTrialDialog(
        state = uiState.stealthTrial,
        onChooseAction = viewModel::advanceStealthStep,
        onRetry = viewModel::openStealthTrial,
        onDismiss = viewModel::closeStealthTrial
    )
}
