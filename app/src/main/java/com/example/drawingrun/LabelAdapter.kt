package com.example.drawingrun

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
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
            layoutParams = FrameLayout.LayoutParams(72, 72, Gravity.CENTER)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        init {
            frame.addView(icon)
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

        // ✅ 선택된 라벨 배경 강조
        if (item.label == selectedLabel) {
            holder.frame.setBackgroundColor(Color.parseColor("#D6CBC8"))
        } else {
            holder.frame.setBackgroundColor(Color.WHITE)
        }

        Glide.with(holder.itemView).load(item.iconUrl).into(holder.icon)

        holder.frame.setOnClickListener {
            selectedLabel = item.label
            notifyDataSetChanged() // 전체 갱신
            onClick(item)
        }
    }

    override fun getItemCount() = items.size
}
