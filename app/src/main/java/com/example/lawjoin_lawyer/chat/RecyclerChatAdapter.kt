package com.example.lawjoin_lawyer.chat

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.lawjoin.R
import com.example.lawjoin.common.AuthUtils
import com.example.lawjoin.data.model.AuthUserDto
import com.example.lawjoin.data.model.ChatRoom
import com.example.lawjoin.data.model.Message
import com.example.lawjoin.data.repository.ChatRoomRepository
import com.example.lawjoin.data.repository.LawyerRepository
import com.example.lawjoin.databinding.ChatMessageSenderBinding
import com.example.lawjoin.databinding.ChatMessageReceiverBinding
import com.google.firebase.storage.FirebaseStorage
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.StringBuilder

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerChatAdapter(private val context: Context, private var chatRoomKey: String, ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val storage = FirebaseStorage.getInstance()
    private val lawyerRepository = LawyerRepository.getInstance()
    private val chatRoomRepository = ChatRoomRepository.getInstance()

    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
    private var lastFetchedMessageTimestamp: ZonedDateTime = ZonedDateTime.parse("2000-01-01T00:00:00Z[UTC]")

    var messages: MutableList<Message> = mutableListOf()
    var messageKeys: MutableList<String> = mutableListOf()

    private lateinit var currentUser: AuthUserDto

    private val recyclerView = (context as ChatRoomActivity).recyclerView

    init {
        AuthUtils.getCurrentUser { authUserDto, _ ->
            currentUser = authUserDto!!
            getMessages()
        }
    }

    private fun getMessages() {
        chatRoomRepository.findUserChatRoomByKey(currentUser.uid!!, chatRoomKey) {
            val messagesData = it.child("messages")
            val newMessages = mutableListOf<Message>()
            val newMessageKeys = mutableListOf<String>()

            messagesData.children.forEach { messageSnapshot ->
                val message = messageSnapshot.getValue(Message::class.java)
                val messageKey = messageSnapshot.key
                if (message != null && messageKey != null) {
                    val messageTimestamp = ZonedDateTime.parse(message.sendDate)
                    if (messageTimestamp.isAfter(lastFetchedMessageTimestamp)) {
                        newMessages.add(message)
                        newMessageKeys.add(messageKey)
                    }
                }
            }

            messages.addAll(newMessages)
            messageKeys.addAll(newMessageKeys)

            if (newMessages.isNotEmpty()) {
                val lastNewMessageTimestamp = ZonedDateTime.parse(newMessages.last().sendDate)
                lastFetchedMessageTimestamp = lastNewMessageTimestamp
            }

            notifyDataSetChanged()
            recyclerView.scrollToPosition(messages.size - 1)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderUid == currentUser.uid!!) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.chat_message_sender, parent, false)

                ChatMessageSenderViewHolder(ChatMessageSenderBinding.bind(view))
            }

            else -> {
                val view =
                    LayoutInflater.from(context)
                        .inflate(R.layout.chat_message_receiver, parent, false)
                ChatMessageReceiverViewHolder(ChatMessageReceiverBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (messages[position].senderUid == currentUser.uid!!) {
            (holder as ChatMessageSenderViewHolder).bind(position)
        } else {
            (holder as ChatMessageReceiverViewHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class ChatMessageReceiverViewHolder(itemView: ChatMessageReceiverBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        private val message = itemView.tvReceiverText
        private val date = itemView.tvReceiverDate
        private val isShown = itemView.tvReceiverIsRead
        private val profile = itemView.ivReceiverProfile

        fun bind(position: Int) {
            message.text = messages[position].content

            date.text = getDateText(ZonedDateTime.parse(messages[position].sendDate, formatter))

            setupMessageProfile(position)

            if (messages[position].senderUid != "GPT" && messages[position].senderUid != "BOT") {
                if (messages[position].confirmed)
                    isShown.visibility = View.GONE
                else
                    isShown.visibility = View.VISIBLE
                setShown(position)
            }

            when (messages[position].senderUid) {
                "GPT" -> {
                    retrieveProfileUrl("profile/GPT.png") { url ->
                        setProfileAndConfigureScreen(this, url)
                    }
                }
                "BOT" -> {
                    retrieveProfileUrl("profile/BOT.png") { url ->
                        setProfileAndConfigureScreen(this, url)
                    }
                }
                else -> {
                    lawyerRepository.findLawyerById(messages[position].senderUid) {
                        setProfileAndConfigureScreen(this, it.profile_url)
                    }
                }
            }
        }

        private fun retrieveProfileUrl(path: String, callback: (String) -> Unit) {
            storage.reference.child(path)
                .downloadUrl.addOnSuccessListener { uri ->
                    val url = uri.toString()
                    callback(url)
                }
        }

        private fun setProfileAndConfigureScreen(holder: ChatMessageReceiverViewHolder, url: String) {
            Glide.with(context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.profile)
        }

        private fun setupMessageProfile(position: Int) {
            if (position != 0) {
                val currentMsgMinute = ZonedDateTime.parse(messages[position - 1].sendDate).minute
                val nextMsgMinute = ZonedDateTime.parse(messages[position].sendDate).minute
                if (nextMsgMinute - currentMsgMinute < 10) {
                    return
                }
            }
        }
    }

    inner class ChatMessageSenderViewHolder(itemView: ChatMessageSenderBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        private var txtMessage = itemView.tvSenderText
        private var txtDate = itemView.tvSenderDate
        private var isShown = itemView.tvSenderIsRead

        fun bind(position: Int) {
            txtMessage.text = messages[position].content

            txtDate.text = getDateText(ZonedDateTime.parse(messages[position].sendDate, formatter))

            if (messages[position].senderUid != "GPT" && messages[position].senderUid != "BOT") {
                if (messages[position].confirmed)
                    isShown.visibility = View.GONE
                else
                    isShown.visibility = View.VISIBLE
            }
        }
    }

    private fun getDateText(sendDate: ZonedDateTime): String {
        val zoneId = ZoneId.systemDefault()
        val messageTime = sendDate.withZoneSameInstant(zoneId)

        val dateText = StringBuilder()
        val timeFormat = "%02d:%02d"

        val hour = messageTime.hour
        val minute = messageTime.minute
        if (hour > 11) {
            dateText.append("오후 ")
            dateText.append(timeFormat.format(hour - 12, hour))
        } else {
            dateText.append("오전 ")
            dateText.append(timeFormat.format(hour, minute))
        }
        return dateText.toString()
    }

    private fun setShown(position: Int) {
        chatRoomRepository.updateMessageConfirm(currentUser.uid!!, chatRoomKey, messageKeys[position])
    }
}
