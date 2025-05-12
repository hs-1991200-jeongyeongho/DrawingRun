package com.example.drawingrun

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvShapes: TextView
    private lateinit var ivProfile: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvName = findViewById(R.id.tvUserName)
        tvAge = findViewById(R.id.tvUserAge)
        tvHeight = findViewById(R.id.tvUserHeight)
        tvWeight = findViewById(R.id.tvUserWeight)
        tvDistance = findViewById(R.id.tvTotalDistance)
        tvShapes = findViewById(R.id.tvShapeCount)

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name") ?: "이름 없음"
                    val age = document.getLong("age")?.toString() ?: "미입력"
                    val height = document.getDouble("height")?.toString() ?: "미입력"
                    val weight = document.getDouble("weight")?.toString() ?: "미입력"
                    val distance = document.getDouble("distance")?.toString() ?: "0"
                    val shapes = document.getLong("shapeCount")?.toString() ?: "0"

                    tvName.text = name
                    tvAge.text = "나이: $age"
                    tvHeight.text = "키: $height cm"
                    tvWeight.text = "몸무게: $weight kg"
                    tvDistance.text = "누적 달린 거리: $distance km"
                    tvShapes.text = "만든 도형 수: $shapes"


                }
            }
            .addOnFailureListener {
                tvName.text = "불러오기 실패"
            }
    }
}
