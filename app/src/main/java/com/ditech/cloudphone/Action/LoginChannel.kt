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
        fun switchAccount(
            context: Context,
            accessibilityService: AccessibilityManager,
            userName: String,
            onComplete: () -> Unit
        ) {
            processListChannelClick(context, accessibilityService, userName) {
                Log.d("SwitchAccount", "Finished switching account to: $userName")
                onComplete()
            }
        }

        private fun processListChannelClick(
            context: Context,
            accessibilityService: AccessibilityManager,
            userName: String,
            onComplete: () -> Unit
        ) {
            AccessibilityUtils.checkElementWithRetries(
                "com.zhiliaoapp.musically:id/hol", // ID của danh sách tài khoản
                retries = 5,
                delayMillis = 2000
            ) { found ->
                if (found) {
                    Log.d("SwitchAccount", "Found account list. Clicking to expand.")
                    AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/hol") {
                        // Sau khi click vào danh sách tài khoản, chờ 5 giây và chọn tài khoản
                        Handler(Looper.getMainLooper()).postDelayed({
                            val success = AccessibilityUtils.clickByViewIdAndDescription(
                                "com.zhiliaoapp.musically:id/fil", // ID của phần tử
                                userName
                            )
                            if (success) {
                                Log.d("SwitchAccount", "Successfully switched to account: $userName")
                                onComplete()
                            } else {
                                Log.e("SwitchAccount", "Failed to switch to account: $userName")
                                onComplete()
                            }
                        }, 5000)
                    }
                } else {
                    Log.e("SwitchAccount", "Account list element not found.")
                    onComplete() // Gọi callback ngay cả khi thất bại
                }
            }
        }
    }
}