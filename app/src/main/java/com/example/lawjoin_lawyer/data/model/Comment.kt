package com.example.lawjoin_lawyer.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.ZoneId
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
data class Comment (
    val id: String = "",
    val title: String = "",
    val detail: String = "",
    val owner: String = "",
    val createTime: String = ZonedDateTime.now(ZoneId.of("UTC")).toString(),
    val modifyTime: String = ""
): Serializable