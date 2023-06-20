package com.example.lawjoin_lawyer.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.ZoneId
import java.time.ZonedDateTime


@RequiresApi(Build.VERSION_CODES.O)
data class Post(
    var id: String = "",
    val title: String = "",
    val detail: String = "",
    val ownerId: String = "",
    val commentList: MutableList<Comment> = mutableListOf(),
    val createTime: String = ZonedDateTime.now(ZoneId.of("UTC")).toString(),
    val modifyTime: String = "",
    val isAnonymous: Boolean = false,
    val recommendationCount: Int = 0
) : Serializable