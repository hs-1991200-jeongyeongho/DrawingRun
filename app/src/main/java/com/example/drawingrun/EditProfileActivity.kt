package com.example.drawingrun

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        val age = etAge.text.toString().trim().toIntOrNull()
        val height = etHeight.text.toString().trim().toDoubleOrNull()
        val weight = etWeight.text.toString().trim().toDoubleOrNull()

        if (name.isEmpty() || age == null || height == null || weight == null) {
            Toast.makeText(this, "모든 항목을 올바르게 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val data = hashMapOf(
            "name" to name,
            "age" to age,
            "height" to height,
            "weight" to weight
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(data)
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
