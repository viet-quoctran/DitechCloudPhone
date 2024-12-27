package com.ditech.cloudphone.AppInfo

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.ditech.cloudphone.Accessibility.AccessibilityUtils
import com.ditech.cloudphone.Accessibility.SaveInstance

class AppInfoManager {
    companion object {
        private val tiktokLink = "https://vt.tiktok.com/ZS6LfUQwy/"
        fun openAppInfoWithRecent(context: Context, packageName: String,onComplete: () -> Unit) {
            val accessibilityService = SaveInstance.accessibilityService
            if (accessibilityService == null) {
                Log.e("OpenAppInfo", "AccessibilityManager instance is null. Cannot perform action.")
                return
            }

            // Thực hiện hành động GLOBAL_ACTION_RECENTS để bật màn hình đa nhiệm
            val success = accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            if (success) {
                Log.d("OpenAppInfo", "Recent apps screen opened successfully.")
                // Đợi 2 giây để màn hình đa nhiệm xuất hiện
                Handler(Looper.getMainLooper()).postDelayed({
                    // Lấy root node
                    val rootNode = AccessibilityUtils.getRootNodeSafe()
                    if (rootNode != null) {
                        // Tìm element với ID "com.android.systemui:id/recents_view"
                        val recentsView = rootNode.findAccessibilityNodeInfosByViewId("com.android.systemui:id/recents_view")?.firstOrNull()
                        if (recentsView != null) {
                            // Tìm TextView có text là "Thông tin ứng dụng"
                            val targetTextView = findTextViewByText(recentsView, "Thông tin ứng dụng")
                            if (targetTextView != null) {
                                Log.d("OpenAppInfo", "Found TextView with text 'Thông tin ứng dụng'. Searching for parent...")

                                // Tìm FrameLayout là cha của FrameLayout chứa TextView
                                val parentFrameLayout = findParentFrameLayoutOfFrameLayout(targetTextView)
                                if (parentFrameLayout != null) {
                                    Log.d("OpenAppInfo", "Found parent FrameLayout. Performing action...")
                                    parentFrameLayout.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        clickForceStopButton(context, packageName, 0, 5, onComplete,skipBack = true)
                                    }, 3000)
                                } else {
                                    Log.e("OpenAppInfo", "Parent FrameLayout not found for TextView.")
                                }
                            } else {
                                Log.e("OpenAppInfo", "TextView with text 'Thông tin ứng dụng' not found.")
                            }
                        } else {
                            Log.e("OpenAppInfo", "Element with ID 'com.android.systemui:id/recents_view' not found.")
                        }
                    } else {
                        Log.e("OpenAppInfo", "Root node is null. Cannot search for elements.")
                    }
                }, 2000)
            } else {
                Log.e("OpenAppInfo", "Failed to open recent apps screen.")
            }
        }
        fun openTiktokWithRecent(context: Context, packageName: String,onComplete: () -> Unit) {
            val accessibilityService = SaveInstance.accessibilityService
            if (accessibilityService == null) {
                Log.e("OpenAppInfo", "AccessibilityManager instance is null. Cannot perform action.")
                return
            }

            // Thực hiện hành động GLOBAL_ACTION_RECENTS để bật màn hình đa nhiệm
            val success = accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            if (success) {
                Log.d("OpenAppInfo", "Recent apps screen opened successfully.")
                // Đợi 2 giây để màn hình đa nhiệm xuất hiện
                Handler(Looper.getMainLooper()).postDelayed({
                    // Lấy root node
                    val rootNode = AccessibilityUtils.getRootNodeSafe()
                    if (rootNode != null) {
                        // Tìm element với ID "com.android.systemui:id/recents_view"
                        val recentsView = rootNode.findAccessibilityNodeInfosByViewId("com.android.systemui:id/recents_view")?.firstOrNull()
                        if (recentsView != null) {
                            // Tìm TextView có text là "Thông tin ứng dụng"
                            val targetTextView = findTextViewByText(recentsView, "TikTok")
                            if (targetTextView != null) {
                                Log.d("OpenAppInfo", "Found TextView with text 'Thông tin ứng dụng'. Searching for parent...")

                                // Tìm FrameLayout là cha của FrameLayout chứa TextView
                                val parentFrameLayout = findParentFrameLayoutOfFrameLayout(targetTextView)
                                if (parentFrameLayout != null) {
                                    Log.d("OpenAppInfo", "Found parent FrameLayout. Performing action...")
                                    parentFrameLayout.performAction(AccessibilityNodeInfo.ACTION_CLICK)


                                } else {
                                    Log.e("OpenAppInfo", "Parent FrameLayout not found for TextView.")
                                }
                            } else {
                                Log.e("OpenAppInfo", "TextView with text 'Thông tin ứng dụng' not found.")
                            }
                        } else {
                            Log.e("OpenAppInfo", "Element with ID 'com.android.systemui:id/recents_view' not found.")
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
                    Log.e("OpenTiktok", "Max retries reached. Unable to click 'Buộc đóng' button.")
                }
                return
            }

            // Tìm nút "Buộc đóng"
            val forceStopButton = AccessibilityUtils.findNodeByDescription(rootNode, "Buộc đóng")
            if (forceStopButton != null) {
                if (forceStopButton.isEnabled) {
                    forceStopButton.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("OpenTiktok", "Clicked 'Buộc đóng' button.")
                    clickConfirmOkButton(context,onComplete, skipBack)
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
                    Log.e("OpenTiktok", "Max retries reached. Unable to click 'Buộc đóng' button.")
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
                Log.e("OpenTiktok", "Root node is null. Cannot search for 'OK' button.")
                return
            }

            val okButton = AccessibilityUtils.findNodeByText(rootNode, "OK")
            if (okButton != null) {
                okButton.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                if (!skipBack) {
                    performBackAction(onComplete)
                } else {
                    openTiktokWithRecent(context,"com.zhiliaoapp.musically")
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