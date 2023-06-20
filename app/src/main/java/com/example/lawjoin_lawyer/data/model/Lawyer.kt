package com.example.lawjoin_lawyer.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
data class Lawyer(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileUrl: String = "",
    val office: LawyerOffice = LawyerOffice(
        "", "", ZonedDateTime.now().toString(), ZonedDateTime.now().toString(), ""
    ),
    val career: List<String> = listOf(),
    val counselReviewList: MutableList<CounselReview> = mutableListOf(),
    val counselCaseList: MutableList<Post> = mutableListOf(),
    val introduce: String = "",
    val categories: List<String> = listOf(),
    val certificate: List<String> = listOf(),
    val basicCounselTime: Long = 30L,
    val unavailableTime: List<String> = listOf(),
    val reviewCount: Int = 0,
    val likeCount: Int = 0
) : Serializable{

    fun toData(): CharSequence {
        return "변호사 이름: $name\n법률 사무소 이름: ${office.name}\n" +
                "법률 사무소 전화번호: ${office.phone}\n법률 사무소 위치${office.location}" +
                "법률 사무소 운영시간: ${office.openingTime} ~ ${office.closingTime}"
    }
}