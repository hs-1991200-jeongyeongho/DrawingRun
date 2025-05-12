package com.example.drawingrun

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_workout_summary)

        val time = intent.getStringExtra("time") ?: "00:00"
        val calories = intent.getDoubleExtra("calories", 0.0)
        val distance = intent.getDoubleExtra("distance", 0.0)

        val timeText = findViewById<TextView>(R.id.summary_time)
        val calText = findViewById<TextView>(R.id.summary_calories)
        val distText = findViewById<TextView>(R.id.summary_distance)
        val confirmButton = findViewById<Button>(R.id.btn_confirm)

        timeText.text = "시간: $time"
        calText.text = "칼로리: ${calories.toInt()} kcal"
        distText.text = "거리: %.2f km".format(distance)

        // 🔸 Firebase 저장
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val workoutData = hashMapOf(
                "time" to time,
                "calories" to calories,
                "distance" to distance,
                "timestamp" to System.currentTimeMillis()
            )

            FirebaseFirestore.getInstance()
                .collection("workouts")
                .document(userId)
                .collection("records")
                .add(workoutData)
                .addOnSuccessListener {
                    Toast.makeText(this, "운동 기록 저장 완료", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "운동 기록 저장 실패", Toast.LENGTH_SHORT).show()
                }
        }

        confirmButton.setOnClickListener {
            finish()
        }
    }
}
