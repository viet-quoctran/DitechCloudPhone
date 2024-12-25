package com.example.cloudphone.Action

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.cloudphone.Accessibility.AccessibilityManager
import com.example.cloudphone.Accessibility.AccessibilityUtils
import kotlin.random.Random

class Swipe(
    private val context: Context,
    private val accessibilityService: AccessibilityManager
) {
    fun swipeMultipleTimes(minTimes: Int, maxTimes: Int, onComplete: () -> Unit) {
        val totalSwipes = Random.nextInt(minTimes, maxTimes + 1) // Chọn số lần swipe ngẫu nhiên
        var currentSwipe = 0

        fun swipeAndPause() {
            if (currentSwipe < totalSwipes) {
                currentSwipe++
                swipeToNextVideo()

                val delay = Random.nextLong(5000, 10000) // Random delay giữa các lần swipe
                Log.d("OpenTiktok", "Swipe $currentSwipe/$totalSwipes done. Waiting $delay ms.")
                Handler(Looper.getMainLooper()).postDelayed({ swipeAndPause() }, delay)
            } else {
                Log.d("OpenTiktok", "All swipes completed.")
                onComplete() // Gọi callback khi hoàn thành tất cả các swipes
            }
        }

        swipeAndPause() // Bắt đầu thực hiện swipe
    }

    private fun swipeToNextVideo() {
        val displayMetrics = context.resources.displayMetrics
        val startX = displayMetrics.widthPixels * 0.5f
        val startY = displayMetrics.heightPixels * 0.8f
        val endY = displayMetrics.heightPixels * 0.2f

        // Thực hiện thao tác vuốt
        AccessibilityManager.getInstance()?.performSwipeGesture(
            startX, startY, startX, endY, Random.nextLong(300, 500)
        )
    }
}
