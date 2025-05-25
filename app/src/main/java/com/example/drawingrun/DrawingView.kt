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

    private fun getNormalizedPoints(padding: Boolean = false): List<Pair<Float, Float>> {
        if (points.isEmpty()) return emptyList()

        val minX = points.minOf { it.first }
        val maxX = points.maxOf { it.first }
        val minY = points.minOf { it.second }
        val maxY = points.maxOf { it.second }
        val width = maxX - minX
        val height = maxY - minY

        return points.map { (x, y) ->
            val normX = if (width != 0f) (x - minX) / width else 0.5f
            val normY = if (height != 0f) (y - minY) / height else 0.5f

            if (padding) {
                0.1f + 0.8f * normX to 0.1f + 0.8f * normY
            } else {
                normX to normY
            }
        }
    }


    fun processForModel(): FloatArray {
        if (points.isEmpty()) {
            Log.d("DrawingView", "No points available")
            return FloatArray(0)
        }

        val canvasSize = 300
        val tempBitmap = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        val drawPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 30f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        tempCanvas.drawColor(Color.WHITE)

        val normalizedPoints = getNormalizedPoints()
        Log.d("DrawingView", "✅ Normalized Points Count: ${normalizedPoints.size}")
        Log.d("DrawingView", "✅ First Norm Point: ${normalizedPoints.firstOrNull()}")

        val drawPath = Path()
        if (normalizedPoints.isNotEmpty()) {
            drawPath.moveTo(normalizedPoints[0].first * (canvasSize - 1), normalizedPoints[0].second * (canvasSize - 1))
            for (i in 1 until normalizedPoints.size) {
                drawPath.lineTo(normalizedPoints[i].first * (canvasSize - 1), normalizedPoints[i].second * (canvasSize - 1))
            }

            Log.d("DrawingView", "✅ Path ready, drawing to canvas now")
            tempCanvas.drawPath(drawPath, drawPaint)
        } else {
            Log.d("DrawingView", "❌ No normalized points to draw")
        }

        // 비트맵을 모델 입력 크기(96x96)로 리사이즈
        val resizedBitmap = Bitmap.createScaledBitmap(tempBitmap, 96, 96, true)

        // 저장해보기
        val file = File(context.getExternalFilesDir(null), "preprocessed_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        Log.d("DrawingView", "Preprocessed image saved to: ${file.absolutePath}")

        // 픽셀 -> FloatArray (흑백)
        val pixels = IntArray(96 * 96)
        resizedBitmap.getPixels(pixels, 0, 96, 0, 0, 96, 96)
        val floatArray = FloatArray(96 * 96)
        for (i in pixels.indices) {
            val r = Color.red(pixels[i])
            val g = Color.green(pixels[i])
            val b = Color.blue(pixels[i])
            val gray = (0.299f * r + 0.587f * g + 0.114f * b)
            floatArray[i] = 1f - (gray / 255f)
        }

        val avg = floatArray.average()
        Log.d("DrawingView", "✅ Input Data Sample: ${floatArray.take(10).joinToString()}")
        Log.d("DrawingView", "✅ Input Data Avg: $avg")

        return floatArray
    }




    fun predictWithModel(interpreter: Interpreter): String {
        Log.d("DrawingView", "predictWithModel called")
        val inputData = processForModel()
        if (inputData.isEmpty()) return "No Data"

        // 96x96 입력을 4차원 배열로 구성
        val input = Array(1) { Array(96) { Array(96) { FloatArray(1) } } }
        for (i in 0 until 96) {
            for (j in 0 until 96) {
                input[0][i][j][0] = inputData[i * 96 + j]
            }
        }

        val output = Array(1) { FloatArray(9) }  // 클래스 개수
        interpreter.run(input, output)

        val classes = arrayOf("circle", "square", "triangle", "diamond", "star", "hand", "butterfly", "key", "nail")

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