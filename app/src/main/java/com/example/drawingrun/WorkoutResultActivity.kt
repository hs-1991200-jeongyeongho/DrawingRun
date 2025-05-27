package com.example.drawingrun

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WorkoutResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_summary)

        val imagePath = intent.getStringExtra("imagePath")
        val time = intent.getStringExtra("time") ?: "00:00"
        val calories = intent.getDoubleExtra("calories", 0.0)
        val distance = intent.getDoubleExtra("distance", 0.0)
        val date = intent.getStringExtra("date") ?: ""

        val imageView = findViewById<ImageView>(R.id.resultImage)
        val infoText = "$date • %.2fkm • $time".format(distance)
        findViewById<TextView>(R.id.image_description).text = infoText

        if (!imagePath.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
        }

        val saveButton = findViewById<Button>(R.id.btn_save_image)
        val confirmButton = findViewById<Button>(R.id.btn_confirm)

        saveButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("이미지 저장")
                .setMessage("이미지를 저장하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    val drawable = imageView.drawable
                    val bitmap = (drawable as? BitmapDrawable)?.bitmap

                    if (bitmap != null) {
                        val filename = "drawing_result_${System.currentTimeMillis()}.png"
                        val uri = saveBitmapToGallery(bitmap, filename)

                        if (uri != null) {
                            Toast.makeText(this, "갤러리에 이미지가 저장되었습니다", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "이미지 저장 실패", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "이미지가 유효하지 않습니다", Toast.LENGTH_SHORT).show()
                    }

                    val intent = Intent(this, WorkoutSummaryActivity::class.java).apply {
                        putExtra("distance", distance)
                        putExtra("time", time)
                        putExtra("calories", calories)
                        putExtra("date", date)
                    }
                    startActivity(intent)
                }
                .setNegativeButton("취소", null)
                .show()
        }

        confirmButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("이미지를 저장하지 않았습니다")
                .setMessage("이미지를 저장하지 않고 넘어가시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    val intent = Intent(this, WorkoutSummaryActivity::class.java).apply {
                        putExtra("distance", distance)
                        putExtra("time", time)
                        putExtra("calories", calories)
                        putExtra("date", date)
                    }
                    startActivity(intent)
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap, filename: String): android.net.Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DrawingRun") // Android 10 이상
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return uri
    }
}
