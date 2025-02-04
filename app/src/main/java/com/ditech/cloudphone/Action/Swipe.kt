package com.ditech.cloudphone.Action

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ditech.cloudphone.Accessibility.AccessibilityManager
import com.ditech.cloudphone.Accessibility.SaveInstance
import com.ditech.cloudphone.Network.ApiClient
import com.ditech.cloudphone.Tiktok.MainActionTiktok
import com.ditech.cloudphone.Utils.TaskManager
import com.ditech.cloudphone.Utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.random.Random

class Swipe(
    private val context: Context,
    private val accessibilityService: AccessibilityManager
) {
    private val sharedPreferences = context.getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE)

    // Hàm kiểm tra trạng thái từ SharedPreferences
    private fun isTaskRunning(): Boolean {
        return sharedPreferences.getBoolean("isRunning", false)
    }
    fun swipeMultipleTimes(minVideos: Int, maxVideos: Int, minTimes: Int, maxTimes: Int, type: String, id: Int, onComplete: () -> Unit) {
        val totalSwipes = Random.nextInt(minVideos, maxVideos + 1)
        var currentSwipe = 0

        val handler = Handler(Looper.getMainLooper())
        fun swipeAndPause() {
            if (currentSwipe < totalSwipes) {
                currentSwipe++
                swipeToNextVideo()

                val delay = Random.nextLong(minTimes.toLong(), maxTimes.toLong()) * 1000
                sendStatusTokenToBackend(TokenManager.getToken(context) ?: "", type, id)

                // Gọi lại chính nó sau thời gian trì hoãn
                handler.postDelayed({ swipeAndPause() }, delay)
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
    private fun sendStatusTokenToBackend(token: String,type: String, id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val apiUrl = "https://deca8.com/api/device-activity/send?"
            val payload = JSONObject().apply {
                put("token", token)
                put("type", type)
                put("channel_id", id)
            }.toString()
            val response = ApiClient.post(apiUrl, payload)
            withContext(Dispatchers.Main) {
                if (response != null) {
                    val message = response.optString("message", "")
                    if (message == "Channel status updated successfully.") {
                        Log.d("checkStatus", "oke")
                    } else {
                        Log.d("checkStatus", "fail: $message")
                    }
                } else {
                    Log.d("checkStatus", "Không thể kết nối đến máy chủ.")
                }
            }
        }
    }
}
