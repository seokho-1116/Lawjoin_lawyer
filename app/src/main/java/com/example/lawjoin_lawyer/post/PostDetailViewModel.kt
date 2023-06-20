package com.example.lawjoin_lawyer.post

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lawjoin_lawyer.data.model.User
import com.example.lawjoin_lawyer.data.repository.UserRepository

class PostDetailViewModel: ViewModel(){
    private val userRepository = UserRepository.getInstance()
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    @RequiresApi(Build.VERSION_CODES.O)
    fun findUser(uid: String, callback: (User?) -> Unit) {
        userRepository.findUser(uid) {
            _user.value = it.getValue(User::class.java)

            for (postSnapshot in it.child("bookmarked_posts").children) {
                val postId = postSnapshot.key
                _user.value?.bookmarkedPosts!!.add(postId ?: "")
            }

            for (postSnapshot in it.child("recommended_posts").children) {
                val postId = postSnapshot.key
                _user.value?.recommendPosts!!.add(postId ?: "")
            }

            callback(_user.value)
        }
    }

    fun updateUserBookmark(postId: String) {
        userRepository.updateBookmark(user.value?.uid!!, postId)
    }

    fun updateUserRecommendPost(postId: String) {
        userRepository.updateRecommendedPost(user.value?.uid!!, postId)
    }

    fun deleteBookmark(postId: String) {
        if (user.value?.bookmarkedPosts!!.contains(postId)) {
            userRepository.deleteBookmark(user.value?.uid!!, postId)
        }
    }

    fun deleteRecommend(postId: String) {
        if (user.value?.recommendPosts!!.contains(postId)) {
            userRepository.deleteRecommend(user.value?.uid!!, postId)
        }
    }
}
