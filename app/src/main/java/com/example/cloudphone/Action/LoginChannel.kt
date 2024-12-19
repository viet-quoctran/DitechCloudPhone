package com.example.cloudphone.Action

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.cloudphone.Accessibility.AccessibilityManager
import com.example.cloudphone.Accessibility.AccessibilityUtils
import com.example.cloudphone.Tiktok.MainActionTiktok

class LoginChannel {
    companion object {
        fun switchAccount(context: Context, accessibilityService: AccessibilityManager) {
            processListChannelClick(context, accessibilityService)
        }

        private fun processListChannelClick(context: Context, accessibilityService: AccessibilityManager) {
            AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/hol", 5, 2000) { found ->
                if (found) {
                    AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/hol")
                    Handler(Looper.getMainLooper()).postDelayed({
                        AccessibilityUtils.clickByViewIdAndDescription("com.zhiliaoapp.musically:id/fil", "xoatikroikaka")
                        val mainActionTiktok = MainActionTiktok(context, accessibilityService)
                        mainActionTiktok.processProfileClick()
                    }, 5000)
                } else {
                    Log.e("OpenTiktok", "Profile element not found after retries.")
                }
            }
        }
    }
}
