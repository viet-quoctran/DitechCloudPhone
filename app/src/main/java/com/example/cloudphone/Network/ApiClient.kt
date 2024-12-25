package com.example.cloudphone.Network

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
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
}
