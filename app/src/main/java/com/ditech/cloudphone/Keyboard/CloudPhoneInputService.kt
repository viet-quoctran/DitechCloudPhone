package com.ditech.cloudphone.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.ditech.cloudphone.R

class CloudPhoneInputService : InputMethodService() {

    override fun onCreateInputView(): View {
        // Tạo giao diện bàn phím của bạn (hoặc sử dụng View mặc định)
        val inputView = layoutInflater.inflate(R.layout.input_view, null)
        return inputView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Cập nhật giao diện nếu cần
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        // Xử lý khi bàn phím đóng
    }
}
