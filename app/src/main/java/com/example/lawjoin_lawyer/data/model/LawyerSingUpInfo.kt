package com.example.lawjoin_lawyer.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
data class LawyerSingUpInfo(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var profileUrl: String = "",
    var office: LawyerOffice = LawyerOffice(
        "", "", ZonedDateTime.now().toString(), ZonedDateTime.now().toString(), ""
    ),
    var career: List<String> = listOf(),
    var counselReviewList: MutableList<CounselReview> = mutableListOf(),
    var counselCaseList: MutableList<Post> = mutableListOf(),
    var introduce: String = "",
    var categories: List<String> = listOf(),
    var certificate: List<String> = listOf(),
    var basicCounselTime: Long = 30L,
    var unavailableTime: List<String> = listOf(),
    var reviewCount: Int = 0,
    var likeCount: Int = 0
) : Serializable{

    fun toData(): CharSequence {
        return "변호사 이름: $name\n법률 사무소 이름: ${office.name}\n" +
                "법률 사무소 전화번호: ${office.phone}\n법률 사무소 위치${office.location}" +
                "법률 사무소 운영시간: ${office.openingTime} ~ ${office.closingTime}"
    }
}