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
import com.melon.meloscan.R

@Composable
fun AppBottomBar(currentScreen: String) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = currentScreen == "Home",
            onClick = { /* TODO: Navigate to Home */ },
            icon = { Icon(painterResource(R.drawable.ic_home), contentDescription = "Home") },
            label = { Text("Home") },
            modifier = Modifier.size(28.dp)
        )
        NavigationBarItem(
            selected = currentScreen == "Scan",
            onClick = { /* TODO: Navigate to Scan */ },
            icon = { Icon(painterResource(R.drawable.ic_scnner), contentDescription = "Scan") },
            label = { Text("Scan") },
            modifier = Modifier.size(28.dp)
        )
        NavigationBarItem(
            selected = currentScreen == "History",
            onClick = { /* TODO: Navigate to History */ },
            icon = { Icon(painterResource(R.drawable.ic_history), contentDescription = "History") },
            label = { Text("History") },
            modifier = Modifier.size(28.dp)
        )
        NavigationBarItem(
            selected = currentScreen == "Guide",
            onClick = { /* TODO: Navigate to Guide */ },
            icon = { Icon(painterResource(R.drawable.ic_guide), contentDescription = "Guide") },
            label = { Text("Guide") },
            modifier = Modifier.size(28.dp)

        )
    }
}
