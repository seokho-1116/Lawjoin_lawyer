package com.example.lawjoin.common

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object FireBaseStorageUtils {

    fun setupProfile(uid: String, profile: String, callback: (String) -> Unit) {
        when (uid) {
            "GPT" -> {
                retrieveProfileUrl("profile/GPT.png") { url ->
                    callback(url)
                }
            }

            "BOT" -> {
                retrieveProfileUrl("profile/BOT.png") { url ->
                    callback(url)
                }
            }

            else -> {
                callback(profile)
            }
        }
    }

    private fun retrieveProfileUrl(path: String, callback: (String) -> Unit) {
        Firebase.storage.reference.child(path)
            .downloadUrl.addOnSuccessListener { uri ->
                val url = uri.toString()
                callback(url)
            }
    }
}