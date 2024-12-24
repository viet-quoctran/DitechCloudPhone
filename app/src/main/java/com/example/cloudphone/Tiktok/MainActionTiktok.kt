package com.example.cloudphone.Tiktok

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.cloudphone.Accessibility.AccessibilityManager
import com.example.cloudphone.Accessibility.AccessibilityUtils
import com.example.cloudphone.AppInfo.AppInfoManager
import com.example.cloudphone.Action.Swipe
import com.example.cloudphone.Utils.CONFIG
import com.example.cloudphone.Action.LoginChannel
import com.example.cloudphone.Action.Upload
class MainActionTiktok(
    private val context: Context,
    private val accessibilityService: AccessibilityManager
) {
    private val swipe = Swipe(context, accessibilityService)
    private val userName = "xóa top top luôn"
    private val caseOption = 2
    private val tiktokLink = "https://vt.tiktok.com/ZS6LfUQwy/"
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
    private fun openTikTokWithLink(link: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(link)
                setPackage("com.zhiliaoapp.musically") // Specify TikTok package
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
    private fun launchTikTokDirect(packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Log.d("OpenTiktok", "TikTok launched successfully using package name: $packageName")
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
        val normalizedUserName = CONFIG.normalizeText(userName)

        if (normalizedText == normalizedUserName) {
            when (caseOption) {
                1 -> swipe.swipeMultipleTimes(2, 4) {
                    Log.d("checkAndCompareUsername", "Swipe completed successfully.")
                }
                2 -> {
                    Log.d("checkAndCompareUsername", "Preparing to return to home screen before opening App Info.")
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        Log.d("checkAndCompareUsername", "Opening App Info for TikTok.")
////                        AppInfoManager.openAppInfo(context, "com.zhiliaoapp.musically") {
////                            Handler(Looper.getMainLooper()).postDelayed({
////                                openTikTokWithLink(tiktokLink)
////                            }, 3000)
////                        }
//                    }, 2000)

                }
                else -> Log.e("checkAndCompareUsername", "Invalid case option: $caseOption")
            }
        } else {
            LoginChannel.switchAccount(context, accessibilityService)
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
