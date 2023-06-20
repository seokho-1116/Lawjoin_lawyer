package com.example.lawjoin_lawyer.chat

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lawjoin_lawyer.data.repository.ChatRoomRepository
import com.example.lawjoin_lawyer.MainActivity
import com.example.lawjoin_lawyer.common.AuthUtils
import com.example.lawjoin_lawyer.data.model.AuthUserDto
import com.example.lawjoin_lawyer.data.model.ChatRoom
import com.example.lawjoin_lawyer.data.model.Message
import com.example.lawjoin_lawyer.data.model.UserDto
import com.example.lawjoin_lawyer.databinding.ActivityChatBinding
import java.io.Serializable
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone


@RequiresApi(Build.VERSION_CODES.O)
class ChatRoomActivity : AppCompatActivity() {
    private val chatRoomRepository = ChatRoomRepository.getInstance()
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatRoom: ChatRoom
    private lateinit var currentUser: AuthUserDto
    private lateinit var receiver: UserDto
    private lateinit var chatRoomKey: String
    private lateinit var chatAdapter: RecyclerChatAdapter
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        initializeProperties()
        initializeListeners()
        setContentView(binding.root)
    }

    private fun initializeProperties() {

        AuthUtils.getCurrentUser { authUserDto, _ ->
            currentUser = authUserDto!!

            Glide.with(this)
                .load(currentUser.chatProfile)
                .circleCrop()
                .into(binding.btnChatProfile)
        }

        chatRoom = intent.serializable("chat_room")!!
        chatRoomKey = intent.serializable("chat_room_key")!!
        receiver = intent.serializable("receiver")!!


        binding.tvReceiverDate.text = ZonedDateTime.now(TimeZone.getDefault().toZoneId())
            .toLocalDate().toString()
        binding.tvChatReceiver.text = receiver.name

        recyclerView = binding.rvChatContent
        val spaceDecoration = VerticalSpaceItemDecoration(20)
        recyclerView.addItemDecoration(spaceDecoration)
        chatAdapter = RecyclerChatAdapter(this, receiver.uid!!, chatRoomKey)
        binding.rvChatContent.layoutManager = LinearLayoutManager(this)
        binding.rvChatContent.adapter = chatAdapter
    }

    private fun initializeListeners() {
        binding.ivChatBack.setOnClickListener {
            startActivity(Intent(this@ChatRoomActivity, MainActivity::class.java))
        }
        binding.ibChatSend.setOnClickListener {
            putMessage()
        }
    }

    private fun putMessage() {
        toReceiver()
        binding.edtChatInput.text.clear()
    }

    private fun toReceiver() {
        val message = Message(
            currentUser.uid!!,
            ZonedDateTime.now(ZoneId.of("UTC")).toString(),
            binding.edtChatInput.text.toString(),
            false
        )

        chatRoomRepository.saveChatRoomMessage(receiver.uid!!, chatRoomKey, message) {}
    }

    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
        }
    }

    private inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }
}
