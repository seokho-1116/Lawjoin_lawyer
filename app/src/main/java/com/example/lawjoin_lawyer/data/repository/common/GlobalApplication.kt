package com.example.lawjoin.common

import android.app.Application
import com.example.lawjoin.R
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, getString(R.string.kakao_native_app_keys))
    }
}