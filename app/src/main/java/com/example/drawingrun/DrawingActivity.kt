package com.example.drawingrun

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
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
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var labelMenu: FrameLayout
    private lateinit var labelMenuInner: LinearLayout
    private lateinit var btnToggleSidebarOpen: ImageButton
    private lateinit var btnToggleSidebarClosed: ImageButton
    private lateinit var startButton: FrameLayout
    private val db = FirebaseFirestore.getInstance()

    private val routePolylines = mutableListOf<Polyline>()
    private var selectedRoute: Polyline? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_activity)

        Log.d("Lifecycle", "onCreate 호출됨")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLastLocation()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        drawerLayout = findViewById(R.id.drawer_layout)
        labelMenu = findViewById(R.id.label_menu)
        labelMenuInner = findViewById(R.id.label_menu_inner)
        btnToggleSidebarOpen = findViewById(R.id.btn_toggle_sidebar_open)
        btnToggleSidebarClosed = findViewById(R.id.btn_toggle_sidebar_closed)
        startButton = findViewById(R.id.btn_start_running)

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        btnToggleSidebarClosed.visibility = View.VISIBLE

        btnToggleSidebarClosed.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnToggleSidebarOpen.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                btnToggleSidebarOpen.visibility = View.VISIBLE
                btnToggleSidebarClosed.visibility = View.GONE
            }

            override fun onDrawerClosed(drawerView: View) {
                btnToggleSidebarOpen.visibility = View.GONE
                btnToggleSidebarClosed.visibility = View.VISIBLE
            }
        })

        drawerLayout.openDrawer(GravityCompat.START)
        btnToggleSidebarOpen.visibility = View.VISIBLE
        btnToggleSidebarClosed.visibility = View.GONE

        startButton.setOnClickListener {
            Toast.makeText(this, "러닝 시작 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }

        fetchAndDisplayLabelIcons()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("MapReady", "지도 준비 완료")
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnPolylineClickListener { clicked ->
            Log.d("MapReady", "Polyline 클릭됨")

            if (selectedRoute == clicked) {
                clicked.color = Color.rgb(100, 150, 160)
                clicked.width = 6f
                selectedRoute = null
                return@setOnPolylineClickListener
            }

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
            Log.d("MapReady", "Polyline tag: $routeDocId")
            if (routeDocId != null) {
                db.collection("route").document(routeDocId).get().addOnSuccessListener { doc ->
                    Log.d("RouteDebug", "문서 로드 성공: ${doc.id}")
                    val label = doc.getString("label") ?: "알 수 없음"
                    val labelKr = doc.getString("label_kr") ?: "이름 없음"
                    val distance = doc.getDouble("distance") ?: 0.0
                    val routePoints = (doc["points"] as? List<GeoPoint>)?.map { LatLng(it.latitude, it.longitude) } ?: emptyList()

                    val userDistanceText = lastLocation?.let { location ->
                        val minDistance = DistanceUtils.calculateMinDistanceToRoute(location, routePoints)
                        "🚶 내 위치에서 거리: ${DistanceUtils.formatDistance(minDistance)}"
                    } ?: "🚶 현재 위치 정보 없음"

                    val infoText = """
                        🏷️ 라벨: $label
                        📏 거리: ${String.format("%.2f", distance)} km
                        $userDistanceText
                    """.trimIndent()

                    val dialog = RouteInfoDialog.newInstance(labelKr, infoText)
                    dialog.show(supportFragmentManager, "route_info_dialog")
                }
            }
        }
    }

    private fun requestLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            lastLocation = location
            Log.d("Location", "마지막 위치: $location")
        }
    }

    private fun fetchAndDisplayLabelIcons() {
        Log.d("LabelLoad", "라벨 불러오기 시작")
        db.collection("route").get().addOnSuccessListener { documents ->
            val labelMap = linkedMapOf<String, String>()
            for (doc in documents) {
                val label = doc.getString("label")?.lowercase() ?: continue
                val labelKr = doc.getString("label_kr") ?: continue
                Log.d("LabelLoad", "경로 라벨 수집됨: $label → $labelKr")
                if (!labelMap.containsKey(label)) labelMap[label] = labelKr
            }

            db.collection("label_icon").get().addOnSuccessListener { iconDocs ->
                val iconMap = mutableMapOf<String, String>()
                for (doc in iconDocs) {
                    val iconURL = doc.getString("iconURL") ?: continue
                    val iconId = doc.id.removePrefix("icon_").lowercase()
                    iconMap[iconId] = iconURL
                }

                labelMenuInner.removeAllViews()

                for ((label, labelKr) in labelMap) {
                    val container = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(16, 24, 16, 24)
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(12, 12, 12, 12)
                        }
                        setBackgroundColor(Color.parseColor("#F5F5F5"))
                        isClickable = true
                        isFocusable = true
                        contentDescription = "경로 라벨: $labelKr"

                        setOnClickListener {
                            Toast.makeText(context, "클릭됨: $label", Toast.LENGTH_SHORT).show()
                            Log.d("LabelClick", "✅ 클릭 성공: $label")
                            loadPolylineFromFirestore(label)
                            for (i in 0 until labelMenuInner.childCount) {
                                val child = labelMenuInner.getChildAt(i)
                                child.setBackgroundColor(Color.parseColor("#F5F5F5"))
                            }
                            this.setBackgroundColor(Color.parseColor("#D6CBC8"))
                        }
                    }

                    val imageView = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(64, 64).apply {
                            setMargins(0, 0, 12, 0)
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        adjustViewBounds = true
                    }

                    val textView = TextView(this).apply {
                        text = labelKr
                        textSize = 15f
                        setTextColor(Color.BLACK)
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val iconURL = iconMap[label]
                    if (iconURL != null) {
                        Glide.with(imageView).load(iconURL).into(imageView)
                    }

                    container.addView(imageView)
                    container.addView(textView)
                    labelMenuInner.addView(container)
                }
                Log.d("LabelLoad", "사이드바 항목 수: ${labelMenuInner.childCount}")
            }
        }
    }

    private fun loadPolylineFromFirestore(label: String) {
        val lowercaseLabel = label.lowercase()
        Log.d("RouteDebug", "Firestore에서 '$lowercaseLabel' 경로 조회 시작")

        db.collection("route")
            .whereEqualTo("label", lowercaseLabel)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("RouteDebug", "문서 수: ${documents.size()}개")
                if (documents.isEmpty) {
                    Log.w("RouteDebug", "❗ 해당 라벨의 경로가 없습니다: $lowercaseLabel")
                    return@addOnSuccessListener
                }

                routePolylines.forEach { it.remove() }
                routePolylines.clear()
                selectedRoute = null

                documents.forEachIndexed { index, doc ->
                    val geoPoints = doc["points"] as? List<GeoPoint>
                    if (geoPoints == null) {
                        Log.w("RouteDebug", "⚠️ ${doc.id}에 points 필드 없음")
                        return@forEachIndexed
                    }

                    val latLngList = geoPoints.map { LatLng(it.latitude, it.longitude) }
                    Log.d("RouteDebug", "📍 경로 $index 점 개수: ${latLngList.size}")

                    val polyline = mMap.addPolyline(
                        PolylineOptions()
                            .addAll(latLngList)
                            .color(Color.MAGENTA)
                            .width(14f)
                            .clickable(true)
                    )
                    polyline.tag = doc.id
                    routePolylines.add(polyline)

                    if (index == 0) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList.first(), 15f))
                    }
                }
            }
            .addOnFailureListener {
                Log.e("RouteDebug", "❌ Firestore 조회 실패: ${it.message}")
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLastLocation()
        }
    }
}