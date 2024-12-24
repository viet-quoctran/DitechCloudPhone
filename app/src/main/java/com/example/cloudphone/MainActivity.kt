package com.example.cloudphone

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.cloudphone.Utils.TokenManager
import com.example.cloudphone.Accessibility.AccessibilityManager
import kotlinx.coroutines.*
import com.example.cloudphone.qrcode.QrCodeScannerActivity
import com.example.cloudphone.Network.ApiClient
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager
    private lateinit var keyboardManager: KeyboardManager
    private var isCheckingPermissions = false
    private var isAccessibilityDialogShown = false
    private lateinit var qrCodeLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Khởi tạo ActivityResultLauncher
        qrCodeLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val qrResult = result.data?.getStringExtra("qr_result")
                if (!qrResult.isNullOrEmpty()) {
                    // Xử lý mã QR (ví dụ: gửi token lên backend)
                    handleQrCodeScanned(qrResult)
                } else {
                    showToast("Không có mã QR nào được quét.")
                }
            } else {
                showToast("Quét mã QR thất bại hoặc bị hủy.")
            }
        }
        checkToken()
        permissionManager = PermissionManager(this)
        keyboardManager = KeyboardManager(this)
    }
    private fun checkToken() {
        // Lấy token từ SharedPreferences
        val token = TokenManager.getToken(this)
        if (token.isNullOrEmpty()) {
            // Nếu không có token, chuyển đến màn hình quét mã QR
            showToast("Không tìm thấy token. Hãy quét mã QR để xác thực.")
            showQRCodeScanner()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val apiUrl = "http://103.216.119.245/api/device/authenticate?token=$token"
                val response = ApiClient.get(apiUrl)
                withContext(Dispatchers.Main) {
                    if (response != null) {
                        val device = response.optJSONObject("data")?.optJSONObject("device")
                        val serverToken = device?.optString("token")
                        if (serverToken == token) {
                            showToast("Login thành công!")
                            ShowMainApp()
                        } else {
                            showToast("Token không hợp lệ. Vui lòng quét mã QR.")
                            showQRCodeScanner()
                        }
                    } else {
                        showQRCodeScanner()
                    }
                }
            }
        }
    }
    private fun showQRCodeScanner() {
        val intent = Intent(this, QrCodeScannerActivity::class.java)
        qrCodeLauncher.launch(intent)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Xử lý mã QR hoặc kết quả trả về
    }
    private fun handleQrCodeScanned(qrResult: String) {
        // Giả sử mã QR chỉ chứa token
        val token = qrResult
        // Lấy device name từ Build.MODEL
        val deviceName = getDeviceName()
        Log.d("devicesqr",token)
        // Gửi token và device name lên backend
        sendTokenToBackend(token, deviceName)
    }
    private fun getDeviceName(): String {
        val deviceName = Build.MODEL ?: "Unknown Device" // Lấy tên thiết bị hoặc trả về giá trị mặc định
        return URLEncoder.encode(deviceName, StandardCharsets.UTF_8.toString()) // Mã hóa tên thiết bị để sử dụng an toàn trong URL
    }
    private fun sendTokenToBackend(token: String, deviceName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val apiUrl = "http://103.216.119.245/api/device/connect"
            val payload = JSONObject().apply {
                put("token", token)
                put("device_name", deviceName)
            }.toString()

            val response = ApiClient.post(apiUrl, payload)
            withContext(Dispatchers.Main) {
                val message = response?.optString("message") ?: ""
                if (message == "Device authenticated successfully") {
                    showToast("Thiết bị đã được xác thực thành công.")
                    TokenManager.saveToken(this@MainActivity, token)
                    ShowMainApp()
                } else {
                    showToast("Xác thực thất bại. Vui lòng thử lại.")
                }
            }
        }
    }
    companion object {
        const val QR_CODE_REQUEST_CODE = 1001
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
    private fun ShowMainApp()
    {
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
