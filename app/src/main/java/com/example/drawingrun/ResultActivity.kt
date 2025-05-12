package com.example.drawingrun

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val calories = intent.getDoubleExtra("calories", 0.0)
        val distance = intent.getDoubleExtra("distance", 0.0)
        val time = intent.getDoubleExtra("time", 0.0)

        val textResult = findViewById<TextView>(R.id.result_summary)
        textResult.text = """
            🏃 운동 결과 요약 🏁

            🔸 이동 거리: %.2f km
            🔸 운동 시간: %.0f 분
            🔸 소모 칼로리: %d kcal
        """.trimIndent().format(distance, time / 60, calories.roundToInt())
    }
}
