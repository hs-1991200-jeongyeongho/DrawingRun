package com.example.drawingrun

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            val isLoggedIn = checkLoginState() // 예: FirebaseAuth.getInstance().currentUser != null
            val intent = if (isLoggedIn) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 1500)
    }

    private fun checkLoginState(): Boolean {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        return user != null
    }
}
