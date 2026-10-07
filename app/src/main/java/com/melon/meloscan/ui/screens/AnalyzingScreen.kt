package com.melon.meloscan.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log

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
//import androidx.compose.runtime.rememberInfiniteTransition

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController

import coil.compose.AsyncImage

import androidx.compose.ui.platform.LocalContext

import com.melon.meloscan.ml.YOLO11mLiteRTDetector


@Composable
fun AnalyzingScreen(
    navController: NavController,
    scanType: String,
    imageUri: String?
) {

    val context = LocalContext.current

    /*
     * ========================================================================
     * IMAGE URI
     * ========================================================================
     *
     * This screen is now ONLY for Leaf Disease.
     *
     * Fruit Quality has been moved to:
     *
     *     AnalyzingFruit.kt
     *
     * Therefore this file does not contain:
     *
     *     - Random Forest
     *     - ONNX
     *     - FruitQualityFeatureExtractor
     *     - Fruit validation
     *     - Good / Bad classification
     */

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
     * ========================================================================
     * PROCESSING ANIMATION
     * ========================================================================
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
     * ========================================================================
     * LEAF DISEASE YOLO11m INFERENCE
     * ========================================================================
     *
     * Pipeline:
     *
     *     Captured/Gallery Image
     *             ↓
     *         Bitmap
     *             ↓
     *     YOLO11m LiteRT
     *             ↓
     *        Detections
     *             ↓
     *   Highest Confidence
     *             ↓
     *       ResultScreen
     *
     * IMPORTANT:
     *
     * The YOLO11m model and detector are NOT modified here.
     */

    LaunchedEffect(imageUri) {

        /*
         * ------------------------------------------------------------
         * Validate image URI
         * ------------------------------------------------------------
         */

        if (imageUri == null) {

            Log.e(
                "YOLO11mCaptureTest",
                "Image URI is null."
            )

            return@LaunchedEffect
        }


        /*
         * ------------------------------------------------------------
         * Variables
         * ------------------------------------------------------------
         */

        var bitmap: android.graphics.Bitmap? = null
        var detector: YOLO11mLiteRTDetector? = null


        try {

            /*
             * ------------------------------------------------------------
             * Decode image
             * ------------------------------------------------------------
             */

            val uri = Uri.parse(imageUri)

            bitmap = context.contentResolver
                .openInputStream(uri)
                ?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }


            /*
             * ------------------------------------------------------------
             * Check decoded bitmap
             * ------------------------------------------------------------
             */

            if (bitmap == null) {

                Log.e(
                    "YOLO11mCaptureTest",
                    "Unable to decode captured image."
                )

                return@LaunchedEffect
            }


            Log.d(
                "YOLO11mCaptureTest",
                "Captured image size: " +
                        "${bitmap.width} x ${bitmap.height}"
            )


            /*
             * ------------------------------------------------------------
             * Create the real YOLO11m LiteRT detector
             * ------------------------------------------------------------
             */

            detector =
                YOLO11mLiteRTDetector(context)


            /*
             * ------------------------------------------------------------
             * Run YOLO11m inference
             * ------------------------------------------------------------
             */

            val detections =
                detector.detect(bitmap)


            Log.d(
                "YOLO11mCaptureTest",
                "Number of detections: ${detections.size}"
            )


            /*
             * ------------------------------------------------------------
             * Log every detection
             * ------------------------------------------------------------
             */

            detections.forEachIndexed { index, detection ->

                Log.d(
                    "YOLO11mCaptureTest",
                    "Detection #${index + 1}: " +
                            "class=${detection.className}, " +
                            "confidence=${detection.confidence}, " +
                            "box=${detection.boundingBox}"
                )
            }


            /*
             * =================================================================
             * DISEASE DETECTED
             * =================================================================
             */

            if (detections.isNotEmpty()) {

                /*
                 * Select the detection with the highest confidence.
                 */

                val bestDetection =
                    detections.maxByOrNull {
                        it.confidence
                    }


                if (bestDetection != null) {

                    /*
                     * ---------------------------------------------------------
                     * Convert model class name into display name.
                     *
                     * Example:
                     *
                     * anthracnose
                     *      ↓
                     * Anthracnose
                     *
                     * downy_mildew
                     *      ↓
                     * Downy Mildew
                     *
                     * mosaic_disease
                     *      ↓
                     * Mosaic Disease
                     * ---------------------------------------------------------
                     */

                    val resultName =
                        bestDetection.className
                            .replace("_", " ")
                            .split(" ")
                            .joinToString(" ") { word ->
                                word.replaceFirstChar { char ->
                                    char.uppercase()
                                }
                            }


                    /*
                     * ---------------------------------------------------------
                     * Convert confidence to percentage.
                     * ---------------------------------------------------------
                     */

                    val confidencePercent =
                        (
                                bestDetection.confidence *
                                        100
                                ).toInt()


                    Log.d(
                        "YOLO11mCaptureTest",
                        "Best detection: " +
                                "class=$resultName, " +
                                "confidence=$confidencePercent%"
                    )


                    /*
                     * ---------------------------------------------------------
                     * Encode navigation parameters.
                     * ---------------------------------------------------------
                     */

                    val encodedType =
                        Uri.encode(scanType)

                    val encodedResult =
                        Uri.encode(resultName)

                    val encodedMedicine =
                        Uri.encode(
                            "No specific recommendation available."
                        )

                    val encodedImageUri =
                        Uri.encode(imageUri)


                    /*
                     * ---------------------------------------------------------
                     * Navigate to the existing ResultScreen.
                     *
                     * IMPORTANT:
                     *
                     * ResultScreen.kt is NOT changed.
                     * ---------------------------------------------------------
                     */

                    navController.navigate(
                        "result?" +
                                "type=$encodedType" +
                                "&result=$encodedResult" +
                                "&confidence=$confidencePercent" +
                                "&medicine=$encodedMedicine" +
                                "&imageUri=$encodedImageUri"
                    )
                }


            } else {

                /*
                 * =================================================================
                 * NO DISEASE DETECTED
                 * =================================================================
                 */

                Log.d(
                    "YOLO11mCaptureTest",
                    "No supported disease detected."
                )


                val encodedType =
                    Uri.encode(scanType)

                val encodedResult =
                    Uri.encode(
                        "No Disease Detected"
                    )

                val encodedMedicine =
                    Uri.encode(
                        "The system could not identify a supported " +
                                "watermelon leaf disease in this image."
                    )

                val encodedImageUri =
                    Uri.encode(imageUri)


                /*
                 * Confidence is 0 because no disease was detected.
                 */

                navController.navigate(
                    "result?" +
                            "type=$encodedType" +
                            "&result=$encodedResult" +
                            "&confidence=0" +
                            "&medicine=$encodedMedicine" +
                            "&imageUri=$encodedImageUri"
                )
            }


        } catch (e: Exception) {

            /*
             * ------------------------------------------------------------
             * Keep the actual error in Logcat.
             *
             * We do NOT create a fake disease result.
             * ------------------------------------------------------------
             */

            Log.e(
                "YOLO11mCaptureTest",
                "YOLO11m inference failed.",
                e
            )


        } finally {

            /*
             * ------------------------------------------------------------
             * Release detector and bitmap.
             * ------------------------------------------------------------
             */

            detector?.close()

            bitmap?.recycle()
        }
    }


    /*
     * ========================================================================
     * ANALYZING SCREEN UI
     * ========================================================================
     *
     * This keeps the Leaf Disease analyzing UI from the original file.
     */

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF7F9F7)
            )
    ) {

        /*
         * ====================================================================
         * TOP HEADER
         * ====================================================================
         */

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 55.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "Disease Detection",
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
         * ====================================================================
         * IMAGE PREVIEW
         * ====================================================================
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
                .background(
                    Color(0xFFE8ECE8)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFD8DED8),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment =
                Alignment.Center
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
             * =================================================================
             * DARK OVERLAY
             * =================================================================
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
             * =================================================================
             * PROCESSING INDICATOR
             * =================================================================
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
                contentAlignment =
                    Alignment.Center
            ) {

                CircularProgressIndicator(
                    modifier = Modifier
                        .size(62.dp)
                        .rotate(rotation),
                    strokeWidth = 4.dp
                )
            }


            /*
             * =================================================================
             * SCANNING LABEL
             * =================================================================
             */

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = 22.dp
                    )
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
                    text = "Detecting leaf disease...",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }


        /*
         * ====================================================================
         * PROCESSING INFORMATION
         * ====================================================================
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
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    "Checking the watermelon leaf for disease patterns",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF242424),
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "The image will be processed by the selected detection model.",
                fontSize = 12.sp,
                color = Color(0xFF777777),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )


            /*
             * =================================================================
             * PROCESSING STEPS
             * =================================================================
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
 * ============================================================================
 * SMALL PROCESSING STEP INDICATOR
 * ============================================================================
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
 * ============================================================================
 * CONNECTOR BETWEEN PROCESSING STEPS
 * ============================================================================
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