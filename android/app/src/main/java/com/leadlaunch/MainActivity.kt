package com.leadlaunch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.leadlaunch.data.local.SessionStore
import com.leadlaunch.data.remote.RetrofitClient
import com.leadlaunch.data.repository.*
import com.leadlaunch.navigation.LeadLaunchNavGraph
import com.leadlaunch.navigation.Routes
import com.leadlaunch.ui.theme.LeadLaunchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionStore = SessionStore(applicationContext)

        setContent {
            LeadLaunchTheme {
                val navController = rememberNavController()
                val apiService = remember {
                    RetrofitClient.create(
                        sessionStore = sessionStore,
                        onUnauthorized = {
                            runOnUiThread {
                                navController.navigate(Routes.S02_LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                val authRepo = remember { AuthRepository(apiService, sessionStore) }
                val bizRepo = remember { BusinessRepository(apiService, sessionStore) }
                val prodRepo = remember { ProductRepository(apiService, sessionStore) }
                val rulesRepo = remember { RulesRepository(apiService, sessionStore) }
                val radarRepo = remember { RadarRepository(apiService, sessionStore) }

                LeadLaunchNavGraph(
                    navController = navController,
                    sessionStore = sessionStore,
                    authRepository = authRepo,
                    businessRepository = bizRepo,
                    productRepository = prodRepo,
                    rulesRepository = rulesRepo,
                    radarRepository = radarRepo
                )
            }
        }
    }
}
