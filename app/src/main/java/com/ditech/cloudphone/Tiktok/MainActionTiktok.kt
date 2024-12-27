package com.ditech.cloudphone.Tiktok

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ditech.cloudphone.Accessibility.AccessibilityManager
import com.ditech.cloudphone.Accessibility.AccessibilityUtils
import com.ditech.cloudphone.AppInfo.AppInfoManager
import com.ditech.cloudphone.Action.Swipe
import com.ditech.cloudphone.Utils.CONFIG
import com.ditech.cloudphone.Action.LoginChannel
import com.ditech.cloudphone.Action.Upload
class MainActionTiktok(
    private val context: Context,
    private val accessibilityService: AccessibilityManager,
    private val name: String,
    private val userName: String,
    private val action: Int,
    private val watchTimeFrom: Int,
    private val watchTimeTo: Int,
    private val videoLimitFrom: Int,
    private val videoLimitTo: Int,
) {
    private val swipe = Swipe(context, accessibilityService)
    private val tiktokLinkSound = "https://vt.tiktok.com/ZS6LfUQwy/"
    fun launchTikTok(packageName: String = "com.zhiliaoapp.musically") {
        try {
            AppInfoManager.openAppInfo(context, packageName) {
                // Sau khi "Buộc đóng" hoàn tất, thực hiện mở TikTok
                Handler(Looper.getMainLooper()).postDelayed({
                    launchTikTokDirect(packageName)
                }, 3000) // Đợi thêm 3 giây để đảm bảo giao diện đã đóng hoàn toàn
            }
        } catch (e: Exception) {
            Log.e("OpenTiktok", "Error in launchTikTok: ${e.message}")
        }
    }

    private fun launchTikTokDirect(packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            } else {
                Log.e("OpenTiktok", "Unable to find launch intent for package: $packageName")
            }
            // Thêm logic sau khi mở TikTok
            Handler(Looper.getMainLooper()).postDelayed({
                processProfileClick()
            }, 5000)
        } catch (e: Exception) {
            Log.e("OpenTiktok", "Error launching TikTok: ${e.message}")
        }
    }
    private fun openTikTokWithLink(link: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(link)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                setPackage("com.zhiliaoapp.musically") // Specify TikTok package
            }
            context.startActivity(intent)
            Log.d("OpenTikTok", "TikTok opened with link: $link")

            // Đợi một khoảng thời gian để TikTok tải giao diện xong
            Handler(Looper.getMainLooper()).postDelayed({
                Upload.clickVideoSound() // TikTok đã sẵn sàng, tiếp tục
            }, 5000) // Thời gian chờ (điều chỉnh phù hợp, ví dụ: 5 giây)
        } catch (e: Exception) {
            Log.e("OpenTikTok", "Error opening TikTok with link: ${e.message}")
        }
    }
    fun processProfileClick() {
        AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/h3j", 5, 2000) { found ->
            if (found) {
                Log.d("OpenTiktok", "Element found! Click profile")
                AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/h3j") // Click profile
                getUsername()
            } else {
                Log.e("OpenTiktok", "Profile element not found after retries.")
            }
        }
    }
    private fun getUsername() {
        Handler(Looper.getMainLooper()).postDelayed({
            AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/hol", 5, 2000) { found ->
                if (found) {
                    checkAndCompareUsername()
                } else {
                    Log.e("OpenTiktok", "Username element not found after retries.")
                    processProfileClick()
                }
            }
        }, 5000)
    }
    private fun checkAndCompareUsername() {
        val text = AccessibilityUtils.getTextByResourceId("com.zhiliaoapp.musically:id/hol")
        val normalizedText = CONFIG.normalizeText(text ?: "")
        val normalizedUserName = CONFIG.normalizeText(name)
        if (normalizedText == normalizedUserName) {
            processHomeClick()
            when (action) {
                1 -> swipe.swipeMultipleTimes(videoLimitFrom, videoLimitTo,watchTimeTo,watchTimeFrom) {

                }
                2 -> {
                    Log.d("checkAndCompareUsername", "Preparing to return to home screen before opening App Info.")
                    processHomeClick()

                }
                else -> Log.e("checkAndCompareUsername", "Invalid case option: $action")
            }
        } else {
            LoginChannel.switchAccount(context, accessibilityService,userName)
        }
    }
    private fun processHomeClick() {
        AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/h3h", 5, 2000) { found ->
            if (found) {
                Log.d("OpenTiktok", "Element found! Click Home")
                AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/h3h") // Click home
            } else {
                Log.e("OpenTiktok", "Profile element not found after retries.")
            }
        }
    }
}
