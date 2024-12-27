package com.ditech.cloudphone.Accessibility

import android.app.Application

class SaveInstance : Application() {
    companion object {
        var accessibilityService: AccessibilityManager? = null
    }
}