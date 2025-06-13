package com.example.drawingrun

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 9001
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val idEditText = findViewById<EditText>(R.id.editTextId)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val registerButton = findViewById<Button>(R.id.buttonGoToRegister)
        val googleButton = findViewById<SignInButton>(R.id.buttonGoogleLogin)

        // 👉 Google 로그인 옵션 설정 (client ID는 web client ID로 대체해야 함)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("666022316419-9hdimg9oiuomkesss0ttrfn5iff7bgh4.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 🔸 일반 로그인
        loginButton.setOnClickListener {
            val rawId = idEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (rawId.isBlank() || password.isBlank()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = if (rawId.contains("@")) rawId.trim() else "${rawId.trim()}@dummy.com"

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    checkUserInfo(auth.currentUser?.uid)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "로그인 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 🔸 Google 로그인
        googleButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // 🔸 회원가입 화면으로 이동
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // 🔹 Google 로그인 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔹 Google 계정으로 Firebase 인증
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                checkUserInfo(auth.currentUser?.uid)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Firebase 인증 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔹 Firestore 사용자 정보 확인 → Main 또는 UserInfo로 이동
    private fun checkUserInfo(userId: String?) {
        if (userId == null) return

        val user = auth.currentUser ?: return
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // 🔸 문서가 없으면 사용자 정보 일부라도 생성
                    val initData = mapOf(
                        "name" to (user.displayName ?: ""),
                    )
                    userRef.set(initData)
                        .addOnSuccessListener {
                            goToUserInfo(userId)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "사용자 문서 생성 실패", Toast.LENGTH_SHORT).show()
                        }
                } else if (
                    !document.contains("age") ||
                    !document.contains("height") ||
                    !document.contains("weight")
                ) {
                    // 🔸 필드가 부족하면 UserInfoActivity로
                    goToUserInfo(userId)
                } else {
                    // 🔸 모든 필드가 있으면 MainActivity로 이동
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "사용자 정보 확인 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToUserInfo(userId: String) {
        val intent = Intent(this, UserInfoActivity::class.java).apply {
            putExtra("userId", userId)
        }
        startActivity(intent)
        finish()
    }
}
