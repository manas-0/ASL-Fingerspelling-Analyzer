package com.example.fingerspellinginterpreter

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.framework.image.BitmapImageBuilder

class HandLandmarkerHelper(
    context: Context,
    private val onResult: (HandLandmarkerResult) -> Unit
) {
    private var handLandmarker: HandLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setNumHands(1)
            .setMinHandDetectionConfidence(0.5f)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ -> onResult(result) }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    fun detectAsync(bitmap: Bitmap, timestampMs: Long) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        handLandmarker.detectAsync(mpImage, timestampMs)
    }

    fun close() {
        handLandmarker.close()
    }
}