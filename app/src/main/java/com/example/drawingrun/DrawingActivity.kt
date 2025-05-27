    package com.example.drawingrun

    import android.Manifest
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.graphics.Color
    import android.location.Location
    import android.os.Bundle
    import android.view.View
    import android.widget.*
    import androidx.appcompat.app.AppCompatActivity
    import androidx.cardview.widget.CardView
    import androidx.core.app.ActivityCompat
    import androidx.core.view.GravityCompat
    import androidx.drawerlayout.widget.DrawerLayout
    import android.util.Log
    import androidx.lifecycle.lifecycleScope
    import androidx.recyclerview.widget.GridLayoutManager
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.google.android.gms.location.*
    import com.google.android.gms.maps.CameraUpdateFactory
    import com.google.android.gms.maps.GoogleMap
    import com.google.android.gms.maps.OnMapReadyCallback
    import com.google.android.gms.maps.SupportMapFragment
    import com.google.android.gms.maps.model.*
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.GeoPoint
    import kotlinx.coroutines.Job
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.delay
    import kotlin.math.*

    class DrawingActivity : BaseActivity(), OnMapReadyCallback {

        private lateinit var mMap: GoogleMap
        private lateinit var guideCard: CardView
        private lateinit var drawerLayout: DrawerLayout
        private lateinit var labelMenu: FrameLayout
        private lateinit var btnToggleSidebarOpen: ImageButton
        private lateinit var btnToggleSidebarClosed: ImageButton
        private lateinit var startButton: FrameLayout
        private val db = FirebaseFirestore.getInstance()

        private val routePolylines = mutableListOf<Polyline>()
        private var selectedRoute: Polyline? = null
        private var selectedRouteFromList: Polyline? = null
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private var lastLocation: Location? = null
        private lateinit var labelRecycler: RecyclerView
        private lateinit var routeInfoRecycler: RecyclerView
        private var routeInfoAdapter: RouteInfoItemAdapter? = null

        private var selectedRoutePoints: List<LatLng>? = null

        private var mockRunnerCircle: Circle? = null
        private var mockRunningJob: Job? = null
        private var tracePolyline: Polyline? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.drawing_activity)

            guideCard = findViewById(R.id.guide_message_card)
            routeInfoRecycler = findViewById(R.id.recycler_route_info)
            routeInfoRecycler.layoutManager = LinearLayoutManager(this)

            setupToolbarWithProfileAlwaysBack()

            drawerLayout = findViewById(R.id.drawer_layout)
            drawerLayout.setScrimColor(Color.TRANSPARENT)

            labelRecycler = findViewById(R.id.recycler_labels)
            labelRecycler.layoutManager = GridLayoutManager(this, 1)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            requestLastLocation()

            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

            labelMenu = findViewById(R.id.label_menu)
            btnToggleSidebarOpen = findViewById(R.id.btn_toggle_sidebar_open)
            btnToggleSidebarClosed = findViewById(R.id.btn_toggle_sidebar_closed)
            startButton = findViewById(R.id.btn_start_running)

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
                    ArrayList(selectedRoutePoints)
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
                        LatLng(37.5826, 127.0101)
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 16f))
                }
            } else {
                val hansung = LatLng(37.5826, 127.0101)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hansung, 16f))
            }

            mMap.setOnPolylineClickListener { clicked ->
                selectedRoute = clicked
                selectedRoutePoints = clicked.points

                // ✅ 전체 확대 처리 (mapView.post 이용)
                val boundsBuilder = LatLngBounds.Builder()
                clicked.points.forEach { boundsBuilder.include(it) }
                val bounds = boundsBuilder.build()
                val padding = 250

                // 지도가 레이아웃에 완전히 그려진 이후에 카메라 이동
                (supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.view?.post {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                }

                // ✅ 경로 강조 및 리스트 스크롤
                routePolylines.forEachIndexed { index, polyline ->
                    polyline.color = if (polyline == clicked) Color.RED else Color.MAGENTA
                    polyline.width = if (polyline == clicked) 18f else 14f

                    if (polyline == clicked) {
                        val layoutManager = routeInfoRecycler.layoutManager as? LinearLayoutManager
                        layoutManager?.scrollToPositionWithOffset(index, 100)
                        routeInfoAdapter?.highlightItemAt(index)
                    }
                }

                // ✅ 점 애니메이션 실행
                startMockRunningAnimation(clicked.points)
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

        private fun startMockRunningAnimation(points: List<LatLng>) {
            if (points.size < 2) {
                Log.d("MockRun", "애니메이션 시작 실패: point 부족")
                return
            }

            // 이전 점/경로 제거
            mockRunnerCircle?.remove()
            mockRunnerCircle = null
            mockRunningJob?.cancel()
            mockRunningJob = null
            tracePolyline?.remove()
            tracePolyline = null

            // 새 점 생성
            mockRunnerCircle = mMap.addCircle(
                CircleOptions()
                    .center(points[0])
                    .radius(9.0)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.BLUE)
                    .zIndex(10f)
            )

            // 잔상용 polyline 생성
            tracePolyline = mMap.addPolyline(
                PolylineOptions()
                    .color(Color.GREEN)
                    .width(11f)
                    .zIndex(9f)
            )

            val loopedPoints = if (points.first() != points.last()) points + points.first() else points

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loopedPoints[0], 17f))

            mockRunningJob = lifecycleScope.launch {
                val trace = mutableListOf<LatLng>()
                trace.add(loopedPoints[0]) // 시작점 포함

                val speedMps = 250.0  // 초당 2m 이동
                val delayMillis = 30L
                val stepDurationSec = delayMillis / 1000.0  // 0.05초

                for (i in 1 until loopedPoints.size) {
                    val start = loopedPoints[i - 1]
                    val end = loopedPoints[i]

                    // 거리 계산
                    val result = FloatArray(1)
                    Location.distanceBetween(
                        start.latitude, start.longitude,
                        end.latitude, end.longitude,
                        result
                    )
                    val segmentDistance = result[0] // in meters

                    // 해당 구간의 steps 계산 (거리 ÷ 속도 × 프레임 시간)
                    val steps = maxOf(1, (segmentDistance / (speedMps * stepDurationSec)).toInt())

                    for (step in 1..steps) {
                        val lat = start.latitude + (end.latitude - start.latitude) * step / steps
                        val lng = start.longitude + (end.longitude - start.longitude) * step / steps
                        val nextPos = LatLng(lat, lng)

                        mockRunnerCircle?.center = nextPos
                        trace.add(nextPos)
                        tracePolyline?.points = trace

                        delay(delayMillis)
                    }
                }
            }
        }

        private fun resetMockRunner() {
            mockRunnerCircle?.remove()
            mockRunnerCircle = null

            tracePolyline?.remove()
            tracePolyline = null

            mockRunningJob?.cancel()
            mockRunningJob = null
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

                    labelRecycler.adapter = LabelAdapter(
                        items,
                        onClick = { item ->
                            // ✅ 기존 경로, 애니메이션 초기화
                            resetMockRunner()

                            // ✅ 경로 불러오기
                            loadPolylineFromFirestore(item.label, item.labelKr)

                            guideCard.visibility = View.GONE
                            routeInfoRecycler.visibility = View.VISIBLE
                        },
                        onAddClick = {
                            // ✅ + 버튼 눌렀을 때 처리
                            Toast.makeText(this, "경로 추가 화면으로 이동할 수 있습니다.", Toast.LENGTH_SHORT).show()
                            // 또는 startActivity(Intent(this, AddRouteActivity::class.java))
                        }
                    )

                }
            }
        }

        private fun loadPolylineFromFirestore(label: String, labelKr: String) {
            val lowercaseLabel = label.lowercase()
            db.collection("route").whereEqualTo("label", lowercaseLabel).get().addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener

                routePolylines.forEach { it.remove() }
                routePolylines.clear()
                selectedRoute = null
                selectedRouteFromList = null
                selectedRoutePoints = null

                val routeItems = mutableListOf<RouteInfoItemAdapter.RouteInfoItem>()

                documents.forEachIndexed { index, doc ->
                    val geoPoints = doc["points"] as? List<GeoPoint> ?: return@forEachIndexed
                    val latLngList = geoPoints.map { LatLng(it.latitude, it.longitude) }

                    val polyline = mMap.addPolyline(
                        PolylineOptions().addAll(latLngList).color(Color.MAGENTA).width(14f).clickable(true)
                    )
                    polyline.tag = doc.id
                    routePolylines.add(polyline)

                    //if (index == 0) {
                    //    selectedRoutePoints = latLngList
                    //    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList.first(), 15f))
                    //}

                    val distance = doc.getDouble("distance") ?: 0.0

                    val userDistanceText = lastLocation?.let {
                        val minDistance = DistanceUtils.calculateMinDistanceToRoute(it, latLngList)
                        "🚶 내 위치에서 거리: ${DistanceUtils.formatDistance(minDistance)}"
                    } ?: "🚶 현재 위치 정보 없음"

                    val description = """
                        📏 거리: ${"%.2f".format(distance)} km
                        $userDistanceText
                    """.trimIndent()

                    val title = "$labelKr ${index + 1}"
                    routeItems.add(RouteInfoItemAdapter.RouteInfoItem(title, description, latLngList))
                }

                routeInfoAdapter = RouteInfoItemAdapter(routeItems) { selectedItem ->

                    // ✅ 전체 경로 확대 (뷰 그려진 이후 실행)
                    val boundsBuilder = LatLngBounds.Builder()
                    selectedItem.points.forEach { boundsBuilder.include(it) }
                    val bounds = boundsBuilder.build()
                    val padding = 250

                    (supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.view?.post {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                    }

                    // ✅ 경로 강조
                    selectedRouteFromList = null
                    routePolylines.forEach {
                        it.color = Color.MAGENTA
                        it.width = 14f
                        if (it.points == selectedItem.points) {
                            it.color = Color.RED
                            it.width = 18f
                            selectedRouteFromList = it
                        }
                    }

                    selectedRoutePoints = selectedItem.points

                    val selectedIndex = routeItems.indexOfFirst { it.points == selectedItem.points }
                    val layoutManager = routeInfoRecycler.layoutManager as? LinearLayoutManager
                    layoutManager?.scrollToPositionWithOffset(selectedIndex, 100)

                    startMockRunningAnimation(selectedItem.points)
                }


                routeInfoRecycler.adapter = routeInfoAdapter
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLastLocation()
            }
        }
    }
