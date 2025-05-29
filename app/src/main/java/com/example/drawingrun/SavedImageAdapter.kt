package com.example.drawingrun

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SavedImageAdapter(
    private val imageUris: List<Uri>,
    private val showAllCallback: () -> Unit,
    private val showLimited: Boolean = true// "..." 클릭 시 실행할 콜백
) : RecyclerView.Adapter<SavedImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_thumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (showLimited && imageUris.size > 6) 6 else imageUris.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val context = holder.imageView.context

        // 🔸 제한 모드이고 마지막(6번째)이면 "..." 이미지 표시
        if (showLimited && imageUris.size > 6 && position == 5) {
            holder.imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            holder.imageView.setImageResource(R.drawable.ic_more_images)
            holder.imageView.setOnClickListener {
                showAllCallback()
            }
        } else {
            holder.imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            holder.imageView.adjustViewBounds = true

            val uri = imageUris[position]
            Glide.with(context)
                .load(uri)
                .into(holder.imageView)

            holder.imageView.setOnClickListener {
                val intent = Intent(context, FullscreenImageActivity::class.java).apply {
                    putExtra("imageUri", uri.toString())
                }
                context.startActivity(intent)
            }
        }
    }

}
