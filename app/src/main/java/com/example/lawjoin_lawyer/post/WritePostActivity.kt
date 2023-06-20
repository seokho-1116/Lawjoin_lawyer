package com.example.lawjoin_lawyer.post

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.lawjoin_lawyer.common.AuthUtils
import com.example.lawjoin_lawyer.data.model.AuthUserDto
import com.example.lawjoin_lawyer.data.model.Post
import com.example.lawjoin_lawyer.data.repository.PostRepository
import com.example.lawjoin_lawyer.databinding.ActivityPostWriteBinding
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class WritePostActivity: AppCompatActivity() {
    private val postRepository: PostRepository = PostRepository.getInstance()
    private lateinit var binding: ActivityPostWriteBinding
    private lateinit var currentUser: AuthUserDto
    private lateinit var newPost: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val postType = intent.getStringExtra("type")
        var moveActivityName = ""

        AuthUtils.getCurrentUser { authUserDto, _ ->
            currentUser = authUserDto!!
        }

        binding.btnWrite.setOnClickListener {
            val id = ""
            var ownerId: String = currentUser.uid!!
            if(binding.cbWriteAnonymous.isChecked){
                ownerId = "익명"
            }

            newPost = Post(
                id,
                binding.edtPostTitle.text.toString(),
                binding.edtPostContent.text.toString(),
                ownerId,
                mutableListOf(),
                ZonedDateTime.now().toString(),
                ZonedDateTime.now().toString(),
                binding.cbWriteAnonymous.isChecked,
                0
            )

            if(postType == "free_post") {
                moveActivityName = "BoardFreeActivity"
            }
            else if (postType == "counsel_post") {
                moveActivityName = "BoardCounselActivity"
            }

            postRepository.savePost(postType!!) {
                it.setValue(newPost)
            }

            val outIntent = Intent(applicationContext, moveActivityName::class.java)
            setResult(Activity.RESULT_OK, outIntent)
            finish()
        }
    }
}



