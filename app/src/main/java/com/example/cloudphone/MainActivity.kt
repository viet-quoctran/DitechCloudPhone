package com.example.cloudphone

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cloudphone.keyboard.KeyboardManager
import com.example.cloudphone.permission.PermissionManager
import com.example.cloudphone.permission.showToast
import com.example.cloudphone.ui.theme.CloudPhoneTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.cloudphone.Tiktok.MainActionTiktok
import com.example.cloudphone.Accessibility.AccessibilityManager

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager
    private lateinit var keyboardManager: KeyboardManager
    private var isCheckingPermissions = false
    private var isAccessibilityDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize PermissionManager and KeyboardManager
        permissionManager = PermissionManager(this)
        keyboardManager = KeyboardManager(this)

        setContent {
            CloudPhoneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onCheckPermissions = { startPermissionCheck() },
                        onOpenKeyboardSettings = { handleKeyboardSettings() },
                        onStartAction = { handleStartAction() }
                    )
                }
            }
        }
    }

    private fun startPermissionCheck() {
        if (!isCheckingPermissions) {
            isCheckingPermissions = true
            permissionManager.checkPermissions { allGranted ->
                if (allGranted) {
                    showToast("Tất cả quyền đã được bật!")
                } else {
                    showToast("Hãy bật tất cả các quyền!")
                }
                isCheckingPermissions = false
            }
        }
    }

    private fun handleKeyboardSettings() {
        if (keyboardManager.isKeyboardEnabled()) {
            if (isCloudPhoneKeyboardSelected()) {
                showToast("Keyboard CloudPhone đã được bật!")
            } else {
                showToast("Bạn phải chọn CloudPhone làm bàn phím!")
                keyboardManager.showInputMethodPicker()
            }
        } else {
            keyboardManager.openKeyboardSettings()
            showToast("Vào cài đặt và bật bàn phím CloudPhone!")
        }
    }

    private fun handleStartAction() {
        // Kiểm tra Permissions
        permissionManager.checkPermissions { permissionsGranted ->
            if (!permissionsGranted) {
                // Hiển thị thông báo yêu cầu bật các quyền
                showPermissionRequiredDialog()
                return@checkPermissions
            }

            // Kiểm tra Keyboard
//            if (!keyboardManager.isKeyboardEnabled()) {
//                showToast("Hãy bật bàn phím của bạn!")
//                return@checkPermissions
//            }

            // Kiểm tra Accessibility
            if (!isAccessibilityEnabled()) {
                showAccessibilityDialogOnce()
                return@checkPermissions
            }
            else
            {
                val accessibilityManager = AccessibilityManager()

                val mainActionTiktok = MainActionTiktok(this, accessibilityManager)
                mainActionTiktok.launchTikTok()
            }

        }
    }

    private fun showPermissionRequiredDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Quyền cần thiết")
            .setMessage("Bạn cần bật tất cả các quyền cần thiết để tiếp tục sử dụng ứng dụng.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun isCloudPhoneKeyboardSelected(): Boolean {
        val currentInputMethod = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        return currentInputMethod?.contains("CloudPhone") == true
    }

    override fun onResume() {
        super.onResume()
        if (isCheckingPermissions) {
            permissionManager.handleResume { allGranted ->
                if (allGranted) {
                    showToast("Tất cả quyền đã được bật!")
                } else {
                    showToast("Hãy bật các quyền cần thiết!")
                }
                isCheckingPermissions = false
            }
        }
    }

    private fun handleAccessibilitySettings() {
        if (!isAccessibilityEnabled()) {
            showAccessibilityDialogOnce()
        } else {
            showToast("Quyền Accessibility đã được bật!")
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val service = "$packageName/com.example.cloudphone.Accessibility.AccessibilityManager"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            0
        )
        return accessibilityEnabled == 1 && enabledServices?.contains(service) == true
    }

    private fun showAccessibilityDialogOnce() {
        if (!isAccessibilityDialogShown) {

            MaterialAlertDialogBuilder(this)
                .setTitle("Quyền Accessibility")
                .setMessage("Ứng dụng cần quyền Accessibility để hoạt động. Vui lòng bật quyền này trong cài đặt.")
                .setPositiveButton("Next") { _, _ ->
                    openAccessibilitySettings()
                }
                .setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss()
                    isAccessibilityDialogShown = false
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Không thể mở màn hình Accessibility!")
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onCheckPermissions: () -> Unit,
    onOpenKeyboardSettings: () -> Unit,
    onStartAction: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onCheckPermissions,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Permissions")
            }
            Button(
                onClick = onOpenKeyboardSettings,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Keyboard")
            }
            Button(
                onClick = onStartAction,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Start")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    CloudPhoneTheme {
        MainScreen(
            onCheckPermissions = {},
            onOpenKeyboardSettings = {},
            onStartAction = {}
        )
    }
}
