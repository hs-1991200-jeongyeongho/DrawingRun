package com.example.drawingrun

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class UserInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        val nameEditText = findViewById<EditText>(R.id.editTextName)
        val ageEditText = findViewById<EditText>(R.id.editTextAge)
        val heightEditText = findViewById<EditText>(R.id.editTextHeight)
        val weightEditText = findViewById<EditText>(R.id.editTextWeight)
        val submitButton = findViewById<Button>(R.id.buttonSubmit)

        val userId = intent.getStringExtra("userId") ?: return

        submitButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val age = ageEditText.text.toString()
            val height = heightEditText.text.toString()
            val weight = weightEditText.text.toString()

            if (name.isBlank() || age.isBlank() || height.isBlank() || weight.isBlank()) {
                Toast.makeText(this, "모든 정보를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = hashMapOf(
                "name" to name,
                "age" to age.toInt(),
                "height" to height.toInt(),
                "weight" to weight.toDouble()
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "저장 완료!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DrawingActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
