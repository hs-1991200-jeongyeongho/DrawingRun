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

            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("예측한 경로예요")
                .setMessage("그림을 \"$result\"으로 인식했어요.\n이 경로로 시작할까요?")
                .setPositiveButton("시작할게요") { dialogInterface, _ ->
                    parentFragmentManager.setFragmentResult(
                        "prediction_result",
                        Bundle().apply { putString("shape", result) }
                    )
                    dialogInterface.dismiss()
                    dismiss()
                }
                .setNegativeButton("다시 그릴게요") { dialogInterface, _ ->
                    drawingView.clearDrawing()
                    Toast.makeText(requireContext(), "다시 그려주세요.", Toast.LENGTH_SHORT).show()
                    dialogInterface.dismiss()
                }
                .create()

            dialog.setOnShowListener {
                // 🎨 색상 지정
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    ?.setTextColor(requireContext().getColor(R.color.blue_confirm)) // 파랑
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    ?.setTextColor(requireContext().getColor(R.color.red_cancel))   // 빨강
            }

            dialog.show()
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
