package com.example.lawjoin_lawyer

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lawjoin_lawyer.common.AuthUtils
import com.bumptech.glide.Glide
import com.example.lawjoin_lawyer.data.model.AuthUserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: AuthUserDto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        auth = Firebase.auth
        initializeView()
        initializeListener()
        setupRecycler()
        setContentView(binding.root)
    }

    private fun initializeView() {
        AuthUtils.getCurrentUser { authUserDto, _ ->
            currentUser = authUserDto!!

            Glide.with(this)
                .load(currentUser.chatProfile)
                .circleCrop()
                .into(binding.btnChatProfile)
        }

        binding.ibMainMessage.isClickable = false
        binding.ibMainMessage.isPressed = true
    }

    private fun initializeListener() {
        //TODO: 변호사 리스트 액티비티 추가
        binding.btnChatProfile.setOnClickListener {
            startActivity(Intent(this, AccountManagementActivity::class.java))
        }
        binding.btnStartChat.setOnClickListener {
            startActivity(Intent(this, LawyerListActivity::class.java))
            finish()
        }
        binding.ibMainLawyer.setOnClickListener {
            startActivity(Intent(this, LawyerListActivity::class.java))
        }
        binding.ibMainLawWord.setOnClickListener {
            startActivity(Intent(this, LawWord::class.java))
        }
        binding.ibMainPost.setOnClickListener {
            //TODO: 게시글 리스트로
            startActivity(Intent(this, BoardFreeActivity::class.java))
        }
        binding.edtChatSearch
    }

    private fun setupRecycler() {
        binding.rvChatList.layoutManager = LinearLayoutManager(this)
        binding.rvChatList.adapter = RecyclerChatRoomAdapter(this)
    }
}