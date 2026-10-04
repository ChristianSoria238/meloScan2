package com.melon.meloscan.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import kotlin.math.max
import kotlin.math.min

class YOLO11mLiteRTDetector(
    private val context: Context
) {

    companion object {
        private const val INPUT_SIZE = 640
        private const val NUM_CLASSES = 3
        private const val NUM_CANDIDATES = 8400

        private const val CONFIDENCE_THRESHOLD = 0.25f
        private const val IOU_THRESHOLD = 0.45f

        private val CLASS_NAMES = arrayOf(
            "anthracnose",
            "downy_mildew",
            "mosaic_disease"
        )
    }

    data class Detection(
        val classIndex: Int,
        val className: String,
        val confidence: Float,
        val boundingBox: RectF
    )

    private val model = CompiledModel.create(
        context.assets,
        "yolo11m.tflite",
        CompiledModel.Options(Accelerator.CPU),
        null
    )

    private val inputBuffers = model.createInputBuffers()
    private val outputBuffers = model.createOutputBuffers()

    /**
     * Runs YOLO11m detection on a Bitmap.
     *
     * The returned bounding boxes use the coordinate
     * system of the original Bitmap.
     */
    fun detect(bitmap: Bitmap): List<Detection> {

        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        val resizedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            INPUT_SIZE,
            INPUT_SIZE,
            true
        )

        val inputData = FloatArray(
            1 * 3 * INPUT_SIZE * INPUT_SIZE
        )

        val pixels = IntArray(
            INPUT_SIZE * INPUT_SIZE
        )

        resizedBitmap.getPixels(
            pixels,
            0,
            INPUT_SIZE,
            0,
            0,
            INPUT_SIZE,
            INPUT_SIZE
        )

        val channelSize =
            INPUT_SIZE * INPUT_SIZE

        /*
         * Convert Android Bitmap:
         *
         * ARGB
         *
         * to YOLO:
         *
         * RGB FLOAT32
         * NCHW
         * 0.0 - 1.0
         */
        for (y in 0 until INPUT_SIZE) {

            for (x in 0 until INPUT_SIZE) {

                val pixelIndex =
                    y * INPUT_SIZE + x

                val pixel =
                    pixels[pixelIndex]

                val red =
                    ((pixel shr 16) and 0xFF) / 255.0f

                val green =
                    ((pixel shr 8) and 0xFF) / 255.0f

                val blue =
                    (pixel and 0xFF) / 255.0f

                inputData[pixelIndex] =
                    red

                inputData[
                    channelSize + pixelIndex
                ] = green

                inputData[
                    (2 * channelSize) + pixelIndex
                ] = blue
            }
        }

        /*
         * Send input to LiteRT.
         */
        inputBuffers[0].writeFloat(
            inputData
        )

        /*
         * Run YOLO11m.
         */
        model.run(
            inputBuffers,
            outputBuffers
        )

        /*
         * Read:
         *
         * [1, 7, 8400]
         *
         * 0 = x center
         * 1 = y center
         * 2 = width
         * 3 = height
         * 4 = Anthracnose
         * 5 = Downy Mildew
         * 6 = Mosaic Disease
         */
        val outputData =
            outputBuffers[0].readFloat()

        val candidates =
            mutableListOf<Detection>()

        /*
         * Decode the 8400 candidates.
         */
        for (
        candidate in 0 until NUM_CANDIDATES
        ) {

            var bestClassIndex = -1
            var bestConfidence = 0f

            /*
             * Find the strongest class
             * for this candidate.
             */
            for (
            classIndex in 0 until NUM_CLASSES
            ) {

                val scoreIndex =
                    ((4 + classIndex) *
                            NUM_CANDIDATES) +
                            candidate

                val score =
                    outputData[scoreIndex]

                if (score > bestConfidence) {

                    bestConfidence = score
                    bestClassIndex = classIndex
                }
            }

            /*
             * Confidence filtering.
             */
            if (
                bestConfidence <
                CONFIDENCE_THRESHOLD
            ) {
                continue
            }

            val xCenter =
                outputData[candidate]

            val yCenter =
                outputData[
                    NUM_CANDIDATES +
                            candidate
                ]

            val width =
                outputData[
                    (2 * NUM_CANDIDATES) +
                            candidate
                ]

            val height =
                outputData[
                    (3 * NUM_CANDIDATES) +
                            candidate
                ]

            /*
             * Convert normalized xywh
             * into 640x640 xyxy.
             */
            val x1 =
                (xCenter - width / 2f) *
                        INPUT_SIZE

            val y1 =
                (yCenter - height / 2f) *
                        INPUT_SIZE

            val x2 =
                (xCenter + width / 2f) *
                        INPUT_SIZE

            val y2 =
                (yCenter + height / 2f) *
                        INPUT_SIZE

            /*
             * Convert the 640x640 coordinates
             * back to the original Bitmap size.
             */
            val scaleX =
                originalWidth.toFloat() /
                        INPUT_SIZE

            val scaleY =
                originalHeight.toFloat() /
                        INPUT_SIZE

            val originalX1 =
                (x1 * scaleX).coerceIn(
                    0f,
                    originalWidth.toFloat()
                )

            val originalY1 =
                (y1 * scaleY).coerceIn(
                    0f,
                    originalHeight.toFloat()
                )

            val originalX2 =
                (x2 * scaleX).coerceIn(
                    0f,
                    originalWidth.toFloat()
                )

            val originalY2 =
                (y2 * scaleY).coerceIn(
                    0f,
                    originalHeight.toFloat()
                )

            candidates.add(
                Detection(
                    classIndex =
                        bestClassIndex,

                    className =
                        CLASS_NAMES[
                            bestClassIndex
                        ],

                    confidence =
                        bestConfidence,

                    boundingBox =
                        RectF(
                            originalX1,
                            originalY1,
                            originalX2,
                            originalY2
                        )
                )
            )
        }

        /*
         * Class-aware NMS.
         */
        val finalDetections =
            mutableListOf<Detection>()

        val grouped =
            candidates.groupBy {
                it.classIndex
            }

        for (
        (_, classDetections)
        in grouped
        ) {

            val remaining =
                classDetections
                    .sortedByDescending {
                        it.confidence
                    }
                    .toMutableList()

            while (
                remaining.isNotEmpty()
            ) {

                val best =
                    remaining.removeAt(0)

                finalDetections.add(best)

                val filtered =
                    remaining.filter { candidate ->

                        calculateIoU(
                            best.boundingBox,
                            candidate.boundingBox
                        ) < IOU_THRESHOLD
                    }

                remaining.clear()
                remaining.addAll(filtered)
            }
        }

        /*
         * Release only the temporary resized bitmap.
         *
         * The original Bitmap belongs to the caller
         * and must NOT be recycled here.
         */
        if (
            resizedBitmap !== bitmap
        ) {
            resizedBitmap.recycle()
        }

        return finalDetections
            .sortedByDescending {
                it.confidence
            }
    }

    /**
     * Calculates Intersection over Union.
     */
    private fun calculateIoU(
        first: RectF,
        second: RectF
    ): Float {

        val intersectionLeft =
            max(
                first.left,
                second.left
            )

        val intersectionTop =
            max(
                first.top,
                second.top
            )

        val intersectionRight =
            min(
                first.right,
                second.right
            )

        val intersectionBottom =
            min(
                first.bottom,
                second.bottom
            )

        val intersectionWidth =
            max(
                0f,
                intersectionRight -
                        intersectionLeft
            )

        val intersectionHeight =
            max(
                0f,
                intersectionBottom -
                        intersectionTop
            )

        val intersectionArea =
            intersectionWidth *
                    intersectionHeight

        val firstArea =
            max(
                0f,
                first.width()
            ) *
                    max(
                        0f,
                        first.height()
                    )

        val secondArea =
            max(
                0f,
                second.width()
            ) *
                    max(
                        0f,
                        second.height()
                    )

        val unionArea =
            firstArea +
                    secondArea -
                    intersectionArea

        if (
            unionArea <= 0f
        ) {
            return 0f
        }

        return intersectionArea /
                unionArea
    }

    /**
     * Releases LiteRT resources.
     */
    fun close() {

        inputBuffers.forEach {
            it.close()
        }

        outputBuffers.forEach {
            it.close()
        }

        model.close()
    }
}