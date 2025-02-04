package com.ditech.cloudphone.AppInfo

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.ditech.cloudphone.Accessibility.AccessibilityManager
import com.ditech.cloudphone.Accessibility.AccessibilityUtils
import com.ditech.cloudphone.Accessibility.SaveInstance
import com.ditech.cloudphone.Network.ApiClient
import com.ditech.cloudphone.Utils.CONFIG
import com.ditech.cloudphone.Utils.TokenManager

class AppInfoManager {
    companion object {
        fun openAppWithText(
            context: Context,
            appName: String,
            packageName: String? = null,
            onComplete: () -> Unit
        ) {
            val accessibilityService = SaveInstance.accessibilityService
            if (accessibilityService == null) {
                ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_ACCESSIBILITY_DISABLE,TokenManager.getToken(context),context)
                System.exit(0)
            }

            // Thực hiện hành động GLOBAL_ACTION_RECENTS để bật màn hình đa nhiệm
            val success = accessibilityService?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            if (success != null) {
                // Đợi 2 giây để màn hình đa nhiệm xuất hiện
                Handler(Looper.getMainLooper()).postDelayed({
                    // Lấy root node
                    val rootNode = AccessibilityUtils.getRootNodeSafe()
                    if (rootNode != null) {
                        // Tìm TextView với text là appName
                        val targetTextView = findTextViewByText(rootNode, appName)
                        if (targetTextView != null) {
                            // Lấy tọa độ (rect bounds) của TextView
                            val rect = Rect()
                            targetTextView.getBoundsInScreen(rect)
                            val clickX = rect.centerX().toFloat()
                            val clickY = rect.centerY().toFloat()

                            // Thực hiện thao tác click vào tọa độ đã lấy
                            accessibilityService.performClickGesture(clickX, clickY) {
                                // Nếu packageName được cung cấp, thực hiện thêm hành động clickForceStopButton
                                if (appName == "App info" && packageName != null) {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        clickForceStopButton(context, packageName, 0, 5, onComplete, skipBack = true)
                                    }, 3000)
                                } else {
                                    onComplete()
                                }
                            }
                        } else {
                            Log.e("OpenAppInfo", "TextView with text '$appName' not found.")
                        }
                    } else {
                        Log.e("OpenAppInfo", "Root node is null. Cannot search for elements.")
                    }
                }, 2000)
            } else {
                Log.e("OpenAppInfo", "Failed to open recent apps screen.")
            }
        }
        // Hàm đệ quy để tìm TextView có nội dung cụ thể
        fun findTextViewByText(node: AccessibilityNodeInfo, targetText: String): AccessibilityNodeInfo? {
            Log.d("FindTextView", "Searching for TextView with text: $targetText")
            if (node.className == "android.widget.TextView" && node.text?.toString() == targetText) {
                return node
            }
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val result = findTextViewByText(child, targetText)
                if (result != null) {
                    return result
                }
            }
            return null
        }

        // Hàm tìm FrameLayout là cha của FrameLayout chứa TextView
        fun findParentFrameLayoutOfFrameLayout(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            var parent = node.parent
            while (parent != null) {
                // Kiểm tra nếu node hiện tại là FrameLayout
                if (parent.className == "android.widget.FrameLayout") {
                    val grandParent = parent.parent // Kiểm tra cha của FrameLayout này
                    if (grandParent != null && grandParent.className == "android.widget.FrameLayout") {
                        Log.d("OpenAppInfo", "Grandparent FrameLayout found.")
                        return grandParent // Trả về FrameLayout cha của FrameLayout
                    }
                }
                parent = parent.parent
            }
            return null
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
                    clickForceStopButton(context, packageName, 0, 5, onComplete,skipBack = false)
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
            onComplete: () -> Unit,
            skipBack: Boolean
        ) {
            // Lấy instance của AccessibilityManager từ Application Class
            val accessibilityService = SaveInstance.accessibilityService

            if (accessibilityService == null) {
                Log.e("OpenTiktok", "AccessibilityManager instance is null. Cannot proceed.")
                if (retryCount < maxRetries) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        clickForceStopButton(context, packageName, retryCount + 1, maxRetries, onComplete,skipBack)
                    }, 3000)
                } else {
                    Log.e("OpenTiktok", "Max retries reached. Unable to proceed without AccessibilityManager.")
                }
                return
            }

            // Lấy root node từ AccessibilityService
            val rootNode = accessibilityService.rootInActiveWindow

            if (rootNode == null) {
                Log.e("OpenTiktok", "Root node is null. Cannot search for 'Buộc đóng' button.")
                if (retryCount < maxRetries) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        clickForceStopButton(context, packageName, retryCount + 1, maxRetries, onComplete, skipBack)
                    }, 3000)
                } else {
                    ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_BUTTON_FORCE_STOP,TokenManager.getToken(context), context)
                }
                return
            }

            // Tìm nút "Buộc đóng"
            val forceStopButton = AccessibilityUtils.findNodeByDescription(rootNode, "Force stop")
            if (forceStopButton != null) {
                if (forceStopButton.isEnabled) {
                    val rect = Rect()
                    forceStopButton.getBoundsInScreen(rect)

                    // Tính toán tọa độ trung tâm của button
                    val clickX = rect.centerX().toFloat()
                    val clickY = rect.centerY().toFloat()

                    // Sử dụng gesture để click
                    val accessibilityService = SaveInstance.accessibilityService
                    if (accessibilityService != null) {
                        accessibilityService.performClickGesture(clickX, clickY) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                clickConfirmOkButton(context, onComplete, skipBack)
                            }, 3000) // Chờ 1 giây (có thể tăng/giảm tùy vào thử nghiệm)
                        }
                    } else {
                        Log.e("OpenTiktok", "AccessibilityManager instance is null.")
                    }
                } else {
                    Log.d("OpenTiktok", "'Buộc đóng' button is disabled. Performing Back action.")
                    performBackAction {
                        onComplete()
                    }
                }
            } else {
                Log.e("OpenTiktok", "Unable to find 'Buộc đóng' button! Retry count: $retryCount")
                if (retryCount < maxRetries) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        clickForceStopButton(context, packageName, retryCount + 1, maxRetries, onComplete,skipBack)
                    }, 3000)
                } else {
                    ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_BUTTON_FORCE_STOP,TokenManager.getToken(context), context)
                }
            }
        }
        private fun performBackAction(onComplete: () -> Unit) {
            Handler(Looper.getMainLooper()).postDelayed({
                val accessibilityService = SaveInstance.accessibilityService
                if (accessibilityService != null) {
                    accessibilityService.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
                    Log.d("OpenTiktok", "Performed Back action.")
                } else {
                    Log.e("OpenTiktok", "Cannot perform Back action. AccessibilityManager instance is null.")
                }
                onComplete()
            }, 1000)
        }
        private fun clickConfirmOkButton(context: Context, onComplete: () -> Unit, skipBack: Boolean) {
            val accessibilityService = SaveInstance.accessibilityService
            val rootNode = accessibilityService?.rootInActiveWindow

            if (rootNode == null) {
                return
            }

            val okButton = AccessibilityUtils.findNodeByText(rootNode, "OK")
            if (okButton != null) {
                okButton.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                if (!skipBack) {
                    performBackAction(onComplete)
                } else {
                    openAppWithText(context,"TikTok",CONFIG.PACKAGE_NAME_TIKTOK)
                    {
                        onComplete()
                    }
                }
            } else {
                Log.e("OpenTiktok", "Unable to find 'OK' button!")
            }
        }
    }
}