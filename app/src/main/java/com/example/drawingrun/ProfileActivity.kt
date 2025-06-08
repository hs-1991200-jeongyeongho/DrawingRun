package com.example.drawingrun

import android.widget.Toast
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
                loadSavedImagesIntoRecyclerView()
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
        loadSavedImagesIntoRecyclerView()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    try {
                        val name = AESUtil.decrypt(document.getString("name") ?: "")
                        val age = AESUtil.decrypt(document.getString("age") ?: "")
                        val height = AESUtil.decrypt(document.getString("height") ?: "")
                        val weight = AESUtil.decrypt(document.getString("weight") ?: "")
                        val distance = document.getDouble("totalDistance") ?: 0.0

                        tvName.text = name
                        tvAge.text = "나이: $age"
                        tvHeight.text = "키: $height cm"
                        tvWeight.text = "몸무게: $weight kg"
                        tvDistance.text = "누적 달린 거리: %.2f km".format(distance)
                    } catch (e: Exception) {
                        Toast.makeText(this, "복호화 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "불러오기 실패", Toast.LENGTH_SHORT).show()
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

    private fun loadSavedImagesIntoRecyclerView() {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.RELATIVE_PATH
        )
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

        val recyclerView = findViewById<RecyclerView>(R.id.rvSavedImages)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = SavedImageAdapter(imageUris, showAllCallback = {
            startActivity(Intent(this, AllSavedImagesActivity::class.java))
        })
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
