package com.melon.meloscan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melon.meloscan.R

import com.melon.meloscan.ui.navigation.AppBottomBar

val GreenTint = Color(0xFF3ED47A)
val RedTint = Color(0xFFFF6B6B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = "Logo",
                            tint = Color(0xFF3ED47A),
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MelonVision",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = { AppBottomBar("Home") },
        containerColor = Color.White
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Check Watermelon\nQuality Instantly",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI-powered image analysis for fast and reliable results.",
                fontSize = 14.sp,
                color = Color(0xFF8A8A8A)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Gradient Start Scan Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF3ED47A),
                                Color(0xFFFF6B6B)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Start Scan",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFE5E5E5), Color(0xFFE5E5E5))
                    )
                )
            ) {
                Text("Upload Image", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Key Features",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Feature Cards
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeatureCard(
                        "AI Image Analysis",
                        "Our advanced AI detects subtle quality indicators.",
                        R.drawable.lightning,
                        iconTint = GreenTint,
                        Modifier.weight(1f)
                    )
                    FeatureCard(
                        "Fast Detection",
                        "Get instant results in seconds, no waiting.",
                        R.drawable.reset,
                        iconTint = RedTint,
                        Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeatureCard(
                        "Accurate Results",
                        "High confidence scores for reliable quality checks.",
                        R.drawable.result,
                        iconTint = GreenTint,
                        Modifier.weight(1f)
                    )
                    FeatureCard(
                        "Quality Guide",
                        "Learn what makes a watermelon perfect.",
                        R.drawable.book,
                        iconTint = RedTint,
                        Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    iconRes: Int,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(title, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                description,
                fontSize = 12.sp,
                color = Color(0xFF8A8A8A)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
