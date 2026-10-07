package com.melon.meloscan.ml.fruitquality

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer

class FruitQualityONNXTester(
    private val context: Context
) {

    companion object {
        private const val TAG = "FruitQualityONNX"
        private const val MODEL_NAME = "random_forest_classifier.onnx"
        private const val FEATURE_COUNT = 1993
    }

    private val environment: OrtEnvironment =
        OrtEnvironment.getEnvironment()

    private val session: OrtSession by lazy {
        val modelBytes = context.assets
            .open(MODEL_NAME)
            .use { it.readBytes() }

        environment.createSession(
            modelBytes,
            OrtSession.SessionOptions()
        )
    }

    fun predict(bitmap: Bitmap): PredictionResult {

        Log.d(TAG, "===== RANDOM FOREST ONNX TEST =====")

        // ---------------------------------------------------------
        // 1. Extract the exact 1,993 features
        // ---------------------------------------------------------
        val features =
            FruitQualityFeatureExtractor.extractAllFeatures(bitmap)

        Log.d(TAG, "Feature count: ${features.size}")
        Log.d(TAG, "Expected feature count: $FEATURE_COUNT")

        require(features.size == FEATURE_COUNT) {
            "Invalid feature count: ${features.size}, expected $FEATURE_COUNT"
        }

        // ---------------------------------------------------------
        // 2. Feature diagnostics
        // ---------------------------------------------------------
        var featureSum = 0.0
        var featureMin = Float.POSITIVE_INFINITY
        var featureMax = Float.NEGATIVE_INFINITY

        for (value in features) {
            featureSum += value.toDouble()

            if (value < featureMin) {
                featureMin = value
            }

            if (value > featureMax) {
                featureMax = value
            }
        }

        Log.d(TAG, "Feature sum: $featureSum")
        Log.d(TAG, "Feature minimum: $featureMin")
        Log.d(TAG, "Feature maximum: $featureMax")

        // ---------------------------------------------------------
        // 3. Create ONNX input tensor
        // ---------------------------------------------------------
        val featureArray = features.copyOf()

        val inputTensor =
            OnnxTensor.createTensor(
                environment,
                FloatBuffer.wrap(featureArray),
                longArrayOf(
                    1L,
                    FEATURE_COUNT.toLong()
                )
            )

        try {

            // ---------------------------------------------------------
            // 4. Get ONNX input name
            // ---------------------------------------------------------
            val inputName =
                session.inputNames.first()

            Log.d(
                TAG,
                "ONNX input name: $inputName"
            )

            val inputs =
                mapOf(
                    inputName to inputTensor
                )

            // ---------------------------------------------------------
            // 5. Run Random Forest ONNX
            // ---------------------------------------------------------
            val result = session.run(inputs)

            try {

                // ---------------------------------------------------------
                // 6. Read predicted class
                // ---------------------------------------------------------
                val labelOutput =
                    result[0].value

                Log.d(
                    TAG,
                    "Raw label output type: " +
                            "${labelOutput?.javaClass?.name}"
                )

                val predictedClass =
                    extractPredictedClass(labelOutput)

                // ---------------------------------------------------------
                // 7. Read probability output
                // ---------------------------------------------------------
                var probabilities: FloatArray? = null

                if (result.count() > 1) {

                    val probabilityOutput =
                        result[1].value

                    Log.d(
                        TAG,
                        "Raw probability output type: " +
                                "${probabilityOutput?.javaClass?.name}"
                    )

                    probabilities =
                        extractProbability(probabilityOutput)
                }

                // ---------------------------------------------------------
                // 8. Convert class ID to name
                // ---------------------------------------------------------
                val predictedName =
                    when (predictedClass) {
                        0 -> "Bad"
                        1 -> "Good"
                        else -> "Unknown"
                    }

                // ---------------------------------------------------------
                // 9. Log final prediction
                // ---------------------------------------------------------
                Log.d(
                    TAG,
                    "Predicted class: $predictedClass"
                )

                Log.d(
                    TAG,
                    "Predicted result: $predictedName"
                )

                probabilities?.let { values ->
                    Log.d(
                        TAG,
                        "Probability array size: ${values.size}"
                    )

                    Log.d(
                        TAG,
                        "Probability Bad: ${values.getOrNull(0)}"
                    )

                    Log.d(
                        TAG,
                        "Probability Good: ${values.getOrNull(1)}"
                    )
                }

                Log.d(
                    TAG,
                    "===== END RANDOM FOREST ONNX TEST ====="
                )

                return PredictionResult(
                    classId = predictedClass,
                    label = predictedName,
                    probabilities = probabilities
                )

            } finally {
                result.close()
            }

        } finally {
            inputTensor.close()
        }
    }

    // =============================================================
    // PREDICTED CLASS EXTRACTION
    // =============================================================
    private fun extractPredictedClass(
        output: Any?
    ): Int {

        return when (output) {

            is LongArray -> {
                output.firstOrNull()?.toInt() ?: -1
            }

            is IntArray -> {
                output.firstOrNull() ?: -1
            }

            is Array<*> -> {
                val first = output.firstOrNull()

                when (first) {
                    is LongArray ->
                        first.firstOrNull()?.toInt() ?: -1

                    is IntArray ->
                        first.firstOrNull() ?: -1

                    is Long ->
                        first.toInt()

                    is Int ->
                        first

                    is Number ->
                        first.toInt()

                    else ->
                        -1
                }
            }

            is Number -> {
                output.toInt()
            }

            else -> {
                -1
            }
        }
    }

    // =============================================================
    // PROBABILITY EXTRACTION
    // =============================================================
    private fun extractProbability(
        output: Any?
    ): FloatArray? {

        Log.d(
            TAG,
            "Probability object class: " +
                    "${output?.javaClass?.name}"
        )

        if (output == null) {
            Log.e(TAG, "Probability output is null")
            return null
        }

        // The Random Forest ONNX export normally returns:
        // sequence(map(int64,float))
        // which ONNX Runtime exposes as a List containing OnnxMap.
        if (output is List<*>) {

            Log.d(
                TAG,
                "Probability output list size: ${output.size}"
            )

            if (output.isEmpty()) {
                Log.e(TAG, "Probability output list is empty")
                return null
            }

            val first = output[0]

            Log.d(
                TAG,
                "Probability first element type: " +
                        "${first?.javaClass?.name}"
            )

            if (first is ai.onnxruntime.OnnxMap) {

                Log.d(
                    TAG,
                    "Detected ONNX Runtime OnnxMap"
                )

                val map = first.getValue()

                Log.d(
                    TAG,
                    "OnnxMap value type: " +
                            "${map?.javaClass?.name}"
                )

                return extractProbabilityFromMap(map)
            }

            if (first is Map<*, *>) {

                Log.d(
                    TAG,
                    "Detected normal Map"
                )

                return extractProbabilityFromMap(first)
            }
        }

        // Some ONNX Runtime versions/wrappers may expose the map directly.
        if (output is ai.onnxruntime.OnnxMap) {

            Log.d(
                TAG,
                "Detected direct ONNX Runtime OnnxMap"
            )

            return extractProbabilityFromMap(
                output.getValue()
            )
        }

        if (output is Map<*, *>) {

            Log.d(
                TAG,
                "Detected direct normal Map"
            )

            return extractProbabilityFromMap(output)
        }

        Log.e(
            TAG,
            "Unsupported probability output type: " +
                    output.javaClass.name
        )

        return null
    }

    private fun extractProbabilityFromMap(
        map: Any?
    ): FloatArray? {

        if (map !is Map<*, *>) {

            Log.e(
                TAG,
                "Probability map is not a Map: " +
                        "${map?.javaClass?.name}"
            )

            return null
        }

        val probabilities = FloatArray(2)

        for ((key, value) in map) {

            Log.d(
                TAG,
                "Probability entry: key=$key value=$value"
            )

            val classId =
                when (key) {
                    is Long -> key.toInt()
                    is Int -> key
                    is Short -> key.toInt()
                    is Byte -> key.toInt()
                    is Number -> key.toInt()
                    else -> continue
                }

            val probability =
                when (value) {
                    is Float -> value
                    is Double -> value.toFloat()
                    is Number -> value.toFloat()
                    else -> continue
                }

            if (classId in probabilities.indices) {
                probabilities[classId] = probability
            }
        }

        Log.d(
            TAG,
            "Extracted probability Bad: ${probabilities.getOrNull(0)}"
        )

        Log.d(
            TAG,
            "Extracted probability Good: ${probabilities.getOrNull(1)}"
        )

        return probabilities
    }

    // =============================================================
    // RESULT DATA CLASS
    // =============================================================
    data class PredictionResult(
        val classId: Int,
        val label: String,
        val probabilities: FloatArray?
    )

    // =============================================================
    // CLOSE SESSION
    // =============================================================
    fun close() {
        session.close()
    }
}
