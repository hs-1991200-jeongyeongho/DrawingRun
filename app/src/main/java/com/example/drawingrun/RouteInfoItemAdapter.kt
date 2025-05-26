package com.example.drawingrun

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

        // 선택 강조 색상 처리
        val context = holder.itemView.context
        holder.itemView.setBackgroundColor(
            if (position == selectedIndex)
                ContextCompat.getColor(context, android.R.color.darker_gray)
            else
                ContextCompat.getColor(context, android.R.color.transparent)
        )

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
