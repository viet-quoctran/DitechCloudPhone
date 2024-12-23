package com.example.cloudphone.qrcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.zxing.integration.android.IntentIntegrator

class QrCodeScannerActivity : ComponentActivity() {

    private lateinit var qrCodeLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Đăng ký ActivityResultLauncher
        qrCodeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val qrResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
                if (qrResult.contents != null) {
                    // Trả về mã QR sau khi quét thành công
                    val intent = Intent().apply {
                        putExtra("qr_result", qrResult.contents) // Lưu nội dung mã QR
                    }
                    setResult(Activity.RESULT_OK, intent)
                } else {
                    // Người dùng nhấn hủy
                    setResult(Activity.RESULT_CANCELED)
                }
            } else {
                // Người dùng hủy quét
                setResult(Activity.RESULT_CANCELED)
            }
            finish() // Đóng Activity sau khi xử lý xong
        }

        // Bắt đầu quét mã QR
        startQrCodeScanner()
    }

    private fun startQrCodeScanner() {
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // Chỉ quét mã QR
        intentIntegrator.setPrompt("Quét mã QR để xác minh thiết bị") // Hướng dẫn người dùng
        intentIntegrator.setCameraId(0) // Sử dụng camera sau
        intentIntegrator.setBeepEnabled(true) // Phát âm thanh khi quét thành công
        intentIntegrator.setOrientationLocked(false) // Cho phép xoay màn hình
        val scanIntent = intentIntegrator.createScanIntent() // Tạo Intent quét mã QR
        qrCodeLauncher.launch(scanIntent) // Sử dụng ActivityResultLauncher để khởi chạy
    }
}
