package com.melon.meloscan.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController

import coil.compose.AsyncImage

import com.melon.meloscan.ml.fruitquality.FruitQualityFeatureExtractor
import com.melon.meloscan.ml.fruitquality.FruitQualityONNXTester


/*
 * ============================================================================
 * FRUIT QUALITY INPUT-VALIDATION THRESHOLD
 * ============================================================================
 *
 * This is the empirical input-validation safeguard established during testing.
 *
 * 200 watermelon images
 * 1,380 non-watermelon images
 *
 * At 76%:
 *
 * Watermelon accepted:
 * 158 / 200 = 79.0%
 *
 * Non-watermelon rejected:
 * 1313 / 1380 = 95.14%
 *
 * IMPORTANT:
 *
 * This is NOT a third model.
 * This is NOT a watermelon detector.
 *
 * It is only an input-validation safeguard using the probability produced
 * by the existing Random Forest classifier.
 */

private const val FRUIT_VALIDATION_THRESHOLD = 0.76f


@Composable
fun AnalyzingFruit(
    navController: NavController,
    scanType: String,
    imageUri: String?
) {

    val context = LocalContext.current


    /*
     * ========================================================================
     * IMAGE URI
     * ========================================================================
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
            label = "fruit_processing_animation"
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
        label = "fruit_rotation"
    )


    /*
     * ========================================================================
     * FRUIT QUALITY PROCESSING
     * ========================================================================
     *
     * Pipeline:
     *
     * Android Image
     *      ↓
     * Bitmap
     *      ↓
     * Feature Extraction
     *      ↓
     * 1,993 features
     *      ↓
     * Random Forest ONNX
     *      ↓
     * Probability
     *      ↓
     * 76% validation safeguard
     *      ↓
     * Good / Bad
     *      ↓
     * ResultScreen
     */

    LaunchedEffect(imageUri, scanType) {

        /*
         * ------------------------------------------------------------
         * Validate image URI
         * ------------------------------------------------------------
         */

        if (imageUri == null) {

            Log.e(
                "FruitQualityFeatureTest",
                "Image URI is null."
            )

            return@LaunchedEffect
        }


        /*
         * ------------------------------------------------------------
         * Bitmap
         * ------------------------------------------------------------
         */

        var bitmap: android.graphics.Bitmap? = null


        try {

            val uri = Uri.parse(imageUri)


            /*
             * ------------------------------------------------------------
             * Decode image
             * ------------------------------------------------------------
             */

            bitmap = context.contentResolver
                .openInputStream(uri)
                ?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }


            /*
             * ------------------------------------------------------------
             * Check bitmap
             * ------------------------------------------------------------
             */

            if (bitmap == null) {

                Log.e(
                    "FruitQualityFeatureTest",
                    "Unable to decode fruit image."
                )

                return@LaunchedEffect
            }


            Log.d(
                "FruitQualityFeatureTest",
                "Fruit image size: " +
                        "${bitmap.width} x ${bitmap.height}"
            )


            /*
             * =================================================================
             * FEATURE COUNT VALIDATION
             * =================================================================
             */

            val validationResult =
                FruitQualityFeatureExtractor
                    .validateFeatureCounts(
                        bitmap
                    )

            Log.d(
                "FruitQualityFeatureTest",
                "\n$validationResult"
            )


            /*
             * =================================================================
             * RESIZE VALIDATION
             * =================================================================
             */

            val resizeValidation =
                FruitQualityFeatureExtractor
                    .validateResizePixels(
                        bitmap
                    )

            Log.d(
                "FruitQualityResize",
                "\n$resizeValidation"
            )


            /*
             * =================================================================
             * COLOR BIN VALIDATION
             * =================================================================
             */

            val colorBinValidation =
                FruitQualityFeatureExtractor
                    .validateColorFeatureBins(
                        bitmap
                    )

            Log.d(
                "FruitQualityColorBins",
                "\n$colorBinValidation"
            )


            /*
             * =================================================================
             * HSV PIXEL VALIDATION
             * =================================================================
             */

            val hsvPixelValidation =
                FruitQualityFeatureExtractor
                    .validateHsvPixelConversion(
                        bitmap
                    )

            Log.d(
                "FruitQualityHsvPixels",
                "\n$hsvPixelValidation"
            )


            /*
             * =================================================================
             * EXTRACT COMPLETE 1,993-FEATURE VECTOR
             * =================================================================
             */

            val allFeatures =
                FruitQualityFeatureExtractor
                    .extractAllFeatures(
                        bitmap
                    )


            /*
             * =================================================================
             * COLOR FEATURE STAGE VALIDATION
             * =================================================================
             */

            val colorValidation =
                FruitQualityFeatureExtractor
                    .validateColorFeatureStages(
                        bitmap
                    )

            Log.d(
                "FruitQualityColor",
                "\n$colorValidation"
            )


            /*
             * =================================================================
             * FEATURE BLOCK VALIDATION
             * =================================================================
             */

            val blockValidation =
                FruitQualityFeatureExtractor
                    .validateFeatureBlocks(
                        bitmap
                    )

            Log.d(
                "FruitQualityFeatureBlocks",
                "\n$blockValidation"
            )


            /*
             * =================================================================
             * HOG GRADIENT VALIDATION
             * =================================================================
             */

            val hogGradientValidation =
                FruitQualityFeatureExtractor
                    .validateHogGradientStages(
                        bitmap
                    )

            Log.d(
                "FruitQualityHogGradient",
                "\n$hogGradientValidation"
            )


            /*
             * =================================================================
             * HOG CELL HISTOGRAM VALIDATION
             * =================================================================
             *
             * This function already prints its own diagnostic information.
             */

            FruitQualityFeatureExtractor
                .validateHogCellHistograms(
                    bitmap
                )


            /*
             * =================================================================
             * COMPLETE FEATURE VECTOR INFORMATION
             * =================================================================
             */

            Log.d(
                "FruitQualityFeatureTest",
                "Complete feature vector size: ${allFeatures.size}"
            )


            /*
             * =================================================================
             * FEATURE SUM
             * =================================================================
             */

            var featureSum = 0.0


            /*
             * =================================================================
             * FEATURE ABSOLUTE SUM
             * =================================================================
             */

            var featureAbsoluteSum = 0.0


            /*
             * =================================================================
             * FEATURE MINIMUM / MAXIMUM
             * =================================================================
             */

            var featureMin =
                Float.POSITIVE_INFINITY

            var featureMax =
                Float.NEGATIVE_INFINITY


            for (value in allFeatures) {

                featureSum +=
                    value.toDouble()

                featureAbsoluteSum +=
                    kotlin.math.abs(
                        value.toDouble()
                    )


                if (value < featureMin) {

                    featureMin = value
                }


                if (value > featureMax) {

                    featureMax = value
                }
            }


            /*
             * =================================================================
             * FEATURE FINGERPRINT
             * =================================================================
             */

            Log.d(
                "FruitQualityFeatureTest",
                "Feature sum: $featureSum"
            )

            Log.d(
                "FruitQualityFeatureTest",
                "Feature absolute sum: $featureAbsoluteSum"
            )

            Log.d(
                "FruitQualityFeatureTest",
                "Feature minimum: $featureMin"
            )

            Log.d(
                "FruitQualityFeatureTest",
                "Feature maximum: $featureMax"
            )


            /*
             * =================================================================
             * FEATURE WEIGHTED SUM
             * =================================================================
             */

            var featureWeightedSum = 0.0


            for (index in allFeatures.indices) {

                featureWeightedSum +=
                    (index + 1).toDouble() *
                            allFeatures[index].toDouble()
            }


            Log.d(
                "FruitQualityFeatureTest",
                "Feature weighted sum: $featureWeightedSum"
            )


            /*
             * =================================================================
             * RANDOM FOREST ONNX TEST
             * =================================================================
             */

            val onnxTester =
                FruitQualityONNXTester(context)


            try {

                /*
                 * ------------------------------------------------------------
                 * Use the already decoded fruit Bitmap directly.
                 *
                 * This is the exact image sent to the feature extractor.
                 * ------------------------------------------------------------
                 */

                val fruitBitmap = bitmap
                    ?: throw IllegalStateException(
                        "Fruit bitmap is null before ONNX prediction."
                    )


                /*
                 * ------------------------------------------------------------
                 * Run Random Forest ONNX prediction.
                 * ------------------------------------------------------------
                 */

                val prediction =
                    onnxTester.predict(fruitBitmap)


                Log.d(
                    "FruitQualityONNX",
                    "FINAL RESULT = ${prediction.label}"
                )

                Log.d(
                    "FruitQualityONNX",
                    "FINAL CLASS ID = ${prediction.classId}"
                )


                /*
                 * ------------------------------------------------------------
                 * Probabilities
                 * ------------------------------------------------------------
                 */

                val probabilities =
                    prediction.probabilities


                val maximumProbability =
                    probabilities?.maxOrNull() ?: 0f


                val confidence =
                    (maximumProbability * 100f).toInt()


                Log.d(
                    "FruitQualityONNX",
                    "FINAL BAD PROBABILITY = " +
                            "${probabilities?.getOrNull(0)}"
                )

                Log.d(
                    "FruitQualityONNX",
                    "FINAL GOOD PROBABILITY = " +
                            "${probabilities?.getOrNull(1)}"
                )

                Log.d(
                    "FruitQualityONNX",
                    "FINAL CONFIDENCE = $confidence%"
                )


                /*
                 * =================================================================
                 * FRUIT QUALITY INPUT VALIDATION
                 * =================================================================
                 *
                 * The Random Forest classifier only has:
                 *
                 * 0 = Bad
                 * 1 = Good
                 *
                 * Therefore, the system uses the maximum probability as
                 * an empirical input-validation safeguard.
                 *
                 * It does NOT create a third class.
                 */

                Log.d(
                    "FruitQualityValidation",
                    "Maximum probability = $maximumProbability"
                )

                Log.d(
                    "FruitQualityValidation",
                    "Maximum probability percent = " +
                            "${maximumProbability * 100f}%"
                )

                Log.d(
                    "FruitQualityValidation",
                    "Validation threshold = " +
                            "${FRUIT_VALIDATION_THRESHOLD * 100f}%"
                )


                /*
                 * ------------------------------------------------------------
                 * Validation failed
                 * ------------------------------------------------------------
                 */

                if (
                    maximumProbability <
                    FRUIT_VALIDATION_THRESHOLD
                ) {

                    Log.d(
                        "FruitQualityValidation",
                        "VALIDATION FAILED. " +
                                "Image confidence is below 76%."
                    )


                    Toast.makeText(
                        context,
                        "Image not accepted. Please select or capture a clear watermelon fruit image.",
                        Toast.LENGTH_LONG
                    ).show()


                    /*
                     * Return to Fruit Quality scan screen.
                     *
                     * No Good/Bad result is created.
                     */

                    navController.popBackStack()

                    return@LaunchedEffect
                }


                /*
                 * ------------------------------------------------------------
                 * Validation passed
                 * ------------------------------------------------------------
                 */

                Log.d(
                    "FruitQualityValidation",
                    "VALIDATION PASSED. " +
                            "Continuing to Good/Bad result."
                )


                /*
                 * =================================================================
                 * RESULT NAVIGATION
                 * =================================================================
                 */

                val encodedType =
                    Uri.encode(scanType)


                val encodedResult =
                    Uri.encode(prediction.label)


                val encodedMedicine =
                    Uri.encode(
                        if (prediction.label == "Good") {

                            "The watermelon fruit was classified as Good quality based on the trained Random Forest model."

                        } else {

                            "The watermelon fruit was classified as Bad quality based on the trained Random Forest model."
                        }
                    )


                val encodedImageUri =
                    Uri.encode(imageUri)


                /*
                 * ------------------------------------------------------------
                 * Navigate to existing ResultScreen.
                 *
                 * ResultScreen.kt remains unchanged.
                 * ------------------------------------------------------------
                 */

                navController.navigate(
                    "result?" +
                            "type=$encodedType" +
                            "&result=$encodedResult" +
                            "&confidence=$confidence" +
                            "&medicine=$encodedMedicine" +
                            "&imageUri=$encodedImageUri"
                )


            } catch (e: Exception) {

                Log.e(
                    "FruitQualityONNX",
                    "ONNX prediction failed.",
                    e
                )


            } finally {

                /*
                 * Always release ONNX Runtime resources.
                 */

                onnxTester.close()
            }


        } catch (e: Exception) {

            Log.e(
                "FruitQualityFeatureTest",
                "Fruit quality feature extraction failed.",
                e
            )


        } finally {

            /*
             * Release Bitmap.
             */

            bitmap?.recycle()
        }
    }


    /*
     * ========================================================================
     * ANALYZING SCREEN UI
     * ========================================================================
     *
     * The UI remains visually consistent with the original AnalyzingScreen.
     *
     * This version is dedicated ONLY to Fruit Quality.
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
                text = "Fruit Quality Evaluation",
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
                    text = "Evaluating fruit quality...",
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
                    "Analyzing visible fruit quality characteristics",
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
                    "The image will be processed using the fruit quality model.",
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