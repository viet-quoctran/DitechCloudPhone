package com.example.cloudphone.Action

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.cloudphone.Accessibility.AccessibilityManager
import com.example.cloudphone.Accessibility.AccessibilityUtils
import com.example.cloudphone.Tiktok.MainActionTiktok
import com.example.cloudphone.Action.Swipe
class Upload(
    private val context: Context,
    private val accessibilityService: AccessibilityManager
) {
    private val swipe = Swipe(context, accessibilityService)
    fun upLoad() {
        swipe.swipeMultipleTimes(3, 10) {
            Log.d("Upload", "Swipe completed. Proceeding to next step...")
            clickAddVideo {
                clickSourceVideo{
                    clickVideo{
                        Log.d("Upload", "All actions completed.")
                    }
                }
            }
        }
    }

    private fun clickAddVideo(onComplete: () -> Unit) {
        AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/h3e", 5, 2000) { found ->
            if (found) {
                Log.d("OpenTiktok", "Element found! Click add video")
                AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/h3e")
            } else {
                Log.e("OpenTiktok", "Add video button not found after retries.")
            }
            onComplete() // Gọi callback dù có tìm thấy hay không
        }
    }

    private fun clickSourceVideo(onComplete: () -> Unit) {
        AccessibilityUtils.checkElementWithRetries("com.zhiliaoapp.musically:id/b5g", 5, 2000) { found ->
            if (found) {
                Log.d("OpenTiktok", "Element found! Click find video")
                AccessibilityUtils.clickByViewId("com.zhiliaoapp.musically:id/b5g")
            } else {
                Log.e("OpenTiktok", "Source video button not found after retries.")
            }
            onComplete()
        }
    }
    private fun clickVideo(onComplete: () -> Unit) {
        AccessibilityUtils.clickFirstVideoInRecyclerView("com.zhiliaoapp.musically:id/eh4") {
            Log.d("OpenTiktok", "Click action completed for the first video.")
        }
    }


}