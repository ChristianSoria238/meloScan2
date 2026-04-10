package com.melon.meloscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.melon.meloscan.ui.screens.HomeScreen
import com.melon.meloscan.ui.screens.SplashScreen
import com.melon.meloscan.ui.theme.MeloScanTheme
import androidx.compose.material3.Text
import androidx.navigation.NavController
import com.melon.meloscan.ui.screens.QualityGuideScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeloScanTheme {
                AppNavigation()
            }
        }
    }
}

// Inside D:/meloScan/app/src/main/java/com/melon/meloscan/MainActivity.kt

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home" // or "splash" if you have one
    ) {
        composable("home") {
            // FIX: Pass the navController instance here
            HomeScreen(navController = navController)
        }

        // Other routes...
        composable("leaf_scan") { /* LeafScanScreen(navController) */ }
        composable("fruit_scan") { /* FruitScanScreen(navController) */ }
        composable("history") { /* HistoryScreen(navController) */ }
        composable("guide_route") {
            QualityGuideScreen(navController = navController)
        }
    }
}

