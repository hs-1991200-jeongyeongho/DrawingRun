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
            val visualRadius = getDeleteRadius() * 1.1f // 빨간 원은 1.1배 크게
            canvas.drawCircle(deleteTouchPoint!!.x, deleteTouchPoint!!.y, visualRadius, deleteCirclePaint)
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
                    val radiusPixels = getDeleteRadius() * 0.8f // 실제 삭제는 0.8배 작게
                    val touchLatLng = proj.fromScreenLocation(Point(x.toInt(), y.toInt()))

                    val newPathSegments = mutableListOf<MutableList<LatLng>>()

                    for (segment in pathSegments) {
                        if (segment.isEmpty()) continue
                        val startScreen = proj.toScreenLocation(segment.first())
                        val endScreen = proj.toScreenLocation(segment.last())

                        val distToStart = distance(deleteTouchPoint!!, PointF(startScreen.x.toFloat(), startScreen.y.toFloat()))
                        val distToEnd = distance(deleteTouchPoint!!, PointF(endScreen.x.toFloat(), endScreen.y.toFloat()))

                        val mutableSegment = segment.toMutableList()
                        when {
                            distToEnd <= radiusPixels -> {
                                // 끝점이 삭제 반경 안에 있으면 삭제 반경만큼 부드럽게 지우기
                                removeFromEndWithRadiusSmooth(mutableSegment, radiusPixels, proj)
                            }
                            distToStart <= radiusPixels -> {
                                // 시작점이 삭제 반경 안에 있으면 삭제 반경만큼 부드럽게 지우기
                                removeFromStartWithRadiusSmooth(mutableSegment, radiusPixels, proj)
                            }
                        }

                        if (mutableSegment.size >= 2) {
                            newPathSegments.add(mutableSegment)
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

    // 부드러운 보간 삭제: 끝점부터
    private fun removeFromEndWithRadiusSmooth(segment: MutableList<LatLng>, radiusPixels: Float, proj: Projection) {
        var remainingRadiusMeters = pixelsToMeters(radiusPixels, segment.last(), proj)
        val stepMeters = 0.5 // 0.5m 단위로 쪼개서 보간 (보간 점 간격)

        while (segment.size > 1 && remainingRadiusMeters > 0) {
            val last = segment.last()
            val secondLast = segment[segment.size - 2]
            val dist = distanceInMeters(secondLast, last)

            if (dist <= stepMeters) {
                // 점 제거
                segment.removeAt(segment.size - 1)
                remainingRadiusMeters -= dist
            } else {
                // 일정 step 거리만큼 보간해서 새로운 끝점 생성
                val ratio = stepMeters / dist
                val newLat = secondLast.latitude + (last.latitude - secondLast.latitude) * ratio
                val newLng = secondLast.longitude + (last.longitude - secondLast.longitude) * ratio
                segment[segment.size - 1] = LatLng(newLat, newLng)
                remainingRadiusMeters -= stepMeters
            }
        }
    }

    // 부드러운 보간 삭제: 시작점부터
    private fun removeFromStartWithRadiusSmooth(segment: MutableList<LatLng>, radiusPixels: Float, proj: Projection) {
        var remainingRadiusMeters = pixelsToMeters(radiusPixels, segment.first(), proj)
        val stepMeters = 1.0 // 1m 단위로 쪼개서 보간 (값 조절 가능)

        while (segment.size > 1 && remainingRadiusMeters > 0) {
            val first = segment.first()
            val second = segment[1]
            val dist = distanceInMeters(first, second)

            if (dist <= stepMeters) {
                // 점 제거
                segment.removeAt(0)
                remainingRadiusMeters -= dist
            } else {
                // 일정 step 거리만큼 보간해서 새로운 시작점 생성
                val ratio = stepMeters / dist
                val newLat = first.latitude + (second.latitude - first.latitude) * ratio
                val newLng = first.longitude + (second.longitude - first.longitude) * ratio
                segment[0] = LatLng(newLat, newLng)
                remainingRadiusMeters -= stepMeters
            }
        }
    }

    private fun pixelsToMeters(pixels: Float, latLng: LatLng, proj: Projection): Double {
        val center = proj.toScreenLocation(latLng)
        val edge = Point((center.x + pixels).toInt(), center.y)
        val latLngEdge = proj.fromScreenLocation(edge)
        return distanceInMeters(latLng, latLngEdge)
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
            zoom >= 16 -> 50f
            zoom >= 14 -> 60f
            else -> 80f
        }
    }

    fun onMapMoved() {
        invalidate()
    }
}
