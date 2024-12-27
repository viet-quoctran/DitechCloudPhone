package com.ditech.cloudphone.Action

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ditech.cloudphone.Accessibility.AccessibilityManager
import com.ditech.cloudphone.Accessibility.AccessibilityUtils
import com.ditech.cloudphone.Tiktok.MainActionTiktok

class LoginChannel {
    companion object {
        fun switchAccount(context: Context, accessibilityService: AccessibilityManager,userName: String) {
            processListChannelClick(context, accessibilityService,userName)
        }

        private fun processListChannelClick(context: Context, accessibilityService: AccessibilityManager, userName: String) {
            AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/hol", 5, 2000) { found ->
                if (found) {
                    AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/hol")
                    Handler(Looper.getMainLooper()).postDelayed({
                        AccessibilityUtils.clickByViewIdAndDescription("com.zhiliaoapp.musically:id/fil", userName)
                    }, 5000)
                } else {
                    Log.e("OpenTiktok", "Profile element not found after retries.")
                }
            }
        }
    }
}
