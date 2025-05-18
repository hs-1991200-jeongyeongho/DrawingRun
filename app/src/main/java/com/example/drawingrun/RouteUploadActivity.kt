package com.example.drawingrun

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class RouteUploadActivity : AppCompatActivity() {
    private lateinit var labelInput: EditText
    private lateinit var coordsInput: EditText
    private lateinit var uploadBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_upload)

        labelInput = findViewById(R.id.editLabel)
        coordsInput = findViewById(R.id.editCoords)
        uploadBtn = findViewById(R.id.btnUpload)

        uploadBtn.setOnClickListener {
            val label = labelInput.text.toString()
            val rawCoords = coordsInput.text.toString()

            val points = parseCoordinates(rawCoords)
            if (points.isNotEmpty()) {
                uploadRoute(label, points)
            }
        }
    }

    private fun parseCoordinates(input: String): List<GeoPoint> {
        return input.lines().mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size == 2) {
                val lat = parts[0].trim().toDoubleOrNull()
                val lng = parts[1].trim().toDoubleOrNull()
                if (lat != null && lng != null) {
                    GeoPoint(lat, lng)
                } else null
            } else null
        }
    }

    private fun uploadRoute(label: String, points: List<GeoPoint>) {
        val db = FirebaseFirestore.getInstance()

        db.collection("route")
            .get()
            .addOnSuccessListener { docs ->
                val nextIndex = docs.size() + 1
                val routeId = "route_" + String.format("%03d", nextIndex)

                val routeData = hashMapOf(
                    "label" to label,
                    "points" to points
                )

                db.collection("route")
                    .document(routeId)
                    .set(routeData)
                    .addOnSuccessListener {
                        Log.d("Firestore", "$routeId 저장 완료")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "저장 실패", e)
                    }
            }
    }
}
