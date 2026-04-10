package com.melon.meloscan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.melon.meloscan.R
import com.melon.meloscan.ui.navigation.AppBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualityGuideScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Farmer's Guide",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            AppBottomBar(
                navController = navController,
                currentScreen = "Guide"
            )
        },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 30.dp)
        ) {
            // Header Description
            item {
                Text(
                    text = "Learn how to use MelonVision AI to monitor your farm's health and maximize harvest value.",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    lineHeight = 22.sp
                )
            }

            // SECTION 1: LEAF DISEASE (Deep Learning)
            item {
                ThesisFeatureGuide(
                    title = "Leaf Disease Detection",
                    subtitle = "Powered by Deep Learning (CNN)",
                    description = "Identify diseases like Downy Mildew or Anthracnose instantly to prevent crop loss.",
                    iconRes = R.drawable.watermelon, // Replace with leaf icon
                    steps = listOf(
                        "Place the infected leaf in the center of the frame.",
                        "Ensure natural daylight for better AI accuracy.",
                        "Avoid blurry images; keep the camera steady."
                    ),
                    accentColor = Color(0xFF3ED47A)
                )
            }

            // SECTION 2: FRUIT QUALITY (Regression ML)
            item {
                ThesisFeatureGuide(
                    title = "Fruit Quality Evaluation",
                    subtitle = "Regression Machine Learning",
                    description = "Estimate ripeness and sugar levels (Brix value) based on external visual features.",
                    iconRes = R.drawable.fruity,
                    steps = listOf(
                        "Capture the 'Ground Spot' (yellowish area) of the fruit.",
                        "Ensure the tendril and stem are visible in the photo.",
                        "The AI calculates quality based on color intensity."
                    ),
                    accentColor = Color(0xFFFF9800)
                )
            }

            // Best Practices / Requirements
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7F9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "For best results, use a clean camera lens and avoid using flash at night.",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThesisFeatureGuide(
    title: String,
    subtitle: String,
    description: String,
    iconRes: Int,
    steps: List<String>,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accentColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = subtitle, fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.DarkGray,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Steps Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9F9F9), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            steps.forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = step,
                        fontSize = 13.sp,
                        color = Color(0xFF444444),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QualityGuideScreenPreview() {
    MaterialTheme {
        QualityGuideScreen(rememberNavController())
    }
}
