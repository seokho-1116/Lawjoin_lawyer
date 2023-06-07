package com.example.lawjoin.common

import android.util.Log
import com.example.lawjoin.data.model.AuthUserDto
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.user.UserApiClient

object AuthUtils {
    fun getCurrentUser(callback: (AuthUserDto?, Throwable?) -> Unit) {
        val user = Firebase.auth.currentUser
        val currentUser = AuthUserDto(
            user?.uid,
            user?.displayName,
            user?.email,
            user?.photoUrl.toString()
        )

        if (user == null) {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("RecyclerChatAdapter", "사용자 정보 요청 실패", error)
                    callback(null, error)
                } else if (user != null) {
                    val authUserDto = AuthUserDto(
                        user.id.toString(),
                        user.kakaoAccount?.name,
                        user.kakaoAccount?.email,
                        user.kakaoAccount?.profile?.thumbnailImageUrl.toString()
                    )
                    callback(authUserDto, null)
                }
            }
        } else {
            callback(currentUser, null)
        }
    }
}