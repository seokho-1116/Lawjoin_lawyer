package com.example.lawjoin_lawyer.post

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lawjoin_lawyer.data.model.Post
import com.example.lawjoin_lawyer.data.repository.PostRepository

@RequiresApi(Build.VERSION_CODES.O)
class PostViewModel: ViewModel(){
    private val postRepository = PostRepository.getInstance()
    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts
    private val _counselPosts = MutableLiveData<List<Post>>()
    val counselPosts: LiveData<List<Post>> = _counselPosts

    fun findAllFreePosts(category: String) {
        postRepository.findAllPosts(category) { posts ->
            _posts.value = posts
        }
    }

    fun findAllCounselPosts(category: String) {
        postRepository.findAllPosts(category) { posts ->
            _counselPosts.value = posts
        }
    }
}
