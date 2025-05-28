package com.example.drawingrun

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent> // 🔧 선언만

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 🔐 launcher 초기화
        editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadUserProfile()
            }
        }

        // 툴바 설정
        val toolbar = findViewById<Toolbar>(R.id.profileToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 뷰 바인딩
        tvName = findViewById(R.id.tvUserName)
        tvAge = findViewById(R.id.tvUserAge)
        tvHeight = findViewById(R.id.tvUserHeight)
        tvWeight = findViewById(R.id.tvUserWeight)
        tvDistance = findViewById(R.id.tvTotalDistance)

        // 프로필 수정 버튼
        val editButton = findViewById<Button>(R.id.btnEditProfile)
        editButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            editProfileLauncher.launch(intent) // ✅ 안전한 방식
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
                    val distance = document.getDouble("totalDistance") ?: 0.0

                    tvName.text = name
                    tvAge.text = "나이: $age"
                    tvHeight.text = "키: $height cm"
                    tvWeight.text = "몸무게: $weight kg"
                    tvDistance.text = "누적 달린 거리: %.2f km".format(distance)
                }
            }
            .addOnFailureListener {
                tvName.text = "불러오기 실패"
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(R.color.blue_confirm))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(getColor(R.color.red_cancel))
        }

        dialog.show()
    }
}
