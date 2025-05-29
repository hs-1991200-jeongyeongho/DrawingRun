package com.example.drawingrun

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import com.bumptech.glide.Glide

class FullscreenImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        val imageView = findViewById<ImageView>(R.id.fullscreenImageView)
        val uriString = intent.getStringExtra("imageUri")
        val uri = Uri.parse(uriString)

        Glide.with(this)
            .load(uri)
            .into(imageView)

        // 클릭하면 종료되도록
        imageView.setOnClickListener {
            finish()
        }
    }
}
