package com.example.drawingrun

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.Projection
import kotlin.math.pow
import kotlin.math.sqrt

enum class Mode {
    NONE, DRAW, DELETE
}

class TouchOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var map: GoogleMap? = null
        set(value) {
            field = value
            invalidate()
        }

    var mode: Mode = Mode.NONE

    private val pathPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val deleteCirclePaint = Paint().apply {
        color = Color.argb(80, 255, 0, 0)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val pathSegments = mutableListOf<MutableList<LatLng>>()
    private var currentSegment = mutableListOf<LatLng>()
    private var deleteTouchPoint: PointF? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val proj = map?.projection ?: return

        for (segment in pathSegments) {
            if (segment.isEmpty()) continue
            val path = Path()
            val firstScreenPoint = proj.toScreenLocation(segment[0])
            path.moveTo(firstScreenPoint.x.toFloat(), firstScreenPoint.y.toFloat())
            for (point in segment.drop(1)) {
                val screenPoint = proj.toScreenLocation(point)
                path.lineTo(screenPoint.x.toFloat(), screenPoint.y.toFloat())
            }
            canvas.drawPath(path, pathPaint)
        }

        if (currentSegment.isNotEmpty()) {
            val path = Path()
            val firstScreenPoint = proj.toScreenLocation(currentSegment[0])
            path.moveTo(firstScreenPoint.x.toFloat(), firstScreenPoint.y.toFloat())
            for (point in currentSegment.drop(1)) {
                val screenPoint = proj.toScreenLocation(point)
                path.lineTo(screenPoint.x.toFloat(), screenPoint.y.toFloat())
            }
            canvas.drawPath(path, pathPaint)
        }

        if (mode == Mode.DELETE && deleteTouchPoint != null) {
            val radius = getDeleteRadius()
            canvas.drawCircle(deleteTouchPoint!!.x, deleteTouchPoint!!.y, radius, deleteCirclePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val proj = map?.projection ?: return false

        when (mode) {
            Mode.NONE -> return false

            Mode.DRAW -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        currentSegment = mutableListOf()
                        currentSegment.add(proj.fromScreenLocation(Point(x.toInt(), y.toInt())))
                        invalidate()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        currentSegment.add(proj.fromScreenLocation(Point(x.toInt(), y.toInt())))
                        invalidate()
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (currentSegment.size > 1) {
                            pathSegments.add(currentSegment)
                        }
                        currentSegment = mutableListOf()
                        invalidate()
                        return true
                    }
                }
            }

            Mode.DELETE -> {
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    deleteTouchPoint = PointF(x, y)
                    val radiusPixels = getDeleteRadius()
                    val latLngTouch = proj.fromScreenLocation(Point(x.toInt(), y.toInt()))

                    val newPathSegments = mutableListOf<MutableList<LatLng>>()

                    for (segment in pathSegments) {
                        var tempSegment = mutableListOf<LatLng>()

                        // 화면 좌표로 변환해 놓기 (최적화)
                        val segmentScreenPoints = segment.map { proj.toScreenLocation(it) }

                        for (i in segment.indices) {
                            val screenPt = segmentScreenPoints[i]

                            // 픽셀 거리 계산 - 빠름
                            val distPixels = distance(deleteTouchPoint!!, PointF(screenPt.x.toFloat(), screenPt.y.toFloat()))

                            if (distPixels > radiusPixels) {
                                tempSegment.add(segment[i])
                            } else {
                                // 픽셀 거리 내에 있으면 위도/경도 미터 단위 정확한 거리 재서 2중 필터링
                                val distMeters = distanceInMeters(latLngTouch, segment[i])
                                if (distMeters > getDeleteRadiusInMeters(proj, x, y)) {
                                    tempSegment.add(segment[i])
                                } else {
                                    // 삭제 영역 안에 들어가면 segment 자르기
                                    if (tempSegment.size >= 2) {
                                        newPathSegments.add(tempSegment)
                                    }
                                    tempSegment = mutableListOf()
                                }
                            }
                        }
                        if (tempSegment.size >= 2) {
                            newPathSegments.add(tempSegment)
                        }
                    }
                    pathSegments.clear()
                    pathSegments.addAll(newPathSegments)
                    invalidate()
                    return true
                } else if (event.action == MotionEvent.ACTION_UP) {
                    deleteTouchPoint = null
                    invalidate()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun getAllPoints(): List<LatLng> = pathSegments.flatten()

    private fun distance(p1: PointF, p2: PointF): Float {
        return sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
    }

    private fun distanceInMeters(p1: LatLng, p2: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude, p2.latitude, p2.longitude, results
        )
        return results[0].toDouble()
    }

    private fun getDeleteRadius(): Float {
        val zoom = map?.cameraPosition?.zoom ?: 15f
        return when {
            zoom >= 18 -> 40f
            zoom >= 16 -> 60f
            zoom >= 14 -> 80f
            else -> 100f
        }
    }

    private fun getDeleteRadiusInMeters(proj: Projection, x: Float, y: Float): Double {
        val center = proj.fromScreenLocation(Point(x.toInt(), y.toInt()))
        val edge = proj.fromScreenLocation(Point((x + getDeleteRadius()).toInt(), y.toInt()))
        return distanceInMeters(center, edge)
    }

    fun onMapMoved() {
        invalidate()
    }
}
