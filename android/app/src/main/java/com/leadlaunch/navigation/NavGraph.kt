package com.leadlaunch.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.leadlaunch.data.local.SessionStore
import com.leadlaunch.data.repository.*
import com.leadlaunch.ui.components.BottomNavBar
import com.leadlaunch.ui.screens.phase2.CampaignsPlaceholderScreen
import com.leadlaunch.ui.screens.phase2.MoreScreen
import com.leadlaunch.ui.screens.phase2.QuotesPlaceholderScreen
import com.leadlaunch.ui.screens.s01_splash.SplashScreen
import com.leadlaunch.ui.screens.s02_login.LoginScreen
import com.leadlaunch.ui.screens.s03_register.RegisterScreen
import com.leadlaunch.ui.screens.s04_business.RegisterBusinessScreen
import com.leadlaunch.ui.screens.s05_home.HomeScreen
import com.leadlaunch.ui.screens.s06_catalog.CatalogScreen
import com.leadlaunch.ui.screens.s07_product.CreateProductScreen
import com.leadlaunch.ui.screens.s08_rules.RulesScreen
import com.leadlaunch.ui.screens.s09_radar.RadarScreen
import com.leadlaunch.ui.screens.s10_findings.FindingsScreen

@Composable
fun LeadLaunchNavGraph(
    navController: NavHostController,
    sessionStore: SessionStore,
    authRepository: AuthRepository,
    businessRepository: BusinessRepository,
    productRepository: ProductRepository,
    rulesRepository: RulesRepository,
    radarRepository: RadarRepository
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { target ->
                        navController.navigate(target) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.S01_SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.S01_SPLASH) {
                SplashScreen(
                    sessionStore = sessionStore,
                    onGoHome = {
                        navController.navigate(Routes.S05_HOME) { popUpTo(Routes.S01_SPLASH) { inclusive = true } }
                    },
                    onGoBusiness = {
                        navController.navigate(Routes.S04_BUSINESS) { popUpTo(Routes.S01_SPLASH) { inclusive = true } }
                    },
                    onGoLogin = {
                        navController.navigate(Routes.S02_LOGIN) { popUpTo(Routes.S01_SPLASH) { inclusive = true } }
                    }
                )
            }

            composable(Routes.S02_LOGIN) {
                LoginScreen(
                    authRepository = authRepository,
                    onLoginSuccess = { hasBusiness ->
                        val dest = if (hasBusiness) Routes.S05_HOME else Routes.S04_BUSINESS
                        navController.navigate(dest) { popUpTo(Routes.S02_LOGIN) { inclusive = true } }
                    },
                    onGoRegister = { navController.navigate(Routes.S03_REGISTER) }
                )
            }

            composable(Routes.S03_REGISTER) {
                RegisterScreen(
                    authRepository = authRepository,
                    onRegisterSuccess = {
                        navController.navigate(Routes.S04_BUSINESS) { popUpTo(Routes.S02_LOGIN) { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.S04_BUSINESS) {
                RegisterBusinessScreen(
                    businessRepository = businessRepository,
                    onBusinessCreated = {
                        navController.navigate(Routes.S05_HOME) { popUpTo(Routes.S04_BUSINESS) { inclusive = true } }
                    }
                )
            }

            composable(Routes.S05_HOME) {
                HomeScreen(
                    sessionStore = sessionStore,
                    businessRepository = businessRepository,
                    onOpenRadar = { navController.navigate(Routes.S09_RADAR) },
                    onOpenRules = { navController.navigate(Routes.S08_RULES) },
                    onOpenCatalog = { navController.navigate(Routes.S06_CATALOG) }
                )
            }

            composable(Routes.S06_CATALOG) {
                CatalogScreen(
                    productRepository = productRepository,
                    onAddProduct = { navController.navigate(Routes.S07_PRODUCT) }
                )
            }

            composable(Routes.S07_PRODUCT) {
                CreateProductScreen(
                    productRepository = productRepository,
                    onProductCreated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.S08_RULES) {
                RulesScreen(
                    rulesRepository = rulesRepository,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.S09_RADAR) {
                RadarScreen(
                    radarRepository = radarRepository,
                    onScanComplete = { navController.navigate(Routes.S10_FINDINGS) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.S10_FINDINGS) {
                FindingsScreen(
                    radarRepository = radarRepository,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.S15_QUOTES) { QuotesPlaceholderScreen() }
            composable(Routes.S11_CAMPAIGNS) { CampaignsPlaceholderScreen() }
            composable(Routes.S17_MORE) {
                MoreScreen(
                    authRepository = authRepository,
                    onOpenRadar = { navController.navigate(Routes.S09_RADAR) },
                    onOpenRules = { navController.navigate(Routes.S08_RULES) },
                    onLogout = {
                        navController.navigate(Routes.S02_LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
        }
    }
}
