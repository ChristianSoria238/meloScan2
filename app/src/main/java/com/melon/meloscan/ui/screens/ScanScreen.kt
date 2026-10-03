package com.melon.meloscan.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.melon.meloscan.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import java.io.File
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate

@Composable
fun ScanScreen(
    navController: NavController,
    scanType: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isPreview = LocalInspectionMode.current

    val isLeafDisease = scanType == "Leaf Disease"

    val instructionText = if (isLeafDisease) {
        "Position a watermelon leaf inside the frame"
    } else {
        "Position the watermelon fruit inside the frame"
    }

    val helperText = if (isLeafDisease) {
        "Make sure the diseased leaf area is clearly visible and well lit."
    } else {
        "Capture the whole fruit so its color, shape, size, and stripe pattern are visible."
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isCapturing by remember {
        mutableStateOf(false)
    }

    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasCameraPermission = granted

            if (!granted) {
                cameraError = "Camera permission is required to take a photo."
            } else {
                cameraError = null
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->

            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            isCapturing = true

            scope.launch {
                try {
                    val cachedUri = copyUriToCache(
                        context = context,
                        sourceUri = uri
                    )

                    val encodedUri = Uri.encode(cachedUri.toString())
                    val encodedType = Uri.encode(scanType)

                    navController.navigate(
                        "analyzing?type=$encodedType&uri=$encodedUri"
                    )
                } catch (e: Exception) {
                    cameraError =
                        "Unable to prepare the selected image. Please try again."
                } finally {
                    isCapturing = false
                }
            }
        }

    /*
     * Ask for camera permission when the screen first opens.
     */
    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !isPreview) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /*
     * Initialize CameraX only after permission has been granted.
     */
    DisposableEffect(
        hasCameraPermission,
        lifecycleOwner,
        isPreview
    ) {
        if (!hasCameraPermission || isPreview) {
            onDispose {
                cameraProvider?.unbindAll()
            }
        } else {
            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)

            val executor =
                ContextCompat.getMainExecutor(context)

            val listener = Runnable {
                try {
                    val provider = cameraProviderFuture.get()

                    cameraProvider = provider

                    provider.unbindAll()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(
                                previewView.surfaceProvider
                            )
                        }

                    imageCapture.targetRotation =
                        previewView.display?.rotation
                            ?: Surface.ROTATION_0

                    preview.targetRotation =
                        previewView.display?.rotation
                            ?: Surface.ROTATION_0

                    val cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA

                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    cameraError = null

                } catch (e: Exception) {
                    cameraError =
                        "Unable to start the camera on this device."
                }
            }

            cameraProviderFuture.addListener(
                listener,
                executor
            )

            onDispose {
                cameraProvider?.unbindAll()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        /*
         * CAMERA PREVIEW
         */
        if (hasCameraPermission && !isPreview) {

            AndroidView(
                factory = {
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            /*
             * Android Studio Preview placeholder.
             */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111111)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera Preview",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }

        /*
         * TOP GRADIENT
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
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
         * TOP BAR
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            /*
             * CLOSE BUTTON
             */
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Black.copy(alpha = 0.55f)
                    )
                    .clickable(enabled = !isCapturing) {
                        navController.popBackStack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "×",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light
                )
            }

            /*
             * TITLE
             */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isLeafDisease) {
                        "Leaf Disease Detection"
                    } else {
                        "Fruit Quality Evaluation"
                    },
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "Camera",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }

            /*
             * BALANCING SPACE
             */
            Spacer(
                modifier = Modifier.size(44.dp)
            )
        }

        /*
         * CENTER SCANNING AREA
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 130.dp,
                    bottom = 220.dp,
                    start = 34.dp,
                    end = 34.dp
                ),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        if (isLeafDisease) {
                            330.dp
                        } else {
                            280.dp
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(28.dp)
                    )
            )

            /*
             * INNER CORNER GUIDES
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        if (isLeafDisease) {
                            330.dp
                        } else {
                            280.dp
                        }
                    )
                    .padding(2.dp)
            ) {

                ScanCorner(
                    modifier = Modifier.align(
                        Alignment.TopStart
                    )
                )

                ScanCorner(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .rotate(90f)
                )

                ScanCorner(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .rotate(180f)
                )

                ScanCorner(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .rotate(270f)
                )
            }
        }

        /*
         * INSTRUCTION CARD
         */
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = 112.dp,
                    start = 48.dp,
                    end = 48.dp
                )
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Color.Black.copy(alpha = 0.58f)
                )
                .padding(
                    horizontal = 18.dp,
                    vertical = 13.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = instructionText,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = helperText,
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }

        /*
         * CAMERA ERROR
         */
        cameraError?.let { message ->

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Color.Black.copy(alpha = 0.82f)
                    )
                    .padding(20.dp)
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    if (!hasCameraPermission) {

                        Text(
                            text = "Allow Camera",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(10.dp)
                                )
                                .background(
                                    MaterialTheme.colorScheme.primary
                                )
                                .clickable {
                                    cameraPermissionLauncher.launch(
                                        Manifest.permission.CAMERA
                                    )
                                }
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 10.dp
                                )
                        )
                    }
                }
            }
        }

        /*
         * BOTTOM CONTROL AREA
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        /*
         * BOTTOM INSTRUCTIONS + CONTROLS
         */
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 30.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (isLeafDisease) {
                    "Capture a clear image of the leaf"
                } else {
                    "Capture the whole watermelon fruit"
                },
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                /*
                 * GALLERY BUTTON
                 */
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(alpha = 0.16f)
                        )
                        .clickable(
                            enabled = !isCapturing
                        ) {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "▣",
                        color = Color.White,
                        fontSize = 24.sp
                    )
                }

                /*
                 * MAIN SHUTTER BUTTON
                 */
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(alpha = 0.22f)
                        )
                        .padding(7.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(
                            enabled =
                                hasCameraPermission &&
                                        !isCapturing &&
                                        cameraError == null
                        ) {

                            val capture =
                                imageCapture

                            isCapturing = true
                            cameraError = null

                            val photoFile =
                                File.createTempFile(
                                    "meloscan_capture_",
                                    ".jpg",
                                    context.cacheDir
                                )

                            val outputOptions =
                                ImageCapture.OutputFileOptions
                                    .Builder(photoFile)
                                    .build()

                            capture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(
                                    context
                                ),
                                object :
                                    ImageCapture.OnImageSavedCallback {

                                    override fun onImageSaved(
                                        output:
                                        ImageCapture.OutputFileResults
                                    ) {

                                        val uri =
                                            Uri.fromFile(
                                                photoFile
                                            )

                                        val encodedUri =
                                            Uri.encode(
                                                uri.toString()
                                            )

                                        val encodedType =
                                            Uri.encode(
                                                scanType
                                            )

                                        isCapturing = false

                                        navController.navigate(
                                            "analyzing?type=$encodedType&uri=$encodedUri"
                                        )
                                    }

                                    override fun onError(
                                        exception:
                                        ImageCaptureException
                                    ) {

                                        isCapturing = false

                                        cameraError =
                                            "Photo capture failed. Please try again."
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {

                    if (isCapturing) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(34.dp),
                            strokeWidth = 3.dp
                        )

                    } else {

                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primary
                                )
                        )
                    }
                }

                /*
                 * SECONDARY BALANCING SPACE
                 */
                Spacer(
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Gallery",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.sp
            )
        }
    }
}

/*
 * Scanner corner guide.
 */
@Composable
private fun ScanCorner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .border(
                width = 4.dp,
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
    )
}

/*
 * Copies a selected gallery image into the app cache.
 *
 * This gives both camera images and gallery images
 * the same app-controlled file format/location before
 * they are sent to the analyzing/backend stage.
 */
private suspend fun copyUriToCache(
    context: Context,
    sourceUri: Uri
): Uri = withContext(Dispatchers.IO) {

    val destinationFile =
        File.createTempFile(
            "meloscan_gallery_",
            ".jpg",
            context.cacheDir
        )

    context.contentResolver
        .openInputStream(sourceUri)
        ?.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        ?: throw IllegalStateException(
            "Unable to open selected image."
        )

    Uri.fromFile(destinationFile)
}