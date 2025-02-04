package com.ditech.cloudphone.Firebase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ditech.cloudphone.Utils.TaskBroadcastReceiver
import com.ditech.cloudphone.Utils.TaskManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.Calendar

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }
    private var taskManager: TaskManager? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            try {
                val jsonData = JSONObject(data as Map<String, Any>)
                val status = jsonData.optString("status", "inactive")
                val schedule = jsonData.optString("schedule", "00:00:00")
                val taskId = jsonData.optInt("script_id", 0)
                val timeParts = schedule.split(":")
                val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
                val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                val second = timeParts.getOrNull(2)?.toIntOrNull() ?: 0
                if (taskManager == null) {
                    taskManager = TaskManager(applicationContext)
                }
                when (status) {
                    "active" -> {
                        val taskData = jsonData.toString()
                        Log.d("taskData",taskData)
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, second)
                        }

                        // Nếu giờ hẹn đã qua trong ngày, đặt task cho ngày hôm sau
                        if (calendar.timeInMillis <= System.currentTimeMillis()) {
                            calendar.add(Calendar.DAY_OF_YEAR, 1)
                        }

                        // Cài đặt báo thức với AlarmManager
                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent = Intent(this, TaskBroadcastReceiver::class.java).apply {
                            putExtra("taskData", taskData) // Truyền dữ liệu task
                        }

                        val pendingIntent = PendingIntent.getBroadcast(
                            this,
                            taskId, // `taskId` làm requestCode để phân biệt các task
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )

                        taskManager?.addTaskId(taskId)
                    }
                    "inactive" -> {
                        cancelTask(taskId)
                        taskManager?.stopTask(taskId)
                        Log.d(TAG, "Task $taskId đã bị hủy.")
                    }
                    else -> {
                        Log.w(TAG, "Trạng thái không xác định: $status")
                    }
                }
                taskManager?.logCurrentTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi xử lý JSON hoặc quản lý task: ${e.message}")
            }
        }
    }

    private fun cancelTask(taskId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TaskBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent) // Hủy task dựa trên taskId
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New token: $token")
    }
}
