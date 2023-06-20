package com.example.lawjoin_lawyer.data.model

import java.io.Serializable

data class ChatRoom(val messages: Map<String, Message>,
                    val users: List<String>) : Serializable {
    constructor() : this(mapOf(), listOf())
}