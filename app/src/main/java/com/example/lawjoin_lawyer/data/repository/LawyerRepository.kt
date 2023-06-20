package com.example.lawjoin_lawyer.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.lawjoin_lawyer.data.model.Lawyer
import com.example.lawjoin_lawyer.data.model.LawyerSingUpInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@RequiresApi(Build.VERSION_CODES.O)
class LawyerRepository private constructor() {

    private val databaseReference: DatabaseReference = Firebase.database.reference.child("lawyers")

    companion object{
        private val INSTANCE = LawyerRepository()

        fun getInstance(): LawyerRepository {
            return INSTANCE
        }
    }

    fun findLawyerById(uid: String, callback: (Lawyer?) -> Unit) {
        databaseReference
            .child("lawyer")
            .child(uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lawyer = snapshot.getValue(Lawyer::class.java)
                    callback(lawyer)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseQuery", "Query canceled or encountered an error: ${error.message}")
                }
            })
    }

    fun saveWaitList(lawyer: LawyerSingUpInfo, callback: () -> Unit) {
        databaseReference
            .child("lawyer_wait")
            .child(lawyer.uid).setValue(lawyer).addOnSuccessListener {
                callback()
            }
    }

    fun findWaitLawyerById(uid: String, callback: (Lawyer?) -> Unit) {
        databaseReference
            .child("lawyer_wait")
            .child(uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lawyer = snapshot.getValue(Lawyer::class.java)
                    callback(lawyer)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseQuery", "Query canceled or encountered an error: ${error.message}")
                }
            })
    }
}