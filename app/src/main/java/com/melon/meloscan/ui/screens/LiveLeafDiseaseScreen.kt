package com.melon.meloscan.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.melon.meloscan.ml.YOLO11mLiteRTDetector
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val LIVE_MIN_CONFIDENCE = 0.75f

@Composable
fun LiveLeafDiseaseScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    var detection by remember {
        mutableStateOf<YOLO11mLiteRTDetector.Detection?>(null)
    }

    var isProcessing by remember {
        mutableStateOf(false)
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode =
                PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val detector = remember {
        YOLO11mLiteRTDetector(context)
    }

    val processingLock = remember {
        AtomicBoolean(false)
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            hasCameraPermission = granted

            cameraError =
                if (granted) {
                    null
                } else {
                    "Camera access is needed for live disease detection."
                }
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    DisposableEffect(
        hasCameraPermission,
        lifecycleOwner
    ) {

        if (!hasCameraPermission) {

            onDispose {
                cameraExecutor.shutdown()

                try {
                    detector.close()
                } catch (_: Exception) {
                }
            }

        } else {

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)

            val executor =
                ContextCompat.getMainExecutor(context)

            val listener = Runnable {

                try {

                    val cameraProvider =
                        cameraProviderFuture.get()

                    cameraProvider.unbindAll()

                    val preview =
                        Preview.Builder()
                            .setTargetResolution(
                                Size(640, 640)
                            )
                            .build()
                            .also {
                                it.setSurfaceProvider(
                                    previewView.surfaceProvider
                                )
                            }

                    val imageAnalysis =
                        ImageAnalysis.Builder()
                            .setTargetResolution(
                                Size(640, 640)
                            )
                            .setBackpressureStrategy(
                                ImageAnalysis
                                    .STRATEGY_KEEP_ONLY_LATEST
                            )
                            .setOutputImageFormat(
                                ImageAnalysis
                                    .OUTPUT_IMAGE_FORMAT_RGBA_8888
                            )
                            .build()

                    imageAnalysis.setAnalyzer(
                        cameraExecutor
                    ) { imageProxy ->

                        if (
                            !processingLock.compareAndSet(
                                false,
                                true
                            )
                        ) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        try {

                            isProcessing = true

                            val bitmap =
                                imageProxy.toBitmap()

                            if (bitmap != null) {

                                val results =
                                    detector.detect(bitmap)

                                /*
                                 * Keep only detections whose
                                 * confidence is between 75% and 100%.
                                 *
                                 * 0.75 = 75%
                                 * 1.00 = 100%
                                 */
                                val validDetections =
                                    results.filter { result ->
                                        result.confidence >=
                                                LIVE_MIN_CONFIDENCE &&
                                                result.confidence <=
                                                1.0f
                                    }

                                /*
                                 * If multiple valid detections exist,
                                 * display the one with the highest
                                 * confidence.
                                 */
                                val bestDetection =
                                    validDetections
                                        .maxByOrNull {
                                            it.confidence
                                        }

                                detection =
                                    bestDetection
                            }

                        } catch (e: Exception) {

                            cameraError =
                                "Live detection failed."

                        } finally {

                            isProcessing = false
                            processingLock.set(false)
                            imageProxy.close()
                        }
                    }

                    val cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )

                    cameraError = null

                } catch (e: Exception) {

                    cameraError =
                        "Unable to start the live camera."
                }
            }

            cameraProviderFuture.addListener(
                listener,
                executor
            )

            onDispose {

                try {
                    cameraProviderFuture.get()
                        .unbindAll()
                } catch (_: Exception) {
                }

                cameraExecutor.shutdown()

                try {
                    detector.close()
                } catch (_: Exception) {
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        if (hasCameraPermission) {

            AndroidView(
                factory = {
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            detection?.let { currentDetection ->

                DetectionOverlay(
                    detection = currentDetection,
                    previewWidth = previewView.width,
                    previewHeight = previewView.height
                )
            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111111)),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Camera Preview",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        /*
         * Top gradient.
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(
                    width = 1.dp,
                    height = 145.dp
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.75f),
                            Color.Transparent
                        )
                    )
                )
        )

        /*
         * Top navigation bar.
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Black.copy(alpha = 0.45f)
                    )
                    .clickable {
                        navController.popBackStack()
                    },
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }

            Text(
                text = "Live Leaf Disease",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(
                modifier = Modifier.size(46.dp)
            )
        }

        /*
         * Camera instruction.
         */
        Text(
            text = "Point the camera at a watermelon leaf",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = 88.dp,
                    start = 24.dp,
                    end = 24.dp
                )
        )

        /*
         * Detection result.
         */
        detection?.let { currentDetection ->

            val confidence =
                (currentDetection.confidence * 100f)
                    .coerceIn(0f, 100f)

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom =
                            35.dp +
                                    WindowInsets
                                        .navigationBars
                                        .asPaddingValues()
                                        .calculateBottomPadding()
                    )
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
                    .background(
                        Color.Black.copy(alpha = 0.82f)
                    )
                    .padding(
                        horizontal = 22.dp,
                        vertical = 14.dp
                    )
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = formatDiseaseName(
                            currentDetection.className
                        ),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.size(4.dp)
                    )

                    Text(
                        text =
                            "Confidence: %.1f%%"
                                .format(confidence),
                        color =
                            Color.White.copy(
                                alpha = 0.82f
                            ),
                        fontSize = 13.sp
                    )
                }
            }
        }

        /*
         * No valid detection.
         *
         * This includes detections below the
         * 75% confidence threshold.
         */
        if (detection == null && !isProcessing) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom =
                            42.dp +
                                    WindowInsets
                                        .navigationBars
                                        .asPaddingValues()
                                        .calculateBottomPadding()
                    )
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        Color.Black.copy(alpha = 0.65f)
                    )
                    .padding(
                        horizontal = 18.dp,
                        vertical = 10.dp
                    )
            ) {

                Text(
                    text = "No disease detected",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        /*
         * Camera/detection error.
         */
        cameraError?.let { message ->

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(
                        Color.Black.copy(alpha = 0.88f)
                    )
                    .padding(
                        horizontal = 22.dp,
                        vertical = 20.dp
                    )
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Live Camera Error",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(
                        modifier = Modifier.size(8.dp)
                    )

                    Text(
                        text = message,
                        color =
                            Color.White.copy(
                                alpha = 0.78f
                            ),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    if (!hasCameraPermission) {

                        Spacer(
                            modifier = Modifier.size(16.dp)
                        )

                        Text(
                            text = "Allow Camera",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                                .background(
                                    Color(0xFF2E7D32)
                                )
                                .clickable {
                                    cameraPermissionLauncher
                                        .launch(
                                            Manifest.permission.CAMERA
                                        )
                                }
                                .padding(
                                    horizontal = 22.dp,
                                    vertical = 12.dp
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetectionOverlay(
    detection: YOLO11mLiteRTDetector.Detection,
    previewWidth: Int,
    previewHeight: Int
) {

    if (
        previewWidth <= 0 ||
        previewHeight <= 0
    ) {
        return
    }

    val box = detection.boundingBox

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        val scaleX =
            size.width / 640f

        val scaleY =
            size.height / 640f

        val left =
            box.left * scaleX

        val top =
            box.top * scaleY

        val right =
            box.right * scaleX

        val bottom =
            box.bottom * scaleY

        drawRect(
            color = Color.Green,
            topLeft = androidx.compose.ui.geometry.Offset(
                left,
                top
            ),
            size = androidx.compose.ui.geometry.Size(
                right - left,
                bottom - top
            ),
            style = Stroke(
                width = 5f
            )
        )
    }
}

private fun formatDiseaseName(
    name: String
): String {

    return name
        .split("_")
        .joinToString(" ") { word ->
            word.replaceFirstChar {
                it.uppercase()
            }
        }
}

private fun androidx.camera.core.ImageProxy.toBitmap():
        android.graphics.Bitmap? {

    if (
        format != android.graphics.PixelFormat.RGBA_8888
    ) {
        return null
    }

    val bitmap =
        android.graphics.Bitmap.createBitmap(
            width,
            height,
            android.graphics.Bitmap.Config.ARGB_8888
        )

    val buffer = planes[0].buffer

    buffer.rewind()

    bitmap.copyPixelsFromBuffer(buffer)

    return bitmap
}