package com.example.lawjoin_lawyer.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
data class User(
    val uid: String? = "", val name:String? = "", val email: String? = "",
    val phone: String? = "",
    val likeLawyers: MutableList<String> = mutableListOf(),
    val recommendPosts: MutableList<String> = mutableListOf(),
    val likeComments: MutableList<String> = mutableListOf(),
    val bookmarkedPosts: MutableList<String> = mutableListOf(),
    val enterTime: String? = "",
    val chatProfile: String? = ""
): Serializable {
    constructor(uid: String?, email: String?, name: String?, chatProfile: String?) :
            this(uid, name, email, "", mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(),
                ZonedDateTime.now().toString(), chatProfile)
}