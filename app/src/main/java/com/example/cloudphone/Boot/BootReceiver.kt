package com.example.cloudphone.Boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val currentInputMethod = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            )

            // Kiểm tra nếu phương thức nhập mặc định không phải CloudPhone
            if (!currentInputMethod.contains("CloudPhone")) {
                Toast.makeText(
                    context,
                    "Hãy đặt CloudPhone làm bàn phím mặc định!",
                    Toast.LENGTH_LONG
                ).show()
                // Gợi ý người dùng vào phần cài đặt nếu cần
                val settingsIntent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(settingsIntent)
            }
        }
    }
}