package com.example.lawjoin_lawyer.data.model

import java.io.Serializable

class UserDto(val uid: String?, val name: String?, val email: String?, val profile: String?): Serializable {
    constructor(): this("", "", "", "")
}