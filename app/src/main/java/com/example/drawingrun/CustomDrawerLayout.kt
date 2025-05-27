package com.example.drawingrun

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class CustomDrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : DrawerLayout(context, attrs) {

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        // 왼쪽 사이드바가 열려 있어도 메인 콘텐츠 터치 허용
        val isDrawerOpen = isDrawerOpen(GravityCompat.START)
        return if (isDrawerOpen) false else super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        val isDrawerOpen = isDrawerOpen(GravityCompat.START)
        return if (isDrawerOpen) false else super.onTouchEvent(ev)
    }
}
