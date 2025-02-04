package com.ditech.cloudphone.Accessibility

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import com.ditech.cloudphone.AppInfo.AppInfoManager
import com.ditech.cloudphone.Network.ApiClient
import com.ditech.cloudphone.Utils.CONFIG
import com.ditech.cloudphone.Utils.TokenManager


class AccessibilityUtils {
    companion object {

        // Lấy root node an toàn
        fun getRootNodeSafe(): AccessibilityNodeInfo? {
            val service = SaveInstance.accessibilityService
            val rootNode = service?.rootInActiveWindow
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


        fun findNodeByTextAndId(rootNode: AccessibilityNodeInfo?, text: String, resourceId: String): AccessibilityNodeInfo? {
            if (rootNode == null) return null

            // Tìm kiếm các node có resourceId khớp
            val nodesWithId = rootNode.findAccessibilityNodeInfosByViewId(resourceId)
            if (nodesWithId.isNullOrEmpty()) {
                Log.e("AccessibilityUtils", "No nodes found with resourceId: $resourceId")
                return null
            }

            // Lọc các node có text khớp
            for (node in nodesWithId) {
                val nodeText = node.text?.toString()?.trim() ?: ""
                if (nodeText.equals(text.trim(), ignoreCase = true)) {
                    Log.d("AccessibilityUtils", "Found matching node with text: $text and resourceId: $resourceId")
                    return node
                }
            }

            Log.e("AccessibilityUtils", "No node found with text: $text and resourceId: $resourceId")
            return null
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
        fun clickByText(text: String, delayMillis: Long = 0L, onComplete: (() -> Unit)? = null) {
            val rootNode = getRootNodeSafe() ?: return
            val targetNode = findNodeByText(rootNode, text)
            performClickAction(targetNode, text, delayMillis, onComplete)
        }
        // Hàm tìm phần tử bằng description
        fun findNodeByDescription(root: AccessibilityNodeInfo?, description: String): AccessibilityNodeInfo? {
            if (root == null) return null
            if (root.text == description || root.contentDescription == description) {
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
        fun findNodeByText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
            if (root == null) return null // Kiểm tra null trước
            if (root.text?.toString() == text) return root
            for (i in 0 until root.childCount) {
                val child = root.getChild(i) // Lấy node con
                if (child != null) { // Kiểm tra node con có null không
                    findNodeByText(child, text)?.let { return it }
                }
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
        fun clickByViewIdAndDescription(viewId: String, description: String, delayMillis: Long = 0L, onComplete: (() -> Unit)? = null): Boolean {
            val rootNode = getRootNodeSafe() ?: return false
            val targetNode = findNodeByResourceIdAndDescription(rootNode, viewId, description)
            return if (targetNode != null) {
                Log.d("AccessibilityUtils", "Attempting to click node with resourceId: $viewId and description: $description")
                performClickAction(targetNode, "$viewId with description $description", delayMillis, onComplete)
                true
            } else {
                Log.e("AccessibilityUtils", "Unable to find node with resourceId: $viewId and description: $description")
                false
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
            val rootNode = getRootNodeSafe()
            val targetNode = rootNode?.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()
            if (targetNode != null && targetNode.isEditable) {
                val focusResult = targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                val clickResult = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Handler(Looper.getMainLooper()).postDelayed({
                    val arguments = Bundle().apply {
                        putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToPaste)
                    }
                    val setTextResult = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

                    if (setTextResult) {
                        onComplete?.invoke()
                    } else {
                        Log.e("AccessibilityUtils", "Failed to set text for element with ID: $viewId")
                        onComplete?.invoke()
                    }
                }, 500)
            } else {
                Log.e("AccessibilityUtils", "Editable node not found or node is not editable for ID: $viewId")
                onComplete?.invoke()
            }
        }
        fun clickFirstFrameLayoutInRecyclerView(context: Context,recyclerViewId: String, onComplete: (() -> Unit)? = null) {
            val rootNode = getRootNodeSafe() ?: return
            val recyclerViewNode = rootNode.findAccessibilityNodeInfosByViewId(recyclerViewId)?.firstOrNull()

            if (recyclerViewNode != null) {
                Log.d("AccessibilityUtils", "RecyclerView found with ID: $recyclerViewId")
                val firstFrameLayout = recyclerViewNode.getChild(0)
                if (firstFrameLayout != null &&
                    (firstFrameLayout.className == "android.widget.FrameLayout" ||
                            firstFrameLayout.className == "android.widget.ImageView")) {
                    val imageView = firstFrameLayout.getChild(0) // ImageView[1]
                    if (imageView != null && imageView.className == "android.widget.ImageView") {
                        imageView.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        onComplete?.invoke()
                        return
                    }
                }
                ApiClient.sendNotiToTelegram("1", TokenManager.getToken(context),context)
            } else {
                ApiClient.sendNotiToTelegram("2", TokenManager.getToken(context),context)
            }
        }
        fun findElementByClassName(node: AccessibilityNodeInfo, className: String): AccessibilityNodeInfo? {
            if (node.className == className) {
                return node // Trả về phần tử nếu className khớp
            }
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                val result = findElementByClassName(child, className)
                if (result != null) return result
            }
            return null // Không tìm thấy
        }
        fun pasteTextUsingClipboard(context: Context, text: String, onComplete: (() -> Unit)? = null) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

            // Xóa clipboard cũ
            clearClipboard(context)

            // Sao chép nội dung mới vào clipboard
            val clip = android.content.ClipData.newPlainText("label", text)
            clipboard.setPrimaryClip(clip)
            Log.d("ClipboardCheck", "Text '$text' copied to clipboard.")

            // Chờ hệ thống đồng bộ và thực hiện hành động paste
            Handler(Looper.getMainLooper()).postDelayed({
                val rootNode = getRootNodeSafe() ?: run {
                    Log.e("ClipboardCheck", "Root node is null.")
                    onComplete?.invoke()
                    return@postDelayed
                }
                val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

                if (focusedNode != null) {
                    // Focus vào node trước khi paste
                    val focusResult = focusedNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                    Log.d("ClipboardCheck", "Focus result: $focusResult")

                    // Thực hiện paste
                    val arguments = Bundle().apply {
                        putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                    }
                    val setTextResult = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                    if (setTextResult) {
                        clickSearchProduct(context)
                    } else {
                        Log.e("ClipboardCheck", "Failed to set text '$text' using ACTION_SET_TEXT.")
                    }
                    onComplete?.invoke()
                } else {
                    Log.e("ClipboardCheck", "No focused node found for pasting text.")
                    onComplete?.invoke()
                }
            }, 500) // Tăng thời gian chờ lên 500ms để đảm bảo clipboard đồng bộ
        }
        fun clickSearchProduct(context: Context) {
            val accessibilityService = SaveInstance.accessibilityService
            if (accessibilityService != null) {
                val windows = accessibilityService.windows // Lấy danh sách tất cả các windows hiện có
                for (window in windows) {
                    val rootNode = window.root // Lấy root node của từng window
                    if (rootNode != null) {
                        // Tìm node có resourceId là "com.google.android.inputmethod.latin:id/key_pos_ime_action"
                        val searchNode = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.inputmethod.latin:id/key_pos_ime_action")?.firstOrNull()
                        if (searchNode != null) {
                            // Thực hiện click
                            val clickResult = searchNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (clickResult) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    clickAddProduct(context)
                                },5000)

                            } else {
                                Log.e("ClickSearch", "Failed to click on node with resourceId 'com.google.android.inputmethod.latin:id/key_pos_ime_action'.")
                            }
                            break // Dừng sau khi tìm thấy và click
                        } else {
                            Log.d("ClickSearch", "Node with resourceId 'com.google.android.inputmethod.latin:id/key_pos_ime_action' not found.")
                        }
                    } else {
                        Log.d("ClickSearch", "Root node is null for one of the windows.")
                    }
                }
            } else {
                Log.e("AccessibilityUtils", "AccessibilityService instance is null.")
            }
        }
        fun clickDoneProduct(context: Context) {
            val accessibilityService = SaveInstance.accessibilityService
            if (accessibilityService != null) {
                val windows = accessibilityService.windows // Lấy danh sách tất cả các windows hiện có
                for (window in windows) {
                    val rootNode = window.root // Lấy root node của từng window
                    if (rootNode != null) {
                        // Tìm node có resourceId là "com.google.android.inputmethod.latin:id/key_pos_ime_action"
                        val searchNode = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.inputmethod.latin:id/key_pos_ime_action")?.firstOrNull()
                        if (searchNode != null) {
                            // Thực hiện click
                            val clickResult = searchNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (clickResult) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    clickPostVideo(context)
                                },5000)

                            } else {
                                Log.e("ClickSearch", "Failed to click on node with resourceId 'com.google.android.inputmethod.latin:id/key_pos_ime_action'.")
                            }
                            break // Dừng sau khi tìm thấy và click
                        } else {
                            Handler(Looper.getMainLooper()).postDelayed({
                                clickPostVideo(context)
                            },10000)
                        }
                    } else {
                        Log.d("ClickSearch", "Root node is null for one of the windows.")
                    }
                }
            } else {
                Log.e("AccessibilityUtils", "AccessibilityService instance is null.")
            }
        }
        fun clearClipboard(context: Context) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val emptyClip = android.content.ClipData.newPlainText("", "") // Tạo một Clipboard trống
            clipboard.setPrimaryClip(emptyClip)
            Log.d("ClipboardCheck", "Clipboard has been cleared.")
        }

        fun checkClipboardContent(context: Context) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val primaryClip = clipboard.primaryClip
            if (primaryClip != null && primaryClip.itemCount > 0) {
                val text = primaryClip.getItemAt(0).text
                Log.d("ClipboardCheck", "Current clipboard content: $text")
            } else {
                Log.d("ClipboardCheck", "Clipboard is empty.")
            }
        }
        fun clickAddProduct(context: Context)
        {
            val accessibilityService = SaveInstance.accessibilityService
            if (accessibilityService != null) {
                val windows = accessibilityService.windows // Lấy danh sách tất cả các windows hiện có
                for (window in windows) {
                    val rootNode = window.root // Lấy root node của từng window
                    if (rootNode != null) {
                        // Duyệt qua tất cả các phần tử để tìm className và text
                        traverseNode(rootNode) { node ->
                            if (node.className == "com.lynx.tasm.behavior.ui.view.UIView") {
                                // Lấy tọa độ của phần tử
                                val rect = android.graphics.Rect()
                                node.getBoundsInScreen(rect)
                                val clickX = rect.centerX().toFloat()
                                val clickY = rect.centerY().toFloat()
                                // Thực hiện click vào tọa độ
                                accessibilityService.performClickGesture(clickX, clickY) {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        clickDoneProduct(context)
                                    },5000)
                                }
                                return@traverseNode // Dừng sau khi tìm thấy và click
                            }
                        }
                    }
                }
            } else {
                Log.e("AccessibilityUtils", "AccessibilityService instance is null.")
            }
        }
        fun clickPostVideo(context: Context) {
            val accessibilityService = SaveInstance.accessibilityService
            if (accessibilityService != null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val rootNode = accessibilityService.rootInActiveWindow
                    if (rootNode != null) {
                        // Tìm node dựa trên cấu trúc XPath-lite
                        val targetNode = findNodeByXPathLite(rootNode)
                        if (targetNode != null) {
                            val rect = Rect()
                            targetNode.getBoundsInScreen(rect)
                            val clickX = rect.centerX().toFloat()
                            val clickY = rect.centerY().toFloat()

                            // Thực hiện click vào node được tìm thấy
                            accessibilityService.performClickGesture(clickX, clickY) {
                                clickPost(context)
                            }
                        } else {
                            Log.e("XPathLiteClick", "Không tìm thấy node với XPath-lite.")
                        }
                    } else {
                        Log.e("XPathLiteClick", "Root node không tồn tại.")
                    }
                }, 3000) // Delay tuỳ chỉnh nếu cần
            } else {
                Log.e("AccessibilityUtils", "AccessibilityService instance is null.")
            }
        }
        fun clickPost(context: Context) {
            // Lấy instance của AccessibilityManager
            clickByViewId(CONFIG.ELEMENT_POST_VIDEO) {
                ApiClient.deleteVideoWithApiFromStorage()
                Handler(Looper.getMainLooper()).postDelayed({
                    // Mở ứng dụng CloudPhone sau khi hoàn tất
                    AppInfoManager.openAppWithText(context, "CloudPhone", CONFIG.PACKAGE_NAME_TIKTOK) {
                        // Xóa video bằng API
                        Log.d("Status","Da dang thanh cong")
                    }
                }, 10000)
            }
        }
        fun findNodeByXPathLite(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            // Bước 1: Tìm node `com.zhiliaoapp.musically:id/qb`
            val parentNode = rootNode.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/qb")?.firstOrNull()
            if (parentNode == null) {
                Log.e("XPathLiteFind", "Không tìm thấy node `com.zhiliaoapp.musically:id/qb`.")
                return null
            }

            // Bước 2: Truy cập các FrameLayout theo thứ tự
            val frameLayout1 = parentNode.getChild(0)?.getChild(0) // Truy cập FrameLayout[1]/FrameLayout[1]
            if (frameLayout1 == null) {
                Log.e("XPathLiteFind", "Không tìm thấy FrameLayout[1]/FrameLayout[1].")
                return null
            }

            // Bước 3: Lấy phần tử FlattenUIText[11]
            var flattenUITextNode: AccessibilityNodeInfo? = null
            var count = 0
            for (i in 0 until frameLayout1.childCount) {
                val child = frameLayout1.getChild(i)
                if (child?.className == "com.lynx.tasm.behavior.ui.text.FlattenUIText") {
                    count++
                    if (count == 11) {
                        flattenUITextNode = child
                        break
                    }
                } else if (child?.className == "com.lynx.tasm.behavior.ui.view.UIView") {
                    count++
                    if (count == 1) {
                        flattenUITextNode = child
                        break
                    }
                }
            }

            if (flattenUITextNode == null) {
                Log.e("XPathLiteFind", "Không tìm thấy FlattenUIText[11].")
            }
            return flattenUITextNode
        }
        private fun traverseNode(node: AccessibilityNodeInfo, action: (AccessibilityNodeInfo) -> Unit) {
            action(node)
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                traverseNode(child, action)
            }
        }
    }


}
