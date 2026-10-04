package com.melon.meloscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.melon.meloscan.ui.screens.*
import com.melon.meloscan.ui.theme.MeloScanTheme
import android.graphics.BitmapFactory
import android.util.Log
import com.melon.meloscan.ml.YOLO11mLiteRTDetector


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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("leaf_scan") {
            ScanScreen(navController = navController, scanType = "Leaf Disease")
        }

        composable("fruit_scan") {
            ScanScreen(navController = navController, scanType = "Fruit Quality")
        }

        composable("library") {
            LibraryScreen(navController = navController)
        }

        composable("library_diseases_list") {
            LeafDiseasesListScreen(navController = navController)
        }

        composable(
            route = "library_disease_detail?name={name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            LeafDiseaseDetailScreen(navController = navController, diseaseName = name)
        }

        composable("library_medicines_list") {
            MedicinesListScreen(navController = navController)
        }

        composable(
            route = "library_medicine_detail?name={name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            MedicineDetailScreen(navController = navController, medicineName = name)
        }

        composable("guide") {
            GuideScreen(navController = navController)
        }

        composable(
            route = "analyzing?type={type}&uri={uri}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("uri") { 
                    type = NavType.StringType
                    nullable = true 
                }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "Leaf Disease"
            val uri = backStackEntry.arguments?.getString("uri")
            AnalyzingScreen(navController = navController, scanType = type, imageUri = uri)
        }

        composable(
            route = "result?type={type}&result={result}&confidence={confidence}&medicine={medicine}&imageUri={imageUri}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                },
                navArgument("result") {
                    type = NavType.StringType
                },
                navArgument("confidence") {
                    type = NavType.IntType
                },
                navArgument("medicine") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("imageUri") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->

            val type =
                backStackEntry.arguments?.getString("type")
                    ?: "Leaf Disease"

            val result =
                backStackEntry.arguments?.getString("result")
                    ?: "Unknown"

            val confidence =
                backStackEntry.arguments?.getInt("confidence")
                    ?: 0

            val medicine =
                backStackEntry.arguments?.getString("medicine")

            val imageUri =
                backStackEntry.arguments?.getString("imageUri")

            ScanResultScreen(
                navController = navController,
                scanType = type,
                result = result,
                confidence = confidence,
                medicine = medicine,
                imageUri = imageUri
            )
        }

        composable("about") {
            AboutScreen(navController = navController)
        }
    }
}
