package com.example.cloudphone.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    var permissionIndex = 0 // Biến lưu trạng thái quyền đang được kiểm tra

    fun areAllPermissionsGranted(): Boolean {
        return Settings.canDrawOverlays(context) &&
                context.packageManager.canRequestPackageInstalls() &&
                Settings.System.canWrite(context) &&
                isFileAndMediaPermissionGranted()
    }

    fun checkPermissions(onPermissionResult: (Boolean) -> Unit) {
        when (permissionIndex) {
            0 -> {
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    permissionIndex++
                    checkPermissions(onPermissionResult)
                }
            }

            1 -> {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    permissionIndex++
                    checkPermissions(onPermissionResult)
                }
            }

            2 -> {
                if (!Settings.System.canWrite(context)) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } else {
                    permissionIndex++
                    checkPermissions(onPermissionResult)
                }
            }

            3 -> {
                if (!isFileAndMediaPermissionGranted()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else {
                        Log.e("PermissionManager", "File and Media không hỗ trợ trên phiên bản này.")
                    }
                } else {
                    permissionIndex++
                    checkPermissions(onPermissionResult)
                }
            }

            else -> {
                permissionIndex = 0 // Reset lại sau khi kiểm tra xong tất cả các quyền
                onPermissionResult(true)
            }
        }
    }

    fun handleResume(onPermissionResult: (Boolean) -> Unit) {
        if (permissionIndex in 0..3) {
            checkPermissions(onPermissionResult)
        }
    }

    private fun isFileAndMediaPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val readPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            val writePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            readPermission && writePermission
        }
    }
}
