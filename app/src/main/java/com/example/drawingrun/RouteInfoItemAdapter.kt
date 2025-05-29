package com.example.drawingrun

import android.graphics.drawable.GradientDrawable


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import android.app.AlertDialog
import android.view.MenuItem
import android.widget.PopupMenu
import android.graphics.Color

class RouteInfoItemAdapter(
    private val items: MutableList<RouteInfoItem>,
    private val onClick: (RouteInfoItem) -> Unit,
    private val onDelete: (RouteInfoItem) -> Unit
) : RecyclerView.Adapter<RouteInfoItemAdapter.ViewHolder>() {

    data class RouteInfoItem(
        val title: String,
        val description: String,
        val points: List<LatLng>,
        val isMine: Boolean = false
    )

    private var selectedIndex: Int = RecyclerView.NO_POSITION

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.route_title)
        val descText: TextView = view.findViewById(R.id.route_description)
        val badgeText: TextView = view.findViewById(R.id.my_route_badge)
        val menuButton: ImageButton = view.findViewById(R.id.menu_button)// ✅ 추가
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_route_info, parent, false)
        return ViewHolder(view)
    }

    fun removeItem(item: RouteInfoItem) {
        val index = items.indexOf(item)
        if (index != -1 && items is MutableList) {
            (items as MutableList).removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.descText.text = item.description
        holder.badgeText.visibility = if (item.isMine) View.VISIBLE else View.GONE // ✅ 뱃지 표시

        if (item.isMine) {
            holder.menuButton.visibility = View.VISIBLE
            holder.menuButton.setOnClickListener {
                val popup = PopupMenu(holder.itemView.context, holder.menuButton)
                popup.menuInflater.inflate(R.menu.route_item_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.itemId == R.id.menu_delete_route) {
                        val dialog = AlertDialog.Builder(holder.itemView.context)
                            .setTitle("경로 삭제")
                            .setMessage("정말로 이 경로를 삭제하시겠습니까?")
                            .setPositiveButton("삭제", null) // 리스너 나중에 지정
                            .setNegativeButton("취소", null)
                            .create()

                        dialog.setOnShowListener {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                                setTextColor(Color.parseColor("#1976D2")) // 파란색 (Material Blue 700)
                                setOnClickListener {
                                    onDelete(item)
                                    dialog.dismiss()
                                }
                            }

                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                                setTextColor(Color.parseColor("#D32F2F")) // 빨간색 (Material Red 700)
                            }
                        }

                        dialog.show()
                        true
                    } else false
                }
                popup.show()
            }
        } else {
            holder.menuButton.visibility = View.GONE
        }

        // 선택 강조 스타일
        holder.itemView.scaleX = if (position == selectedIndex) 1.05f else 1.0f
        holder.itemView.scaleY = if (position == selectedIndex) 1.05f else 1.0f
        holder.itemView.elevation = if (position == selectedIndex) 8f else 0f
        val border = GradientDrawable().apply {
            setColor(android.graphics.Color.TRANSPARENT)
            setStroke(4, android.graphics.Color.parseColor("#FF6700"))
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
