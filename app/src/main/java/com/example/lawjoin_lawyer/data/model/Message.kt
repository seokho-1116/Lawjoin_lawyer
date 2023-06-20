package com.example.lawjoin_lawyer.data.model

import java.io.Serializable
import java.time.ZonedDateTime

data class Message(
    var senderUid: String = "",
    var sendDate: String = "",
    var content: String = "",
    var confirmed:Boolean=false
) : Serializable {
}