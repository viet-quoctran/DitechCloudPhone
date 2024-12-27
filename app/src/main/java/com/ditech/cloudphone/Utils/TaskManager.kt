//package com.ditech.cloudphone.Utils
//
//import android.content.Context
//import android.util.Log
//import com.ditech.cloudphone.Accessibility.AccessibilityManager
//import com.ditech.cloudphone.Tiktok.MainActionTiktok
//import kotlinx.coroutines.*
//
//class TaskManager(private val context: Context) {
//    private var job: Job? = null
//
//    fun startTask() {
//        if (job?.isActive == true) {
//            Log.d("TaskManager", "Task is already running.")
//            return // Nếu task đang chạy, không khởi động lại
//        }
//
//        job = CoroutineScope(Dispatchers.IO).launch {
//            while (isActive) {
//                Log.d("TaskManager", "Task is running...")
//                try {
//                    val accessibilityManager = AccessibilityManager()
//                    val mainActionTiktok = MainActionTiktok(context, accessibilityManager)
//                    mainActionTiktok.launchTikTok()
//                } catch (e: Exception) {
//                    Log.e("TaskManager", "Error launching TikTok: ${e.message}")
//                }
//                delay(1000) // Chờ 1 giây giữa mỗi lần lặp
//            }
//        }
//    }
//
//    fun stopTask() {
//        if (job?.isActive == true) {
//            job?.cancel() // Hủy job nếu đang chạy
//            Log.d("TaskManager", "Task is stopped.")
//        } else {
//            Log.d("TaskManager", "No task to stop.")
//        }
//    }
//}
