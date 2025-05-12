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

class DrawingActivity : BaseActivity(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var mMap: GoogleMap
    private val routePolylines = mutableListOf<Polyline>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: LatLng? = null

    // ✅ 선택된 경로 저장 변수
    private var selectedRoute: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_activity)

        setupToolbarWithProfileAlwaysBack()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // ✅ "운동 시작" 버튼 클릭 시
        findViewById<Button>(R.id.btn_start_running).setOnClickListener {
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

        // "그림판 보기" 버튼 클릭 시, DrawingBottomSheetFragment 다이얼로그 표시
        findViewById<Button>(R.id.btn_show_drawing).setOnClickListener {
            val fragment = DrawingBottomSheetFragment()
            fragment.show(supportFragmentManager, fragment.tag)  // 다이얼로그 형태로 보여주기
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        val defaultLatLng = LatLng(37.6067, 127.0232)
        val startLatLng = currentLocation ?: defaultLatLng
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 14f))

        val route1 = listOf(
            LatLng(37.6067, 127.0232),
            LatLng(37.6075, 127.0255),
            LatLng(37.6082, 127.0270),
            LatLng(37.6070, 127.0285)
        )

        val route2 = listOf(
            LatLng(37.6067, 127.0232),
            LatLng(37.6050, 127.0225),
            LatLng(37.6040, 127.0210),
            LatLng(37.6035, 127.0195)
        )

        val route3 = listOf(
            LatLng(37.6067, 127.0232),
            LatLng(37.6062, 127.0248),
            LatLng(37.6055, 127.0265),
            LatLng(37.6048, 127.0280)
        )

        val polyline1 = mMap.addPolyline(
            PolylineOptions().addAll(route1).color(0xFF00AAFF.toInt()).width(10f).clickable(true)
        )
        val polyline2 = mMap.addPolyline(
            PolylineOptions().addAll(route2).color(0xFFFFAA00.toInt()).width(10f).clickable(true)
        )
        val polyline3 = mMap.addPolyline(
            PolylineOptions().addAll(route3).color(0xFF00CC66.toInt()).width(10f).clickable(true)
        )

        routePolylines.clear()
        routePolylines.addAll(listOf(polyline1, polyline2, polyline3))

        mMap.setOnPolylineClickListener { clicked ->
            selectedRoute = clicked
            routePolylines.forEach { it.isVisible = (it == clicked) }
            Log.d("RouteSelect", "✅ 경로 선택됨: ${clicked.points}")
        }

        Log.d("MapReady", "✅ 예시 경로 3개가 지도에 표시됨")
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
