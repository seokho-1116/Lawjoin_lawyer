package com.example.lawjoin.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lawjoin.counselreservation.CounselReservationViewModel
import com.example.lawjoin.lawyer.LawyerListViewModel
import com.example.lawjoin.lawyerdetail.LawyerDetailViewModel
import com.example.lawjoin.post.PostDetailViewModel

class ViewModelFactory() : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(LawyerDetailViewModel::class.java)) {
            LawyerDetailViewModel() as T
        } else if (modelClass.isAssignableFrom(CounselReservationViewModel::class.java)) {
            CounselReservationViewModel() as T
        } else if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            PostDetailViewModel() as T
        } else if (modelClass.isAssignableFrom(LawyerListViewModel::class.java)){
            LawyerListViewModel() as T
        }else {
            throw IllegalArgumentException()
        }
    }
}