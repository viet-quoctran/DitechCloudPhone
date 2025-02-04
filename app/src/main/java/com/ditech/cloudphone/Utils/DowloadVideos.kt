package com.ditech.cloudphone.Utils

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DowloadVideos {
    companion object {
        suspend fun downloadUsingDownloadManager(
            context: Context,
            videoUrl: String,
            fileName: String,
            onDownloadComplete: (Boolean) -> Unit
        ) {
            // Kiểm tra URL đầu vào
            if (videoUrl.isBlank()) {
                Log.e("DownloadManager", "Invalid video URL: $videoUrl")
                onDownloadComplete(false)
                return
            }

            // Kiểm tra nếu tệp đã tồn tại
            val file = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.let {
                File(it, fileName)
            }

            if (file != null && file.exists()) {
                Log.d("DownloadManager", "File already exists: ${file.absolutePath}")
                onDownloadComplete(true)
                return
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(videoUrl)

            val request = DownloadManager.Request(uri).apply {
                setTitle("Downloading Video")
                setDescription("Downloading $fileName")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, fileName)
            }

            try {
                val downloadId = downloadManager.enqueue(request)
                Log.d("DownloadManager", "Download started for $fileName with ID: $downloadId")

                // Kiểm tra trạng thái tải xuống
                withContext(Dispatchers.IO) {
                    var isDownloading = true
                    while (isDownloading) {
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor: Cursor? = downloadManager.query(query)

                        if (cursor != null && cursor.moveToFirst()) {
                            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    Log.d("DownloadManager", "Download completed for $fileName")
                                    isDownloading = false
                                    onDownloadComplete(true)
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    Log.e("DownloadManager", "Download failed for $fileName")
                                    isDownloading = false
                                    onDownloadComplete(false)
                                }
                            }
                        }
                        cursor?.close()
                        Thread.sleep(1000) // Đợi 1 giây trước khi kiểm tra lại
                    }
                }
            } catch (e: Exception) {
                Log.e("DownloadManager", "Error downloading video: ${e.message}")
                onDownloadComplete(false)
            }
        }
    }
}
