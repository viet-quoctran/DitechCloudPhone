package com.ditech.cloudphone.Firebase

import android.util.Log

class FirebaseManager {
    companion object {
        fun fetchFirebaseMessagingToken(onTokenFetched: (String) -> Unit) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d("FCMTOKEN", "Firebase Messaging Token: $token")
                        onTokenFetched(token) // Gọi callback với FCM token
                    } else {
                        Log.e("FCMTOKEN", "Failed to fetch FCM token: ${task.exception?.message}")
                    }
                }
        }
    }
}
