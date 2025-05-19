package com.example.drawingrun

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvShapes: TextView
    private lateinit var ivProfile: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 툴바 설정
        val toolbar = findViewById<Toolbar>(R.id.profileToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvName = findViewById(R.id.tvUserName)
        tvAge = findViewById(R.id.tvUserAge)
        tvHeight = findViewById(R.id.tvUserHeight)
        tvWeight = findViewById(R.id.tvUserWeight)
        tvDistance = findViewById(R.id.tvTotalDistance)
        tvShapes = findViewById(R.id.tvShapeCount)

        val editButton = findViewById<Button>(R.id.btnEditProfile)
        editButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name") ?: "이름 없음"
                    val age = document.getLong("age")?.toString() ?: "미입력"
                    val height = document.getDouble("height")?.toString() ?: "미입력"
                    val weight = document.getDouble("weight")?.toString() ?: "미입력"
                    val distance = document.getDouble("distance")?.toString() ?: "0"
                    val shapes = document.getLong("shapeCount")?.toString() ?: "0"

                    tvName.text = name
                    tvAge.text = "나이: $age"
                    tvHeight.text = "키: $height cm"
                    tvWeight.text = "몸무게: $weight kg"
                    tvDistance.text = "누적 달린 거리: $distance km"
                    tvShapes.text = "만든 도형 수: $shapes"
                }
            }
            .addOnFailureListener {
                tvName.text = "불러오기 실패"
            }
    }


    // 메뉴 연결
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    // 로그아웃 메뉴 클릭 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // 또는 finish()
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    // 로그아웃 확인 다이얼로그
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
