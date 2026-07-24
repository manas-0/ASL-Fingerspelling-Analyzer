package com.example.fingerspellinginterpreter

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.concurrent.Executors

private const val CONFIDENCE_THRESHOLD = 0.75f
private const val SMOOTHING_WINDOW = 8
private const val AGREEMENT_RATIO = 0.7

@Composable
fun CameraPreviewScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var landmarkResult by remember { mutableStateOf<HandLandmarkerResult?>(null) }
    var predictedLetter by remember { mutableStateOf("") }
    var confidence by remember { mutableStateOf(0f) }
    var stableLetter by remember { mutableStateOf("") }
    val recentPredictions = remember { mutableStateListOf<String>() }

    val handLandmarkerHelper = remember {
        HandLandmarkerHelper(context) { result -> landmarkResult = result }
    }
    val classifier = remember { LetterClassifier(context) }

    // Runs every time a new detection result comes in from the camera.
    LaunchedEffect(landmarkResult) {
        landmarkResult?.let { result ->
            if (result.landmarks().isNotEmpty()) {
                val features = normalizeLandmarks(result.landmarks()[0])
                val (letter, conf) = classifier.predict(features)
                predictedLetter = letter
                confidence = conf

                if (conf >= CONFIDENCE_THRESHOLD) {
                    recentPredictions.add(letter)
                    if (recentPredictions.size > SMOOTHING_WINDOW) {
                        recentPredictions.removeAt(0)
                    }
                    // Only "commit" to a letter if it dominates the recent window -
                    // this is what prevents a single bad-angle frame from flashing
                    // a wrong letter on screen.
                    val mostCommon = recentPredictions
                        .groupingBy { it }
                        .eachCount()
                        .maxByOrNull { it.value }

                    if (mostCommon != null && mostCommon.value >= SMOOTHING_WINDOW * AGREEMENT_RATIO) {
                        stableLetter = mostCommon.key
                    }
                } else {
                    // low-confidence frame - don't let it pollute the smoothing window
                }
            } else {
                predictedLetter = ""
                recentPredictions.clear()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { handLandmarkerHelper.close() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val analysisExecutor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()

                    imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        val rotation = imageProxy.imageInfo.rotationDegrees
                        val bitmap = imageProxy.toBitmap().rotate(rotation)
                        handLandmarkerHelper.detectAsync(bitmap, System.currentTimeMillis())
                        imageProxy.close()
                    }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Draw the 21 landmark dots over the camera preview
        landmarkResult?.let { result ->
            if (result.landmarks().isNotEmpty()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val hand = result.landmarks()[0]
                    for (point in hand) {
                        val x = size.width - (point.x() * size.width)
                        val y = point.y() * size.height
                        drawCircle(color = Color.Green, radius = 8f, center = Offset(x, y))
                    }
                }
            }
        }

        // Smoothed, trustworthy letter - this is the one to actually rely on
        if (stableLetter.isNotEmpty()) {
            Text(
                text = stableLetter,
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            )
        }

        // Raw per-frame prediction + confidence - useful for debugging while we build,
        // safe to remove later once you trust the smoothed version
        if (predictedLetter.isNotEmpty()) {
            Text(
                text = "raw: $predictedLetter (${(confidence * 100).toInt()}%)",
                color = Color.Yellow,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 120.dp)
            )
        }
    }
}

private fun ImageProxy.toBitmap(): Bitmap {
    val plane = planes[0]
    val buffer = plane.buffer
    val pixelStride = plane.pixelStride
    val rowStride = plane.rowStride
    val rowPadding = rowStride - pixelStride * width

    val bitmap = Bitmap.createBitmap(
        width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888
    )
    bitmap.copyPixelsFromBuffer(buffer)
    return if (rowPadding == 0) bitmap else Bitmap.createBitmap(bitmap, 0, 0, width, height)
}

private fun Bitmap.rotate(degrees: Int): Bitmap {
    if (degrees == 0) return this
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}