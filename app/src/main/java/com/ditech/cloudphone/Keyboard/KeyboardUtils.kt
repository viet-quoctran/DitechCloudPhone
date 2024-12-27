package com.ditech.cloudphone.keyboard

import android.content.Context
import android.widget.Toast

fun Context.showKeyboardToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
