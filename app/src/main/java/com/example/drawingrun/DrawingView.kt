package com.example.drawingrun

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val path = Path()
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val points = mutableListOf<Pair<Float, Float>>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        // 스무딩: 이전 점과 5픽셀 미만 거리는 무시
        if (points.isNotEmpty()) {
            val lastPoint = points.last()
            val distance = sqrt(((x - lastPoint.first) * (x - lastPoint.first) +
                    (y - lastPoint.second) * (y - lastPoint.second)).toDouble())
            if (distance < 5.0) return true
        }

        Log.d("DrawingView", "Point: ($x, $y)")

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                points.add(x to y)
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                points.add(x to y)
            }
        }
        invalidate()
        return true
    }

    fun clearDrawing() {
        path.reset()
        points.clear()
        invalidate()
    }

    private fun getNormalizedPoints(): List<Pair<Float, Float>> {
        if (points.isEmpty()) return emptyList()
        Log.d("DrawingView", "Raw Points: ${points.take(10).joinToString()}")
        val minX = points.minOf { it.first }
        val maxX = points.maxOf { it.first }
        val minY = points.minOf { it.second }
        val maxY = points.maxOf { it.second }
        val width = maxX - minX
        val height = maxY - minY
        val normalized = points.map { (x, y) ->
            val normX = if (width != 0f) (x - minX) / width else 0f
            val normY = if (height != 0f) (y - minY) / height else 0f
            normX to normY
        }
        Log.d("DrawingView", "Normalized Points: ${normalized.take(10).joinToString()}")
        return normalized
    }

    fun processForModel(): FloatArray {
        if (points.isEmpty()) {
            Log.d("DrawingView", "No points available")
            return FloatArray(0)
        }

        val tempBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val drawPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 10f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        tempCanvas.drawColor(Color.WHITE)
        val normalizedPoints = getNormalizedPoints()
        val drawPath = Path()
        if (normalizedPoints.isNotEmpty()) {
            drawPath.moveTo(normalizedPoints[0].first * 299, normalizedPoints[0].second * 299)
            for (i in 1 until normalizedPoints.size) {
                drawPath.lineTo(normalizedPoints[i].first * 299, normalizedPoints[i].second * 299)
            }
            tempCanvas.drawPath(drawPath, drawPaint)
        }

        val resizedBitmap = Bitmap.createScaledBitmap(tempBitmap, 28, 28, true)

        // 비트맵 저장
        val file = File(context.getExternalFilesDir(null), "preprocessed_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        Log.d("DrawingView", "Preprocessed image saved to: ${file.absolutePath}")

        val pixels = IntArray(28 * 28)
        resizedBitmap.getPixels(pixels, 0, 28, 0, 0, 28, 28)
        val floatArray = FloatArray(28 * 28)
        for (i in pixels.indices) {
            val r = Color.red(pixels[i])
            val g = Color.green(pixels[i])
            val b = Color.blue(pixels[i])
            val gray = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
            floatArray[i] = 1f - (gray / 255f)
        }

        Log.d("DrawingView", "Input Data Sample: ${floatArray.take(10).joinToString()}")
        return floatArray
    }

    fun predictWithModel(interpreter: Interpreter): String {
        Log.d("DrawingView", "predictWithModel called")
        val inputData = processForModel()
        if (inputData.isEmpty()) return "No Data"

        val input = Array(1) { Array(28) { Array(28) { FloatArray(1) } } }
        for (i in 0 until 28) {
            for (j in 0 until 28) {
                input[0][i][j][0] = inputData[i * 28 + j]
            }
        }

        val output = Array(1) { FloatArray(16) }
        interpreter.run(input, output)

        val classes = arrayOf(
            "circle", "square", "triangle", "diamond", "star", "heart", "moon", "cup",
            "hand", "house", "spoon", "fork", "eyeglasses", "butterfly", "grapes", "light bulb"
        )

        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        Log.d("DrawingView", "Output: ${output[0].joinToString()}")
        Log.d("DrawingView", "Class Scores: ${
            classes.zip(output[0].toList()).joinToString { "${it.first}: ${it.second}" }
        }")
        Log.d("DrawingView", "Predicted Index: $predictedIndex, Class: ${if (predictedIndex in classes.indices) classes[predictedIndex] else "Invalid"}")

        return if (predictedIndex in classes.indices) {
            classes[predictedIndex]
        } else {
            "Invalid Prediction"
        }
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
}