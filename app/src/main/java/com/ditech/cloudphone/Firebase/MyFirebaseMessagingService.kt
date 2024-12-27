package com.ditech.cloudphone.Firebase

import android.util.Log
import com.ditech.cloudphone.Accessibility.AccessibilityManager
import com.ditech.cloudphone.Tiktok.MainActionTiktok
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "firebase_channel"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        Log.d("data",data.toString())
        if(data.isNotEmpty())
        {
            try {
                val jsonData = JSONObject(data as Map<String, Any>)
                val channel = JSONObject(jsonData.getString("channel")) // Chuyển chuỗi thành JSONObject
                val name = channel.getString("name") // Lấy "name" từ JSONObject
                val userName = channel.getString("username") // Lấy "username" từ JSONObject
                // Parse trường "action_data"
                val actionDataArray = JSONArray(jsonData.getString("action_data"))
                // Lặp qua từng phần tử trong mảng
                var action = 0
                var watchTimeFrom = 0
                var watchTimeTo = 0
                var videoLimitFrom = 0
                var videoLimitTo = 0
                for (i in 0 until actionDataArray.length()) {
                    val actionObject = actionDataArray.getJSONObject(i) // Lấy từng đối tượng JSON trong mảng
                    val type = actionObject.getString("type") // Lấy giá trị "type"
                    action = when (type) {
                        "randomVideo" -> 1
                        else -> 0
                    }
                    // Lấy watchTime (from, to)
                    val watchTime = actionObject.getJSONObject("options").getJSONObject("watchTime")
                    watchTimeFrom = watchTime.getInt("from")
                    watchTimeTo = watchTime.getInt("to")

                    // Lấy videoLimit (from, to)
                    val videoLimit = actionObject.getJSONObject("options").getJSONObject("videoLimit")
                    videoLimitFrom = videoLimit.getInt("from")
                    videoLimitTo = videoLimit.getInt("to")

                }

                val context = applicationContext
                val accessibilityManager = AccessibilityManager()

                val mainActionTiktok = MainActionTiktok(
                    context,
                    accessibilityManager,
                    name,
                    userName,
                    action,
                    watchTimeFrom,
                    watchTimeTo,
                    videoLimitFrom,
                    videoLimitTo

                )
                mainActionTiktok.launchTikTok()
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing JSON: ${e.message}")
            }
        }



    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New token: $token")
    }

}
