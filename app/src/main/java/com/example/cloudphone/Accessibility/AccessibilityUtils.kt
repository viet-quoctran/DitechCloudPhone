package com.example.cloudphone.Accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import kotlin.system.exitProcess
import android.accessibilityservice.GestureDescription
import android.graphics.Path


class AccessibilityUtils {
    companion object {
        // Kiểm tra xem Accessibility Service đã sẵn sàng chưa
        fun isAccessibilityServiceReady(): Boolean {
            val service = AccessibilityManager.getInstance()
            return if (service == null) {
                Log.e("AccessibilityUtils", "AccessibilityManager instance is null!")
                false
            } else if (!service.isServiceReady()) {
                Log.e("AccessibilityUtils", "Accessibility Service is not ready!")
                false
            } else {
                Log.d("AccessibilityUtils", "Accessibility Service is ready.")
                true
            }
        }

        // Lấy root node an toàn
        fun getRootNodeSafe(): AccessibilityNodeInfo? {
            val rootNode = AccessibilityManager.getInstance()?.rootInActiveWindow
            if (rootNode == null) {
                Log.e("AccessibilityUtils", "Failed to retrieve root node!")
            } else {
                Log.d("AccessibilityUtils", "Root node retrieved successfully.")
            }
            return rootNode
        }

        // Hàm helper chung để thực hiện click và xử lý callback
        private fun performClickAction(
            targetNode: AccessibilityNodeInfo?,
            identifier: String,
            delayMillis: Long,
            onComplete: (() -> Unit)?
        ) {
            if (targetNode != null) {
                targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("AccessibilityUtils", "Clicked element with identifier: $identifier")

                if (delayMillis > 0) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        onComplete?.invoke()
                    }, delayMillis)
                } else {
                    onComplete?.invoke()
                }
            } else {
                Log.e("AccessibilityUtils", "Unable to find element with identifier: $identifier")
            }
        }

        // Click vào phần tử bằng ViewID
        fun clickByViewId(viewId: String, delayMillis: Long = 0L, onComplete: (() -> Unit)? = null) {
            val rootNode = getRootNodeSafe() ?: return
            val targetNode = rootNode.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()
            performClickAction(targetNode, viewId, delayMillis, onComplete)
        }

        // Click vào phần tử bằng contentDescription
        fun clickByDescription(description: String, delayMillis: Long = 0L, onComplete: (() -> Unit)? = null) {
            val targetNode = findNodeInAllWindowsByDescription(description)
            if (targetNode != null) {
                Log.d("AccessibilityUtils", "Clicking node with description: $description")
                performClickAction(targetNode, description, delayMillis, onComplete)
            } else {
                Log.e("AccessibilityUtils", "Unable to find element with description: $description in all windows")
            }
        }
        fun findNodeInAllWindowsByDescription(description: String): AccessibilityNodeInfo? {
            val service = AccessibilityManager.getInstance() ?: return null
            val windows = service.windows // Lấy danh sách windows từ service

            if (windows == null) {
                Log.e("AccessibilityUtils", "Windows list is null!")
                return null
            }

            // Duyệt qua tất cả windows và tìm phần tử theo contentDescription
            for (window in windows) {
                val root = window.root ?: continue // Bỏ qua các window không có root
                val result = findNodeByDescription(root, description)
                if (result != null) return result
            }
            return null
        }
        // Click vào phần tử bằng text
        fun clickByText(text: String, delayMillis: Long = 0L, onComplete: (() -> Unit)? = null) {
            val rootNode = getRootNodeSafe() ?: return
            val targetNode = findNodeByText(rootNode, text)
            performClickAction(targetNode, text, delayMillis, onComplete)
        }

        // Hàm tìm phần tử bằng description
        fun findNodeByDescription(root: AccessibilityNodeInfo?, description: String): AccessibilityNodeInfo? {
            if (root == null) return null
            if (root.contentDescription == description) {
                Log.d("AccessibilityUtils", "Node found: $description")
                return root
            }
            for (i in 0 until root.childCount) {
                val child = root.getChild(i)
                if (child != null) {
                    findNodeByDescription(child, description)?.let {
                        Log.d("AccessibilityUtils", "Child node found: $description")
                        return it
                    }
                }
            }
            return null
        }

        // Hàm tìm phần tử bằng text
        fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
            if (root.text?.toString() == text) return root
            for (i in 0 until root.childCount) {
                findNodeByText(root.getChild(i), text)?.let { return it }
            }
            return null
        }

        // Lấy text từ phần tử bằng resource-id
        fun getTextByResourceId(targetResourceId: String): String? {
            val rootNode = getRootNodeSafe() ?: return null

            fun traverse(node: AccessibilityNodeInfo?): String? {
                if (node == null) return null
                if (node.viewIdResourceName == targetResourceId) {
                    return node.text?.toString()
                }
                for (i in 0 until node.childCount) {
                    val result = traverse(node.getChild(i))
                    if (result != null) return result
                }
                return null
            }

            return traverse(rootNode)
        }

        // Kiểm tra phần tử với số lần thử lại
        fun checkElementWithRetries(
            viewId: String,
            retries: Int,
            delayMillis: Long,
            onComplete: (Boolean) -> Unit
        ) {
            var attempt = 0

            fun check() {
                val rootNode = getRootNodeSafe()
                val element = rootNode?.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()

                if (element != null) {
                    Log.d("AccessibilityUtils", "Element '$viewId' found on attempt ${attempt + 1}.")
                    onComplete(true)
                } else if (attempt < retries - 1) {
                    attempt++
                    Log.d("AccessibilityUtils", "Element '$viewId' not found. Retrying... (${attempt + 1}/$retries)")
                    Handler(Looper.getMainLooper()).postDelayed({ check() }, delayMillis)
                } else {
                    Log.e("AccessibilityUtils", "Element '$viewId' not found after $retries attempts.")
                    onComplete(false)
                }
            }

            check()
        }

        fun findNodeByResourceIdAndDescription(root: AccessibilityNodeInfo?, resourceId: String, description: String): AccessibilityNodeInfo? {
            if (root == null) return null

            val nodes = root.findAccessibilityNodeInfosByViewId(resourceId)
            if (nodes.isNullOrEmpty()) {
                Log.e("AccessibilityUtils", "No nodes found with resourceId: $resourceId")
                return null
            }

            // Lọc phần tử có contentDescription khớp
            for (node in nodes) {
                val nodeDescription = node.contentDescription?.toString()?.trim() ?: ""
                if (nodeDescription.equals(description.trim(), ignoreCase = true)) {
                    Log.d("AccessibilityUtils", "Found matching node with resourceId: $resourceId and description: $description")
                    return node
                }
            }

            Log.e("AccessibilityUtils", "No node found with resourceId: $resourceId and description: $description")
            return null
        }
        fun clickByViewIdAndDescription(viewId: String, description: String, delayMillis: Long = 0L, onComplete: (() -> Unit)? = null) {
            val rootNode = getRootNodeSafe() ?: return
            val targetNode = findNodeByResourceIdAndDescription(rootNode, viewId, description)
            if (targetNode != null) {
                Log.d("AccessibilityUtils", "Attempting to click node with resourceId: $viewId and description: $description")
                performClickAction(targetNode, "$viewId with description $description", delayMillis, onComplete)
            } else {
                Log.e("AccessibilityUtils", "Unable to find node with resourceId: $viewId and description: $description")
            }
        }
        fun clickFirstVideoInRecyclerView(recyclerViewId: String, onComplete: () -> Unit) {
            val rootNode = getRootNodeSafe()
            val recyclerView = rootNode?.findAccessibilityNodeInfosByViewId(recyclerViewId)?.firstOrNull()

            if (recyclerView != null) {
                Log.d("AccessibilityUtils", "RecyclerView found with ID: $recyclerViewId")

                for (i in 0 until recyclerView.childCount) {
                    val childNode = recyclerView.getChild(i)
                    if (childNode != null && childNode.isClickable) {
                        Log.d("AccessibilityUtils", "First clickable video item found at position $i")
                        performClickAction(childNode, "Video item at position $i", 0L, onComplete)
                        return
                    }
                }

                Log.e("AccessibilityUtils", "No clickable video item found in RecyclerView.")
                onComplete() // Call onComplete even if no video was found
            } else {
                Log.e("AccessibilityUtils", "RecyclerView with ID $recyclerViewId not found.")
                onComplete() // Call onComplete if RecyclerView is not found
            }
        }
        fun pasteTextIntoElement(
            context: Context,
            viewId: String,
            textToPaste: String,
            onComplete: (() -> Unit)? = null
        ) {
            val rootNode = AccessibilityManager.getInstance()?.rootInActiveWindow ?: return

            val targetNode = rootNode.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()

            if (targetNode != null && targetNode.isEditable) {
                Log.d("AccessibilityUtils", "Editable node found with ID: $viewId")

                // Đảm bảo focus vào trường nhập liệu và click vào nó
                val focusResult = targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                val clickResult = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)

                Log.d(
                    "AccessibilityUtils",
                    "Focus result: $focusResult, Click result: $clickResult for element with ID: $viewId"
                )

                // Thêm delay để đảm bảo thao tác focus và click được hoàn tất trước khi set text
                Handler(Looper.getMainLooper()).postDelayed({
                    // Sử dụng ACTION_SET_TEXT để đặt văn bản trực tiếp
                    val arguments = Bundle().apply {
                        putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToPaste)
                    }
                    val setTextResult = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

                    if (setTextResult) {
                        Log.d("AccessibilityUtils", "Text set directly into element with ID: $viewId")

                        // Gọi clickByViewId sau khi set text thành công
                        val clickAfterPasteResult = clickByViewId("com.zhiliaoapp.musically:id/jfv")
                        Log.d(
                            "AccessibilityUtils",
                            "Click after paste result: $clickAfterPasteResult for element with ID: com.zhiliaoapp.musically:id/jfv"
                        )

                        onComplete?.invoke() // Gọi callback nếu có
                    } else {
                        Log.e("AccessibilityUtils", "Failed to set text for element with ID: $viewId")
                        onComplete?.invoke()
                    }
                }, 500) // Thêm 500ms delay để đảm bảo hành động trước đó hoàn thành
            } else {
                Log.e("AccessibilityUtils", "Editable node not found or node is not editable for ID: $viewId")
                onComplete?.invoke()
            }
        }
        fun clickFirstFrameLayoutInRecyclerView(recyclerViewId: String, onComplete: (() -> Unit)? = null) {
            val rootNode = getRootNodeSafe() ?: return
            val recyclerViewNode = rootNode.findAccessibilityNodeInfosByViewId(recyclerViewId)?.firstOrNull()

            if (recyclerViewNode != null) {
                Log.d("AccessibilityUtils", "RecyclerView found with ID: $recyclerViewId")

                // Tìm FrameLayout[1]/ImageView[1]
                val firstFrameLayout = recyclerViewNode.getChild(0) // FrameLayout[1]
                if (firstFrameLayout != null && firstFrameLayout.className == "android.widget.FrameLayout") {
                    val imageView = firstFrameLayout.getChild(0) // ImageView[1]
                    if (imageView != null && imageView.className == "android.widget.ImageView") {
                        Log.d("AccessibilityUtils", "ImageView found inside FrameLayout. Attempting to click.")
                        imageView.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        onComplete?.invoke()
                        return
                    }
                }
                Log.e("AccessibilityUtils", "FrameLayout[1]/ImageView[1] not found.")
            } else {
                Log.e("AccessibilityUtils", "RecyclerView with ID $recyclerViewId not found.")
            }
        }



    }
}
