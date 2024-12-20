package com.example.cloudphone.Action

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.cloudphone.Accessibility.AccessibilityManager
import com.example.cloudphone.Accessibility.AccessibilityUtils
import com.example.cloudphone.Tiktok.MainActionTiktok
import com.example.cloudphone.Action.Swipe
import com.example.cloudphone.AppInfo.AppInfoManager

class Upload() {
    companion object{
        fun clickAddVideo() {
            AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/h3e", 5, 2000) { found ->
                if (found) {
                    Log.d("OpenTiktok", "Element found! Click add video")
                    AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/h3e")
                } else {
                    Log.e("OpenTiktok", "Add video button not found after retries.")
                }

            }
        }

        private fun clickSourceVideo() {
            AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/b5g", 5, 2000) { found ->
                if (found) {
                    Log.d("OpenTiktok", "Element found! Click find video")
                    AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/b5g")
                    clickVideo()
                } else {
                    Log.e("OpenTiktok", "Source video button not found after retries.")
                }
            }
        }
        private fun clickVideo() {
            AccessibilityUtils.clickFirstVideoInRecyclerView("com.zhiliaoapp.musically:id/eh4") {
                Log.d("OpenTiktok", "Click action completed for the first video.")
                clickSound()
            }
        }
        fun clickVideoSound() {
            AccessibilityUtils.clickFirstFrameLayoutInRecyclerView("com.zhiliaoapp.musically:id/dnc") {
                Log.d("AccessibilityUtils", "Click vào video sound hoàn tất.")
                clickVideoHaveSound()
            }
        }
        private fun clickVideoHaveSound() {
            AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/atq", 5, 2000) { found ->
                if (found) {
                    Log.d("OpenTiktok", "Element found! Click add video")
                    AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/atq")
                    clickSourceVideo()
                } else {
                    Log.e("OpenTiktok", "Add video button not found after retries.")
                }

            }
        }
        private fun clickSound() {
            AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/bkb", 5, 2000) { found ->
                if (found) {
                    Log.d("OpenTiktok", "Element found! Click add video")
                    AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/bkb")
                } else {
                    Log.e("OpenTiktok", "Add video button not found after retries.")
                }

            }
        }
    }


}