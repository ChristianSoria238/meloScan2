package com.melon.meloscan.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.melon.meloscan.R

@Composable
fun AppBottomBar(navController: NavController, currentScreen: String) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == "Home",
            onClick = {
                if (currentScreen != "Home") {
                    navController.navigate("home") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { 
                Icon(
                    painterResource(R.drawable.ic_home), 
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Home", fontSize = 12.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == "Library",
            onClick = {
                if (currentScreen != "Library") {
                    navController.navigate("library") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { 
                Icon(
                    painterResource(R.drawable.book), 
                    contentDescription = "Library",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Library", fontSize = 12.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == "Guide",
            onClick = {
                if (currentScreen != "Guide") {
                    navController.navigate("guide") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { 
                Icon(
                    painterResource(R.drawable.ic_guide), 
                    contentDescription = "Guide",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Guide", fontSize = 12.sp) }
        )
    }
}
