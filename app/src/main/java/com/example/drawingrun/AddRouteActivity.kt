package com.example.drawingrun

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.auth.FirebaseAuth // 추가
import com.google.firebase.Timestamp // 추가



class AddRouteActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var touchOverlayView: TouchOverlayView

    private lateinit var drawButton: Button
    private lateinit var deleteButton: Button
    private lateinit var saveButton: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_route)

        // 지도 프래그먼트 준비
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        touchOverlayView = findViewById(R.id.touchOverlay)
        drawButton = findViewById(R.id.btnDraw)
        deleteButton = findViewById(R.id.btnDelete)
        saveButton = findViewById(R.id.btnSave)

        // 초기 모드는 NONE (지도 이동 가능)
        touchOverlayView.mode = Mode.NONE
        updateButtonUI()

        drawButton.setOnClickListener {
            touchOverlayView.mode = if (touchOverlayView.mode != Mode.DRAW) Mode.DRAW else Mode.NONE
            updateButtonUI()
        }

        deleteButton.setOnClickListener {
            touchOverlayView.mode = if (touchOverlayView.mode != Mode.DELETE) Mode.DELETE else Mode.NONE
            updateButtonUI()
        }

        saveButton.setOnClickListener {
            val allPoints = touchOverlayView.getAllPoints()
            if (allPoints.isEmpty()) {
                Toast.makeText(this, "저장할 경로가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val geoPoints = allPoints.map { GeoPoint(it.latitude, it.longitude) }
            val userId = auth.currentUser?.uid ?: "unknown_user"

            // UID와 타임스탬프 함께 저장
            val routeData = hashMapOf(
                "points" to geoPoints,
                "userId" to userId,
                "createdAt" to Timestamp.now()
            )

            firestore.collection("routes")
                .add(routeData)
                .addOnSuccessListener {
                    Toast.makeText(this, "경로 저장 완료!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        touchOverlayView.map = mMap

        // 지도가 움직일 때마다 다시 그려지게
        mMap.setOnCameraMoveListener {
            touchOverlayView.invalidate()
        }

        val seoul = LatLng(37.5665, 126.9780)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))
    }

    private fun updateButtonUI() {
        val activeColor = Color.parseColor("#2196F3") // 파란색
        val inactiveColor = Color.parseColor("#CCCCCC") // 연한 회색

        drawButton.setBackgroundColor(if (touchOverlayView.mode == Mode.DRAW) activeColor else inactiveColor)
        deleteButton.setBackgroundColor(if (touchOverlayView.mode == Mode.DELETE) activeColor else inactiveColor)
    }
}
