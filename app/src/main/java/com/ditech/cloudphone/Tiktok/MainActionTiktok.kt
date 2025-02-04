package com.ditech.cloudphone.Tiktok

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.session.MediaSession.Token
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import com.ditech.cloudphone.Accessibility.AccessibilityManager
import com.ditech.cloudphone.Accessibility.AccessibilityUtils
import com.ditech.cloudphone.AppInfo.AppInfoManager
import com.ditech.cloudphone.Action.Swipe
import com.ditech.cloudphone.Utils.CONFIG
import com.ditech.cloudphone.Action.LoginChannel
import com.ditech.cloudphone.Action.Upload
import com.ditech.cloudphone.Model.Channel
import com.ditech.cloudphone.Model.DataVideoDelete
import com.ditech.cloudphone.Utils.TaskManager
import com.ditech.cloudphone.Model.RandomVideoParams
import com.ditech.cloudphone.Network.ApiClient
import com.ditech.cloudphone.Utils.DowloadVideos
import com.ditech.cloudphone.Utils.TokenManager
import kotlinx.coroutines.*
class MainActionTiktok(
    private val context: Context,
    private val accessibilityService: AccessibilityManager,
    private var id: Int,
    private var name: String,
    private var userName: String,
    private val type: String,
    private val data: Any?
) {
    init {
        Log.d("MainActionTiktok", "Initialized with data: $data")
    }
    val randomVideoData = data as Map<String, Any>

    private val swipe = Swipe(context, accessibilityService)
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE)
    private fun isTaskRunning(): Boolean {
        return sharedPreferences.getBoolean("isRunning", false)
    }

    fun launchTikTok(channels: List<Channel>) {
        try {
            AppInfoManager.openAppWithText(context,"CloudPhone",CONFIG.PACKAGE_NAME_TIKTOK){
                AppInfoManager.openAppInfo(context, CONFIG.PACKAGE_NAME_TIKTOK) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(3000)
                        launchTikTokDirect(CONFIG.PACKAGE_NAME_TIKTOK, channels)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("OpenTiktok", "Error in launchTikTok: ${e.message}")
        }
    }

    fun processChannelsSequentially(channels: List<Channel>, index: Int = 0) {
        if (index >= channels.size) {
            AppInfoManager.openAppWithText(context,"CloudPhone",CONFIG.PACKAGE_NAME_TIKTOK){
                Log.d("OpenTiktok", "Done swipe")
            }
        }
        val channel = channels[index]
        this.id = channel.id
        this.name = channel.name
        this.userName = channel.username
        processProfileClick {
            processChannelsSequentially(channels, index + 1)
        }
    }
    private fun launchTikTokDirect(packageName: String, channels: List<Channel>) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            } else {
                ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_OPEN_TIKTOK,TokenManager.getToken(context),context)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                processChannelsSequentially(channels)
            }, 5000)
        } catch (e: Exception) {
            Log.e("OpenTiktok", "Error launching TikTok: ${e.message}")
        }
    }
    private fun openTikTokWithLink(link: String, context: Context, maxRetries: Int = 10, retryDelay: Long = 5000) {
        var attempts = 0

        fun checkAndRetry() {
            AccessibilityUtils.checkElementWithRetries(CONFIG.ELEMENT_DNC, 1, 0) { found ->
                if (found) {
                    Log.d("OpenTiktok", "CONFIG.ELEMENT_DNC found!")
                    Upload.clickVideoSound(context) // Tiếp tục xử lý khi tìm thấy phần tử
                } else {
                    attempts++
                    if (attempts < maxRetries) {
                        // Intent lại link TikTok
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(link)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            setPackage(CONFIG.PACKAGE_NAME_TIKTOK) // Specify TikTok package
                        }
                        context.startActivity(intent)

                        // Đợi trước khi kiểm tra lại
                        Handler(Looper.getMainLooper()).postDelayed({ checkAndRetry() }, retryDelay)
                    } else {
                        Log.e("OpenTiktok", "Failed to find CONFIG.ELEMENT_DNC after $maxRetries attempts.")
                    }
                }
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({ checkAndRetry() }, retryDelay)
    }
    private fun processProfileClick(onComplete: () -> Unit) {
        AccessibilityUtils.checkElementWithRetries(CONFIG.BUTTON_PROFILE, 5, 2000) { found ->
            if (found) {
                AccessibilityUtils.clickByViewId(CONFIG.BUTTON_PROFILE) // Click profile
                getUsername(onComplete)
            } else {
                ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_FIND_NAME,TokenManager.getToken(context),context)
                onComplete()
            }
        }
    }
    private fun getUsername(onComplete: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            AccessibilityUtils.checkElementWithRetries(CONFIG.BUTTON_LIST_CHANNELS, 5, 2000) { found ->
                if (found) {
                    checkAndCompareUsername(onComplete)
                } else {
                    ApiClient.sendNotiToTelegram("Username not found",TokenManager.getToken(context),context)
                    onComplete()
                }
            }
        }, 5000)
    }
    fun getRandomVideoParameters(): Map<String, Int> {
        if (type == "randomVideo" && data is Map<*, *>) {
            return mapOf(
                "videoLimitFrom" to (data["videoLimitFrom"] as? Int ?: 0),
                "videoLimitTo" to (data["videoLimitTo"] as? Int ?: 0),
                "watchTimeFrom" to (data["watchTimeFrom"] as? Int ?: 0),
                "watchTimeTo" to (data["watchTimeTo"] as? Int ?: 0)
            )
        }
        return emptyMap()
    }
    fun handleAction(type: String, context: Context, id: Int, onComplete: () -> Unit) {
        when (type) {
            "randomVideo" -> {
                val randomVideoParams = getRandomVideoParameters()
                val videoLimitFrom = randomVideoParams["videoLimitFrom"] ?: 0
                val videoLimitTo = randomVideoParams["videoLimitTo"] ?: 0
                val watchTimeFrom = randomVideoParams["watchTimeFrom"] ?: 0
                val watchTimeTo = randomVideoParams["watchTimeTo"] ?: 0

                swipe.swipeMultipleTimes(videoLimitFrom, videoLimitTo, watchTimeFrom, watchTimeTo, type, id) {
                    AppInfoManager.openAppWithText(context,"App info",CONFIG.PACKAGE_NAME_TIKTOK) {
                        onComplete()
                    }
                }
            }
            "uploadVideo" -> {
                val dataMap = data as? Map<String, Any>
                Log.d("checkScript",dataMap.toString())
                if (dataMap != null) {
                    val scriptId = dataMap["scriptId"] as? Int
                    val waitTimeFrom = dataMap["waitTimeFrom"] as? Int
                    val waitTimeTo = dataMap["waitTimeTo"] as? Int
                    val numberPerDayFrom = dataMap["numberPerDayFrom"] as? Int
                    val numberPerDayTo = dataMap["numberPerDayTo"] as? Int
                    val uuid = dataMap["uuid"] as? String
                    val status = dataMap["status"] as? String
                    val sound = dataMap["sound"] as? String
                    val product_name = dataMap["shop_product_name"] as? String
                    val hashtag = dataMap["hashtag"] as? String
                    if (!hashtag.isNullOrEmpty()) {
                        CONFIG.setHashtag(hashtag) // Lưu hashtag vào CONFIG
                    }
                    if (!product_name.isNullOrEmpty()) {
                        CONFIG.setProductName(product_name) // Lưu hashtag vào CONFIG
                    }
                    val videoFileUrl = dataMap["video_file_url"] as? String
                    if (!videoFileUrl.isNullOrEmpty()) {
                        Log.d("UploadVideo", "Downloading video from URL: $videoFileUrl")
                        val fileName = "TikTok_${uuid ?: System.currentTimeMillis()}.mp4"
                        // Gọi hàm tải video và chờ khi hoàn tất
                        CoroutineScope(Dispatchers.IO).launch {
                            DowloadVideos.downloadUsingDownloadManager(context, videoFileUrl, fileName) { success ->
                                if (success) {
                                    swipe.swipeMultipleTimes(2, 5, 3, 10, type, id) {
                                        DataVideoDelete.uuid = uuid
                                        DataVideoDelete.scriptId = scriptId
                                        if (sound == "null") {
                                            CONFIG.setSound("null")
                                            Upload.clickAddVideo(context)
                                        } else {
                                            CONFIG.setSound(sound.toString())
                                            AppInfoManager.openAppWithText(context, "CloudPhone", CONFIG.PACKAGE_NAME_TIKTOK) {
                                                openTikTokWithLink(sound ?: "null", context)
                                                onComplete()
                                            }
                                        }
                                    }
                                } else {
                                    ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_DOWLOAD_VIDEO,TokenManager.getToken(context), context)
                                }
                                onComplete()
                            }
                        }
                    }
                } else {
                    Log.e("DataMap", "Data is null or not a Map")
                }
                onComplete()
            }
            else -> {
                Log.e("handleAction", "Invalid case option: $type")
                onComplete()
            }
        }
    }
    private fun checkAndCompareUsername(onComplete: () -> Unit) {
        val text = AccessibilityUtils.getTextByResourceId(CONFIG.BUTTON_LIST_CHANNELS)
        val normalizedText = CONFIG.normalizeText(text ?: "")
        val normalizedUserName = CONFIG.normalizeText(name)

        if (normalizedText == normalizedUserName) {
            delayHome()
            handleAction(type, context, id, onComplete)
        } else {
            LoginChannel.switchAccount(context, accessibilityService, userName) {
                delayHome()
                handleAction(type, context, id, onComplete)
            }
        }
    }
    private fun processHomeClick() {
        AccessibilityUtils.checkElementWithRetries(CONFIG.BUTTON_HOME, 5, 2000) { found ->
            if (found) {
                AccessibilityUtils.clickByViewId(CONFIG.BUTTON_HOME) // Click home
            } else {
                ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_DOWLOAD_VIDEO,TokenManager.getToken(context), context)
            }
        }
    }

    private fun delayHome() {
        Handler(Looper.getMainLooper()).postDelayed({
            processHomeClick()
            Handler(Looper.getMainLooper()).postDelayed({
                // Tiếp tục công việc tại đây (nếu cần)
            }, 10000) // Chờ thêm 10 giây
        }, 5000) // Chờ 5 giây
    }


}
