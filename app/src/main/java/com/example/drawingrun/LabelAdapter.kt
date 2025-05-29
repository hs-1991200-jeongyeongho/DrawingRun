package com.example.drawingrun

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class LabelAdapter(
    private val items: List<LabelItem>,
    private val onClick: (LabelItem) -> Unit,
    private val onAddClick: (() -> Unit)? = null, // 🔹 + 버튼 클릭 시 처리
    private val showAddButton: Boolean = true
) : RecyclerView.Adapter<LabelAdapter.LabelViewHolder>() {

    data class LabelItem(val label: String, val labelKr: String, val iconUrl: String)

    private var selectedLabel: String? = null

    override fun getItemViewType(position: Int): Int {
        return if (position < items.size) 0 else 1 // 0: 라벨, 1: + 버튼
    }

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
            textSize = 11f
            setTextColor(Color.parseColor("#212121"))
            setPadding(0, 4, 0, 4)
            gravity = Gravity.CENTER
            typeface = ResourcesCompat.getFont(context, R.font.spoqa_han_sans_light)
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
        if (position < items.size) {
            val item = items[position]
            Glide.with(holder.itemView).load(item.iconUrl).into(holder.icon)
            holder.labelText.text = item.labelKr

            holder.frame.setBackgroundColor(
                if (item.label == selectedLabel) Color.parseColor("#D6CBC8")
                else Color.WHITE
            )

            holder.frame.setOnClickListener {
                selectedLabel = item.label
                notifyDataSetChanged()
                onClick(item)
            }

        } else {
            // 🔹 + 버튼 처리
            holder.icon.setImageResource(R.drawable.ic_add)
            holder.icon.setColorFilter(Color.parseColor("#212121"))
            holder.labelText.text = "추가"
            holder.labelText.setTextColor(Color.parseColor("#212121"))
            holder.frame.setBackgroundColor(Color.WHITE)

            holder.frame.setOnClickListener {
                onAddClick?.invoke()
            }
        }
    }

    override fun getItemCount(): Int = items.size + if (showAddButton) 1 else 0 // 🔹 마지막에 + 버튼 포함
}
