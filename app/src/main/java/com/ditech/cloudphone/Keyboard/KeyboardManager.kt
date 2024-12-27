package com.ditech.cloudphone.keyboard

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

class KeyboardManager(private val context: Context) {

    fun isKeyboardEnabled(): Boolean {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            ?: return false
        val enabledInputMethods = inputMethodManager.enabledInputMethodList
        for (method in enabledInputMethods) {
            if (method.packageName == context.packageName) {
                return true
            }
        }
        return false
    }

    fun showInputMethodPicker() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.showInputMethodPicker()
    }

    fun openKeyboardSettings() {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    fun isKeyboardSelected(): Boolean {
        val defaultInputMethod = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        return defaultInputMethod?.contains(context.packageName) == true
    }
}
