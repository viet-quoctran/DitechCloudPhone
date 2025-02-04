package com.ditech.cloudphone.Network

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ditech.cloudphone.Model.DataVideoDelete
import com.ditech.cloudphone.Utils.TaskManager
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray

object ApiClient {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    // GET request
    fun get(apiUrl: String): JSONObject? {
        return try {
            val request = Request.Builder()
                .url(apiUrl) // Thêm API Key vào header
                .addHeader("x-api-key", "lwIQHRoYAkZGRW16Zvp1jKBuocAgeQ8U")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                responseBody?.let { JSONObject(it) }
            } else {
                Log.e("API Error", "Message: ${response.code}")
                Log.e("API Error", "Response Body: ${response.body?.string()}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // POST request
    fun post(apiUrl: String, payload: String): JSONObject? {
        return try {
            val requestBody = payload.toRequestBody("application/json;charset=utf-8".toMediaType())

            // Tạo request với header `x-api-key`
            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("x-api-key", "lwIQHRoYAkZGRW16Zvp1jKBuocAgeQ8U") // Thêm API Key vào header
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            // Kiểm tra trạng thái phản hồi
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                responseBody?.let { JSONObject(it) }
            } else {
                Log.e("API Error", "Status Code: ${response.code}")
                Log.e("API Error", "Message: ${response.message}")
                Log.e("API Error", "Body: ${response.body?.string()}")
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun fetchVideosByChannel(scriptId: Int,channelId: Int, limit: Int): JSONArray? {
        val apiUrl = "https://deca8.com/api/videos/by-channel?channel_id=$channelId&limit=$limit&script_id=$scriptId"
        val response = get(apiUrl)

        return response?.optJSONArray("data") // Adjust based on the API's response structure
    }
    fun fetchFirstVideoDetails(scriptId: Int, channelId: Int, limit: Int): Map<String, String>? {
        val videosArray = fetchVideosByChannel(scriptId,channelId, limit)
        return videosArray?.let {
            if (it.length() > 0) {
                val firstVideo = it.getJSONObject(0) // Lấy phần tử đầu tiên
                mapOf(
                    "uuid" to firstVideo.getString("uuid"),
                    "status" to firstVideo.getString("status"),
                    "sound" to firstVideo.getString("sound"),
                    "hashtag" to firstVideo.getString("hashtag"),
                    "video_file_url" to firstVideo.getString("video_file_url"),
                    "shop_product_name" to firstVideo.getString("shop_product_name")
                )
            } else {
                Log.e("Video Details", "No videos found in API response.")
                null
            }
        } ?: run {
            Log.e("Video Details", "Failed to fetch videos from API.")
            null
        }
    }
    fun deleteVideoWithApiFromStorage() {
        val uuid = DataVideoDelete.uuid
        val scriptId = DataVideoDelete.scriptId

        val apiUrl = if (!uuid.isNullOrEmpty() && scriptId != null) {
            "https://deca8.com/api/videos/uploaded?script_id=$scriptId&video_uuid=$uuid"
        } else null

        apiUrl?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val response = post(it, "{}")
                if (response != null) {
                    DataVideoDelete.uuid = null
                    DataVideoDelete.scriptId = null
                    Log.d("API Response", "Video deleted successfully: $response")
                } else {
                    Log.e("API Error", "Failed to delete video with script_id: $scriptId and video_uuid: $uuid")
                }
            }
        } ?: Log.e("Video Delete", "UUID or script_id is missing. Cannot delete video.")
    }
    fun sendNotiToTelegram(message: String?, token: String?, context: Context) {
        // Xử lý null bằng cách cung cấp giá trị mặc định
        val safeMessage = message ?: "No message provided"
        val safeToken = token ?: "NoToken"

        val apiUrl = "https://deca8.com/api/telegram/send?message=${Uri.encode(safeMessage)}&token=$safeToken"

        apiUrl.let {
            CoroutineScope(Dispatchers.IO).launch {
                val response = post(it, "{}")
                if (response != null) {
                    Log.d("API Response", "Notification sent successfully: $response")
                    restartTask(context)
                } else {
                    Log.e("API Error", "Failed to send notification to Telegram.")
                }
            }
        }
    }
    fun sendNotiSuccessToTelegram(message: String?, token: String?) {
        // Xử lý null bằng cách cung cấp giá trị mặc định
        val safeMessage = message ?: "No message provided"
        val safeToken = token ?: "NoToken"

        val apiUrl = "https://deca8.com/api/telegram/send?message=${Uri.encode(safeMessage)}&token=$safeToken"

        apiUrl.let {
            CoroutineScope(Dispatchers.IO).launch {
                val response = post(it, "{}")
                if (response != null) {
                    Log.d("API Response", "Notification sent successfully: $response")
                } else {
                    Log.e("API Error", "Failed to send notification to Telegram.")
                }
            }
        }
    }
    private fun restartTask(context: Context) {
        try {
            val sharedPreferences = context.getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE)
            val taskData = sharedPreferences.getString("lastTaskData", null)

            if (!taskData.isNullOrEmpty()) {
                val taskManager = TaskManager(context)
                taskManager.startTask(taskData) // Chạy lại task với dữ liệu đã lưu
                Log.d("TaskManager", "Task restarted successfully.")
            } else {
                Log.e("TaskManager", "No task data found to restart.")
            }
        } catch (e: Exception) {
            Log.e("TaskManager", "Error restarting task: ${e.message}")
        }
    }

}
