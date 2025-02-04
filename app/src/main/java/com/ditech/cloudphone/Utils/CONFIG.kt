package com.ditech.cloudphone.Utils
import java.text.Normalizer
class CONFIG {
    companion object {
        fun normalizeText(input: String): String {
            return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        }
        val BUTTON_HOME = "com.zhiliaoapp.musically:id/h3h"
        val PACKAGE_NAME_TIKTOK = "com.zhiliaoapp.musically"
        val BUTTON_LIST_CHANNELS = "com.zhiliaoapp.musically:id/hol"
        val BUTTON_PROFILE = "com.zhiliaoapp.musically:id/h3j"
        val ELEMENT_HUC = "com.zhiliaoapp.musically:id/huc"
        val ELEMENT_BKB = "com.zhiliaoapp.musically:id/bkb"
        val ELEMENT_JYQ = "com.zhiliaoapp.musically:id/jyq"
        val INPUT_HASTAG = "com.zhiliaoapp.musically:id/d5k"
        val ELEMENT_KLB = "com.zhiliaoapp.musically:id/klb"
        val ELEMENT_KLC = "com.zhiliaoapp.musically:id/klc"
        val ELEMENT_FN0 = "com.zhiliaoapp.musically:id/fn0"
        val ELEMENT_HU8 = "com.zhiliaoapp.musically:id/hu8"
        val ELEMENT_DNC = "com.zhiliaoapp.musically:id/dnc"
        val ELEMENT_USE_THIS_SOUND = "com.zhiliaoapp.musically:id/atq"
        val ELEMENT_ADD_VIDEO = "com.zhiliaoapp.musically:id/h3e"
        val ELEMENT_SOURCE_VIDEO = "com.zhiliaoapp.musically:id/b5g"
        val ELEMENT_VIDEO_IN_SOURCE = "com.zhiliaoapp.musically:id/eh4"
        val ELEMENT_POST_VIDEO = "com.zhiliaoapp.musically:id/jfv"
        val MESSAGE_ACCESSIBILITY_DISABLE = "Accessibility Disable"
        val MESSAGE_OPEN_TIKTOK = "Don't Open TikTok"
        val MESSAGE_OPEN_TIKTOK_SOUND = "Error opening TikTok with link"
        val MESSAGE_FIND_NAME = "Profile Element Not Found"
        val MESSAGE_DOWLOAD_VIDEO = "Failer Dowload Video"
        val MESSAGE_ACCESSIBILITY_ENABLE = "Service Connected"
        val MESSAGE_ADD_VIDEO = "Can't click to button add video"
        val MESSAGE_SOURCE_VIDEO = "Can't click to source video"
        val MESSAGE_CHOOSE_VIDEO_SOUND = "Can't click to source video have sound"
        val MESSAGE_BUTTON_FORCE_STOP = "Max retries reached. Unable to click 'Force Stop' button."
        val MESSAGE_ELEMENT_BKB = "Element 'com.zhiliaoapp.musically:id/bkb' not found."
        val MESSAGE_ELEMENT_HUC = "Element 'com.zhiliaoapp.musically:id/huc' not found."
        val MESSAGE_ELEMENT_KLB = "Element 'com.zhiliaoapp.musically:id/klb' not found."
        val MESSAGE_ELEMENT_KLC = "Element 'com.zhiliaoapp.musically:id/klc' not found."
        val MESSAGE_ELEMENT_FN0 = "Element 'com.zhiliaoapp.musically:id/fn0' not found."
        // Biến lưu hashtag
        private var currentHashtag: String = ""
        private var productName: String = ""
        private var sound: String? = null
        // Setter để cập nhật hashtag từ API
        fun setHashtag(hashtag: String) {
            currentHashtag = hashtag
        }
        // Getter để lấy hashtag
        fun getHashtag(): String {
            return currentHashtag
        }
        fun setSound(value: String) {
            sound = value
        }
        fun setProductName(hashtag: String) {
            productName = hashtag
        }
        // Getter để lấy hashtag
        fun getProductName(): String {
            return productName
        }
        fun getSound(): String? {
            return sound ?: "null" // Giá trị mặc định là "null"
        }
    }

}