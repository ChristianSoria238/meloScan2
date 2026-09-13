package com.melon.meloscan.ui.screens

import androidx.compose.animation.core.copy
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.melon.meloscan.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    navController: NavController,
    scanType: String,
    result: String,
    confidence: Int,
    medicine: String?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (scanType == "Leaf Disease") "Disease Detection" else "Quality Evaluation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painterResource(R.drawable.back), "Back") 
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Share result */ }) {
                        Icon(painterResource(R.drawable.share), "Share") 
                    }
                    IconButton(onClick = { /* TODO: Save/Copy result */ }) {
                        Icon(painterResource(R.drawable.copy), "Copy")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = if (scanType == "Leaf Disease") R.drawable.watermelon else R.drawable.fruity),
                        contentDescription = "Scanned watermelon",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Result Title
                    Text(
                        text = result,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (result.contains("Low") || result.contains("Disease")) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Confidence Badge
                    Surface(
                        color = Color(0xFFF0F2F5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$confidence% Accuracy",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 0.5.dp, color = Color.LightGray)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description / Recommendation Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = if (scanType == "Leaf Disease") "Recommended Treatment:" else "Quality Analysis:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = medicine ?: "No specific recommendation available.",
                            color = Color.DarkGray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Button(
                onClick = { 
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3ED47A))
            ) {
                Text("Scan Another", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { /* TODO: Save result */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Text("Save Result", color = Color.Black, fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanResultScreenPreview() {
    ScanResultScreen(
        navController = rememberNavController(),
        scanType = "Leaf Disease",
        result = "Downy Mildew",
        confidence = 92,
        medicine = "Fungicide with Mancozeb"
    )
}
