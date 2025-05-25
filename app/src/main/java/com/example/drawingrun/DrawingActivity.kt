package com.example.drawingrun

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class DrawingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var labelMenu: LinearLayout
    private lateinit var startButton: FrameLayout
    private val db = FirebaseFirestore.getInstance()

    private val routePolylines = mutableListOf<Polyline>()
    private var selectedRoute: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_activity)

        // 지도 초기화
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 뷰 참조
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
                            loadPolylineFromFirestore(label) // 실제 쿼리는 label(영문)로
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

                documents.forEach { doc ->
                    val geoPoints = doc["points"] as? List<GeoPoint> ?: return@forEach
                    val latLngList = geoPoints.map { LatLng(it.latitude, it.longitude) }

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
            }
            .addOnFailureListener {
                Toast.makeText(this, "경로 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
