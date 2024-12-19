package com.example.cloudphone.Accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.cloudphone.R
import android.graphics.Path
import com.example.cloudphone.permission.showToast
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
class AccessibilityManager : AccessibilityService() {
    private var isServiceConnected = false // Cờ kiểm tra kết nối dịch vụ
    private var isWallpaperChanged = false
    private var isTextPasted = false
    companion object {
        private var instance: AccessibilityManager? = null

        fun getInstance(): AccessibilityManager? {
            return instance
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this // Gán thể hiện thực sự của dịch vụ
        isServiceConnected = true // Đánh dấu dịch vụ đã kết nối
        Log.d("AccessibilityManager", "Service Connected: $isServiceConnected")
    }

    override fun onInterrupt() {
        // Không cần xử lý
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null // Xóa thể hiện khi dịch vụ bị hủy
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Xử lý sự kiện tại đây nếu cần
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val rootNode = rootInActiveWindow ?: return
//        if (checkAndClickAboveElement(rootNode, "com.zhiliaoapp.musically:id/mgm", 50)) {
//            Log.d("PopupAutoClose", "Popup detected and clicked to close.")
//        }
        AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/huc", 0L) {
            Log.d("PopupAutoClose", "Element 'com.zhiliaoapp.musically:id/huc' clicked successfully.")
        }
        AccessibilityUtils.pasteTextIntoElement(this, "com.zhiliaoapp.musically:id/d5k", "#fyp #tiktok") {
            Log.d("AccessibilityManager", "Text pasted successfully.")
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

    fun isServiceReady(): Boolean {
        val rootNode = rootInActiveWindow
        val isReady = isServiceConnected && rootNode != null
        Log.d("AccessibilityManager", "Service connected: $isServiceConnected, Root node: ${if (rootNode != null) "Available" else "Null"}")
        return isReady
    }

    fun performSwipeGesture(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long) {
        Log.d("AccessibilityManager", "Attempting to perform swipe gesture...")
        if (!isServiceReady()) {
            Log.e("AccessibilityManager", "Service not ready for swipe gesture!")
            return
        }

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
            showToast("Hình nền đã được thay đổi và căn chỉnh!")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Lỗi khi thay đổi hình nền!")
        }
    }

    private fun scaleAndCropBitmap(bitmap: Bitmap, screenWidth: Int, screenHeight: Int): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        // Tính tỷ lệ scale để hình ảnh vừa hoặc lớn hơn kích thước màn hình
        val widthScale = screenWidth.toFloat() / bitmapWidth
        val heightScale = screenHeight.toFloat() / bitmapHeight
        val scale = maxOf(widthScale, heightScale) // Scale theo chiều lớn hơn để tránh bị méo

        // Scale hình ảnh
        val scaledWidth = (bitmapWidth * scale).toInt()
        val scaledHeight = (bitmapHeight * scale).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

        // Crop hình ảnh để căn giữa
        val xOffset = (scaledWidth - screenWidth) / 2
        val yOffset = (scaledHeight - screenHeight) / 2
        return Bitmap.createBitmap(scaledBitmap, xOffset, yOffset, screenWidth, screenHeight)
    }
}
