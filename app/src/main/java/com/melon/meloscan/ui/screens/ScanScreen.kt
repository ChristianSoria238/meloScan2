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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ScanScreen(
    navController: NavController,
    scanType: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isPreview = LocalInspectionMode.current

    val isLeafDisease = scanType == "Leaf Disease"

    val scope = rememberCoroutineScope()

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

    /*
     * CAMERA PREVIEW
     */
    val previewView = remember {
        PreviewView(context).apply {
            scaleType =
                PreviewView.ScaleType.FILL_CENTER

            implementationMode =
                PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    /*
     * IMAGE CAPTURE
     */
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(
                ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
            )
            .build()
    }

    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }

    /*
     * CAMERA PERMISSION
     */
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            hasCameraPermission = granted

            cameraError =
                if (granted) {
                    null
                } else {
                    "Camera access is needed to scan."
                }
        }

    /*
     * GALLERY PICKER
     */
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->

            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            isCapturing = true

            scope.launch {

                try {

                    val cachedUri =
                        copyUriToCache(
                            context = context,
                            sourceUri = uri
                        )

                    val encodedUri =
                        Uri.encode(
                            cachedUri.toString()
                        )

                    val encodedType =
                        Uri.encode(scanType)

                    navController.navigate(
                        "analyzing?type=$encodedType&uri=$encodedUri"
                    )

                } catch (e: Exception) {

                    cameraError =
                        "The selected photo could not be opened."

                } finally {

                    isCapturing = false
                }
            }
        }

    /*
     * REQUEST CAMERA PERMISSION
     */
    LaunchedEffect(Unit) {

        if (
            !hasCameraPermission &&
            !isPreview
        ) {
            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    /*
     * INITIALIZE CAMERAX
     */
    DisposableEffect(
        hasCameraPermission,
        lifecycleOwner,
        isPreview
    ) {

        if (
            !hasCameraPermission ||
            isPreview
        ) {

            onDispose {
                cameraProvider?.unbindAll()
            }

        } else {

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(
                    context
                )

            val executor =
                ContextCompat.getMainExecutor(
                    context
                )

            val listener = Runnable {

                try {

                    val provider =
                        cameraProviderFuture.get()

                    cameraProvider = provider

                    provider.unbindAll()

                    val preview =
                        Preview.Builder()
                            .build()
                            .also {

                                it.setSurfaceProvider(
                                    previewView.surfaceProvider
                                )
                            }

                    val rotation =
                        previewView.display?.rotation
                            ?: Surface.ROTATION_0

                    imageCapture.targetRotation =
                        rotation

                    preview.targetRotation =
                        rotation

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
                        "Unable to start the camera."

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

    /*
     * MAIN SCREEN
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        /*
         * CAMERA PREVIEW
         */
        if (
            hasCameraPermission &&
            !isPreview
        ) {

            AndroidView(
                factory = {
                    previewView
                },
                modifier =
                    Modifier.fillMaxSize()
            )

        } else {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFF111111)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "Camera Preview",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }

        /*
         * TOP GRADIENT
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(
                                alpha = 0.72f
                            ),
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
                    horizontal = 18.dp,
                    vertical = 14.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            /*
             * BACK BUTTON
             */
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Black.copy(
                            alpha = 0.45f
                        )
                    )
                    .clickable(
                        enabled = !isCapturing
                    ) {
                        navController.popBackStack()
                    },
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ArrowBack,
                    contentDescription =
                        "Back",
                    tint = Color.White,
                    modifier =
                        Modifier.size(23.dp)
                )
            }

            /*
             * TITLE
             */
            Text(
                text =
                    if (isLeafDisease) {
                        "Watermelon Leaf Disease"
                    } else {
                        "Watermelon Fruit Quality"
                    },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.weight(1f)
            )

            /*
             * BALANCING SPACE
             */
            Spacer(
                modifier =
                    Modifier.size(46.dp)
            )
        }

        /*
         * SIMPLE INSTRUCTION
         */
        Text(
            text =
                if (isLeafDisease) {
                    "Place the leaf inside the frame"
                } else {
                    "Place the fruit inside the frame"
                },
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = 88.dp,
                        start = 24.dp,
                        end = 24.dp
                    )
        )

        /*
         * RESPONSIVE CAMERA FRAME
         *
         * Only four L-shaped corners.
         */
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = 145.dp,
                        bottom =
                            175.dp +
                                    WindowInsets
                                        .navigationBars
                                        .asPaddingValues()
                                        .calculateBottomPadding()
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            val frameWidth =
                maxWidth * 0.84f

            val frameHeight =
                if (isLeafDisease) {
                    frameWidth * 0.92f
                } else {
                    frameWidth * 0.68f
                }

            Box(
                modifier =
                    Modifier
                        .width(frameWidth)
                        .height(frameHeight)
            ) {

                /*
                 * TOP LEFT
                 */
                ScanCorner(
                    modifier =
                        Modifier.align(
                            Alignment.TopStart
                        )
                )

                /*
                 * TOP RIGHT
                 */
                ScanCorner(
                    modifier =
                        Modifier
                            .align(
                                Alignment.TopEnd
                            )
                            .rotate(90f)
                )

                /*
                 * BOTTOM RIGHT
                 */
                ScanCorner(
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .rotate(180f)
                )

                /*
                 * BOTTOM LEFT
                 */
                ScanCorner(
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomStart
                            )
                            .rotate(270f)
                )
            }
        }

        /*
         * CAMERA ERROR
         */
        cameraError?.let { message ->

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(
                        horizontal = 28.dp
                    )
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(
                        Color.Black.copy(
                            alpha = 0.88f
                        )
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
                        text = "Camera unavailable",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight =
                            FontWeight.Bold,
                        textAlign =
                            TextAlign.Center
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text = message,
                        color =
                            Color.White.copy(
                                alpha = 0.78f
                            ),
                        fontSize = 13.sp,
                        textAlign =
                            TextAlign.Center
                    )

                    if (!hasCameraPermission) {

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Text(
                            text = "Allow Camera",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight =
                                FontWeight.Bold,
                            modifier =
                                Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                    )
                                    .background(
                                        MaterialTheme
                                            .colorScheme
                                            .primary
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

        /*
         * BOTTOM GRADIENT
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
                .align(
                    Alignment.BottomCenter
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(
                                alpha = 0.90f
                            )
                        )
                    )
                )
        )

        /*
         * BOTTOM CONTROLS
         *
         * Gallery label is now directly
         * underneath the Gallery icon.
         *
         * Tap to capture is directly
         * underneath the capture button.
         */
        Row(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .fillMaxWidth()
                    .padding(
                        bottom =
                            10.dp +
                                    WindowInsets
                                        .navigationBars
                                        .asPaddingValues()
                                        .calculateBottomPadding()
                    ),
            verticalAlignment =
                Alignment.Top,
            horizontalArrangement =
                Arrangement.Center
        ) {

            /*
             * GALLERY CONTROL
             */
            Column(
                modifier =
                    Modifier.width(58.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                /*
                 * GALLERY ICON
                 */
                Box(
                    modifier =
                        Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(
                                Color.Black.copy(
                                    alpha = 0.48f
                                )
                            )
                            .border(
                                width = 1.dp,
                                color =
                                    Color.White.copy(
                                        alpha = 0.35f
                                    ),
                                shape =
                                    CircleShape
                            )
                            .clickable(
                                enabled =
                                    !isCapturing
                            ) {

                                galleryLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts
                                            .PickVisualMedia
                                            .ImageOnly
                                    )
                                )
                            },
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "▣",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                /*
                 * GALLERY LABEL
                 */
                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                Text(
                    text = "Gallery",
                    color =
                        Color.White.copy(
                            alpha = 0.82f
                        ),
                    fontSize = 11.sp,
                    fontWeight =
                        FontWeight.Medium,
                    textAlign =
                        TextAlign.Center
                )
            }

            /*
             * SPACE BETWEEN GALLERY
             * AND CAPTURE BUTTON
             */
            Spacer(
                modifier =
                    Modifier.width(48.dp)
            )

            /*
             * CAPTURE CONTROL
             */
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                /*
                 * CAPTURE BUTTON
                 */
                Box(
                    modifier =
                        Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(
                                Color.White.copy(
                                    alpha = 0.25f
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .padding(7.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            )
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
                                    ImageCapture
                                        .OutputFileOptions
                                        .Builder(
                                            photoFile
                                        )
                                        .build()

                                capture.takePicture(
                                    outputOptions,
                                    ContextCompat
                                        .getMainExecutor(
                                            context
                                        ),
                                    object :
                                        ImageCapture
                                        .OnImageSavedCallback {

                                        override fun onImageSaved(
                                            output:
                                            ImageCapture
                                            .OutputFileResults
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

                                            isCapturing =
                                                false

                                            navController
                                                .navigate(
                                                    "analyzing?" +
                                                            "type=$encodedType" +
                                                            "&uri=$encodedUri"
                                                )
                                        }

                                        override fun onError(
                                            exception:
                                            ImageCaptureException
                                        ) {

                                            isCapturing =
                                                false

                                            cameraError =
                                                "Photo capture failed. Please try again."
                                        }
                                    }
                                )
                            },
                    contentAlignment =
                        Alignment.Center
                ) {

                    if (isCapturing) {

                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(32.dp),
                            strokeWidth = 3.dp,
                            color = Color.White
                        )

                    } else {

                        Box(
                            modifier =
                                Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Color.White
                                    )
                        )
                    }
                }

                /*
                 * TAP TO CAPTURE LABEL
                 */
                Spacer(
                    modifier =
                        Modifier.height(7.dp)
                )

                Text(
                    text =
                        if (isCapturing) {
                            "Capturing..."
                        } else {
                            "Tap to capture"
                        },
                    color =
                        Color.White.copy(
                            alpha = 0.88f
                        ),
                    fontSize = 12.sp,
                    fontWeight =
                        FontWeight.Medium,
                    textAlign =
                        TextAlign.Center
                )
            }

            /*
             * BALANCING SPACE
             *
             * Same width as Gallery control,
             * keeping capture button centered.
             */
            Spacer(
                modifier =
                    Modifier
                        .width(58.dp)
            )
        }
    }
}

/*
 * CAMERA FRAME CORNER
 *
 * Creates an L-shaped corner.
 * There is no surrounding rectangle.
 */
@Composable
private fun ScanCorner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier.size(42.dp)
    ) {

        /*
         * VERTICAL LINE
         */
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .height(30.dp)
                    .align(
                        Alignment.TopStart
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 6.dp
                        )
                    )
                    .background(
                        Color.White
                    )
        )

        /*
         * HORIZONTAL LINE
         */
        Box(
            modifier =
                Modifier
                    .width(30.dp)
                    .height(4.dp)
                    .align(
                        Alignment.TopStart
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 6.dp
                        )
                    )
                    .background(
                        Color.White
                    )
        )
    }
}

/*
 * COPIES A GALLERY IMAGE INTO CACHE
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

            destinationFile
                .outputStream()
                .use { output ->

                    input.copyTo(output)
                }
        }
        ?: throw IllegalStateException(
            "Unable to open selected image."
        )

    Uri.fromFile(destinationFile)
}