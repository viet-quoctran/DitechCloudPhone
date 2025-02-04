package com.ditech.cloudphone.Accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ditech.cloudphone.R
import android.graphics.Path
import com.ditech.cloudphone.permission.showToast
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.ditech.cloudphone.Action.Swipe
import com.ditech.cloudphone.AppInfo.AppInfoManager
import com.ditech.cloudphone.Network.ApiClient
import com.ditech.cloudphone.Tiktok.MainActionTiktok
import com.ditech.cloudphone.Utils.CONFIG
import com.ditech.cloudphone.Utils.TokenManager
import kotlin.concurrent.thread

class AccessibilityManager : AccessibilityService() {
    private var isServiceConnected = false // Cờ kiểm tra kết nối dịch vụ
    private var isWallpaperChanged = false
    private var isTextPasted = false
    val clickedElements = mutableSetOf<String>() // Bộ nhớ tạm lưu các element đã xử lý
    companion object {
        private var instance: AccessibilityManager? = null

        fun getInstance(): AccessibilityManager? {
            return instance
        }
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        SaveInstance.accessibilityService = this // Lưu instance vào Application Class
        ApiClient.sendNotiSuccessToTelegram(CONFIG.MESSAGE_ACCESSIBILITY_ENABLE, TokenManager.getToken(this))
        changeWallpaper()
    }

    override fun onInterrupt() {
        // Không cần xử lý
    }

    override fun onDestroy() {
        super.onDestroy()
        SaveInstance.accessibilityService = null // Xóa instance khi dịch vụ bị hủy
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Xử lý sự kiện tại đây nếu cần

        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val rootNode = rootInActiveWindow ?: return
//        if (checkAndClickAboveElement(rootNode, "com.zhiliaoapp.musically:id/mgm", 50)) {
//            Log.d("PopupAutoClose", "Popup detected and clicked to close.")
//        }
        val denyElement = AccessibilityUtils.findNodeByText(rootNode, "Don't allow")
        if (denyElement != null) {
            val rect = Rect()
            denyElement.getBoundsInScreen(rect)
            val clickX = rect.centerX().toFloat()
            val clickY = rect.centerY().toFloat()
            performClickGesture(clickX, clickY) {
                Log.d("AccessibilityEvent", "Clicked on 'Don't allow'")
            }
        }
        val elementMHP = rootNode.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/mhp")?.firstOrNull()
        if (elementMHP != null) {
            // Lấy chiều cao màn hình
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val screenWidth = displayMetrics.widthPixels

            // Tính tọa độ 1/3 màn hình
            val clickX = screenWidth / 2f
            val clickY = screenHeight * 0.3f

            // Thực hiện click vào tọa độ
            performClickGesture(clickX, clickY) {
                Log.d("AccessibilityEvent", "Clicked at 1/3 of the screen for element 'com.zhiliaoapp.musically:id/mhp'.")
            }
        }
        val elementHU8 = rootNode.findAccessibilityNodeInfosByViewId(CONFIG.ELEMENT_HU8)?.firstOrNull()
        if(elementHU8 != null)
        {
            elementHU8.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        val elementHUC = rootNode.findAccessibilityNodeInfosByViewId(CONFIG.ELEMENT_HUC)?.firstOrNull()
        if (elementHUC != null) {
            val sound = CONFIG.getSound()
            if (sound == "null") {
                // Nếu không có sound, click thẳng vào element HUC
                elementHUC.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }else{
                val elementBKB = rootNode.findAccessibilityNodeInfosByViewId(CONFIG.ELEMENT_BKB)?.firstOrNull()
                if (elementBKB != null && !clickedElements.contains("bkb")) {
                    elementBKB.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/bkb' clicked successfully.")
                } else if (clickedElements.contains("bkb")) {
                    elementHUC.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    clickedElements.remove("klb")
                    clickedElements.remove("klc")
                    clickedElements.remove("fn0")
                }

            }

        }
        AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/oqv", 0L) {
            Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/oqv' clicked successfully.")
        }
        val elementJYQ= rootNode.findAccessibilityNodeInfosByViewId(CONFIG.ELEMENT_JYQ)?.firstOrNull()
        if(elementJYQ != null)
        {
            elementJYQ.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/jyq' clicked successfully.")
        }
        val CHECK_CART = rootNode.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/dr5")?.firstOrNull()
        val INPUT_HASTAG = rootNode.findAccessibilityNodeInfosByViewId(CONFIG.INPUT_HASTAG)?.firstOrNull()
        if (INPUT_HASTAG != null && CHECK_CART == null) {
            val hashtag = CONFIG.getHashtag()
            val productName = CONFIG.getProductName()
            AccessibilityUtils.pasteTextIntoElement(this, CONFIG.INPUT_HASTAG, hashtag) {
                if(productName != "null"){
                    processAddLinkButton(rootNode,productName)
                }
                else
                {
                    AccessibilityUtils.clickPost(this)
                }
            }
        }

        if (CHECK_CART != null) {
            AccessibilityUtils.clickPost(this)
        }
        // Xử lý element âm thanh gốc (com.zhiliaoapp.musically:id/klb)
        val elementKLB = rootNode.findAccessibilityNodeInfosByViewId(CONFIG.ELEMENT_KLB)?.firstOrNull()
        if (elementKLB != null && !clickedElements.contains("klb")) {
            clickedElements.add("klb") // Đánh dấu đã xử lý element này
            Thread.sleep(5000)
            clickAtEndOfElement("com.zhiliaoapp.musically:id/klb", 0.95f) {
                Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/klb' clicked successfully.")
                Handler(Looper.getMainLooper()).postDelayed({
                    processKLC(rootNode) // Xử lý element âm thanh được thêm
                }, 3000)
            }
        } else if (clickedElements.contains("klb")) {
            Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/klb' already clicked. Skipping...")
        }

    }
    private fun checkAndClickAboveElement(root: AccessibilityNodeInfo, resourceId: String, offsetY: Int): Boolean {
        val nodes = root.findAccessibilityNodeInfosByViewId(resourceId)
        if (!nodes.isNullOrEmpty()) {
            val targetNode = nodes.first()
            val rect = android.graphics.Rect()
            targetNode.getBoundsInScreen(rect)

            val clickX = rect.centerX()
            val clickY = rect.top - offsetY
            if (clickY > 0) {
                performClickAtPosition(clickX, clickY)
                return true
            }
        }
        return false
    }

    private fun performClickAtPosition(x: Int, y: Int) {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        dispatchGesture(gesture, null, null)
    }
    private fun isAccessibilityEnabled(): Boolean {
        return serviceInfo != null
    }

    fun performSwipeGesture(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long) {
        val path = android.graphics.Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val strokeDescription = GestureDescription.StrokeDescription(path, 0, duration)
        val gesture = GestureDescription.Builder().addStroke(strokeDescription).build()
        val success = dispatchGesture(gesture, object : GestureResultCallback() {

            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Log.d("AccessibilityManager", "Gesture completed successfully.")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.e("AccessibilityManager", "Gesture was cancelled.")
            }
        }, null)

        if (!success) {
            Log.e("AccessibilityManager", "Failed to dispatch gesture.")
        } else {
            Log.d("AccessibilityManager", "Gesture dispatched successfully.")
        }
    }

    fun getRootNode(): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            Log.e("AccessibilityManager", "Root node is null")
        } else {
            Log.d("AccessibilityManager", "Root node retrieved successfully")
        }
        return rootNode
    }

    private fun changeWallpaper() {
        try {
            val wallpaperManager = WallpaperManager.getInstance(this)

            // Load hình nền từ resource
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.my_wallpaper)

            // Lấy kích thước màn hình thiết bị
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // Scale và crop hình ảnh để căn giữa
            val adjustedBitmap = scaleAndCropBitmap(bitmap, screenWidth, screenHeight)

            // Đặt hình nền
            wallpaperManager.setBitmap(adjustedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Lỗi khi thay đổi hình nền!")
        }
    }

    private fun scaleAndCropBitmap(bitmap: Bitmap, screenWidth: Int, screenHeight: Int): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val widthScale = screenWidth.toFloat() / bitmapWidth
        val heightScale = screenHeight.toFloat() / bitmapHeight
        val scale = maxOf(widthScale, heightScale)
        val scaledWidth = (bitmapWidth * scale).toInt()
        val scaledHeight = (bitmapHeight * scale).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        val xOffset = (scaledWidth - screenWidth) / 2
        val yOffset = (scaledHeight - screenHeight) / 2
        return Bitmap.createBitmap(scaledBitmap, xOffset, yOffset, screenWidth, screenHeight)
    }
    private fun clickAtEndOfElement(resourceId: String, endOffset: Float, onComplete: (() -> Unit)? = null) {
        val rootNode = rootInActiveWindow ?: return
        val element = rootNode.findAccessibilityNodeInfosByViewId(resourceId)?.firstOrNull()
        if (element != null) {
            val rect = Rect()
            element.getBoundsInScreen(rect)
            val clickX = rect.right - (rect.width() * (1 - endOffset))
            val clickY = rect.centerY().toFloat()

            performClickGesture(clickX, clickY) {
                onComplete?.invoke()
            }
            Log.d("AccessibilityUtils", "Clicked at the end of element '$resourceId' at ($clickX, $clickY).")
        } else {
            Log.e("AccessibilityUtils", "Element '$resourceId' not found for end click.")
        }
    }

    private fun clickAtStartOfElementWithOffset(resourceId: String, offsetPercentage: Float, onComplete: (() -> Unit)? = null) {
        val rootNode = rootInActiveWindow ?: return
        val element = rootNode.findAccessibilityNodeInfosByViewId(resourceId)?.firstOrNull()
        if (element != null) {
            val rect = Rect()
            element.getBoundsInScreen(rect)

            val clickX = rect.left + (rect.width() * offsetPercentage)
            val clickY = rect.centerY().toFloat()

            performClickGesture(clickX, clickY) {
                onComplete?.invoke()
            }
            Log.d("AccessibilityUtils", "Clicked at the start of element '$resourceId' with offset at ($clickX, $clickY).")
        } else {
            Log.e("AccessibilityUtils", "Element '$resourceId' not found for start click with offset.")
        }
    }
    private fun processKLC(rootNode: AccessibilityNodeInfo) {
        val elementKLC = rootNode.findAccessibilityNodeInfosByViewId(CONFIG.ELEMENT_KLC)?.firstOrNull()
        if (elementKLC != null && !clickedElements.contains("klc")) {
            Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/klc' (âm thanh được thêm) detected.")
            clickedElements.add("klc") // Đánh dấu đã xử lý element này
            clickAtStartOfElementWithOffset(CONFIG.ELEMENT_KLC, 0.06f) { // Thụt vào 8%
                Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/klc' clicked successfully.")
                // Chờ 3 giây trước khi xử lý element tiếp theo
                Handler(Looper.getMainLooper()).postDelayed({
                    processFN0(rootNode)
                }, 3000)
            }
        } else if (clickedElements.contains("klc")) {
            Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/klc' already clicked. Skipping...")
        } else {
            ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_ELEMENT_KLC,TokenManager.getToken(this), this)
        }
    }
    private fun processFN0(rootNode: AccessibilityNodeInfo) {
        val elementFN0 = rootNode.findAccessibilityNodeInfosByViewId(CONFIG.ELEMENT_FN0)?.firstOrNull()
        if (elementFN0 != null && !clickedElements.contains("fn0")) {
            Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/fn0' detected.")
            clickedElements.add("fn0")
            AccessibilityUtils.clickByViewId(CONFIG.ELEMENT_FN0) {
                clickedElements.add("bkb") // Thêm vào danh sách clickedElements
            }
        } else if (clickedElements.contains("fn0")) {
            Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/fn0' already clicked. Skipping...")
        } else {
            ApiClient.sendNotiToTelegram(CONFIG.MESSAGE_ELEMENT_FN0,TokenManager.getToken(this), this)
        }
    }
    fun performClickGesture(x: Float, y: Float, onComplete: (() -> Unit)? = null) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()

        val success = dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Log.d("AccessibilityUtils", "Click gesture performed successfully at ($x, $y).")
                onComplete?.invoke()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.e("AccessibilityUtils", "Failed to perform click gesture at ($x, $y).")
            }
        }, null)

        if (!success) {
            Log.e("AccessibilityUtils", "Failed to dispatch gesture.")
        }
    }


    fun processAddLinkButton(rootNode: AccessibilityNodeInfo,productName: String) {
        val nodes = rootNode.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/bfk")
        if (!nodes.isNullOrEmpty()) {
            for (node in nodes) {
                val description = node.contentDescription?.toString()
                if (description == "Add link") {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("PopupAutoClose", "Clicked on element with description 'Add link'.")
                    break
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({
                processProductElement(productName)
            }, 3000)
        } else {
            Log.e("PopupAutoClose", "No elements found with ID 'com.zhiliaoapp.musically:id/bfk'.")
        }
    }

    fun processProductElement(productName: String) {
        val rootNode = rootInActiveWindow ?: return
        val nodes = rootNode.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/n7y")
        if (!nodes.isNullOrEmpty()) {
            for (node in nodes) {
                val text = node.text?.toString()
                if (text == "Product") {
                    val rect = Rect()
                    node.getBoundsInScreen(rect)
                    val clickX = rect.centerX().toFloat()
                    val clickY = rect.centerY().toFloat()
                    performClickGesture(clickX, clickY) {
                        processLynxInputView(productName)
                    }
                    break
                }
            }
        } else {
            Log.e("AccessibilityManager", "No elements found with ID 'com.zhiliaoapp.musically:id/n7y'.")
        }
    }

    fun processLynxInputView(productName: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            val rootNode = rootInActiveWindow ?: return@postDelayed

            // Tìm phần tử `LynxInputView` theo className
            val targetElement = AccessibilityUtils.findElementByClassName(rootNode, "com.bytedance.ies.xelement.input.LynxInputView")

            if (targetElement != null) {
                val rect = Rect()
                targetElement.getBoundsInScreen(rect) // Lấy tọa độ
                val clickX = rect.centerX().toFloat()
                val clickY = rect.centerY().toFloat()

                // Click vào tọa độ của phần tử
                performClickGesture(clickX, clickY) {
                    Log.d("AccessibilityManager", "Clicked on 'LynxInputView' at ($clickX, $clickY).")
                    AccessibilityUtils.pasteTextUsingClipboard(this, productName) {
                        AccessibilityUtils.checkClipboardContent(this)
                    }
                }
            } else {
                Log.e("AccessibilityManager", "No elements found with className 'com.bytedance.ies.xelement.input.LynxInputView'.")
            }
        }, 5000) // Thời gian delay tùy chỉnh
    }

}