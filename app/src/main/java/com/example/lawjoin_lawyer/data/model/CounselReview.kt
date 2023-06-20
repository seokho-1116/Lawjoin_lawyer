package com.example.lawjoin_lawyer.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class CounselReview(
    val title: String,
    val detail: String,
    val createdTime: String,
    val writerId: String,
    val category: String,
    val lawyerId: String
) : Serializable{
    constructor() : this("", "", ZonedDateTime.now().toString(), "", "", "")
}
