package com.example.drawingrun

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class RunningActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var pathPolyline: Polyline? = null
    private val runningPath = mutableListOf<LatLng>()

    private var isRunning = false
    private var startTime = 0L
    private var elapsedTime = 0L
    private var totalDistance = 0.0
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var timerText: TextView
    private lateinit var distanceText: TextView
    private lateinit var speedText: TextView
    private lateinit var calorieText: TextView
    private lateinit var startButton: AppCompatButton
    private lateinit var pauseButton: AppCompatButton
    private lateinit var endButton: AppCompatButton

    private var lastLocation: Location? = null
    private var weight: Double = 60.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.running_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        timerText = findViewById(R.id.timer_text)
        distanceText = findViewById(R.id.distance_text)
        speedText = findViewById(R.id.speed_text)
        calorieText = findViewById(R.id.calorie_text)
        startButton = findViewById(R.id.start_button)
        pauseButton = findViewById(R.id.pause_button)
        endButton = findViewById(R.id.end_button)

        fetchUserWeight()

        showOnlyStartButton()

        startButton.setOnClickListener {
            if (!isRunning) {
                startTime = System.currentTimeMillis()
                handler.post(timerRunnable)
                Toast.makeText(this, "운동을 시작합니다!", Toast.LENGTH_SHORT).show()
            }
            isRunning = true
            showPauseAndEndButtons()
        }

        pauseButton.setOnClickListener {
            isRunning = !isRunning

            if (isRunning) {
                pauseButton.text = "⏸ 일시정지"
                pauseButton.setBackgroundResource(R.drawable.button_pause_bg)
                startTime = System.currentTimeMillis()
                handler.post(timerRunnable)
            } else {
                pauseButton.text = "▶ 재개"
                pauseButton.setBackgroundResource(R.drawable.button_resume_bg)
                elapsedTime += System.currentTimeMillis() - startTime
            }
        }

        endButton.setOnClickListener {
            onRunEnded()
            showOnlyStartButton()
        }

        setupLocationUpdates()
    }

    private fun fetchUserWeight() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    weight = doc.getDouble("weight") ?: 60.0
                }
            }
    }

    private fun showOnlyStartButton() {
        startButton.visibility = View.VISIBLE
        pauseButton.visibility = View.GONE
        endButton.visibility = View.GONE
        pauseButton.text = "일시정지"
    }

    private fun showPauseAndEndButtons() {
        startButton.visibility = View.GONE
        pauseButton.visibility = View.VISIBLE
        endButton.visibility = View.VISIBLE
        pauseButton.text = "일시정지"
    }

    private fun onRunEnded() {
        fusedLocationClient.removeLocationUpdates(locationCallback)

        val endTimeMillis = System.currentTimeMillis()
        val durationSeconds = (elapsedTime + (if (isRunning) endTimeMillis - startTime else 0L)) / 1000.0
        val durationHours = durationSeconds / 3600.0
        val distanceKm = totalDistance / 1000.0
        val speed = if (durationHours > 0) distanceKm / durationHours else 0.0

        val mets = when {
            speed < 3 -> 2.0
            speed < 5 -> 4.0
            speed < 6.5 -> 5.0
            speed < 8 -> 7.0
            speed < 10 -> 9.0
            else -> 11.0
        }

        val calories = mets * weight * durationHours
        val timeFormatted = String.format("%.0f분 %.0f초", durationSeconds / 60, durationSeconds % 60)

        val dialog = WorkoutSummaryDialog(this, timeFormatted, distanceKm, calories) {
            val intent = Intent(this, WorkoutResultActivity::class.java).apply {
                putExtra("calories", calories)
                putExtra("time", timeFormatted)
                putExtra("distance", distanceKm)
            }
            startActivity(intent)
            finish()
        }
        dialog.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = true
            isRotateGesturesEnabled = true
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        val selectedRoutePoints = intent.getParcelableArrayListExtra<LatLng>("selected_route_points")

        if (!selectedRoutePoints.isNullOrEmpty()) {
            val polylineOptions = PolylineOptions().addAll(selectedRoutePoints).color(0xFF00AAFF.toInt()).width(10f)
            mMap.addPolyline(polylineOptions)

            val bounds = LatLngBounds.builder().apply {
                selectedRoutePoints.forEach { include(it) }
            }.build()

            val mapView = (supportFragmentManager.findFragmentById(R.id.running_map) as SupportMapFragment).view
            mapView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                }
            })
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.6067, 127.0232), 14f))
        }
    }

    private fun setupLocationUpdates() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!isRunning) return

                for (location in result.locations) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    runningPath.add(latLng)

                    if (pathPolyline == null) {
                        pathPolyline = mMap.addPolyline(
                            PolylineOptions().add(latLng).color(0xFF00FF00.toInt()).width(8f)
                        )
                    } else {
                        pathPolyline?.points = runningPath
                    }

                    lastLocation?.let { lastLoc ->
                        val resultArray = FloatArray(1)
                        Location.distanceBetween(
                            lastLoc.latitude, lastLoc.longitude,
                            location.latitude, location.longitude,
                            resultArray
                        )
                        totalDistance += resultArray[0]
                    }
                    lastLocation = location
                }
            }
        }
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val now = System.currentTimeMillis()
                val totalElapsed = elapsedTime + (now - startTime)
                updateStats(totalElapsed)
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun updateStats(elapsedMillis: Long) {
        val seconds = elapsedMillis / 1000
        val minutes = seconds / 60
        val remainSeconds = seconds % 60

        timerText.text = String.format("%02d:%02d", minutes, remainSeconds)

        val distanceInKm = totalDistance / 1000
        distanceText.text = String.format("%.2f km", distanceInKm)

        val speed = if (seconds > 0) (distanceInKm / (seconds / 3600.0)) else 0.0
        speedText.text = String.format("%.2f km/h", speed)

        val mets = when {
            speed < 3 -> 2.0
            speed < 5 -> 4.0
            speed < 6.5 -> 5.0
            speed < 8 -> 7.0
            speed < 10 -> 9.0
            else -> 11.0
        }
        val calorie = mets * weight * (seconds / 3600.0)
        calorieText.text = String.format("%d kcal", calorie.roundToInt())
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
