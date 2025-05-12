package com.example.drawingrun

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView

class WorkoutSummaryDialog(
    context: Context,
    private val time: String,
    private val distance: Double,
    private val calories: Double,
    private val onConfirm: () -> Unit
)
 : Dialog(context) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_workout_summary, null)
        setContentView(view)

        val timeText = view.findViewById<TextView>(R.id.summary_time)
        val calText = view.findViewById<TextView>(R.id.summary_calories)
        val distText = view.findViewById<TextView>(R.id.summary_distance) // 거리도 필요하면 넘겨받아서 추가
        val confirmButton = view.findViewById<Button>(R.id.btn_confirm)

        timeText.text = "시간: $time"
        calText.text = "칼로리: ${calories.toInt()} kcal"

        confirmButton.setOnClickListener {
            onConfirm()
            dismiss()
        }

        setCancelable(false)
    }
}
