package com.example.drawingrun

import android.graphics.drawable.GradientDrawable


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng

class RouteInfoItemAdapter(
    private val items: List<RouteInfoItem>,
    private val onClick: (RouteInfoItem) -> Unit
) : RecyclerView.Adapter<RouteInfoItemAdapter.ViewHolder>() {

    data class RouteInfoItem(
        val title: String,
        val description: String,
        val points: List<LatLng>
    )

    private var selectedIndex: Int = RecyclerView.NO_POSITION

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.route_title)
        val descText: TextView = view.findViewById(R.id.route_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_route_info, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.descText.text = item.description

        // 선택 강조 스타일 (크기 확대 + 그림자)
        holder.itemView.scaleX = if (position == selectedIndex) 1.05f else 1.0f
        holder.itemView.scaleY = if (position == selectedIndex) 1.05f else 1.0f
        holder.itemView.elevation = if (position == selectedIndex) 8f else 0f
        val border = GradientDrawable().apply {

            setColor(android.graphics.Color.TRANSPARENT)
            setStroke(4, android.graphics.Color.parseColor("#FF6700"))  // 시작 버튼과 동일한 색상
            cornerRadius = 20f
        }

        holder.itemView.background = if (position == selectedIndex) border else null

        holder.itemView.setOnClickListener {
            val previousIndex = selectedIndex
            selectedIndex = holder.adapterPosition
            notifyItemChanged(previousIndex)
            notifyItemChanged(selectedIndex)
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun highlightItemAt(index: Int) {
        val previousIndex = selectedIndex
        selectedIndex = index
        notifyItemChanged(previousIndex)
        notifyItemChanged(index)
    }
}
