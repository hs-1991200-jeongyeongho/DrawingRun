package com.example.drawingrun

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AllSavedImagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_saved_images)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAllImages)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 뒤로가기 버튼 눌렀을 때 동작
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%Pictures/DrawingRun%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val imageUris = mutableListOf<Uri>()
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageUris.add(uri)
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvAllImages)
        recyclerView.layoutManager = GridLayoutManager(this, 5)
        recyclerView.adapter = SavedImageAdapter(imageUris, showAllCallback = {}, showLimited = false)
    }
}
