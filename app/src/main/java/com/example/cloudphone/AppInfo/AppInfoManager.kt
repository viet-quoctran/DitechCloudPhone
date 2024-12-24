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
        fun openAppInfoFromRecents() {
            // Mở danh sách ứng dụng gần đây
            AccessibilityManager.getInstance()?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS)

            // Đợi màn hình Recents mở xong
            Handler(Looper.getMainLooper()).postDelayed({
                val rootNode = AccessibilityUtils.getRootNodeSafe()
                if (rootNode == null) {
                    Log.e("AppInfo", "Root node is null. Cannot search for 'Thông tin ứng dụng'.")
                    return@postDelayed
                }

                // Tìm phần tử có text là "Thông tin ứng dụng"
                val appInfoNode = AccessibilityUtils.findNodeByTextAndId(
                    rootNode,
                    "Thông tin ứng dụng", // Văn bản trên nút
                    "com.android.systemui:id/title" // ID của phần tử
                )
                if (appInfoNode != null) {
                    Log.d("AppInfo", "Found 'Thông tin ứng dụng'. Searching for clickable parent...")

                    // Tìm phần tử cha có thể click
                    var clickableParent = appInfoNode.parent
                    while (clickableParent != null && !clickableParent.isClickable) {
                        clickableParent = clickableParent.parent
                    }

                    if (clickableParent != null) {
                        Log.d("AppInfo", "Clickable parent found. Clicking...")
                        clickableParent.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                    } else {
                        Log.e("AppInfo", "No clickable parent found for 'Thông tin ứng dụng'.")
                    }
                } else {
                    Log.e("AppInfo", "'Thông tin ứng dụng' not found in Recents.")
                }
            }, 2000) // Điều chỉnh thời gian chờ tùy theo tốc độ thiết bị
        }
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
                if (AccessibilityUtils.isAccessibilityServiceReady()) {
                    AccessibilityManager.getInstance()?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
                    Log.d("OpenTiktok", "Performed Back action.")
                    onComplete()
                } else {
                    Log.e("OpenTiktok", "Accessibility Service is not ready!")
                }
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