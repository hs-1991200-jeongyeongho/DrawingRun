package com.example.drawingrun

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import kotlin.math.roundToInt

class WorkoutSummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_summary) // ✅ 이게 맞는 레이아웃 파일 이름!

        val date = intent.getStringExtra("date") ?: "--"
        val time = intent.getStringExtra("time") ?: "--"
        val distance = intent.getDoubleExtra("distance", 0.0)
        val calories = intent.getDoubleExtra("calories", 0.0)

        findViewById<TextView>(R.id.summary_date).text = "날짜: $date"
        findViewById<TextView>(R.id.summary_time).text = "시간: $time"
        findViewById<TextView>(R.id.summary_distance).text = "거리: %.2f km".format(distance)
        findViewById<TextView>(R.id.summary_calories).text = "칼로리: ${calories.roundToInt()} kcal"

        findViewById<AppCompatButton>(R.id.btn_confirm).setOnClickListener {
            val intent = Intent(this, DrawingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
