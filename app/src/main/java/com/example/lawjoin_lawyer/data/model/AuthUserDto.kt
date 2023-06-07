package com.example.lawjoin_lawyer.data.model

import java.io.Serializable

class AuthUserDto(
    var uid: String? = "",
    var name:String? = "",
    var email: String? = "",
    var chatProfile: String? = ""
): Serializable {
}