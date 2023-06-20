package com.example.lawjoin_lawyer.reservation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lawjoin_lawyer.MainActivity
import com.example.lawjoin_lawyer.R
import com.example.lawjoin_lawyer.common.AuthUtils
import com.example.lawjoin_lawyer.data.model.AuthUserDto
import com.example.lawjoin_lawyer.data.model.CounselReservation
import com.example.lawjoin_lawyer.databinding.ActivityCounselReservationBinding
import com.example.lawjoin_lawyer.post.BoardFreeActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@RequiresApi(Build.VERSION_CODES.O)
class CounselReservationActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CounselReservationAdapter
    private lateinit var databaseRef: DatabaseReference
    private lateinit var reservationsListener: ValueEventListener
    private lateinit var currentUser: AuthUserDto
    private lateinit var binding: ActivityCounselReservationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCounselReservationBinding.inflate(layoutInflater)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        AuthUtils.getCurrentUser { authUserDto, _ ->
            currentUser = authUserDto!!
        }

        adapter = CounselReservationAdapter(mutableListOf()) // 빈 리스트로 초기화
        recyclerView.adapter = adapter

        databaseRef = FirebaseDatabase.getInstance().reference
            .child("reservations")
            .child("reservation")

        reservationsListener = object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val reservations = mutableListOf<CounselReservation>()

                for (reservationSnapshot in snapshot.children) {
                    val reservation = reservationSnapshot.getValue(CounselReservation::class.java)
                    if (reservation!!.lawyerId == currentUser.uid) {
                        reservation.let {
                            reservations.add(it)
                        }
                    }
                }

                adapter.setData(reservations) // 어댑터에 데이터 설정
            }

            override fun onCancelled(error: DatabaseError) {
                // 데이터 조회 취소 시 처리할 내용
            }
        }

        databaseRef.addValueEventListener(reservationsListener)

        initializeListener()
        setContentView(binding.root)
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

        binding.ibMainReservation.isClickable = false
        binding.ibMainReservation.isSelected = true
    }

    override fun onDestroy() {
        super.onDestroy()

        databaseRef.removeEventListener(reservationsListener)
    }
}