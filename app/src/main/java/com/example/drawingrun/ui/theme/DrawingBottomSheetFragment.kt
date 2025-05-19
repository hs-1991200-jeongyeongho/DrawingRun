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
        val rootView = inflater.inflate(R.layout.drawing_layout, container, false)

        drawingView = rootView.findViewById(R.id.drawing_view)
        interpreter = Interpreter(loadModelFile(requireContext()))

        val resetButton = rootView.findViewById<Button>(R.id.reset_button)
        val btnClose = rootView.findViewById<ImageButton>(R.id.btn_close)
        val btnPredict = rootView.findViewById<Button>(R.id.btn_predict) // ✅ 선언 추가

        resetButton.setOnClickListener {
            drawingView.clearDrawing()
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        btnPredict.setOnClickListener {
            val result = drawingView.predictWithModel(interpreter)

            // 예측 결과 다이얼로그
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("예측 결과 확인")
                .setMessage("그리신 그림이 \"$result\"이 맞나요?")
                .setPositiveButton("맞아요") { dialog, _ ->
                    // 🔥 결과를 DrawingActivity로 전달
                    parentFragmentManager.setFragmentResult(
                        "prediction_result",
                        Bundle().apply { putString("shape", result) }
                    )
                    dialog.dismiss()
                    dismiss() // 이 다이얼로그(Fragment)도 닫기
                }
                .setNegativeButton("아니오") { dialog, _ ->
                    Toast.makeText(requireContext(), "다시 그려주세요.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .show()
        }

        return rootView
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("quickdraw_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }
}
