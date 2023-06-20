package com.example.lawjoin_lawyer.post

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.lawjoin_lawyer.data.model.Comment
import com.example.lawjoin_lawyer.databinding.CommentItemBinding
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class FreePostAdapter(private val comments: MutableList<Comment>) :
    RecyclerView.Adapter<FreePostAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: CommentItemBinding)
        : RecyclerView.ViewHolder(itemView.root) {
        private val writer = itemView.tvCommentWriter
        private val detail = itemView.tvCommentDetail
        private val time = itemView.tvCommentWriteTime

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(comment: Comment) {
            writer.text = comment.owner
            detail.text = comment.detail
            val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
            val zonedDateTime = ZonedDateTime.parse(comment.createTime, formatter).withZoneSameInstant(ZoneId.systemDefault())
            val writeTime = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"))
            time.text = writeTime.toString()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CommentItemBinding.inflate(inflater, parent, false)
        return CommentViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    fun setComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}