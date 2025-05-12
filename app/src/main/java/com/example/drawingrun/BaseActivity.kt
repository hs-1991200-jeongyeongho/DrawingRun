package com.example.drawingrun

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.imageview.ShapeableImageView

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 자식 Activity에서 layout을 지정해야 함
    }

    fun setupToolbarWithProfileAlwaysBack() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 타이틀 제거
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // ✅ 항상 뒤로가기 버튼 표시
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 프로필 버튼 동작
        val profileBtn = toolbar.findViewById<ShapeableImageView>(R.id.btnProfile)
        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
