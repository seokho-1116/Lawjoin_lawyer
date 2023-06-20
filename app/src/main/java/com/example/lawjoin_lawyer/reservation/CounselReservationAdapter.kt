package com.example.lawjoin_lawyer.reservation

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.lawjoin_lawyer.R
import com.example.lawjoin_lawyer.data.model.CounselReservation
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CounselReservationAdapter(private val reservations: MutableList<CounselReservation>) :
    RecyclerView.Adapter<CounselReservationAdapter.ReservationViewHolder>() {

    inner class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val startTimeTextView: TextView = itemView.findViewById(R.id.startTimeTextView)
        private val userIdTextView: TextView = itemView.findViewById(R.id.userIdTextView)
        private val summaryTextView: TextView = itemView.findViewById(R.id.summaryTextView)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(reservation: CounselReservation) {
            val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
            val zonedDateTime = ZonedDateTime.parse(reservation.startTime, formatter).withZoneSameInstant(
                ZoneId.systemDefault())
            val formattedTime = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"))
            startTimeTextView.text = formattedTime
            userIdTextView.text = reservation.userName
            summaryTextView.text = reservation.summary
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ReservationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.bind(reservation)
    }

    override fun getItemCount(): Int {
        return reservations.size
    }

    fun setData(new: List<CounselReservation>) {
        reservations.clear()
        reservations.addAll(new)
        notifyDataSetChanged()
    }
}
