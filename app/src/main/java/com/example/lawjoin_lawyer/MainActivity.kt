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
import com.example.lawjoin_lawyer.databinding.ActivityMainBinding
import com.example.lawjoin_lawyer.post.BoardFreeActivity
import com.example.lawjoin_lawyer.reservation.CounselReservationActivity

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentUser: AuthUserDto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
    }

    private fun initializeListener() {
        binding.ibMainReservation.setOnClickListener {
            startActivity(Intent(this, CounselReservationActivity::class.java))
        }
        binding.ibMainMessage.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.ibMainPost.setOnClickListener {
            startActivity(Intent(this, BoardFreeActivity::class.java))
        }

        binding.ibMainMessage.isClickable = false
        binding.ibMainMessage.isSelected = true
    }

    private fun setupRecycler() {
        binding.rvChatList.layoutManager = LinearLayoutManager(this)
        binding.rvChatList.adapter = RecyclerChatRoomAdapter(this)
    }
}