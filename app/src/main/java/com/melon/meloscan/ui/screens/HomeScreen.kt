package com.melon.meloscan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.melon.meloscan.R
import com.melon.meloscan.ui.navigation.AppBottomBar

// Colors
val GreenTint = Color(0xFF3ED47A)
val LightGray = Color(0xFFF5F7F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) { // Added surface for better top bar rendering
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = "Logo",
                                tint = GreenTint,
                                modifier = Modifier.size(32.dp) // Adjusted size
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MelonVision",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Settings */ }) {
                            Icon(
                                painter = painterResource(R.drawable.settings),
                                contentDescription = "Settings",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            AppBottomBar(navController = navController, currentScreen = "Home")
        },
        containerColor = Color.White
    ) { innerPadding ->
        // The padding from Scaffold MUST be applied to the outermost container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Consumes TopBar and BottomBar space
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to\nMelonVision",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 34.sp,
                color = Color(0xFF2D2D2D)
            )

            Text(
                text = "Choose a specialized AI tool to start.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ServiceCard(
                title = "Leaf Disease",
                description = "Detect infected leaves of your watermelon.",
                buttonText = "Detect Now",
                iconRes = R.drawable.watermelon,
                gradient = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)),
                onClick = { navController.navigate("leaf_scan") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ServiceCard(
                title = "Fruit Quality",
                description = "Detect the quality of your watermelon fruits.",
                buttonText = "Evaluate Quality",
                iconRes = R.drawable.fruity,
                gradient = listOf(Color(0xFFFF9800), Color(0xFFF44336)),
                onClick = { navController.navigate("fruit_scan") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Resources",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFeatureItem(
                    title = "Leaf Disease Medicine",
                    icon = R.drawable.book,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("guide") }
                )
                SmallFeatureItem(
                    title = "Quality Features Guide",
                    icon = R.drawable.book,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("history") }
                )
               
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ServiceCard(
    title: String,
    description: String,
    buttonText: String,
    iconRes: Int,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Improved Gradient Button Logic
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                contentPadding = PaddingValues(0.dp), // Important for gradient to fill
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(gradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SmallFeatureItem(
    title: String,
    icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LightGray)
            .clickable { onClick() }
            .padding(16.dp), // Increased padding for better touch target
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.DarkGray
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Using a simple MaterialTheme wrapper helps with Preview rendering
    MaterialTheme {
        HomeScreen(rememberNavController())
    }
}