package com.example.drawingrun

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.util.Log

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val path = Path()
    private val paint = Paint().apply {
        color = 0xFF000000.toInt()
        strokeWidth = 10f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    val points = mutableListOf<Pair<Float, Float>>() // 좌표 저장용

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        Log.d("DrawingView", "Point: ($x, $y)")  // ✅ 좌표 로그 출력 추가!

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
    // Step 1: 좌표 정규화 함수
    fun getNormalizedPoints(): List<Pair<Float, Float>> {
        if (points.isEmpty()) return emptyList()

        val minX = points.minOf { it.first }
        val maxX = points.maxOf { it.first }
        val minY = points.minOf { it.second }
        val maxY = points.maxOf { it.second }

        val width = maxX - minX
        val height = maxY - minY

        return points.map { (x, y) ->
            val normX = (x - minX) / width
            val normY = (y - minY) / height
            normX to normY
        }
    }
}
