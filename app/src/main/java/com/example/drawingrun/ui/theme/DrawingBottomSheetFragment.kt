package com.example.drawingrun

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class DrawingBottomSheetFragment : DialogFragment() {

    private lateinit var drawingView: DrawingView
    private lateinit var interpreter: Interpreter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 그림판 레이아웃을 인플레이트
        val rootView = inflater.inflate(R.layout.drawing_layout, container, false)

        // DrawingView 연결
        drawingView = rootView.findViewById(R.id.drawing_view)

        // 모델 로드
        interpreter = Interpreter(loadModelFile(requireContext()))

        // 리셋 버튼 클릭 시 그림 초기화
        val resetButton = rootView.findViewById<Button>(R.id.reset_button)
        resetButton.setOnClickListener {
            drawingView.clearDrawing()
        }

        // 닫기 버튼 클릭 시 다이얼로그 종료
        val btnClose = rootView.findViewById<ImageButton>(R.id.btn_close)
        btnClose.setOnClickListener {
            dismiss()
        }

        // 적용(예측) 버튼 클릭 시 모델 추론 수행
        val btnPredict = rootView.findViewById<Button>(R.id.btn_predict)
        btnPredict.setOnClickListener {
            val result = drawingView.predictWithModel(interpreter)
            Toast.makeText(requireContext(), "예측 결과: $result", Toast.LENGTH_SHORT).show()
        }

        return rootView
    }

    // 모델 파일(.tflite)을 assets에서 로드
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("quickdraw_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }
}
