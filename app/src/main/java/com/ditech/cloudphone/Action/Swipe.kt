package com.ditech.cloudphone.Action

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ditech.cloudphone.Accessibility.AccessibilityManager
import com.ditech.cloudphone.Accessibility.SaveInstance
import kotlin.random.Random

class Swipe(
    private val context: Context,
    private val accessibilityService: AccessibilityManager
) {
    fun swipeMultipleTimes(minVideos: Int, maxVideos: Int, minTimes: Int, maxTimes: Int, onComplete: () -> Unit) {
        val totalSwipes = Random.nextInt(minVideos, maxVideos + 1)
        var currentSwipe = 0

        fun swipeAndPause() {
            if (currentSwipe < totalSwipes) {
                currentSwipe++
                swipeToNextVideo()

                val delay = Random.nextLong(minTimes.toLong(), maxTimes.toLong()) // Giá trị mặc định (hoặc có thể nhận từ JSON)
                Log.d("Swipe", "Swipe $currentSwipe/$totalSwipes. Waiting $delay ms.")
                Handler(Looper.getMainLooper()).postDelayed({ swipeAndPause() }, delay)
            } else {
                Log.d("Swipe", "All swipes completed.")
                onComplete()
            }
        }

        swipeAndPause()
    }

    private fun swipeToNextVideo() {
        val displayMetrics = context.resources.displayMetrics
        val startX = displayMetrics.widthPixels * 0.5f
        val startY = displayMetrics.heightPixels * 0.8f
        val endY = displayMetrics.heightPixels * 0.2f
        val accessibilityService = SaveInstance.accessibilityService

        if (accessibilityService != null) {
            accessibilityService.performSwipeGesture(
                startX, startY, startX, endY, Random.nextLong(300, 500)
            )
            Log.d("Swipe", "Performed swipe gesture from ($startX, $startY) to ($startX, $endY).")
        } else {
            Log.e("Swipe", "AccessibilityService is null. Cannot perform swipe gesture.")
        }
    }
}
