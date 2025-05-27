package com.example.drawingrun

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class LabelAdapter(
    private val items: List<LabelItem>,
    private val onClick: (LabelItem) -> Unit
) : RecyclerView.Adapter<LabelAdapter.LabelViewHolder>() {

    data class LabelItem(val label: String, val labelKr: String, val iconUrl: String)

    private var selectedLabel: String? = null

    inner class LabelViewHolder(val frame: FrameLayout) : RecyclerView.ViewHolder(frame) {
        val icon = ImageView(frame.context).apply {
            layoutParams = FrameLayout.LayoutParams(72, 72, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val labelText = TextView(frame.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            )
            textSize = 12f
            setTextColor(Color.BLACK)
            setPadding(0, 4, 0, 4)
            gravity = Gravity.CENTER
        }

        init {
            frame.addView(icon)
            frame.addView(labelText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val frame = FrameLayout(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(160, 160)
            setPadding(8, 8, 8, 8)
            setBackgroundColor(Color.WHITE)
        }
        return LabelViewHolder(frame)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        val item = items[position]

        // 이미지 로딩
        Glide.with(holder.itemView).load(item.iconUrl).into(holder.icon)
        holder.labelText.text = item.labelKr

        // 선택된 라벨 강조
        holder.frame.setBackgroundColor(
            if (item.label == selectedLabel) Color.parseColor("#D6CBC8")
            else Color.WHITE
        )

        holder.frame.setOnClickListener {
            selectedLabel = item.label
            notifyDataSetChanged()
            onClick(item)
        }
    }

    override fun getItemCount() = items.size
}
