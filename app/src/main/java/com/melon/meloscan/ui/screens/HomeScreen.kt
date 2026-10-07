package com.melon.meloscan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
            Surface(
                shadowElevation = 2.dp
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                painter =
                                    painterResource(
                                        R.drawable.logo
                                    ),
                                contentDescription =
                                    "Logo",
                                tint = GreenTint,
                                modifier =
                                    Modifier.size(24.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(8.dp)
                            )

                            Text(
                                text = "MelonVision",
                                fontWeight =
                                    FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    "about"
                                )
                            }
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.Info,
                                contentDescription =
                                    "About",
                                modifier =
                                    Modifier.size(24.dp)
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults
                            .topAppBarColors(
                                containerColor =
                                    Color.White
                            )
                )
            }
        },

        bottomBar = {
            AppBottomBar(
                navController = navController,
                currentScreen = "Home"
            )
        },

        containerColor = Color.White

    ) { innerPadding ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 20.dp
                    )
        ) {

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Text(
                text =
                    "Welcome to\nMelonVision",
                fontSize = 28.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                lineHeight = 34.sp,
                color =
                    Color(0xFF2D2D2D)
            )

            Text(
                text =
                    "Choose a specialized AI tool to start.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier =
                    Modifier.padding(
                        top = 4.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            // ------------------------------------------------
            // LEAF DISEASE CARD
            // ------------------------------------------------

            ServiceCard(
                title = "Leaf Disease",
                description =
                    "Detect infected leaves of your watermelon.",
                buttonText =
                    "Capture Disease",
                secondaryButtonText =
                    "Live Detection",
                iconRes =
                    R.drawable.watermelon,
                gradient =
                    listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF2E7D32)
                    ),
                secondaryGradient =
                    listOf(
                        Color(0xFF66BB6A),
                        Color(0xFF388E3C)
                    ),
                onClick = {
                    navController.navigate(
                        "leaf_scan"
                    )
                },
                onSecondaryClick = {
                    navController.navigate(
                        "leaf_live"
                    )
                }
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            // ------------------------------------------------
            // FRUIT QUALITY CARD
            // ------------------------------------------------

            ServiceCard(
                title = "Fruit Quality",
                description =
                    "Detect the quality of your watermelon fruits.",
                buttonText =
                    "Evaluate Quality",
                iconRes =
                    R.drawable.fruity,
                gradient =
                    listOf(
                        Color(0xFFFF9800),
                        Color(0xFFF44336)
                    ),
                onClick = {
                    navController.navigate(
                        "fruit_scan"
                    )
                }
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )
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
    onClick: () -> Unit,

    secondaryButtonText: String? = null,
    secondaryGradient: List<Color>? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    Card(
        shape =
            RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    LightGray
            ),

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            // --------------------------------------------
            // CARD HEADER
            // --------------------------------------------

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            Color(0xFF2D2D2D)
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color =
                            Color(0xFF666666),
                        lineHeight = 18.sp
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Box(
                    modifier =
                        Modifier
                            .size(70.dp)
                            .clip(
                                RoundedCornerShape(
                                    16.dp
                                )
                            )
                            .background(
                                Color.White
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Image(
                        painter =
                            painterResource(
                                iconRes
                            ),

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(45.dp),

                        contentScale =
                            ContentScale.Fit
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            // --------------------------------------------
            // PRIMARY BUTTON
            // --------------------------------------------

            GradientButton(
                text = buttonText,
                gradient = gradient,
                onClick = onClick
            )

            // --------------------------------------------
            // SECONDARY BUTTON
            // --------------------------------------------

            if (
                secondaryButtonText != null &&
                secondaryGradient != null &&
                onSecondaryClick != null
            ) {

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                GradientButton(
                    text =
                        secondaryButtonText,

                    gradient =
                        secondaryGradient,

                    onClick =
                        onSecondaryClick
                )
            }
        }
    }
}

@Composable
private fun GradientButton(
    text: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,

        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp),

        contentPadding =
            PaddingValues(0.dp),

        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    Color.Transparent
            ),

        shape =
            RoundedCornerShape(14.dp)
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            gradient
                        )
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = text,
                color = Color.White,
                fontWeight =
                    FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            rememberNavController()
        )
    }
}