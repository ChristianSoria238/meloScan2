package com.melon.meloscan.ui.screens

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun AnalyzingScreen(
    navController: NavController,
    scanType: String,
    imageUri: String?
) {
    val isLeafDisease = scanType == "Leaf Disease"

    val image = remember(imageUri) {
        imageUri?.let {
            try {
                Uri.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    /*
     * Processing animation.
     */
    val infiniteTransition =
        rememberInfiniteTransition(
            label = "processing_animation"
        )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    /*
     * IMPORTANT:
     *
     * This is currently only a processing UI.
     *
     * We do NOT generate a fake disease,
     * fake confidence, or fake quality result.
     *
     * The real backend call will be inserted
     * into this processing stage after the
     * backend/API is ready.
     */
    LaunchedEffect(Unit) {

        /*
         * Temporary processing delay.
         *
         * This prevents the screen from immediately
         * jumping away while we are developing the
         * backend integration.
         *
         * It must NOT be treated as AI processing time.
         */
        delay(2500)

        /*
         * DO NOT generate a fake result here.
         *
         * Backend integration will be added next.
         */
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F7))
    ) {

        /*
         * TOP HEADER
         */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 55.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (isLeafDisease) {
                    "Disease Detection"
                } else {
                    "Fruit Quality Evaluation"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B)
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Text(
                text = "Analyzing your image",
                fontSize = 14.sp,
                color = Color(0xFF707070)
            )
        }

        /*
         * IMAGE PREVIEW
         */
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    start = 28.dp,
                    end = 28.dp,
                    bottom = 100.dp
                )
                .fillMaxWidth()
                .height(360.dp)
                .clip(
                    RoundedCornerShape(24.dp)
                )
                .background(Color(0xFFE8ECE8))
                .border(
                    width = 1.dp,
                    color = Color(0xFFD8DED8),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            if (image != null) {

                AsyncImage(
                    model = image,
                    contentDescription =
                        "Image being analyzed",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            } else {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Image unavailable",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF555555)
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text =
                            "The selected image could not be loaded.",
                        fontSize = 12.sp,
                        color = Color(0xFF777777),
                        textAlign = TextAlign.Center
                    )
                }
            }

            /*
             * DARK OVERLAY
             *
             * Keeps the processing indicator readable
             * over the image.
             */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = 0.32f
                        )
                    )
            )

            /*
             * PROCESSING INDICATOR
             */
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Black.copy(
                            alpha = 0.58f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator(
                    modifier = Modifier
                        .size(62.dp)
                        .rotate(rotation),
                    strokeWidth = 4.dp
                )
            }

            /*
             * SCANNING LABEL
             */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 22.dp)
                    .clip(
                        RoundedCornerShape(50.dp)
                    )
                    .background(
                        Color.Black.copy(
                            alpha = 0.65f
                        )
                    )
                    .padding(
                        horizontal = 18.dp,
                        vertical = 9.dp
                    )
            ) {

                Text(
                    text = if (isLeafDisease) {
                        "Detecting leaf disease..."
                    } else {
                        "Evaluating fruit quality..."
                    },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        /*
         * PROCESSING INFORMATION
         */
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = 28.dp,
                    end = 28.dp,
                    bottom = 38.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (isLeafDisease) {
                    "Checking the watermelon leaf for disease patterns"
                } else {
                    "Analyzing visible fruit quality characteristics"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF242424),
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = if (isLeafDisease) {
                    "The image will be processed by the selected detection model."
                } else {
                    "The image will be processed using the fruit quality model."
                },
                fontSize = 12.sp,
                color = Color(0xFF777777),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            /*
             * PROCESSING STEPS
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.Center,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                ProcessingStep(
                    number = "1",
                    text = "Image"
                )

                ProcessingLine()

                ProcessingStep(
                    number = "2",
                    text = "Process"
                )

                ProcessingLine()

                ProcessingStep(
                    number = "3",
                    text = "Result"
                )
            }
        }
    }
}

/*
 * Small processing step indicator.
 */
@Composable
private fun ProcessingStep(
    number: String,
    text: String
) {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    Color(0xFF2E7D32)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = number,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = text,
            color = Color(0xFF666666),
            fontSize = 10.sp
        )
    }
}

/*
 * Connector between processing steps.
 */
@Composable
private fun ProcessingLine() {
    Box(
        modifier = Modifier
            .width(32.dp)
            .height(1.dp)
            .background(
                Color(0xFFB9C3B9)
            )
    )
}