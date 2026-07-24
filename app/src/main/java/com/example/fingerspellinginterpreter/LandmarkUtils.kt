package com.example.fingerspellinginterpreter

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.sqrt

fun normalizeLandmarks(landmarks: List<NormalizedLandmark>): FloatArray {
    val points = Array(21) { i -> floatArrayOf(landmarks[i].x(), landmarks[i].y(), landmarks[i].z()) }

    val wrist = points[0]
    val middleMcp = points[9]

    val dx = middleMcp[0] - wrist[0]
    val dy = middleMcp[1] - wrist[1]
    val dz = middleMcp[2] - wrist[2]
    var scale = sqrt(dx * dx + dy * dy + dz * dz)
    if (scale < 1e-6f) scale = 1e-6f

    val output = FloatArray(63)
    var idx = 0
    for (p in points) {
        output[idx++] = (p[0] - wrist[0]) / scale
        output[idx++] = (p[1] - wrist[1]) / scale
        output[idx++] = (p[2] - wrist[2]) / scale
    }
    return output
}