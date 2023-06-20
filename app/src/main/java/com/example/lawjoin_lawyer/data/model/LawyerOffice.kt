package com.example.lawjoin_lawyer.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class LawyerOffice (
    val name: String,
    val phone: String,
    val openingTime: String,
    val closingTime: String,
    val location: String
    ) : Serializable {
    constructor(): this(
        "",
        "",
        ZonedDateTime.now().toString(),
        ZonedDateTime.now().plusHours(8L).toString(),
        ""
    )
}
