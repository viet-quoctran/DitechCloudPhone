package com.ditech.cloudphone.Action

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ditech.cloudphone.Accessibility.AccessibilityUtils
import com.ditech.cloudphone.Accessibility.SaveInstance
import com.ditech.cloudphone.Network.ApiClient
import com.ditech.cloudphone.Utils.CONFIG
import com.ditech.cloudphone.Utils.TokenManager
import kotlinx.coroutines.*

class Upload() {
    companion object{
        fun clickAddVideo(context: Context) {
            AccessibilityUtils.checkElementWithRetries(CONFIG.ELEMENT_ADD_VIDEO, 5, 2000) { found ->
                if (found) {
                    AccessibilityUtils.clickByViewId(CONFIG.ELEMENT_ADD_VIDEO)
                    Thread.sleep(5000)
                    clickSourceVideo(context)
                } else {
                    ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_ADD_VIDEO, TokenManager.getToken(context), context)
                }

            }
        }

        private fun clickSourceVideo(context: Context) {
            AccessibilityUtils.checkElementWithRetries(CONFIG.ELEMENT_SOURCE_VIDEO, 5, 2000) { found ->
                if (found) {
                    AccessibilityUtils.clickByViewId(CONFIG.ELEMENT_SOURCE_VIDEO)
                    Thread.sleep(5000)
                    clickVideo()
                } else {
                    ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_SOURCE_VIDEO, TokenManager.getToken(context), context)
                }
            }
        }
        private fun clickVideo() {
            AccessibilityUtils.clickFirstVideoInRecyclerView(CONFIG.ELEMENT_VIDEO_IN_SOURCE) {
                Log.d("OpenTiktok", "Click action completed for the first video.")
            }
        }
        fun clickVideoSound(context: Context) {
            val viewId = "com.zhiliaoapp.musically:id/lgz"

            // Kiểm tra phần tử với ID và thực hiện hành động nếu tìm thấy
            AccessibilityUtils.checkElementWithRetries(viewId, 5, 2000) { found ->
                if (found) {
                    Log.d("ClickVideoSound", "Element with ID '$viewId' found. Proceeding to click by coordinates.")

                    val rootNode = AccessibilityUtils.getRootNodeSafe() ?: run {
                        Log.e("ClickVideoSound", "Root node is null.")
                        return@checkElementWithRetries
                    }

                    val targetNode = rootNode.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()
                    if (targetNode != null) {
                        val rect = Rect()
                        targetNode.getBoundsInScreen(rect) // Lấy tọa độ của phần tử
                        val clickX = rect.centerX().toFloat()
                        val clickY = rect.centerY().toFloat()

                        Log.d("ClickVideoSound", "Clicking on element with coordinates: X=$clickX, Y=$clickY")

                        // Thực hiện click bằng tọa độ
                        SaveInstance.accessibilityService?.performClickGesture(clickX, clickY) {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(5000) // Delay 5 giây
                                clickSourceVideo(context)
                            }
                        }
                    } else {
                        Log.e("ClickVideoSound", "Unable to find element with ID '$viewId' after root node traversal.")
                    }
                } else {
                    Log.e("ClickVideoSound", "Element with ID '$viewId' not found.")
                    ApiClient.sendNotiToTelegram("Element with ID '$viewId' not found", TokenManager.getToken(context), context)
                }
            }
        }
        private fun clickVideoHaveSound(context: Context) {
            AccessibilityUtils.checkElementWithRetries(CONFIG.ELEMENT_USE_THIS_SOUND, 5, 2000) { found ->
                if (found) {
                    AccessibilityUtils.clickByViewId(CONFIG.ELEMENT_USE_THIS_SOUND)
                    Thread.sleep(5000)
                    clickSourceVideo(context)
                } else {
                    ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_CHOOSE_VIDEO_SOUND, TokenManager.getToken(context),context)
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