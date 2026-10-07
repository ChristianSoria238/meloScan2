package com.melon.meloscan.ml.fruitquality

import android.graphics.Bitmap
import android.util.Log
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

object FruitQualityFeatureExtractor {

    private const val IMAGE_SIZE = 128

    private const val COLOR_FEATURE_COUNT = 192
    private const val TEXTURE_FEATURE_COUNT = 18
    private const val HOG_FEATURE_COUNT = 1764
    private const val STATISTICAL_FEATURE_COUNT = 19

    private const val FEATURE_COUNT = 1993

    // ============================================================
    // IMAGE PREPARATION
    // ============================================================

    fun validateColorFeatureStages(bitmap: Bitmap): String {
        val image = prepareImage(bitmap)

        val result = StringBuilder()

        result.appendLine("===== COLOR FEATURE DIAGNOSTIC =====")
        result.appendLine("Image: ${image.width}x${image.height}")
        result.appendLine()

        // ------------------------------------------------------------
        // BGR HISTOGRAM FEATURES
        // ------------------------------------------------------------

        val bgrFeatures = mutableListOf<Float>()

        for (channel in 0 until 3) {
            val histogram = FloatArray(32)

            for (y in 0 until IMAGE_SIZE) {
                for (x in 0 until IMAGE_SIZE) {

                    val pixel = image.getPixel(x, y)

                    val value = when (channel) {
                        0 -> pixel and 0xFF
                        1 -> (pixel shr 8) and 0xFF
                        else -> (pixel shr 16) and 0xFF
                    }

                    val bin = ((value * 32) / 256)
                        .coerceIn(0, 31)

                    histogram[bin] += 1f
                }
            }

            // OpenCV normalize(hist, hist) = L2 normalization
            var sumSquares = 0.0

            for (value in histogram) {
                sumSquares += value.toDouble() * value.toDouble()
            }

            val denominator = kotlin.math.sqrt(sumSquares)

            for (i in histogram.indices) {
                histogram[i] =
                    if (denominator > 0.0) {
                        (histogram[i].toDouble() / denominator).toFloat()
                    } else {
                        0f
                    }

                bgrFeatures.add(histogram[i])
            }

            result.appendLine(
                "BGR channel $channel sum: ${
                    histogram.sumOf { it.toDouble() }
                }"
            )
        }

        result.appendLine(
            "BGR total sum: ${
                bgrFeatures.sumOf { it.toDouble() }
            }"
        )

        result.appendLine()

        // ------------------------------------------------------------
        // HSV HISTOGRAM FEATURES
        // ------------------------------------------------------------

        val hsvFeatures = mutableListOf<Float>()

        for (channel in 0 until 3) {
            val histogram = FloatArray(32)

            for (y in 0 until IMAGE_SIZE) {
                for (x in 0 until IMAGE_SIZE) {

                    val pixel = image.getPixel(x, y)

                    val red = ((pixel shr 16) and 0xFF) / 255.0
                    val green = ((pixel shr 8) and 0xFF) / 255.0
                    val blue = (pixel and 0xFF) / 255.0

                    val max = maxOf(red, green, blue)
                    val min = minOf(red, green, blue)
                    val delta = max - min

                    var hue = 0.0

                    if (delta != 0.0) {
                        hue = when (max) {
                            red -> {
                                60.0 * (((green - blue) / delta) % 6.0)
                            }

                            green -> {
                                60.0 * (((blue - red) / delta) + 2.0)
                            }

                            else -> {
                                60.0 * (((red - green) / delta) + 4.0)
                            }
                        }

                        if (hue < 0.0) {
                            hue += 360.0
                        }
                    }

                    val saturation =
                        if (max == 0.0) {
                            0.0
                        } else {
                            delta / max
                        }

                    val value = max

                    val hsvValue = when (channel) {
                        0 -> hue / 2.0       // OpenCV H: 0..180
                        1 -> saturation * 255.0
                        else -> value * 255.0
                    }

                    val maxValue =
                        if (channel == 0) 180.0 else 256.0

                    var bin =
                        kotlin.math.floor(
                            hsvValue * 32.0 / maxValue
                        ).toInt()

                    bin = bin.coerceIn(0, 31)

                    histogram[bin] += 1f
                }
            }

            var sumSquares = 0.0

            for (value in histogram) {
                sumSquares += value.toDouble() * value.toDouble()
            }

            val denominator = kotlin.math.sqrt(sumSquares)

            for (i in histogram.indices) {
                histogram[i] =
                    if (denominator > 0.0) {
                        (histogram[i].toDouble() / denominator).toFloat()
                    } else {
                        0f
                    }

                hsvFeatures.add(histogram[i])
            }

            result.appendLine(
                "HSV channel $channel sum: ${
                    histogram.sumOf { it.toDouble() }
                }"
            )
        }

        result.appendLine(
            "HSV total sum: ${
                hsvFeatures.sumOf { it.toDouble() }
            }"
        )

        result.appendLine()

        result.appendLine(
            "COLOR TOTAL: ${
                (bgrFeatures + hsvFeatures)
                    .sumOf { it.toDouble() }
            }"
        )

        result.appendLine("===== END COLOR DIAGNOSTIC =====")

        return result.toString()
    }

    fun validateColorFeatureBins(bitmap: Bitmap): String {

        val image = prepareImage(bitmap)

        val width = image.width
        val height = image.height

        val result = StringBuilder()

        result.appendLine("===== COLOR BIN VALIDATION =====")
        result.appendLine("Image: ${width}x$height")
        result.appendLine()

        // ---------------------------------------------------------
        // BGR HISTOGRAMS
        // ---------------------------------------------------------

        result.appendLine("===== BGR HISTOGRAMS =====")

        var bgrTotal = 0.0

        for (channel in 0 until 3) {

            val histogram = FloatArray(32)

            for (y in 0 until height) {
                for (x in 0 until width) {

                    val pixel = image.getPixel(x, y)

                    val value = when (channel) {
                        0 -> pixel and 0xFF
                        1 -> (pixel shr 8) and 0xFF
                        else -> (pixel shr 16) and 0xFF
                    }

                    var bin = (value * 32) / 256

                    if (bin >= 32) {
                        bin = 31
                    }

                    histogram[bin] += 1f
                }
            }

            var sumSquares = 0.0

            for (value in histogram) {
                sumSquares += value.toDouble() * value.toDouble()
            }

            val denominator = kotlin.math.sqrt(sumSquares)

            for (i in histogram.indices) {
                histogram[i] =
                    if (denominator > 0.0) {
                        (histogram[i].toDouble() / denominator).toFloat()
                    } else {
                        0f
                    }
            }

            var channelSum = 0.0

            for (value in histogram) {
                channelSum += value.toDouble()
            }

            bgrTotal += channelSum

            result.appendLine("BGR channel $channel")
            result.appendLine("Sum: $channelSum")

            result.append("Bins: ")

            for (i in histogram.indices) {
                result.append(
                    String.format(
                        java.util.Locale.US,
                        "%.8f",
                        histogram[i]
                    )
                )

                if (i < histogram.lastIndex) {
                    result.append(", ")
                }
            }

            result.appendLine()
            result.appendLine()
        }

        result.appendLine("BGR TOTAL: $bgrTotal")
        result.appendLine()

        // ---------------------------------------------------------
        // HSV HISTOGRAMS
        // ---------------------------------------------------------

        result.appendLine("===== HSV HISTOGRAMS =====")

        var hsvTotal = 0.0

        for (channel in 0 until 3) {

            val histogram = FloatArray(32)

            for (y in 0 until height) {
                for (x in 0 until width) {

                    val pixel = image.getPixel(x, y)

                    val red = (pixel shr 16) and 0xFF
                    val green = (pixel shr 8) and 0xFF
                    val blue = pixel and 0xFF

                    val hsv = rgbToOpenCvHsv(
                        red,
                        green,
                        blue
                    )

                    val value = hsv[channel]

                    val maxValue =
                        if (channel == 0) {
                            180
                        } else {
                            256
                        }

                    var bin =
                        kotlin.math.floor(
                            value.toDouble() *
                                    32.0 /
                                    maxValue.toDouble()
                        ).toInt()

                    if (bin >= 32) {
                        bin = 31
                    }

                    if (bin < 0) {
                        bin = 0
                    }

                    histogram[bin] += 1f
                }
            }

            var sumSquares = 0.0

            for (value in histogram) {
                sumSquares += value.toDouble() * value.toDouble()
            }

            val denominator = kotlin.math.sqrt(sumSquares)

            for (i in histogram.indices) {
                histogram[i] =
                    if (denominator > 0.0) {
                        (histogram[i].toDouble() / denominator).toFloat()
                    } else {
                        0f
                    }
            }

            var channelSum = 0.0

            for (value in histogram) {
                channelSum += value.toDouble()
            }

            hsvTotal += channelSum

            result.appendLine("HSV channel $channel")
            result.appendLine("Sum: $channelSum")

            result.append("Bins: ")

            for (i in histogram.indices) {
                result.append(
                    String.format(
                        java.util.Locale.US,
                        "%.8f",
                        histogram[i]
                    )
                )

                if (i < histogram.lastIndex) {
                    result.append(", ")
                }
            }

            result.appendLine()
            result.appendLine()
        }

        result.appendLine("HSV TOTAL: $hsvTotal")
        result.appendLine()

        result.appendLine("COLOR TOTAL: ${bgrTotal + hsvTotal}")
        result.appendLine("===== END COLOR BIN VALIDATION =====")

        return result.toString()
    }
    fun validateHsvPixelConversion(bitmap: Bitmap): String {

        val image = prepareImage(bitmap)

        val width = image.width
        val height = image.height

        val result = StringBuilder()

        result.appendLine("===== HSV PIXEL CONVERSION VALIDATION =====")
        result.appendLine("Image: ${width}x$height")
        result.appendLine()

        val samplePoints = arrayOf(
            intArrayOf(0, 0),
            intArrayOf(1, 1),
            intArrayOf(10, 10),
            intArrayOf(32, 32),
            intArrayOf(64, 64),
            intArrayOf(96, 96),
            intArrayOf(127, 127),
            intArrayOf(64, 10),
            intArrayOf(10, 64),
            intArrayOf(100, 50)
        )

        for (point in samplePoints) {

            val x = point[0]
            val y = point[1]

            val pixel = image.getPixel(x, y)

            val red =
                (pixel shr 16) and 0xFF

            val green =
                (pixel shr 8) and 0xFF

            val blue =
                pixel and 0xFF

            val hsv =
                rgbToOpenCvHsv(
                    red,
                    green,
                    blue
                )

            result.appendLine(
                String.format(
                    java.util.Locale.US,
                    "Pixel (%d,%d) RGB=(%d,%d,%d) -> HSV=(%.6f, %.6f, %.6f)",
                    x,
                    y,
                    red,
                    green,
                    blue,
                    hsv[0],
                    hsv[1],
                    hsv[2]
                )
            )
        }

        result.appendLine()
        result.appendLine("===== END HSV PIXEL CONVERSION VALIDATION =====")

        return result.toString()
    }

    fun validateResizePixels(bitmap: Bitmap): String {

        val resized = prepareImage(bitmap)

        val result = StringBuilder()

        result.appendLine("===== RESIZE PIXEL VALIDATION =====")
        result.appendLine(
            "Original: ${bitmap.width}x${bitmap.height}"
        )
        result.appendLine(
            "Resized: ${resized.width}x${resized.height}"
        )
        result.appendLine()

        val samplePoints = listOf(
            Pair(0, 0),
            Pair(1, 1),
            Pair(10, 10),
            Pair(32, 32),
            Pair(64, 64),
            Pair(96, 96),
            Pair(127, 127),
            Pair(64, 10),
            Pair(10, 64),
            Pair(100, 50)
        )

        for ((x, y) in samplePoints) {

            val pixel = resized.getPixel(x, y)

            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF

            result.appendLine(
                "Pixel ($x,$y) -> " +
                        "R=$red, " +
                        "G=$green, " +
                        "B=$blue"
            )
        }

        result.appendLine()
        result.appendLine(
            "===== END RESIZE PIXEL VALIDATION ====="
        )

        return result.toString()
    }

    private fun prepareImage(bitmap: Bitmap): Bitmap {

        if (
            bitmap.width == IMAGE_SIZE &&
            bitmap.height == IMAGE_SIZE
        ) {
            return bitmap
        }

        /*
         * IMPORTANT: The teammate's Python pipeline uses:
         *
         *     cv2.resize(image, (128, 128))
         *
         * with OpenCV's default INTER_LINEAR interpolation.
         *
         * Android's Bitmap.createScaledBitmap() and a simple floating-point
         * bilinear implementation do not necessarily produce the same 8-bit
         * pixels as OpenCV. Because the Random Forest was trained from the
         * Python feature pipeline, the resize stage must stay compatible.
         *
         * OpenCV INTER_LINEAR uses half-pixel coordinate mapping and a
         * quantized interpolation table. The implementation below mirrors
         * that behavior for an 8-bit ARGB_8888 bitmap.
         */

        val sourceWidth = bitmap.width
        val sourceHeight = bitmap.height

        val destinationWidth = IMAGE_SIZE
        val destinationHeight = IMAGE_SIZE

        val output =
            Bitmap.createBitmap(
                destinationWidth,
                destinationHeight,
                Bitmap.Config.ARGB_8888
            )

        val sourcePixels =
            IntArray(sourceWidth * sourceHeight)

        val destinationPixels =
            IntArray(destinationWidth * destinationHeight)

        bitmap.getPixels(
            sourcePixels,
            0,
            sourceWidth,
            0,
            0,
            sourceWidth,
            sourceHeight
        )

        /*
         * OpenCV uses INTER_BITS = 5 for the normal INTER_LINEAR lookup
         * table, giving 32 interpolation positions.
         */
        val interpolationTableSize = 1 shl 5

        val scaleX =
            sourceWidth.toDouble() /
                    destinationWidth.toDouble()

        val scaleY =
            sourceHeight.toDouble() /
                    destinationHeight.toDouble()

        // ------------------------------------------------------------
        // Pre-compute horizontal source indices and coefficients.
        // ------------------------------------------------------------

        val x0 = IntArray(destinationWidth)
        val x1 = IntArray(destinationWidth)
        val xWeight1 = IntArray(destinationWidth)
        val xWeight0 = IntArray(destinationWidth)

        for (dx in 0 until destinationWidth) {

            val sourceX =
                (dx + 0.5) * scaleX - 0.5

            var sx =
                kotlin.math.floor(sourceX).toInt()

            var fraction =
                sourceX - sx.toDouble()

            /*
             * OpenCV's border handling makes samples outside the image
             * equal to the nearest edge pixel.
             */
            if (sx < 0) {
                sx = 0
                fraction = 0.0
            } else if (sx >= sourceWidth - 1) {
                sx = sourceWidth - 1
                fraction = 0.0
            }

            var tableIndex =
                kotlin.math.floor(
                    fraction * interpolationTableSize + 0.5
                ).toInt()

            if (tableIndex >= interpolationTableSize) {
                tableIndex = interpolationTableSize - 1
            }

            x0[dx] = sx
            x1[dx] =
                if (sx < sourceWidth - 1) {
                    sx + 1
                } else {
                    sx
                }

            xWeight1[dx] = tableIndex
            xWeight0[dx] =
                interpolationTableSize - tableIndex
        }

        // ------------------------------------------------------------
        // Pre-compute vertical source indices and coefficients.
        // ------------------------------------------------------------

        val y0 = IntArray(destinationHeight)
        val y1 = IntArray(destinationHeight)
        val yWeight1 = IntArray(destinationHeight)
        val yWeight0 = IntArray(destinationHeight)

        for (dy in 0 until destinationHeight) {

            val sourceY =
                (dy + 0.5) * scaleY - 0.5

            var sy =
                kotlin.math.floor(sourceY).toInt()

            var fraction =
                sourceY - sy.toDouble()

            if (sy < 0) {
                sy = 0
                fraction = 0.0
            } else if (sy >= sourceHeight - 1) {
                sy = sourceHeight - 1
                fraction = 0.0
            }

            var tableIndex =
                kotlin.math.floor(
                    fraction * interpolationTableSize + 0.5
                ).toInt()

            if (tableIndex >= interpolationTableSize) {
                tableIndex = interpolationTableSize - 1
            }

            y0[dy] = sy
            y1[dy] =
                if (sy < sourceHeight - 1) {
                    sy + 1
                } else {
                    sy
                }

            yWeight1[dy] = tableIndex
            yWeight0[dy] =
                interpolationTableSize - tableIndex
        }

        // ------------------------------------------------------------
        // Separable bilinear interpolation.
        // ------------------------------------------------------------

        val weightScale =
            interpolationTableSize.toDouble() *
                    interpolationTableSize.toDouble()

        for (dy in 0 until destinationHeight) {

            val topRow =
                y0[dy] * sourceWidth

            val bottomRow =
                y1[dy] * sourceWidth

            val wy0 =
                yWeight0[dy].toDouble()

            val wy1 =
                yWeight1[dy].toDouble()

            for (dx in 0 until destinationWidth) {

                val p00 =
                    sourcePixels[
                        topRow + x0[dx]
                    ]

                val p10 =
                    sourcePixels[
                        topRow + x1[dx]
                    ]

                val p01 =
                    sourcePixels[
                        bottomRow + x0[dx]
                    ]

                val p11 =
                    sourcePixels[
                        bottomRow + x1[dx]
                    ]

                val wx0 =
                    xWeight0[dx].toDouble()

                val wx1 =
                    xWeight1[dx].toDouble()

                val weight00 =
                    wx0 * wy0

                val weight10 =
                    wx1 * wy0

                val weight01 =
                    wx0 * wy1

                val weight11 =
                    wx1 * wy1

                val red =
                    kotlin.math.round(
                        (
                                ((p00 shr 16) and 0xFF) * weight00 +
                                        ((p10 shr 16) and 0xFF) * weight10 +
                                        ((p01 shr 16) and 0xFF) * weight01 +
                                        ((p11 shr 16) and 0xFF) * weight11
                                ) / weightScale
                    ).toInt().coerceIn(0, 255)

                val green =
                    kotlin.math.round(
                        (
                                ((p00 shr 8) and 0xFF) * weight00 +
                                        ((p10 shr 8) and 0xFF) * weight10 +
                                        ((p01 shr 8) and 0xFF) * weight01 +
                                        ((p11 shr 8) and 0xFF) * weight11
                                ) / weightScale
                    ).toInt().coerceIn(0, 255)

                val blue =
                    kotlin.math.round(
                        (
                                (p00 and 0xFF) * weight00 +
                                        (p10 and 0xFF) * weight10 +
                                        (p01 and 0xFF) * weight01 +
                                        (p11 and 0xFF) * weight11
                                ) / weightScale
                    ).toInt().coerceIn(0, 255)

                destinationPixels[
                    dy * destinationWidth + dx
                ] =
                    (0xFF shl 24) or
                            (red shl 16) or
                            (green shl 8) or
                            blue
            }
        }

        output.setPixels(
            destinationPixels,
            0,
            destinationWidth,
            0,
            0,
            destinationWidth,
            destinationHeight
        )

        return output
    }

    // ============================================================
    // COLOR FEATURES
    // ============================================================

    fun extractColorFeatures(
        bitmap: Bitmap
    ): FloatArray {

        val image =
            prepareImage(bitmap)

        val width =
            image.width

        val height =
            image.height

        val features =
            mutableListOf<Float>()

        // --------------------------------------------------------
        // BGR HISTOGRAMS
        // --------------------------------------------------------

        val blueHistogram =
            FloatArray(32)

        val greenHistogram =
            FloatArray(32)

        val redHistogram =
            FloatArray(32)

        for (y in 0 until height) {

            for (x in 0 until width) {

                val pixel =
                    image.getPixel(x, y)

                val red =
                    (pixel shr 16) and 0xFF

                val green =
                    (pixel shr 8) and 0xFF

                val blue =
                    pixel and 0xFF

                blueHistogram[
                    histogramBin(
                        blue,
                        256,
                        32
                    )
                ] += 1f

                greenHistogram[
                    histogramBin(
                        green,
                        256,
                        32
                    )
                ] += 1f

                redHistogram[
                    histogramBin(
                        red,
                        256,
                        32
                    )
                ] += 1f
            }
        }

        normalizeL2(
            blueHistogram
        )

        normalizeL2(
            greenHistogram
        )

        normalizeL2(
            redHistogram
        )

        features.addAll(
            blueHistogram.toList()
        )

        features.addAll(
            greenHistogram.toList()
        )

        features.addAll(
            redHistogram.toList()
        )

        // --------------------------------------------------------
        // HSV HISTOGRAMS
        // --------------------------------------------------------

        val hueHistogram =
            FloatArray(32)

        val saturationHistogram =
            FloatArray(32)

        val valueHistogram =
            FloatArray(32)

        for (y in 0 until height) {

            for (x in 0 until width) {

                val pixel =
                    image.getPixel(x, y)

                val red =
                    (pixel shr 16) and 0xFF

                val green =
                    (pixel shr 8) and 0xFF

                val blue =
                    pixel and 0xFF

                val hsv =
                    rgbToOpenCvHsv(
                        red,
                        green,
                        blue
                    )

                val hue =
                    hsv[0]

                val saturation =
                    hsv[1]

                val value =
                    hsv[2]

                hueHistogram[
                    histogramBin(
                        hue,
                        180,
                        32
                    )
                ] += 1f

                saturationHistogram[
                    histogramBin(
                        saturation,
                        256,
                        32
                    )
                ] += 1f

                valueHistogram[
                    histogramBin(
                        value,
                        256,
                        32
                    )
                ] += 1f
            }
        }

        normalizeL2(
            hueHistogram
        )

        normalizeL2(
            saturationHistogram
        )

        normalizeL2(
            valueHistogram
        )

        features.addAll(
            hueHistogram.toList()
        )

        features.addAll(
            saturationHistogram.toList()
        )

        features.addAll(
            valueHistogram.toList()
        )

        val result =
            features.toFloatArray()

        require(
            result.size == COLOR_FEATURE_COUNT
        ) {
            "Expected $COLOR_FEATURE_COUNT color features, " +
                    "got ${result.size}"
        }

        return result
    }

    // ============================================================
    // LBP TEXTURE FEATURES
    // ============================================================

    fun extractTextureFeatures(
        bitmap: Bitmap
    ): FloatArray {

        val image =
            prepareImage(bitmap)

        val gray =
            convertBitmapToGray(image)

        val width =
            image.width

        val height =
            image.height

        val radius =
            2

        val points =
            8 * radius

        val numberOfBins =
            points + 2

        val histogram =
            FloatArray(numberOfBins)

        for (
        y in radius until height - radius
        ) {

            for (
            x in radius until width - radius
            ) {

                val center =
                    gray[y][x]

                val pattern =
                    calculateUniformLbp(
                        gray = gray,
                        x = x,
                        y = y,
                        center = center,
                        radius = radius,
                        points = points
                    )

                histogram[pattern] += 1f
            }
        }

        var total =
            0f

        for (value in histogram) {
            total += value
        }

        val denominator =
            total + 1e-7f

        for (i in histogram.indices) {

            histogram[i] =
                histogram[i] /
                        denominator
        }

        require(
            histogram.size == TEXTURE_FEATURE_COUNT
        ) {
            "Expected $TEXTURE_FEATURE_COUNT LBP features, " +
                    "got ${histogram.size}"
        }

        return histogram
    }

    // ============================================================
    // HOG STAGE VALIDATION
    // ============================================================

    fun validateHogStages(
        bitmap: Bitmap
    ): String {

        val image =
            prepareImage(bitmap)

        val gray =
            convertBitmapToGray(image)

        val height =
            IMAGE_SIZE

        val width =
            IMAGE_SIZE

        val orientations =
            9

        val pixelsPerCell =
            16

        val cellsPerBlock =
            2

        val cellsY =
            height / pixelsPerCell

        val cellsX =
            width / pixelsPerCell

        val orientationHistogram =
            Array(cellsY) {
                Array(cellsX) {
                    FloatArray(orientations)
                }
            }

        var gradientMagnitudeSum =
            0.0

        var gradientMagnitudeMin =
            Float.POSITIVE_INFINITY

        var gradientMagnitudeMax =
            Float.NEGATIVE_INFINITY

        var angleMin =
            Float.POSITIVE_INFINITY

        var angleMax =
            Float.NEGATIVE_INFINITY

        for (y in 0 until height) {

            for (x in 0 until width) {

                val dx =
                    if (
                        x in 1 until width - 1
                    ) {
                        gray[y][x + 1] -
                                gray[y][x - 1]
                    } else {
                        0f
                    }

                val dy =
                    if (
                        y in 1 until height - 1
                    ) {
                        gray[y + 1][x] -
                                gray[y - 1][x]
                    } else {
                        0f
                    }

                val magnitude =
                    sqrt(
                        dx * dx +
                                dy * dy
                    )

                var angle =
                    Math.toDegrees(
                        atan2(
                            dy.toDouble(),
                            dx.toDouble()
                        )
                    ).toFloat()

                angle %= 180f

                if (angle < 0f) {
                    angle += 180f
                }

                gradientMagnitudeSum +=
                    magnitude.toDouble()

                if (
                    magnitude <
                    gradientMagnitudeMin
                ) {
                    gradientMagnitudeMin =
                        magnitude
                }

                if (
                    magnitude >
                    gradientMagnitudeMax
                ) {
                    gradientMagnitudeMax =
                        magnitude
                }

                if (
                    angle <
                    angleMin
                ) {
                    angleMin =
                        angle
                }

                if (
                    angle >
                    angleMax
                ) {
                    angleMax =
                        angle
                }

                if (
                    magnitude == 0f
                ) {
                    continue
                }

                val binPosition =
                    angle /
                            (180f / orientations)

                val lowerBin =
                    floor(
                        binPosition
                    )
                        .toInt()
                        .coerceIn(
                            0,
                            orientations - 1
                        )

                val upperBin =
                    (
                            lowerBin + 1
                            ) %
                            orientations

                val fraction =
                    binPosition -
                            floor(
                                binPosition
                            )

                val cellY =
                    y / pixelsPerCell

                val cellX =
                    x / pixelsPerCell

                if (
                    cellY !in 0 until cellsY ||
                    cellX !in 0 until cellsX
                ) {
                    continue
                }

                orientationHistogram[cellY][cellX][lowerBin] +=
                    magnitude *
                            (1f - fraction)

                orientationHistogram[cellY][cellX][upperBin] +=
                    magnitude *
                            fraction
            }
        }

        var histogramSum =
            0.0

        var histogramMin =
            Float.POSITIVE_INFINITY

        var histogramMax =
            Float.NEGATIVE_INFINITY

        for (cellY in 0 until cellsY) {

            for (cellX in 0 until cellsX) {

                for (
                orientation in 0 until orientations
                ) {

                    val value =
                        orientationHistogram[cellY][cellX][orientation]

                    histogramSum +=
                        value.toDouble()

                    if (
                        value <
                        histogramMin
                    ) {
                        histogramMin =
                            value
                    }

                    if (
                        value >
                        histogramMax
                    ) {
                        histogramMax =
                            value
                    }
                }
            }
        }

        val blocksY =
            cellsY -
                    cellsPerBlock +
                    1

        val blocksX =
            cellsX -
                    cellsPerBlock +
                    1

        val blockCount =
            blocksY *
                    blocksX

        val featureCount =
            blockCount *
                    cellsPerBlock *
                    cellsPerBlock *
                    orientations

        return buildString {

            appendLine(
                "===== HOG STAGE VALIDATION ====="
            )

            appendLine()

            appendLine("IMAGE")
            appendLine("Width: $width")
            appendLine("Height: $height")

            appendLine()

            appendLine("HOG CONFIGURATION")
            appendLine("Orientations: $orientations")
            appendLine("Pixels per cell: $pixelsPerCell")
            appendLine("Cells X: $cellsX")
            appendLine("Cells Y: $cellsY")
            appendLine("Blocks X: $blocksX")
            appendLine("Blocks Y: $blocksY")
            appendLine("Block count: $blockCount")
            appendLine(
                "Expected feature count: $featureCount"
            )

            appendLine()

            appendLine("GRADIENT MAGNITUDE")
            appendLine(
                "Sum: $gradientMagnitudeSum"
            )
            appendLine(
                "Minimum: $gradientMagnitudeMin"
            )
            appendLine(
                "Maximum: $gradientMagnitudeMax"
            )

            appendLine()

            appendLine("GRADIENT ANGLE")
            appendLine(
                "Minimum: $angleMin"
            )
            appendLine(
                "Maximum: $angleMax"
            )

            appendLine()

            appendLine("ORIENTATION HISTOGRAM")
            appendLine(
                "Sum: $histogramSum"
            )
            appendLine(
                "Minimum: $histogramMin"
            )
            appendLine(
                "Maximum: $histogramMax"
            )

            appendLine()

            appendLine(
                "===== END HOG STAGE VALIDATION ====="
            )
        }
    }

    // ============================================================
    // HOG GRADIENT VALIDATION
    // ============================================================

    fun validateHogGradientStages(
        bitmap: Bitmap
    ): String {

        val image =
            prepareImage(bitmap)

        val gray =
            convertBitmapToGray(image)

        val width =
            IMAGE_SIZE

        val height =
            IMAGE_SIZE

        var gradientMagnitudeSum =
            0.0

        var gradientMagnitudeMin =
            Float.POSITIVE_INFINITY

        var gradientMagnitudeMax =
            Float.NEGATIVE_INFINITY

        var gradientDxSum =
            0.0

        var gradientDySum =
            0.0

        var gradientDxMin =
            Float.POSITIVE_INFINITY

        var gradientDxMax =
            Float.NEGATIVE_INFINITY

        var gradientDyMin =
            Float.POSITIVE_INFINITY

        var gradientDyMax =
            Float.NEGATIVE_INFINITY

        var angleMin =
            Float.POSITIVE_INFINITY

        var angleMax =
            Float.NEGATIVE_INFINITY

        var nonZeroGradientCount =
            0

        for (y in 0 until height) {

            for (x in 0 until width) {

                val dx =
                    if (
                        x in 1 until width - 1
                    ) {
                        gray[y][x + 1] -
                                gray[y][x - 1]
                    } else {
                        0f
                    }

                val dy =
                    if (
                        y in 1 until height - 1
                    ) {
                        gray[y + 1][x] -
                                gray[y - 1][x]
                    } else {
                        0f
                    }

                val magnitude =
                    sqrt(
                        dx * dx +
                                dy * dy
                    )

                var angle =
                    Math.toDegrees(
                        atan2(
                            dy.toDouble(),
                            dx.toDouble()
                        )
                    ).toFloat()

                angle %= 180f

                if (
                    angle < 0f
                ) {
                    angle += 180f
                }

                gradientDxSum +=
                    dx.toDouble()

                gradientDySum +=
                    dy.toDouble()

                if (
                    dx < gradientDxMin
                ) {
                    gradientDxMin =
                        dx
                }

                if (
                    dx > gradientDxMax
                ) {
                    gradientDxMax =
                        dx
                }

                if (
                    dy < gradientDyMin
                ) {
                    gradientDyMin =
                        dy
                }

                if (
                    dy > gradientDyMax
                ) {
                    gradientDyMax =
                        dy
                }

                gradientMagnitudeSum +=
                    magnitude.toDouble()

                if (
                    magnitude <
                    gradientMagnitudeMin
                ) {
                    gradientMagnitudeMin =
                        magnitude
                }

                if (
                    magnitude >
                    gradientMagnitudeMax
                ) {
                    gradientMagnitudeMax =
                        magnitude
                }

                if (
                    angle <
                    angleMin
                ) {
                    angleMin =
                        angle
                }

                if (
                    angle >
                    angleMax
                ) {
                    angleMax =
                        angle
                }

                if (
                    magnitude > 0f
                ) {
                    nonZeroGradientCount++
                }
            }
        }

        return buildString {

            appendLine(
                "===== HOG GRADIENT VALIDATION ====="
            )

            appendLine()

            appendLine("IMAGE")
            appendLine("Width: $width")
            appendLine("Height: $height")

            appendLine()

            appendLine("GRADIENT CALCULATION")
            appendLine(
                "Interior central difference: YES"
            )
            appendLine(
                "Boundary gradients: ZERO"
            )

            appendLine()

            appendLine("DX")
            appendLine(
                "Sum: $gradientDxSum"
            )
            appendLine(
                "Minimum: $gradientDxMin"
            )
            appendLine(
                "Maximum: $gradientDxMax"
            )

            appendLine()

            appendLine("DY")
            appendLine(
                "Sum: $gradientDySum"
            )
            appendLine(
                "Minimum: $gradientDyMin"
            )
            appendLine(
                "Maximum: $gradientDyMax"
            )

            appendLine()

            appendLine("GRADIENT MAGNITUDE")
            appendLine(
                "Sum: $gradientMagnitudeSum"
            )
            appendLine(
                "Minimum: $gradientMagnitudeMin"
            )
            appendLine(
                "Maximum: $gradientMagnitudeMax"
            )
            appendLine(
                "Non-zero pixels: $nonZeroGradientCount"
            )

            appendLine()

            appendLine("GRADIENT ANGLE")
            appendLine(
                "Minimum: $angleMin"
            )
            appendLine(
                "Maximum: $angleMax"
            )

            appendLine()

            appendLine(
                "===== END HOG GRADIENT VALIDATION ====="
            )
        }
    }

    // ============================================================
    // HOG CELL HISTOGRAM VALIDATION
    // ============================================================

    fun validateHogCellHistograms(
        bitmap: Bitmap
    ) {

        val tag =
            "FruitQualityHogCells"

        val image =
            prepareImage(bitmap)

        val gray =
            convertBitmapToGray(image)

        val width =
            IMAGE_SIZE

        val height =
            IMAGE_SIZE

        val pixelsPerCell =
            16

        val cellsX =
            width / pixelsPerCell

        val cellsY =
            height / pixelsPerCell

        val orientations =
            9

        val orientationBinSize =
            180.0 /
                    orientations.toDouble()

        Log.d(
            tag,
            "===== HOG CELL HISTOGRAM DIAGNOSTIC ====="
        )

        Log.d(
            tag,
            "IMAGE"
        )

        Log.d(
            tag,
            "Width: $width"
        )

        Log.d(
            tag,
            "Height: $height"
        )

        Log.d(
            tag,
            "CONFIGURATION"
        )

        Log.d(
            tag,
            "Cells X: $cellsX"
        )

        Log.d(
            tag,
            "Cells Y: $cellsY"
        )

        Log.d(
            tag,
            "Pixels per cell: $pixelsPerCell"
        )

        Log.d(
            tag,
            "Orientations: $orientations"
        )

        Log.d(
            tag,
            "Orientation bin size: $orientationBinSize"
        )

        // --------------------------------------------------------
        // GRADIENT STORAGE
        // --------------------------------------------------------

        val gradientRows =
            Array(height) {
                DoubleArray(width)
            }

        val gradientColumns =
            Array(height) {
                DoubleArray(width)
            }

        // --------------------------------------------------------
        // CENTRAL DIFFERENCE
        // --------------------------------------------------------

        for (y in 0 until height) {

            for (x in 0 until width) {

                gradientRows[y][x] =
                    if (
                        y == 0 ||
                        y == height - 1
                    ) {
                        0.0
                    } else {
                        gray[y + 1][x].toDouble() -
                                gray[y - 1][x].toDouble()
                    }

                gradientColumns[y][x] =
                    if (
                        x == 0 ||
                        x == width - 1
                    ) {
                        0.0
                    } else {
                        gray[y][x + 1].toDouble() -
                                gray[y][x - 1].toDouble()
                    }
            }
        }

        // --------------------------------------------------------
        // CELL HISTOGRAMS
        // --------------------------------------------------------

        val cellHistograms =
            Array(cellsY) {
                Array(cellsX) {
                    DoubleArray(orientations)
                }
            }

        var gradientMagnitudeSum =
            0.0

        var processedPixels =
            0

        var skippedPixels =
            0

        var skippedMagnitude =
            0.0

        // --------------------------------------------------------
        // HARD ORIENTATION BINNING
        // --------------------------------------------------------

        for (y in 0 until height) {

            for (x in 0 until width) {

                val rowGradient =
                    gradientRows[y][x]

                val columnGradient =
                    gradientColumns[y][x]

                val magnitude =
                    sqrt(
                        columnGradient *
                                columnGradient +
                                rowGradient *
                                rowGradient
                    )

                if (
                    magnitude <= 0.0
                ) {

                    skippedPixels++

                    skippedMagnitude +=
                        magnitude

                    continue
                }

                processedPixels++

                gradientMagnitudeSum +=
                    magnitude

                var orientationDegrees =
                    Math.toDegrees(
                        atan2(
                            rowGradient,
                            columnGradient
                        )
                    )

                orientationDegrees %= 180.0

                if (
                    orientationDegrees < 0.0
                ) {
                    orientationDegrees +=
                        180.0
                }

                var orientationBin =
                    floor(
                        orientationDegrees /
                                orientationBinSize
                    ).toInt()

                if (
                    orientationBin < 0
                ) {
                    orientationBin = 0
                }

                if (
                    orientationBin >= orientations
                ) {
                    orientationBin =
                        orientations - 1
                }

                val cellX =
                    x / pixelsPerCell

                val cellY =
                    y / pixelsPerCell

                if (
                    cellY !in 0 until cellsY ||
                    cellX !in 0 until cellsX
                ) {
                    continue
                }

                cellHistograms[cellY][cellX][orientationBin] +=
                    magnitude
            }
        }

        // --------------------------------------------------------
        // RAW HISTOGRAM SUM
        // --------------------------------------------------------

        var rawHistogramSum =
            0.0

        for (cellY in 0 until cellsY) {

            for (cellX in 0 until cellsX) {

                for (bin in 0 until orientations) {

                    rawHistogramSum +=
                        cellHistograms[cellY][cellX][bin]
                }
            }
        }

        // --------------------------------------------------------
        // CELL NORMALIZATION
        // --------------------------------------------------------

        val cellArea =
            pixelsPerCell *
                    pixelsPerCell

        var normalizedHistogramSum =
            0.0

        for (cellY in 0 until cellsY) {

            for (cellX in 0 until cellsX) {

                for (bin in 0 until orientations) {

                    val rawValue =
                        cellHistograms[cellY][cellX][bin]

                    val normalizedValue =
                        rawValue /
                                cellArea.toDouble()

                    cellHistograms[cellY][cellX][bin] =
                        normalizedValue

                    normalizedHistogramSum +=
                        normalizedValue
                }
            }
        }

        // --------------------------------------------------------
        // COMPARISON
        // --------------------------------------------------------

        val rawDifference =
            gradientMagnitudeSum -
                    rawHistogramSum

        val reconstructedMagnitude =
            normalizedHistogramSum *
                    cellArea.toDouble()

        val normalizedReconstructionDifference =
            gradientMagnitudeSum -
                    reconstructedMagnitude

        // --------------------------------------------------------
        // NON-ZERO BINS
        // --------------------------------------------------------

        var nonZeroBins =
            0

        val totalBins =
            cellsX *
                    cellsY *
                    orientations

        for (cellY in 0 until cellsY) {

            for (cellX in 0 until cellsX) {

                for (bin in 0 until orientations) {

                    if (
                        cellHistograms[cellY][cellX][bin] >
                        0.0
                    ) {
                        nonZeroBins++
                    }
                }
            }
        }

        // --------------------------------------------------------
        // LOG
        // --------------------------------------------------------

        Log.d(
            tag,
            ""
        )

        Log.d(
            tag,
            "GRADIENTS"
        )

        Log.d(
            tag,
            "Gradient magnitude sum: $gradientMagnitudeSum"
        )

        Log.d(
            tag,
            "Processed pixels: $processedPixels"
        )

        Log.d(
            tag,
            "Skipped pixels: $skippedPixels"
        )

        Log.d(
            tag,
            "Skipped magnitude sum: $skippedMagnitude"
        )

        Log.d(
            tag,
            ""
        )

        Log.d(
            tag,
            "RAW CELL HISTOGRAM"
        )

        Log.d(
            tag,
            "Raw histogram sum: $rawHistogramSum"
        )

        Log.d(
            tag,
            ""
        )

        Log.d(
            tag,
            "NORMALIZED CELL HISTOGRAM"
        )

        Log.d(
            tag,
            "Cell area: $cellArea"
        )

        Log.d(
            tag,
            "Normalized histogram sum: " +
                    normalizedHistogramSum
        )

        Log.d(
            tag,
            "Normalized histogram reconstructed magnitude: " +
                    reconstructedMagnitude
        )

        Log.d(
            tag,
            ""
        )

        Log.d(
            tag,
            "COMPARISON"
        )

        Log.d(
            tag,
            "Raw difference: $rawDifference"
        )

        Log.d(
            tag,
            "Normalized reconstruction difference: " +
                    normalizedReconstructionDifference
        )

        Log.d(
            tag,
            ""
        )

        Log.d(
            tag,
            "Non-zero histogram bins: " +
                    "$nonZeroBins / $totalBins"
        )

        Log.d(
            tag,
            ""
        )

        Log.d(
            tag,
            "===== END HOG CELL HISTOGRAM DIAGNOSTIC ====="
        )
    }

    // ============================================================
    // PRODUCTION HOG EXTRACTION
    // ============================================================

    fun extractHogFeatures(
        bitmap: Bitmap
    ): FloatArray {

        val image =
            prepareImage(bitmap)

        val gray =
            convertBitmapToGray(image)

        val width =
            image.width

        val height =
            image.height

        val orientations =
            9

        val pixelsPerCell =
            16

        val cellsPerBlock =
            2

        val cellsX =
            width / pixelsPerCell

        val cellsY =
            height / pixelsPerCell

        val blocksX =
            cellsX -
                    cellsPerBlock +
                    1

        val blocksY =
            cellsY -
                    cellsPerBlock +
                    1

        // ========================================================
        // CELL HISTOGRAMS
        // ========================================================

        val orientationBins =
            Array(cellsY) {
                Array(cellsX) {
                    DoubleArray(orientations)
                }
            }

        // ========================================================
        // HOG GRADIENT + HARD BINNING
        // ========================================================

        for (y in 0 until height) {

            for (x in 0 until width) {

                val gx =
                    if (
                        x in 1 until width - 1
                    ) {
                        gray[y][x + 1].toDouble() -
                                gray[y][x - 1].toDouble()
                    } else {
                        0.0
                    }

                val gy =
                    if (
                        y in 1 until height - 1
                    ) {
                        gray[y + 1][x].toDouble() -
                                gray[y - 1][x].toDouble()
                    } else {
                        0.0
                    }

                val magnitude =
                    sqrt(
                        gx * gx +
                                gy * gy
                    )

                if (
                    magnitude <= 0.0
                ) {
                    continue
                }

                var theta =
                    Math.toDegrees(
                        atan2(
                            gy,
                            gx
                        )
                    )

                theta %= 180.0

                if (
                    theta < 0.0
                ) {
                    theta += 180.0
                }

                // ------------------------------------------------
                // IMPORTANT:
                //
                // scikit-image uses HARD BINNING here.
                //
                // There is NO interpolation between adjacent
                // orientation bins.
                // ------------------------------------------------

                var orientationBin =
                    floor(
                        theta /
                                (
                                        180.0 /
                                                orientations.toDouble()
                                        )
                    ).toInt()

                orientationBin =
                    orientationBin.coerceIn(
                        0,
                        orientations - 1
                    )

                val cellY =
                    y / pixelsPerCell

                val cellX =
                    x / pixelsPerCell

                if (
                    cellY !in 0 until cellsY ||
                    cellX !in 0 until cellsX
                ) {
                    continue
                }

                orientationBins[cellY][cellX][orientationBin] +=
                    magnitude
            }
        }

        // ========================================================
        // CELL NORMALIZATION
        //
        // scikit-image divides every cell histogram by:
        //
        // 16 × 16 = 256
        // ========================================================

        val cellArea =
            pixelsPerCell *
                    pixelsPerCell

        for (cellY in 0 until cellsY) {

            for (cellX in 0 until cellsX) {

                for (
                orientation in 0 until orientations
                ) {

                    orientationBins[cellY][cellX][orientation] =
                        orientationBins[cellY][cellX][orientation] /
                                cellArea.toDouble()
                }
            }
        }

        // ========================================================
        // HOG OUTPUT
        // ========================================================

        val features =
            FloatArray(
                HOG_FEATURE_COUNT
            )

        var featureIndex =
            0

        // scikit-image HOG epsilon
        val epsilon =
            1e-5

        // L2-Hys clipping value
        val clipValue =
            0.2

        // ========================================================
        // 2 × 2 BLOCKS
        // ========================================================

        for (
        blockY in 0 until blocksY
        ) {

            for (
            blockX in 0 until blocksX
            ) {

                val block =
                    DoubleArray(
                        cellsPerBlock *
                                cellsPerBlock *
                                orientations
                    )

                var blockIndex =
                    0

                // ------------------------------------------------
                // Copy 4 cell histograms into one block
                // ------------------------------------------------

                for (
                cellOffsetY in 0 until cellsPerBlock
                ) {

                    for (
                    cellOffsetX in 0 until cellsPerBlock
                    ) {

                        val histogram =
                            orientationBins[
                                blockY +
                                        cellOffsetY
                            ][
                                blockX +
                                        cellOffsetX
                            ]

                        for (
                        orientation in 0 until orientations
                        ) {

                            block[
                                blockIndex
                            ] =
                                histogram[
                                    orientation
                                ]

                            blockIndex++
                        }
                    }
                }

                // ------------------------------------------------
                // FIRST L2 NORMALIZATION
                // ------------------------------------------------

                var sumSquares =
                    0.0

                for (
                value in block
                ) {

                    sumSquares +=
                        value * value
                }

                val firstDenominator =
                    sqrt(
                        sumSquares +
                                epsilon *
                                epsilon
                    )

                for (
                i in block.indices
                ) {

                    block[i] =
                        block[i] /
                                firstDenominator
                }

                // ------------------------------------------------
                // HYS CLIPPING
                // ------------------------------------------------

                for (
                i in block.indices
                ) {

                    if (
                        block[i] >
                        clipValue
                    ) {

                        block[i] =
                            clipValue
                    }
                }

                // ------------------------------------------------
                // SECOND L2 NORMALIZATION
                // ------------------------------------------------

                var clippedSumSquares =
                    0.0

                for (
                value in block
                ) {

                    clippedSumSquares +=
                        value * value
                }

                val secondDenominator =
                    sqrt(
                        clippedSumSquares +
                                epsilon *
                                epsilon
                    )

                for (
                value in block
                ) {

                    val normalized =
                        value /
                                secondDenominator

                    features[
                        featureIndex
                    ] =
                        normalized.toFloat()

                    featureIndex++
                }
            }
        }

        // ========================================================
        // FINAL FEATURE COUNT CHECK
        // ========================================================

        require(
            featureIndex ==
                    HOG_FEATURE_COUNT
        ) {

            "Expected $HOG_FEATURE_COUNT HOG features, " +
                    "got $featureIndex"
        }

        // ========================================================
        // HOG DIAGNOSTIC
        // ========================================================

        var hogSum =
            0.0

        var hogAbsoluteSum =
            0.0

        var hogMinimum =
            Double.POSITIVE_INFINITY

        var hogMaximum =
            Double.NEGATIVE_INFINITY

        var hogWeightedSum =
            0.0

        for (
        i in features.indices
        ) {

            val value =
                features[i].toDouble()

            hogSum +=
                value

            hogAbsoluteSum +=
                kotlin.math.abs(
                    value
                )

            if (
                value <
                hogMinimum
            ) {
                hogMinimum =
                    value
            }

            if (
                value >
                hogMaximum
            ) {
                hogMaximum =
                    value
            }

            hogWeightedSum +=
                (i + 1).toDouble() *
                        value
        }

        Log.d(
            "FruitQualityHog",
            "===== HOG FINAL DIAGNOSTIC ====="
        )

        Log.d(
            "FruitQualityHog",
            "Image: ${width}x${height}"
        )

        Log.d(
            "FruitQualityHog",
            "Cells: ${cellsX}x${cellsY}"
        )

        Log.d(
            "FruitQualityHog",
            "Pixels per cell: $pixelsPerCell"
        )

        Log.d(
            "FruitQualityHog",
            "Orientations: $orientations"
        )

        Log.d(
            "FruitQualityHog",
            "Blocks: ${blocksX}x${blocksY}"
        )

        Log.d(
            "FruitQualityHog",
            "Block count: " +
                    "${blocksX * blocksY}"
        )

        Log.d(
            "FruitQualityHog",
            "Expected feature count: " +
                    HOG_FEATURE_COUNT
        )

        Log.d(
            "FruitQualityHog",
            "Actual feature count: " +
                    features.size
        )

        Log.d(
            "FruitQualityHog",
            "HOG sum: $hogSum"
        )

        Log.d(
            "FruitQualityHog",
            "HOG absolute sum: $hogAbsoluteSum"
        )

        Log.d(
            "FruitQualityHog",
            "HOG minimum: $hogMinimum"
        )

        Log.d(
            "FruitQualityHog",
            "HOG maximum: $hogMaximum"
        )

        Log.d(
            "FruitQualityHog",
            "HOG weighted sum: $hogWeightedSum"
        )

        Log.d(
            "FruitQualityHog",
            "===== END HOG FINAL DIAGNOSTIC ====="
        )

        return features
    }

    // ============================================================
    // STATISTICAL FEATURES
    // ============================================================

    fun extractStatisticalFeatures(
        bitmap: Bitmap
    ): FloatArray {

        val image =
            prepareImage(bitmap)

        val width =
            image.width

        val height =
            image.height

        val pixelCount =
            width * height

        val gray =
            convertBitmapToGray(image)

        val grayValues =
            FloatArray(pixelCount)

        var grayIndex =
            0

        for (y in 0 until height) {

            for (x in 0 until width) {

                grayValues[
                    grayIndex
                ] =
                    gray[y][x]

                grayIndex++
            }
        }

        val features =
            mutableListOf<Float>()

        features.add(
            calculateMean(
                grayValues
            )
        )

        features.add(
            calculateStandardDeviation(
                grayValues
            )
        )

        features.add(
            calculateMinimum(
                grayValues
            )
        )

        features.add(
            calculateMaximum(
                grayValues
            )
        )

        // --------------------------------------------------------
        // BGR
        // --------------------------------------------------------

        val blueValues =
            FloatArray(pixelCount)

        val greenValues =
            FloatArray(pixelCount)

        val redValues =
            FloatArray(pixelCount)

        var index =
            0

        for (y in 0 until height) {

            for (x in 0 until width) {

                val pixel =
                    image.getPixel(
                        x,
                        y
                    )

                val red =
                    (
                            (pixel shr 16) and
                                    0xFF
                            ).toFloat()

                val green =
                    (
                            (pixel shr 8) and
                                    0xFF
                            ).toFloat()

                val blue =
                    (
                            pixel and
                                    0xFF
                            ).toFloat()

                blueValues[index] =
                    blue

                greenValues[index] =
                    green

                redValues[index] =
                    red

                index++
            }
        }

        features.add(
            calculateMean(
                blueValues
            )
        )

        features.add(
            calculateStandardDeviation(
                blueValues
            )
        )

        features.add(
            calculateMedian(
                blueValues
            )
        )

        features.add(
            calculateMean(
                greenValues
            )
        )

        features.add(
            calculateStandardDeviation(
                greenValues
            )
        )

        features.add(
            calculateMedian(
                greenValues
            )
        )

        features.add(
            calculateMean(
                redValues
            )
        )

        features.add(
            calculateStandardDeviation(
                redValues
            )
        )

        features.add(
            calculateMedian(
                redValues
            )
        )

        // --------------------------------------------------------
        // HSV
        // --------------------------------------------------------

        val hueValues =
            FloatArray(pixelCount)

        val saturationValues =
            FloatArray(pixelCount)

        val valueValues =
            FloatArray(pixelCount)

        index =
            0

        for (y in 0 until height) {

            for (x in 0 until width) {

                val pixel =
                    image.getPixel(
                        x,
                        y
                    )

                val red =
                    (pixel shr 16) and
                            0xFF

                val green =
                    (pixel shr 8) and
                            0xFF

                val blue =
                    pixel and
                            0xFF

                val hsv =
                    rgbToOpenCvHsv(
                        red,
                        green,
                        blue
                    )

                hueValues[index] =
                    hsv[0]

                saturationValues[index] =
                    hsv[1]

                valueValues[index] =
                    hsv[2]

                index++
            }
        }

        features.add(
            calculateMean(
                hueValues
            )
        )

        features.add(
            calculateStandardDeviation(
                hueValues
            )
        )

        features.add(
            calculateMean(
                saturationValues
            )
        )

        features.add(
            calculateStandardDeviation(
                saturationValues
            )
        )

        features.add(
            calculateMean(
                valueValues
            )
        )

        features.add(
            calculateStandardDeviation(
                valueValues
            )
        )

        val result =
            features.toFloatArray()

        require(
            result.size ==
                    STATISTICAL_FEATURE_COUNT
        ) {
            "Expected $STATISTICAL_FEATURE_COUNT " +
                    "statistical features, " +
                    "got ${result.size}"
        }

        return result
    }

    // ============================================================
    // GRAYSCALE
    // ============================================================

    private fun convertBitmapToGray(
        bitmap: Bitmap
    ): Array<FloatArray> {

        val width =
            bitmap.width

        val height =
            bitmap.height

        val gray =
            Array(height) {
                FloatArray(width)
            }

        for (y in 0 until height) {

            for (x in 0 until width) {

                val pixel =
                    bitmap.getPixel(
                        x,
                        y
                    )

                val red =
                    (
                            (pixel shr 16) and
                                    0xFF
                            ).toFloat()

                val green =
                    (
                            (pixel shr 8) and
                                    0xFF
                            ).toFloat()

                val blue =
                    (
                            pixel and
                                    0xFF
                            ).toFloat()

                gray[y][x] =
                    round(
                        0.299f * red +
                                0.587f * green +
                                0.114f * blue
                    )
            }
        }

        return gray
    }

    // ============================================================
    // LBP
    // ============================================================

    private fun calculateUniformLbp(
        gray: Array<FloatArray>,
        x: Int,
        y: Int,
        center: Float,
        radius: Int,
        points: Int
    ): Int {

        val bits =
            IntArray(points)

        for (
        point in 0 until points
        ) {

            val angle =
                2.0 *
                        Math.PI *
                        point.toDouble() /
                        points.toDouble()

            val sampleX =
                x +
                        radius *
                        cos(angle)

            val sampleY =
                y -
                        radius *
                        sin(angle)

            val sample =
                bilinearSample(
                    gray,
                    sampleX,
                    sampleY
                )

            bits[point] =
                if (
                    sample >= center
                ) {
                    1
                } else {
                    0
                }
        }

        var transitions =
            0

        for (
        i in 0 until points
        ) {

            val current =
                bits[i]

            val next =
                bits[
                    (i + 1) %
                            points
                ]

            if (
                current != next
            ) {
                transitions++
            }
        }

        if (
            transitions <= 2
        ) {

            var ones =
                0

            for (
            bit in bits
            ) {

                if (
                    bit == 1
                ) {
                    ones++
                }
            }

            return ones
        }

        return points + 1
    }

    // ============================================================
    // BILINEAR SAMPLING
    // ============================================================

    private fun bilinearSample(
        image: Array<FloatArray>,
        x: Double,
        y: Double
    ): Float {

        val height =
            image.size

        val width =
            image[0].size

        val xClamped =
            x.coerceIn(
                0.0,
                (width - 1).toDouble()
            )

        val yClamped =
            y.coerceIn(
                0.0,
                (height - 1).toDouble()
            )

        val x0 =
            floor(
                xClamped
            ).toInt()

        val y0 =
            floor(
                yClamped
            ).toInt()

        val x1 =
            min(
                x0 + 1,
                width - 1
            )

        val y1 =
            min(
                y0 + 1,
                height - 1
            )

        val dx =
            xClamped -
                    x0

        val dy =
            yClamped -
                    y0

        val top =
            image[y0][x0] *
                    (1.0 - dx) +
                    image[y0][x1] *
                    dx

        val bottom =
            image[y1][x0] *
                    (1.0 - dx) +
                    image[y1][x1] *
                    dx

        return (
                top * (1.0 - dy) +
                        bottom * dy
                ).toFloat()
    }

    // ============================================================
    // STATISTICAL HELPERS
    // ============================================================

    private fun calculateMean(
        values: FloatArray
    ): Float {

        if (
            values.isEmpty()
        ) {
            return 0f
        }

        var sum =
            0.0

        for (
        value in values
        ) {

            sum +=
                value.toDouble()
        }

        return (
                sum /
                        values.size
                ).toFloat()
    }

    private fun calculateStandardDeviation(
        values: FloatArray
    ): Float {

        if (
            values.isEmpty()
        ) {
            return 0f
        }

        val mean =
            calculateMean(
                values
            )

        var sumSquaredDifference =
            0.0

        for (
        value in values
        ) {

            val difference =
                value -
                        mean

            sumSquaredDifference +=
                difference.toDouble() *
                        difference.toDouble()
        }

        return sqrt(
            sumSquaredDifference /
                    values.size
        ).toFloat()
    }

    private fun calculateMinimum(
        values: FloatArray
    ): Float {

        if (
            values.isEmpty()
        ) {
            return 0f
        }

        var minimum =
            values[0]

        for (
        i in 1 until values.size
        ) {

            if (
                values[i] <
                minimum
            ) {

                minimum =
                    values[i]
            }
        }

        return minimum
    }

    private fun calculateMaximum(
        values: FloatArray
    ): Float {

        if (
            values.isEmpty()
        ) {
            return 0f
        }

        var maximum =
            values[0]

        for (
        i in 1 until values.size
        ) {

            if (
                values[i] >
                maximum
            ) {

                maximum =
                    values[i]
            }
        }

        return maximum
    }

    private fun calculateMedian(
        values: FloatArray
    ): Float {

        if (
            values.isEmpty()
        ) {
            return 0f
        }

        val sorted =
            values.copyOf()

        sorted.sort()

        val middle =
            sorted.size / 2

        return if (
            sorted.size % 2 == 0
        ) {

            (
                    sorted[middle - 1] +
                            sorted[middle]
                    ) / 2f

        } else {

            sorted[middle]
        }
    }

    // ============================================================
    // HISTOGRAM HELPERS
    // ============================================================

    private fun histogramBin(
        value: Int,
        rangeMax: Int,
        numberOfBins: Int
    ): Int {

        var bin =
            value *
                    numberOfBins /
                    rangeMax

        if (
            bin < 0
        ) {
            bin = 0
        }

        if (
            bin >= numberOfBins
        ) {
            bin =
                numberOfBins - 1
        }

        return bin
    }

    private fun histogramBin(
        value: Float,
        rangeMax: Int,
        numberOfBins: Int
    ): Int {

        var bin =
            (
                    value *
                            numberOfBins /
                            rangeMax.toFloat()
                    ).toInt()

        if (
            bin < 0
        ) {
            bin = 0
        }

        if (
            bin >= numberOfBins
        ) {
            bin =
                numberOfBins - 1
        }

        return bin
    }

    private fun normalizeL2(
        histogram: FloatArray
    ) {

        var sumSquares =
            0.0

        for (
        value in histogram
        ) {

            sumSquares +=
                value.toDouble() *
                        value.toDouble()
        }

        val norm =
            sqrt(
                sumSquares
            )

        if (
            norm > 0.0
        ) {

            for (
            i in histogram.indices
            ) {

                histogram[i] =
                    (
                            histogram[i] /
                                    norm
                            ).toFloat()
            }
        }
    }

    // ============================================================
    // RGB → OPENCV HSV
    // ============================================================

    private fun rgbToOpenCvHsv(
        red: Int,
        green: Int,
        blue: Int
    ): FloatArray {

        /*
         * OpenCV-compatible 8-bit HSV conversion.
         *
         * Input:
         *   R, G, B = 0..255
         *
         * Output:
         *   H = 0..179
         *   S = 0..255
         *   V = 0..255
         */

        val r = red.coerceIn(0, 255)
        val g = green.coerceIn(0, 255)
        val b = blue.coerceIn(0, 255)

        val maxValue = max(r, max(g, b))
        val minValue = min(r, min(g, b))
        val delta = maxValue - minValue

        // Value
        val value = maxValue

        // Saturation
        val saturation =
            if (maxValue == 0) {
                0
            } else {
                kotlin.math.floor(
                    delta.toDouble() * 255.0 /
                            maxValue.toDouble() +
                            0.5
                ).toInt()
            }

        // Hue
        val hue =
            if (delta == 0) {
                0
            } else {

                val hueDegrees: Double

                if (maxValue == r) {

                    hueDegrees =
                        60.0 *
                                (g - b).toDouble() /
                                delta.toDouble()

                } else if (maxValue == g) {

                    hueDegrees =
                        60.0 *
                                (
                                        (b - r).toDouble() /
                                                delta.toDouble() +
                                                2.0
                                        )

                } else {

                    hueDegrees =
                        60.0 *
                                (
                                        (r - g).toDouble() /
                                                delta.toDouble() +
                                                4.0
                                        )
                }

                var normalizedHue = hueDegrees

                if (normalizedHue < 0.0) {
                    normalizedHue += 360.0
                }

                kotlin.math.floor(
                    normalizedHue / 2.0 + 0.5
                ).toInt() % 180
            }

        return floatArrayOf(
            hue.toFloat(),
            saturation.toFloat(),
            value.toFloat()
        )
    }

    // ============================================================
    // FEATURE COUNT VALIDATION
    // ============================================================

    fun validateFeatureCounts(
        bitmap: Bitmap
    ): String {

        val color =
            extractColorFeatures(
                bitmap
            )

        val texture =
            extractTextureFeatures(
                bitmap
            )

        val hog =
            extractHogFeatures(
                bitmap
            )

        val statistical =
            extractStatisticalFeatures(
                bitmap
            )

        val total =
            color.size +
                    texture.size +
                    hog.size +
                    statistical.size

        return buildString {

            appendLine(
                "===== FRUIT QUALITY FEATURE VALIDATION ====="
            )

            appendLine(
                "Color features: ${color.size}"
            )

            appendLine(
                "Texture features: ${texture.size}"
            )

            appendLine(
                "HOG features: ${hog.size}"
            )

            appendLine(
                "Statistical features: ${statistical.size}"
            )

            appendLine(
                "Total features: $total"
            )

            appendLine(
                "Expected features: 1993"
            )

            appendLine(
                "Feature dimension: " +
                        if (
                            total == 1993
                        ) {
                            "PASS"
                        } else {
                            "FAIL"
                        }
            )
        }
    }

    // ============================================================
    // ALL FEATURES
    // ============================================================

    fun extractAllFeatures(
        bitmap: Bitmap
    ): FloatArray {

        val colorFeatures =
            extractColorFeatures(
                bitmap
            )

        val textureFeatures =
            extractTextureFeatures(
                bitmap
            )

        val hogFeatures =
            extractHogFeatures(
                bitmap
            )

        val statisticalFeatures =
            extractStatisticalFeatures(
                bitmap
            )

        val combinedFeatures =
            FloatArray(
                colorFeatures.size +
                        textureFeatures.size +
                        hogFeatures.size +
                        statisticalFeatures.size
            )

        var index =
            0

        for (
        value in colorFeatures
        ) {

            combinedFeatures[index] =
                value

            index++
        }

        for (
        value in textureFeatures
        ) {

            combinedFeatures[index] =
                value

            index++
        }

        for (
        value in hogFeatures
        ) {

            combinedFeatures[index] =
                value

            index++
        }

        for (
        value in statisticalFeatures
        ) {

            combinedFeatures[index] =
                value

            index++
        }

        require(
            combinedFeatures.size ==
                    FEATURE_COUNT
        ) {
            "Expected $FEATURE_COUNT features, " +
                    "got ${combinedFeatures.size}"
        }

        return combinedFeatures
    }

    // ============================================================
    // FEATURE BLOCK VALIDATION
    // ============================================================

    fun validateFeatureBlocks(
        bitmap: Bitmap
    ): String {

        val colorFeatures =
            extractColorFeatures(
                bitmap
            )

        val textureFeatures =
            extractTextureFeatures(
                bitmap
            )

        val hogFeatures =
            extractHogFeatures(
                bitmap
            )

        val statisticalFeatures =
            extractStatisticalFeatures(
                bitmap
            )
        Log.d(
            "FruitQualityStatistics",
            """
    ===== STATISTICAL FEATURE VALUES =====
    Count: ${statisticalFeatures.size}
    ===== GRAY FEATURES =====
    feature[0]  = ${statisticalFeatures[0]}
    feature[1]  = ${statisticalFeatures[1]}
    feature[2]  = ${statisticalFeatures[2]}
    feature[3]  = ${statisticalFeatures[3]}
    
    ===== BGR FEATURES =====
    feature[4]  = ${statisticalFeatures[4]}
    feature[5]  = ${statisticalFeatures[5]}
    feature[6]  = ${statisticalFeatures[6]}
    
    feature[7]  = ${statisticalFeatures[7]}
    feature[8]  = ${statisticalFeatures[8]}
    feature[9]  = ${statisticalFeatures[9]}
    
    feature[10] = ${statisticalFeatures[10]}
    feature[11] = ${statisticalFeatures[11]}
    feature[12] = ${statisticalFeatures[12]}
    
    ===== HSV FEATURES =====
    feature[13] = ${statisticalFeatures[13]}
    feature[14] = ${statisticalFeatures[14]}
    
    feature[15] = ${statisticalFeatures[15]}
    feature[16] = ${statisticalFeatures[16]}
    
    feature[17] = ${statisticalFeatures[17]}
    feature[18] = ${statisticalFeatures[18]}
    
    ===== END STATISTICAL FEATURE VALUES =====
    """.trimIndent()
        )

        fun sumAsDouble(
            values: FloatArray
        ): Double {

            var total =
                0.0

            for (
            value in values
            ) {

                total +=
                    value.toDouble()
            }

            return total
        }

        fun absoluteSumAsDouble(
            values: FloatArray
        ): Double {

            var total =
                0.0

            for (
            value in values
            ) {

                total +=
                    kotlin.math.abs(
                        value.toDouble()
                    )
            }

            return total
        }

        fun weightedSumAsDouble(
            values: FloatArray
        ): Double {

            var total =
                0.0

            for (
            index in values.indices
            ) {

                total +=
                    (index + 1).toDouble() *
                            values[index].toDouble()
            }

            return total
        }

        return buildString {

            appendLine(
                "===== FRUIT QUALITY BLOCK VALIDATION ====="
            )

            appendLine()

            appendLine(
                "COLOR FEATURES"
            )

            appendLine(
                "Count: ${colorFeatures.size}"
            )

            appendLine(
                "Sum: " +
                        sumAsDouble(
                            colorFeatures
                        )
            )

            appendLine(
                "Absolute sum: " +
                        absoluteSumAsDouble(
                            colorFeatures
                        )
            )

            appendLine(
                "Minimum: " +
                        colorFeatures.minOrNull()
            )

            appendLine(
                "Maximum: " +
                        colorFeatures.maxOrNull()
            )

            appendLine(
                "Weighted sum: " +
                        weightedSumAsDouble(
                            colorFeatures
                        )
            )

            appendLine()

            appendLine(
                "TEXTURE FEATURES"
            )

            appendLine(
                "Count: ${textureFeatures.size}"
            )

            appendLine(
                "Sum: " +
                        sumAsDouble(
                            textureFeatures
                        )
            )

            appendLine(
                "Absolute sum: " +
                        absoluteSumAsDouble(
                            textureFeatures
                        )
            )

            appendLine(
                "Minimum: " +
                        textureFeatures.minOrNull()
            )

            appendLine(
                "Maximum: " +
                        textureFeatures.maxOrNull()
            )

            appendLine(
                "Weighted sum: " +
                        weightedSumAsDouble(
                            textureFeatures
                        )
            )

            appendLine()

            appendLine(
                "HOG FEATURES"
            )

            appendLine(
                "Count: ${hogFeatures.size}"
            )

            appendLine(
                "Sum: " +
                        sumAsDouble(
                            hogFeatures
                        )
            )

            appendLine(
                "Absolute sum: " +
                        absoluteSumAsDouble(
                            hogFeatures
                        )
            )

            appendLine(
                "Minimum: " +
                        hogFeatures.minOrNull()
            )

            appendLine(
                "Maximum: " +
                        hogFeatures.maxOrNull()
            )

            appendLine(
                "Weighted sum: " +
                        weightedSumAsDouble(
                            hogFeatures
                        )
            )

            appendLine()

            appendLine(
                "STATISTICAL FEATURES"
            )

            appendLine(
                "Count: ${statisticalFeatures.size}"
            )

            appendLine(
                "Sum: " +
                        sumAsDouble(
                            statisticalFeatures
                        )
            )

            appendLine(
                "Absolute sum: " +
                        absoluteSumAsDouble(
                            statisticalFeatures
                        )
            )

            appendLine(
                "Minimum: " +
                        statisticalFeatures.minOrNull()
            )

            appendLine(
                "Maximum: " +
                        statisticalFeatures.maxOrNull()
            )

            appendLine(
                "Weighted sum: " +
                        weightedSumAsDouble(
                            statisticalFeatures
                        )
            )

            appendLine()

            appendLine(
                "===== END BLOCK VALIDATION ====="
            )
        }
    }
}