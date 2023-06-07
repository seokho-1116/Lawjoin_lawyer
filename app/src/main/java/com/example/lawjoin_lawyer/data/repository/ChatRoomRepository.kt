package com.example.lawjoin.data.repository

import android.util.Log
import com.example.lawjoin.data.model.Message
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase

class ChatRoomRepository private constructor() {
    private val databaseReference: DatabaseReference = Firebase.database.getReference("chat_rooms").child("chat_room")

    companion object{
        private val INSTANCE = ChatRoomRepository()

        fun getInstance(): ChatRoomRepository {
            return INSTANCE
        }
    }

    fun findAllChatRoomsByUid(uid: String, callback: (DataSnapshot) -> Unit) {
        databaseReference
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.e("CHAT DATA", "failed to get chat rooms data")
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot)
                }
            })
    }

    fun findUserChatRoomsByKey(uid: String, callback: (DataSnapshot) -> Unit) {
        databaseReference
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.e("CHAT DATA", "failed to get chat rooms data")
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot)
                }
            })
    }

    fun findUserChatRoomByKey(uid: String, chatKey: String, callback: (DataSnapshot) -> Unit) {
        databaseReference
            .child(uid)
            .child(chatKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.e("CHAT DATA", "failed to get chat rooms data")
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot)
                }
            })
    }

    fun findUserInitChatRoom(uid: String, bot: String, gpt:String, callback: (DataSnapshot) -> Unit) {
        databaseReference
            .child(uid)
            .startAt(bot)
            .endAt(gpt)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.e("CHAT DATA", "failed to get chat rooms data")
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot)
                }
            })
    }

    fun updateMessageConfirm(uid: String, chatKey: String, messageKey: String) {
        databaseReference
            .child(uid).child(chatKey).child("messages")
            .child(messageKey).child("confirmed").setValue(true)
    }

    fun saveChatRoomUnder(uid: String, callback: (DatabaseReference) -> Task<Void>) {
        callback(databaseReference.child(uid))
    }

    fun saveChatRoomMessage(uid: String, chatKey: String, message: Message, callback: () -> Unit) {
        databaseReference.child(uid)
            .child(chatKey)
            .child("messages")
            .push()
            .setValue(message)
            .addOnSuccessListener {
                callback()
            }
    }

    fun updateMessages(uid: String,
                       chatKey: String,
                       answerMessages: List<Message>) {
        val reference = databaseReference
            .child("$uid/$chatKey/messages")

        val dataMap = mutableMapOf<String, Message>()
        for (message in answerMessages) {
            val newMessageRef = reference.push()
            val messageId = newMessageRef.key
            if (messageId != null) {
                dataMap[messageId] = message
            }
        }

        reference
            .updateChildren(dataMap as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("ChatRoomRepository", "update successful")
            }
    }
}