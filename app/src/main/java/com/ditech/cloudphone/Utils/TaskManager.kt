package com.ditech.cloudphone.Utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ditech.cloudphone.Accessibility.AccessibilityManager
import com.ditech.cloudphone.Model.Channel
import com.ditech.cloudphone.Network.ApiClient
import com.ditech.cloudphone.Tiktok.MainActionTiktok
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject


class TaskManager(private val context: Context) {
    private var job: Job? = null
    private var mainActionTiktok: MainActionTiktok? = null
    private var accessibilityManager: AccessibilityManager? = null
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE)

    fun isTaskRunning(): Boolean {
        return sharedPreferences.getBoolean("isRunning", false)
    }
    private fun setTaskRunning(isRunning: Boolean) {
        sharedPreferences.edit().putBoolean("isRunning", isRunning).apply()
    }
    fun addTaskId(taskId: Int) {
        val taskIds = sharedPreferences.getStringSet("taskIds", mutableSetOf()) ?: mutableSetOf()
        taskIds.add(taskId.toString())
        sharedPreferences.edit().putStringSet("taskIds", taskIds).apply()
    }
    private fun removeTaskId(taskId: Int) {
        val taskIds = sharedPreferences.getStringSet("taskIds", mutableSetOf()) ?: mutableSetOf()
        taskIds.remove(taskId.toString())
        sharedPreferences.edit().putStringSet("taskIds", taskIds).apply()
    }
    private fun getTaskIds(): Set<Int> {
        val taskIds = sharedPreferences.getStringSet("taskIds", mutableSetOf()) ?: mutableSetOf()
        return taskIds.mapNotNull { it.toIntOrNull() }.toSet()
    }
    fun startTask(data: String) {
        if (job?.isActive == true) {
            Log.d("TaskManager", "Task is already running.")
            return
        }

        sharedPreferences.edit().putString("lastTaskData", data).apply()
        setTaskRunning(true)
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonData = JSONObject(data)
                val channelsArray = JSONArray(jsonData.getString("channels"))
                val channels = mutableListOf<Channel>()
                for (i in 0 until channelsArray.length()) {
                    val channelObj = channelsArray.getJSONObject(i)
                    val id = channelObj.getInt("id")
                    val name = channelObj.getString("name")
                    val username = channelObj.getString("username")
                    channels.add(Channel(id, name, username))
                }

                val actionDataArray = JSONArray(jsonData.getString("action_data"))
                for (i in 0 until actionDataArray.length()) {
                    val actionObject = actionDataArray.getJSONObject(i)
                    val type = actionObject.getString("type")
                    // Khởi tạo biến data để lưu thông tin chung
                    val data: Any? = when (type) {
                        "randomVideo" -> {
                            // Nếu type là "randomVideo", gộp các biến liên quan
                            val watchTime = actionObject.getJSONObject("options").getJSONObject("watchTime")
                            val watchTimeFrom = watchTime.getInt("from")
                            val watchTimeTo = watchTime.getInt("to")
                            val videoLimit = actionObject.getJSONObject("options").getJSONObject("videoLimit")
                            val videoLimitFrom = videoLimit.getInt("from")
                            val videoLimitTo = videoLimit.getInt("to")

                            // Gộp thành một đối tượng Map hoặc data class
                            mapOf(
                                "watchTimeFrom" to watchTimeFrom,
                                "watchTimeTo" to watchTimeTo,
                                "videoLimitFrom" to videoLimitFrom,
                                "videoLimitTo" to videoLimitTo
                            )
                        }

                        "uploadVideo" -> {
                            val scriptId = jsonData.getInt("script_id") // Lấy script_id từ JSON
                            // Nếu type là "uploadVideo", lấy thông tin liên quan
                            val uploadVideoOptions = actionObject.getJSONObject("options").getJSONObject("uploadVideoOptions")
                            val waitTimeFrom = uploadVideoOptions.getJSONObject("waitTime").getInt("from")
                            val waitTimeTo = uploadVideoOptions.getJSONObject("waitTime").getInt("to")
                            val numberPerDayFrom = uploadVideoOptions.getJSONObject("numberPerDay").getInt("from")
                            val numberPerDayTo = uploadVideoOptions.getJSONObject("numberPerDay").getInt("to")
                            val selectedVideos = uploadVideoOptions.getJSONArray("selectedVideos")
                            val channelId = channels.firstOrNull()?.id ?: throw Exception("Channel ID not found")
                            val dataMap = if (selectedVideos.length() > 0) {
                                Log.d("SelectVideos", "${selectedVideos.length()} videos selected")
                                mapOf(
                                    "selectedVideos" to (0 until selectedVideos.length()).map { selectedVideos.getString(it) }
                                )
                            } else {
                                Log.d("SelectVideos", "No videos selected, fetching from API...")
                                val apiVideoData = ApiClient.fetchFirstVideoDetails(scriptId,channelId, 1)
                                apiVideoData ?: throw Exception("Failed to fetch video details from API.")
                            }
                            // Gộp thành một đối tượng Map hoặc data class
                            mapOf(
                                "scriptId" to scriptId,
                                "waitTimeFrom" to waitTimeFrom,
                                "waitTimeTo" to waitTimeTo,
                                "numberPerDayFrom" to numberPerDayFrom,
                                "numberPerDayTo" to numberPerDayTo
                            ) + dataMap
                        }

                        else -> null // Xử lý nếu type không khớp
                    }

                    accessibilityManager = AccessibilityManager()
                    mainActionTiktok = MainActionTiktok(
                        context,
                        accessibilityManager!!,
                        id = 0,
                        name = "",
                        userName = "",
                        type,
                        data = data // Truyền toàn bộ `data` vào đây
                    )
                    mainActionTiktok?.launchTikTok(channels)
                }
            } catch (e: Exception) {
                Log.e("TaskManager", "Error processing task: ${e.message}")
            }
        }
    }

    fun stopTask(taskId: Int) {
        removeTaskId(taskId)
        setTaskRunning(false)
        Log.d("TaskManager", "Stopping Task $taskId")
    }
    fun logCurrentTasks() {
        val taskIds = getTaskIds()
        Log.d("TaskManager", "Số lượng task đang chạy: ${taskIds.size}")
        Log.d("TaskManager", "Danh sách task ID: $taskIds")
    }
}

