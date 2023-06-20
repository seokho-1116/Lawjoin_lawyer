package com.example.lawjoin_lawyer.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lawjoin_lawyer.post.PostDetailViewModel
import com.example.lawjoin_lawyer.post.PostViewModel

class ViewModelFactory() : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            PostDetailViewModel() as T
        } else if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            PostViewModel() as T
        } else {
            throw IllegalArgumentException()
        }
    }
}