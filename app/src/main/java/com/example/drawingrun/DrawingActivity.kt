package com.example.drawingrun

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import android.graphics.Color
import android.view.MotionEvent
import android.widget.FrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DrawingActivity : BaseActivity(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var mMap: GoogleMap
    private val routePolylines = mutableListOf<Polyline>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: LatLng? = null
    private var selectedRoute: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_activity)

        setupToolbarWithProfileAlwaysBack()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 예측 결과 수신 처리
        supportFragmentManager.setFragmentResultListener("prediction_result", this) { _, bundle ->
            val shape = bundle.getString("shape")
            if (shape != null) {
                Toast.makeText(this, "도형 \"$shape\" 경로를 불러옵니다...", Toast.LENGTH_SHORT).show()
                loadPolylineFromFirestore(shape)
            }
        }

        findViewById<FrameLayout>(R.id.btn_start_running).setOnClickListener {
            selectedRoute?.let {
                Toast.makeText(this, "🚀 운동 시작!", Toast.LENGTH_SHORT).show()
                Log.d("StartRunning", "📍 선택된 경로: ${it.points}")

                val intent = Intent(this, RunningActivity::class.java)
                val pointList = ArrayList(it.points)
                intent.putParcelableArrayListExtra("selected_route_points", pointList)
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "❌ 먼저 경로를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

// 드로잉 FAB 가져오기
        val fab = findViewById<FloatingActionButton>(R.id.fab_drawing)

// 드래그 이동 처리
        fab.setOnTouchListener(object : View.OnTouchListener {
            var dX = 0f
            var dY = 0f
            var startRawX = 0f
            var startRawY = 0f
            val clickThreshold = 10f

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                        startRawX = event.rawX
                        startRawY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // 계산된 위치
                        val newX = event.rawX + dX
                        val newY = event.rawY + dY
                        view.animate().x(newX).y(newY).setDuration(0).start()
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        val dx = event.rawX - startRawX
                        val dy = event.rawY - startRawY
                        val distance = Math.hypot(dx.toDouble(), dy.toDouble())

                        // 화면 크기 가져오기
                        val screenWidth = resources.displayMetrics.widthPixels
                        val screenHeight = resources.displayMetrics.heightPixels
                        val fabWidth = view.width
                        val fabHeight = view.height

                        // 현재 FAB 위치
                        var finalX = view.x
                        var finalY = view.y

                        // 👉 X 방향: 왼쪽 또는 오른쪽 가장자리로 스냅
                        finalX = if (finalX + fabWidth / 2 < screenWidth / 2) {
                            2f  // 왼쪽 여백
                        } else {
                            (screenWidth - fabWidth - 2).toFloat()  // 오른쪽 여백
                        }

                        // 👉 Y 방향: 상단/하단 경계 내로 제한
                        val topLimit = 100f
                        val bottomLimit = (screenHeight - fabHeight - 100).toFloat()
                        finalY = finalY.coerceIn(topLimit, bottomLimit)

                        // 이동 애니메이션
                        view.animate().x(finalX).y(finalY).setDuration(200).start()

                        // 클릭으로 간주할지 판단
                        if (distance < clickThreshold) {
                            view.performClick()
                        }

                        return true
                    }
                }
                return false
            }
        })

        // 삭제 xxxxxxxxx
        fab.setOnClickListener {
            val fragment = DrawingBottomSheetFragment()
            fragment.show(supportFragmentManager, fragment.tag)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        val defaultLatLng = LatLng(37.6067, 127.0232)
        val startLatLng = currentLocation ?: defaultLatLng
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 14f))

        mMap.setOnPolylineClickListener { clicked ->
            selectedRoute = clicked

            // 선택 효과: 선택된 경로만 굵고 색상 진하게, 나머지는 흐리게
            routePolylines.forEach {
                if (it == clicked) {
                    it.color = Color.MAGENTA
                    it.width = 14f
                } else {
                    it.color = Color.rgb(100, 150, 160)
                    it.width = 6f
                }
            }

            Log.d("RouteSelect", "✅ 선택된 경로 index: ${clicked.tag}")
            Toast.makeText(this, "✅ 경로가 선택되었습니다!", Toast.LENGTH_SHORT).show()
        }

        Log.d("MapReady", "✅ 지도 준비 완료")
    }

    private fun loadPolylineFromFirestore(label: String) {
        val db = FirebaseFirestore.getInstance()

        // 기존 경로 제거
        routePolylines.forEach { it.remove() }
        routePolylines.clear()
        selectedRoute = null

        db.collection("route")
            .whereEqualTo("label", label)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "❌ \"$label\" 경로가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                documents.forEachIndexed { index, doc ->
                    val geoPoints = doc["points"] as? List<GeoPoint>
                    if (geoPoints != null) {
                        val latLngList = geoPoints.map { LatLng(it.latitude, it.longitude) }

                        val polyline = mMap.addPolyline(
                            PolylineOptions()
                                .addAll(latLngList)
                                .color(Color.GRAY)
                                .width(8f)
                                .clickable(true)
                        )

                        // 식별용 태그 저장
                        polyline.tag = index
                        routePolylines.add(polyline)

                        if (index == 0) {
                            // 카메라 처음 위치
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList.first(), 15f))
                        }
                    }
                }

                Toast.makeText(this, "총 ${routePolylines.size}개의 \"$label\" 경로가 지도에 표시되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Firebase 오류: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkLocationPermission() {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocation != PackageManager.PERMISSION_GRANTED || coarseLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}
