package com.example.fingerspellinginterpreter

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class LetterClassifier(context: Context) {
    private val interpreter: Interpreter
    private val labels: List<String>

    init {
        interpreter = Interpreter(loadModelFile(context, "asl_model.tflite"))
        labels = loadLabels(context)
    }

    fun predict(features: FloatArray): Pair<String, Float> {
        val input = arrayOf(features)
        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(input, output)

        val scores = output[0]
        var maxIdx = 0
        for (i in scores.indices) if (scores[i] > scores[maxIdx]) maxIdx = i
        return labels[maxIdx] to scores[maxIdx]
    }

    private fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
        val fd = context.assets.openFd(filename)
        val stream = FileInputStream(fd.fileDescriptor)
        val channel = stream.channel
        return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }

    private fun loadLabels(context: Context): List<String> {
        val json = context.assets.open("label_classes.json").bufferedReader().use { it.readText() }
        return json.trim().removePrefix("[").removeSuffix("]")
            .split(",").map { it.trim().trim('"') }
    }
}