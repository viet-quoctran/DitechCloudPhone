package com.ditech.cloudphone.Utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TaskBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TaskBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskData = intent.getStringExtra("taskData") // Lấy dữ liệu task từ intent
        if (taskData != null) {
            try {
                val taskManager = TaskManager(context)
                taskManager.startTask(taskData) // Kích hoạt task
                Log.d(TAG, "Task đã được thực thi với dữ liệu: $taskData")
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi thực thi task: ${e.message}")
            }
        } else {
            Log.w(TAG, "Không tìm thấy dữ liệu task trong intent.")
        }
    }
}
