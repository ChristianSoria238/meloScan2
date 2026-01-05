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
import com.melon.meloscan.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Result", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Navigate back */ }) {
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Scanned watermelon",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quality Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF3ED47A))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("High Quality", color = Color.White, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Confidence Score
                        Text(
                            text = "92% Confidence",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "This watermelon exhibits excellent texture, vibrant color, and a high sugar content, indicating peak ripeness and freshness.",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Button(
                onClick = { /* TODO: Scan another */ },
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
    ScanResultScreen()
}
