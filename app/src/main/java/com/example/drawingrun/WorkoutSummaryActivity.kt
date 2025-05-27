package com.example.drawingrun

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import kotlin.math.roundToInt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutSummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_summary)

        val date = intent.getStringExtra("date") ?: "--"
        val time = intent.getStringExtra("time") ?: "--"
        val distance = intent.getDoubleExtra("distance", 0.0)
        val calories = intent.getDoubleExtra("calories", 0.0)

        findViewById<TextView>(R.id.summary_date).text = "날짜: $date"
        findViewById<TextView>(R.id.summary_time).text = "시간: $time"
        findViewById<TextView>(R.id.summary_distance).text = "거리: %.2f km".format(distance)
        findViewById<TextView>(R.id.summary_calories).text = "칼로리: ${calories.roundToInt()} kcal"

        // ✅ 여기서 Firestore에 누적 거리 업데이트
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
            userRef.get().addOnSuccessListener { document ->
                val currentTotal = document.getDouble("totalDistance") ?: 0.0
                val updatedTotal = currentTotal + distance
                userRef.update("totalDistance", updatedTotal)
                    .addOnSuccessListener {
                        // 로그 확인 가능 (원하면 Toast도 가능)
                        println("✅ 누적 거리 저장 완료: $updatedTotal km")
                    }
                    .addOnFailureListener {
                        println("❌ 거리 저장 실패: ${it.message}")
                    }

                val currentShapeCount = document.getLong("shapeCount") ?: 0
                val updatedShapeCount = currentShapeCount + 1
                userRef.update("shapeCount", updatedShapeCount)
                    .addOnSuccessListener {
                        Log.d("WorkoutSummary", "✅ shapeCount 증가: $updatedShapeCount")
                    }
                    .addOnFailureListener {
                        Log.e("WorkoutSummary", "❌ shapeCount 저장 실패: ${it.message}")
                    }
            }
        }

        findViewById<AppCompatButton>(R.id.btn_confirm).setOnClickListener {
            val intent = Intent(this, DrawingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
