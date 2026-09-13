package com.melon.meloscan.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AnalyzingScreen(navController: NavController, scanType: String, imageUri: String?) {
    // Simulate analysis delay
    LaunchedEffect(Unit) {
        delay(4000) // Simulate a 4-second analysis
        
        val result: String
        val medicine: String?
        val confidence = (85..99).random()

        if (scanType == "Leaf Disease") {
            val diseases = listOf(
                "Downy Mildew" to "Fungicide with Mancozeb or Chlorothalonil",
                "Anthracnose" to "Apply Benomyl or Carbendazim",
                "Mosaic Disease" to "Remove infected plants; control aphids"
            )
            val selected = diseases.random()
            result = selected.first
            medicine = selected.second
        } else {
            val qualities = listOf("High Quality", "Low Quality")
            result = qualities.random()
            medicine = if (result == "High Quality") "Peak ripeness and high sugar level" else "Low sugar content detected"
        }

        val encodedResult = URLEncoder.encode(result, StandardCharsets.UTF_8.toString())
        val encodedMedicine = URLEncoder.encode(medicine ?: "", StandardCharsets.UTF_8.toString())
        
        navController.navigate("result?type=$scanType&result=$encodedResult&confidence=$confidence&medicine=$encodedMedicine") {
            popUpTo("analyzing") { inclusive = true }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Simple circular loading indicator
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .rotate(rotation)
            ) {
                // You can use a custom drawable here for the dotted circle effect
                // For simplicity, we'll use a placeholder.
                // To create the exact effect, you'd draw custom graphics on a Canvas.
            }

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                "Analyzing watermelon quality...",
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AnalyzingScreenPreview() {
    AnalyzingScreen(rememberNavController(), "Leaf Disease", null)
}
