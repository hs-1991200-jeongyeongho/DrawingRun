package com.example.drawingrun

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import androidx.gridlayout.widget.GridLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DrawingActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var labelMenu: FrameLayout
    private lateinit var btnToggleSidebarOpen: ImageButton
    private lateinit var btnToggleSidebarClosed: ImageButton
    private lateinit var startButton: FrameLayout
    private val db = FirebaseFirestore.getInstance()

    private val routePolylines = mutableListOf<Polyline>()
    private var selectedRoute: Polyline? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private lateinit var labelRecycler: RecyclerView

    private var selectedRoutePoints: List<LatLng>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_activity)

        setupToolbarWithProfileAlwaysBack()

        // ✅ 사이드바 열어도 지도 어두워지지 않게
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setScrimColor(Color.TRANSPARENT)

        labelRecycler = findViewById(R.id.recycler_labels)
        labelRecycler.layoutManager = GridLayoutManager(this, 1)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLastLocation()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        drawerLayout = findViewById(R.id.drawer_layout)
        labelMenu = findViewById(R.id.label_menu)
        btnToggleSidebarOpen = findViewById(R.id.btn_toggle_sidebar_open)
        btnToggleSidebarClosed = findViewById(R.id.btn_toggle_sidebar_closed)
        startButton = findViewById(R.id.btn_start_running)

        //drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
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
            if (selectedRoutePoints == null) {
                Toast.makeText(this, "경로를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, RunningActivity::class.java)
            intent.putParcelableArrayListExtra(
                "selected_route_points",
                ArrayList(selectedRoutePoints)  // List<LatLng> → ArrayList<LatLng>
            )
            startActivity(intent)
        }

        fetchAndDisplayLabelIcons()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val targetLatLng = if (location != null) {
                    LatLng(location.latitude, location.longitude)
                } else {
                    // ✅ 위치 없을 때: 한성대학교
                    LatLng(37.5826, 127.0101)
                }

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 16f))
            }
        } else {
            // ✅ 권한 없을 때도 한성대 위치로
            val hansung = LatLng(37.5826, 127.0101)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hansung, 16f))
        }

        mMap.setOnPolylineClickListener { clicked ->
            if (selectedRoute == clicked) {
                clicked.color = Color.rgb(100, 150, 160)
                clicked.width = 6f
                selectedRoute = null
                return@setOnPolylineClickListener
            }

            selectedRoute = clicked
            routePolylines.forEach {
                it.color = if (it == clicked) Color.MAGENTA else Color.rgb(100, 150, 160)
                it.width = if (it == clicked) 14f else 6f
            }

            val routeDocId = clicked.tag as? String ?: return@setOnPolylineClickListener

            db.collection("route").document(routeDocId).get().addOnSuccessListener { doc ->
                val label = doc.getString("label") ?: "알 수 없음"
                val labelKr = doc.getString("label_kr") ?: "이름 없음"
                val distance = doc.getDouble("distance") ?: 0.0
                val routePoints = (doc["points"] as? List<GeoPoint>)?.map { LatLng(it.latitude, it.longitude) } ?: emptyList()

                val userDistanceText = lastLocation?.let {
                    val minDistance = DistanceUtils.calculateMinDistanceToRoute(it, routePoints)
                    "\uD83D\uDEB6 내 위치에서 거리: ${DistanceUtils.formatDistance(minDistance)}"
                } ?: "\uD83D\uDEB6 현재 위치 정보 없음"

                val infoText = """
                    🏋️ 라벨: $label
                    📏 거리: ${"%.2f".format(distance)} km
                    $userDistanceText
                """.trimIndent()

                RouteInfoDialog.newInstance(labelKr, infoText).show(supportFragmentManager, "route_info_dialog")
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
            if (::mMap.isInitialized) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            }
        }
    }

    private fun fetchAndDisplayLabelIcons() {
        db.collection("route").get().addOnSuccessListener { documents ->
            val labelMap = linkedMapOf<String, String>()
            for (doc in documents) {
                val label = doc.getString("label")?.lowercase() ?: continue
                val labelKr = doc.getString("label_kr") ?: continue
                if (!labelMap.containsKey(label)) labelMap[label] = labelKr
            }

            db.collection("label_icon").get().addOnSuccessListener { iconDocs ->
                val iconMap = mutableMapOf<String, String>()
                for (doc in iconDocs) {
                    val iconURL = doc.getString("iconURL") ?: continue
                    val iconId = doc.id.removePrefix("icon_").lowercase()
                    iconMap[iconId] = iconURL
                }

                val items = labelMap.mapNotNull { (label, labelKr) ->
                    val iconUrl = iconMap[label] ?: return@mapNotNull null
                    LabelAdapter.LabelItem(label, labelKr, iconUrl)
                }

                labelRecycler.adapter = LabelAdapter(items) { item ->
                    loadPolylineFromFirestore(item.label)
                }
            }
        }
    }


    private fun loadPolylineFromFirestore(label: String) {
        val lowercaseLabel = label.lowercase()
        db.collection("route").whereEqualTo("label", lowercaseLabel).get().addOnSuccessListener { documents ->
            if (documents.isEmpty) return@addOnSuccessListener

            routePolylines.forEach { it.remove() }
            routePolylines.clear()
            selectedRoute = null

            // ✅ 여기에서 선택된 경로 저장용 변수 초기화
            selectedRoutePoints = null

            documents.forEachIndexed { index, doc ->
                val geoPoints = doc["points"] as? List<GeoPoint> ?: return@forEachIndexed
                val latLngList = geoPoints.map { LatLng(it.latitude, it.longitude) }

                val polyline = mMap.addPolyline(
                    PolylineOptions().addAll(latLngList).color(Color.MAGENTA).width(14f).clickable(true)
                )
                polyline.tag = doc.id
                routePolylines.add(polyline)

                // ✅ 가장 첫 번째 경로를 선택된 경로로 저장
                if (index == 0) {
                    selectedRoutePoints = latLngList
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList.first(), 15f))
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLastLocation()
        }
    }
}
