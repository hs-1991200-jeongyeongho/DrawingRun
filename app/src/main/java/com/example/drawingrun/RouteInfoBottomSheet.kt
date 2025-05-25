package com.example.drawingrun

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class RouteInfoDialog : DialogFragment() {

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    companion object {
        fun newInstance(label: String, description: String): RouteInfoDialog {
            val fragment = RouteInfoDialog()
            val args = Bundle()
            args.putString("label", label)
            args.putString("description", description)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_route_info)

        // 배경 흐림 제거 + 투명 배경
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 상단에 고정
        dialog.window?.apply {
            setGravity(Gravity.TOP)
            attributes = attributes.apply {
                y = 100  // 위쪽 간격 조정
                width = WindowManager.LayoutParams.MATCH_PARENT
            }
        }

        // 텍스트 설정
        val label = arguments?.getString("label")
        val description = arguments?.getString("description")

        dialog.findViewById<TextView>(R.id.textLabel)?.text = "📍 $label"
        dialog.findViewById<TextView>(R.id.textDescription)?.text = description

        return dialog
    }
}
