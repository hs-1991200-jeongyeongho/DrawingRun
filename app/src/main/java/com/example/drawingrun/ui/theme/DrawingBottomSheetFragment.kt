package com.example.drawingrun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import android.widget.ImageButton
import com.example.drawingrun.R

class DrawingBottomSheetFragment : DialogFragment() {

    private lateinit var drawingView: DrawingView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bottom Sheet 레이아웃을 인플레이트
        val rootView = inflater.inflate(R.layout.drawing_layout, container, false)

        // DrawingView 연결
        drawingView = rootView.findViewById(R.id.drawing_view)

        // 리셋 버튼 클릭 시 그림판 초기화
        val resetButton = rootView.findViewById<Button>(R.id.reset_button)
        resetButton.setOnClickListener {
            drawingView.clearDrawing()
        }

        // X 버튼 클릭 시 다이얼로그 닫기
        val btnClose = rootView.findViewById<ImageButton>(R.id.btn_close)
        btnClose.setOnClickListener {
            dismiss()  // 다이얼로그 닫기
        }

        return rootView
    }
}
