package com.example.cloudphone.AppInfo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.example.cloudphone.Accessibility.AccessibilityManager
import com.example.cloudphone.Accessibility.AccessibilityUtils

class AppInfoManager {
    companion object {
        fun openAppInfo(context: Context, packageName: String, onComplete: () -> Unit) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                // Đợi một thời gian rồi thực hiện các hành động tiếp theo
                Handler(Looper.getMainLooper()).postDelayed({
                    clickForceStopButton(context, packageName, 0, 5, onComplete)
                }, 3000)
            } catch (e: Exception) {
                Log.e("OpenAppInfo", "Error opening App Info: ${e.message}")
            }
        }
        private fun clickForceStopButton(
            context: Context,
            packageName: String,
            retryCount: Int = 0,
            maxRetries: Int = 5,
            onComplete: () -> Unit
        ) {
            val rootNode = AccessibilityUtils.getRootNodeSafe()

            if (rootNode == null) {
                Log.e("OpenTiktok", "Root node is null. Cannot search for 'Buộc đóng' button.")
                openAppInfo(context, packageName){
                    onComplete()
                }
                if (retryCount < maxRetries) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        clickForceStopButton(context, packageName, retryCount + 1, maxRetries, onComplete)
                    }, 3000)
                } else {
                    Log.e("OpenTiktok", "Max retries reached. Unable to click 'Buộc đóng' button.")
                }
                return
            }

            val forceStopButton = AccessibilityUtils.findNodeByDescription(rootNode, "Buộc đóng")
            if (forceStopButton != null) {
                if (forceStopButton.isEnabled) {
                    // Nếu nút "Buộc đóng" có thể click
                    forceStopButton.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("OpenTiktok", "Clicked 'Buộc đóng' button.")
                    clickConfirmOkButton {
                        onComplete()
                    }
                } else {
                    // Nếu nút "Buộc đóng" không thể click
                    Log.d("OpenTiktok", "'Buộc đóng' button is disabled. Performing Back action.")
                    performBackAction {
                        onComplete()
                    }
                }
            } else {
                // Nếu không tìm thấy nút "Buộc đóng"
                Log.e("OpenTiktok", "Unable to find 'Buộc đóng' button! Retry count: $retryCount")
                if (retryCount < maxRetries) {
                    Log.d("OpenTiktok", "Retrying... Opening App Info for $packageName")
                    openAppInfo(context, packageName){
                        onComplete()
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        clickForceStopButton(context, packageName, retryCount + 1, maxRetries, onComplete)
                    }, 3000)
                } else {
                    Log.e("OpenTiktok", "Max retries reached. Unable to click 'Buộc đóng' button.")
                }
            }
        }
        private fun performBackAction(onComplete: () -> Unit) {
            Handler(Looper.getMainLooper()).postDelayed({
                AccessibilityManager.getInstance()?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
                Log.d("OpenTiktok", "Performed Back action.")
                onComplete()
            }, 1000)
        }
        private fun clickConfirmOkButton(onComplete: () -> Unit) {
            val rootNode = AccessibilityUtils.getRootNodeSafe() ?: return

            val okButton = AccessibilityUtils.findNodeByText(rootNode, "OK")
            if (okButton != null) {
                okButton.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("OpenTiktok", "Clicked 'OK' button.")
                performBackAction {
                    onComplete()
                }
            } else {
                Log.e("OpenTiktok", "Unable to find 'OK' button!")
            }
        }
    }
}