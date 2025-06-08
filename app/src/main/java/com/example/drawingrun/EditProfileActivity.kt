package com.example.drawingrun

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        etName = findViewById(R.id.etName)
        etAge = findViewById(R.id.etAge)
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)

        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            saveProfileToFirestore()
        }
    }

    private fun saveProfileToFirestore() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val name = etName.text.toString().trim()
        val age = etAge.text.toString().trim()
        val height = etHeight.text.toString().trim()
        val weight = etWeight.text.toString().trim()

        if (name.isEmpty() || age.isEmpty() || height.isEmpty() || weight.isEmpty()) {
            Toast.makeText(this, "모든 항목을 올바르게 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔐 AES 암호화 적용
        val data = hashMapOf(
            "name" to AESUtil.encrypt(name),
            "age" to AESUtil.encrypt(age),
            "height" to AESUtil.encrypt(height),
            "weight" to AESUtil.encrypt(weight)
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                setResult(RESULT_OK)
                Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show()
            }
    }


}
