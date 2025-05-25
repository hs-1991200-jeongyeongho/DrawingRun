package com.example.drawingrun

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class DrawingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var labelMenu: LinearLayout
    private lateinit var startButton: FrameLayout
    private val db = FirebaseFirestore.getInstance()

    private val routePolylines = mutableListOf<Polyline>()
    private var selectedRoute: Polyline? = null

    // ✅ 위치 관련 추가
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_activity)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLastLocation() // ✅ 현재 위치 요청

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        labelMenu = findViewById(R.id.label_menu)
        startButton = findViewById(R.id.btn_start_running)

        startButton.setOnClickListener {
            Toast.makeText(this, "러닝 시작 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }

        fetchAndDisplayLabelButtons()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnPolylineClickListener { clicked ->
            selectedRoute = clicked
            routePolylines.forEach {
                if (it == clicked) {
                    it.color = Color.MAGENTA
                    it.width = 14f
                } else {
                    it.color = Color.rgb(100, 150, 160)
                    it.width = 6f
                }
            }

            val routeDocId = clicked.tag as? String
            if (routeDocId != null) {
                FirebaseFirestore.getInstance()
                    .collection("route")
                    .document(routeDocId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val label = doc.getString("label") ?: "알 수 없음"
                        val labelKr = doc.getString("label_kr") ?: "이름 없음"
                        val distance = doc.getDouble("distance") ?: 0.0

                        val infoText = """
                🏷️ 라벨: $label
                📏 거리: ${String.format("%.2f", distance)} km
            """.trimIndent()

                        val dialog = RouteInfoDialog.newInstance(labelKr, infoText)
                        dialog.show(supportFragmentManager, "route_info_dialog")
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "❌ 설명 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
            }

        }
    }

    private fun requestLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    lastLocation = location
                    android.util.Log.d("LocationCheck", "현재 위치: ${location.latitude}, ${location.longitude}")
                } else {
                    android.util.Log.d("LocationCheck", "위치 정보 없음")
                }
            }
    }

    private fun fetchAndDisplayLabelButtons() {
        db.collection("route")
            .get()
            .addOnSuccessListener { documents ->
                val labelMap = linkedMapOf<String, String>() // label -> label_kr

                for (doc in documents) {
                    val label = doc.getString("label")
                    val labelKr = doc.getString("label_kr")

                    if (label != null && labelKr != null && !labelMap.containsKey(label)) {
                        labelMap[label] = labelKr
                    }
                }

                labelMenu.removeAllViews()

                for ((label, labelKr) in labelMap) {
                    val button = Button(this).apply {
                        text = labelKr
                        textSize = 12f
                        setBackgroundColor(Color.parseColor("#F5F5F5"))
                        setTextColor(Color.BLACK)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(4, 4, 4, 4)
                        }
                        setOnClickListener {
                            loadPolylineFromFirestore(label)
                        }
                    }
                    labelMenu.addView(button)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "라벨 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPolylineFromFirestore(label: String) {
        db.collection("route")
            .whereEqualTo("label", label)
            .get()
            .addOnSuccessListener { documents ->
                routePolylines.forEach { it.remove() }
                routePolylines.clear()
                selectedRoute = null

                val routeList = mutableListOf<Pair<String, List<LatLng>>>()

                documents.forEachIndexed { index, doc ->
                    val geoPoints = doc["points"] as? List<GeoPoint> ?: return@forEachIndexed
                    val latLngList = geoPoints.map { LatLng(it.latitude, it.longitude) }

                    routeList.add(Pair("경로 $index", latLngList))

                    val polyline = mMap.addPolyline(
                        PolylineOptions()
                            .addAll(latLngList)
                            .color(Color.MAGENTA)
                            .width(14f)
                            .clickable(true)
                    )

                    routePolylines.add(polyline)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList.first(), 15f))
                }

                // ✅ 거리 계산: 현재 위치가 존재할 경우에만 계산
                lastLocation?.let { currentLocation ->
                    for ((name, points) in routeList) {
                        val distance = DistanceUtils.calculateMinDistanceToRoute(currentLocation, points)
                        val formatted = DistanceUtils.formatDistance(distance)
                        android.util.Log.d("DistanceTest", "$name 와의 거리: $formatted")
                    }
                } ?: android.util.Log.d("DistanceTest", "lastLocation이 null이라 거리 계산 불가")
            }
            .addOnFailureListener {
                Toast.makeText(this, "경로 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // (선택) 권한 요청 결과 처리도 가능
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLastLocation()
        }
    }
}
