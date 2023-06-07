package com.example.lawjoin.chat

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.lawjoin.MainActivity
import com.example.lawjoin.R
import com.example.lawjoin.common.AuthUtils
import com.example.lawjoin.data.model.AuthUserDto
import com.example.lawjoin.data.model.ChatRoom
import com.example.lawjoin.data.model.LawyerDto
import com.example.lawjoin.data.model.Message
import com.example.lawjoin.data.repository.ChatBotRepository
import com.example.lawjoin.data.repository.ChatRoomRepository
import com.example.lawjoin.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.user.UserApiClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.Serializable
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone
import java.util.concurrent.TimeUnit


@RequiresApi(Build.VERSION_CODES.O)
class ChatRoomActivity : AppCompatActivity() {
    private val chatRoomRepository = ChatRoomRepository.getInstance()
    private lateinit var binding: ActivityChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var chatRoom: ChatRoom
    private lateinit var currentUser: AuthUserDto
    private lateinit var receiver: LawyerDto
    private lateinit var chatRoomKey: String
    private lateinit var chatAdapter: RecyclerChatAdapter
    private lateinit var client: OkHttpClient
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeProperties()
        initializeListeners()
        setupChatRoom()
    }

    private fun initializeProperties() {
        auth = Firebase.auth

        AuthUtils.getCurrentUser { authUserDto, _ ->
            currentUser = authUserDto!!

            Glide.with(this)
                .load(currentUser.chatProfile)
                .circleCrop()
                .into(binding.btnChatProfile)
        }

        client = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val intent = intent
        chatRoom = intent.serializable("chat_room") ?: return
        chatRoomKey = intent.serializable<String>("chat_room_key").orEmpty()
        receiver = intent.serializable("receiver") ?: return

        binding.tvReceiverDate.text = ZonedDateTime.now(TimeZone.getDefault().toZoneId())
            .toLocalDate().toString()
        binding.tvChatReceiver.text = receiver.name

        recyclerView = binding.rvChatContent
        val spaceDecoration = VerticalSpaceItemDecoration(20)
        recyclerView.addItemDecoration(spaceDecoration)
        chatAdapter = RecyclerChatAdapter(this, chatRoomKey)
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

    private fun setupChatRoom() {
        setupRecycler()
    }

    private fun putMessage() {
        toReceiver()
        when (receiver.uid) {
            "BOT" -> {
                toBOT()
            }
            "GPT" -> {
                toGPT()
            }
        }
        binding.edtChatInput.text.clear()
    }

    private fun toGPT() {
        val requestObject = createRequestObject()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body: RequestBody = requestObject.toString().toRequestBody(mediaType)
        val request: Request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer ${getString(R.string.gpt_api_key)}")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to " + e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    try {
                        val jsonObject = JSONObject(responseBody.toString())
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val result =
                            jsonArray.getJSONObject(0).getJSONObject("message").getString("content")
                        addResponse(result.trim { it <= ' ' })
                    } catch (e: JSONException) {
                        addResponse("시스템에 문제가 발생했습니다. 다시 시도해주세요.")
                    }
                } else {
                    addResponse("Failed to load response due to " + responseBody.toString())
                }
            }
        })
    }

    private fun createRequestObject(): JSONObject {
        val jsonBody = JSONObject()
        try {
            jsonBody.put("model","gpt-3.5-turbo");

            val messageArr = JSONArray();
            val obj = JSONObject();
            obj.put("role","user");
            obj.put("content", binding.edtChatInput.text.toString());
            messageArr.put(obj);

            jsonBody.put("messages",messageArr);
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }

        return jsonBody
    }

    fun addResponse(response: String) {
        val message = Message(
            "GPT",
            ZonedDateTime.now(ZoneId.of("UTC")).toString(),
            response,
            true
        )
        chatRoomRepository.saveChatRoomMessage(currentUser.uid!!, chatRoomKey, message) {
            Log.d("ChatRoomActivity", "message success")
        }
    }

    private fun toBOT() {
        chatBotRepository.findAnswerByQuestion(binding.edtChatInput.text.toString()) {querySnapshot ->
            val answers = mutableListOf<String>()
            val questions = mutableListOf<String>()

            if (!querySnapshot.isEmpty) {
                for (documentSnapshot in querySnapshot) {
                    answers.add(documentSnapshot.getString("answer").toString())
                    questions.add(documentSnapshot.getString("question").toString())
                }
            }

            if (questions.isEmpty() || answers.isEmpty()) {
                val result = mutableListOf(
                    Message(
                        "BOT",
                        ZonedDateTime.now(ZoneId.of("UTC")).toString(),
                        "입력하신 질문에 대한 결과가 존재하지 않습니다. 다시 질문해주세요.",
                        true
                    )
                )
                chatRoomRepository.updateMessages(currentUser.uid!!, chatRoomKey, result)
                return@findAnswerByQuestion
            }

            val questionMessage = Message(
                    "BOT",
                    ZonedDateTime.now(ZoneId.of("UTC")).toString(),
                    "관련 질문: ${questions[0]}",
                true
            )

            val answerMessage = Message(
                    "BOT",
                    ZonedDateTime.now(ZoneId.of("UTC")).toString(),
                    "답: ${answers[0]}",
                true
                )

            val result = mutableListOf(questionMessage, answerMessage)
            if (answers.size > 1) {
                result.add(Message(
                        "BOT",
                        ZonedDateTime.now(ZoneId.of("UTC")).toString(),
                        "더 많은 결과가 존재합니다. 자세히 질문해주세요.",
                    true))
            }

            chatRoomRepository.updateMessages(currentUser.uid!!, chatRoomKey, result)
        }
    }

    private fun toReceiver() {
        val isConfirmed = when (receiver.uid) {
            "BOT" -> true
            "GPT" -> true
            else -> false
        }
        val message = Message(
            currentUser.uid!!,
            ZonedDateTime.now(ZoneId.of("UTC")).toString(),
            binding.edtChatInput.text.toString(),
            isConfirmed
        )

        chatRoomRepository.saveChatRoomMessage(currentUser.uid!!, chatRoomKey, message) {
        }
    }

    private fun setupRecycler() {
        //binding.rvChatContent.layoutManager = LinearLayoutManager(this)
        //binding.rvChatContent.adapter = chatAdapter
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
