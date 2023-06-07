package com.example.lawjoin.data.repository

import com.example.lawjoin.data.model.CounselReservation
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.values
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class CounselReservationRepository private constructor() {
    private val db: FirebaseDatabase = Firebase.database

    companion object{
        private val INSTANCE = CounselReservationRepository()

        fun getInstance(): CounselReservationRepository {
            return INSTANCE
        }
    }

    fun saveCounselReservation(counselReservation: CounselReservation) {
        db.getReference("reservations")
            .child("reservation")
            .child(counselReservation.userId.toString())
            .setValue(counselReservation)
    }
}
