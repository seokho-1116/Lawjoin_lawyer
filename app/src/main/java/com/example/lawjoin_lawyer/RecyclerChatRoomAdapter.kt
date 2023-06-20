package com.example.lawjoin_lawyer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.lawjoin_lawyer.chat.ChatRoomActivity
import com.example.lawjoin_lawyer.common.AuthUtils
import com.example.lawjoin_lawyer.data.model.AuthUserDto
import com.example.lawjoin_lawyer.data.model.ChatRoom
import com.example.lawjoin_lawyer.data.model.UserDto
import com.example.lawjoin_lawyer.data.repository.ChatRoomRepository
import com.example.lawjoin_lawyer.data.repository.UserRepository
import com.example.lawjoin_lawyer.databinding.ChatRoomItemBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerChatRoomAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerChatRoomAdapter.ViewHolder>() {
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
    private val userRepository: UserRepository = UserRepository.getInstance()
    private val chatRoomRepository: ChatRoomRepository = ChatRoomRepository.getInstance()
    private var chatRooms: MutableList<ChatRoom> = mutableListOf()
    private var chatRoomKeys: MutableList<String> = mutableListOf()
    private lateinit var currentUser: AuthUserDto

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        AuthUtils.getCurrentUser { authUserDto, _ ->
            currentUser = authUserDto!!
            setupAllChatRoomList()
        }
    }

    private fun setupAllChatRoomList() {
        chatRoomRepository.findAllChatRoomsByUid {
            chatRooms.clear()
            for (userChatRooms in it.children) {
                for (userChatRoom in userChatRooms.children) {
                    if (userChatRoom.key == currentUser.uid) {
                        val chatRoom = userChatRoom.getValue(ChatRoom::class.java)!!
                        chatRoomKeys.add(userChatRoom.key.toString())
                        chatRooms.add(chatRoom)
                    }
                }
            }
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChatRoomItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val opponent = chatRooms[position].users.first { it != currentUser.uid }

        userRepository.findUser(opponent) {
            val user = it.getValue(UserDto::class.java)!!
            holder.chatProfile = user.profile.orEmpty()
            holder.opponentUser = user
            holder.receiver.text = user.name
        }

        holder.layout.setOnClickListener {
            val intent = Intent(context, ChatRoomActivity::class.java)
            intent.putExtra("chat_room", chatRooms[position])
            intent.putExtra("receiver", holder.opponentUser)
            intent.putExtra("chat_room_key", chatRoomKeys[position])
            context.startActivity(intent)
        }

        setProfileAndConfigureScreen(holder,holder.chatProfile)

        if (chatRooms[position].messages.isNotEmpty()) {
            setupLastMessageAndDate(holder, position)
        }

        setupMessageCount(holder, position)
    }

    private fun setProfileAndConfigureScreen(holder: ViewHolder, url: String) {
        holder.chatProfile = url

        Glide.with(context)
            .load(holder.chatProfile)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.profileImage)
    }


    private fun setupLastMessageAndDate(holder: ViewHolder, position: Int) {
        val lastMessage = chatRooms[position].messages.values
            .sortedWith(compareBy { it.sendDate })
            .last()
        holder.lastMessage.text = lastMessage.content
        holder.date.text = getLastMessageTimeString(ZonedDateTime.parse(lastMessage.sendDate, formatter))
    }

    private fun setupMessageCount(holder: ViewHolder, position: Int) {
        val unconfirmedCount =
            chatRooms[position].messages.values.filter {
                !it.confirmed && it.senderUid != currentUser.uid
            }.size

        if (unconfirmedCount > 0) {
            holder.unreadCount.visibility = View.VISIBLE
            holder.unreadCount.text = unconfirmedCount.toString()
        } else {
            holder.unreadCount.visibility = View.GONE
        }
    }

    private fun getLastMessageTimeString(lastTimeString: ZonedDateTime): String {
        val zoneId = ZoneId.systemDefault()
        val messageTime = lastTimeString.withZoneSameInstant(zoneId)
        val currentTime = LocalDateTime.now().atZone(zoneId)

        val difference = ChronoUnit.MINUTES.between(messageTime, currentTime)
        return when {
            difference < 1 -> "방금 전"
            difference < 60 -> "${difference}분 전"
            difference < 1440 -> "${difference / 60}시간 전"
            difference < 43200 -> "${difference / 1440}일 전"
            else -> "${difference / 43200}달 전"
        }
    }

    override fun getItemCount(): Int {
        return chatRooms.size
    }

    class ViewHolder(binding: ChatRoomItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var chatProfile = ""
        var opponentUser = UserDto()
        var layout = binding.root
        var receiver = binding.tvChatRoomReceiver
        var lastMessage = binding.tvChatRoomText
        var date = binding.tvChatDate
        var unreadCount = binding.tvChatRoomNotificationCount
        var profileImage = binding.ivChatProfile
    }
}