package com.example.lawjoin_lawyer.post

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.lawjoin_lawyer.R
import com.example.lawjoin_lawyer.data.model.Post
import com.example.lawjoin_lawyer.databinding.PostItemBinding
import kotlin.collections.ArrayList

class PostAdapter(var posts: List<Post>,
                  var isCounsel: Boolean,
                  var context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    var filteredPostList: List<Post> = listOf()

    inner class ViewHolder(itemView: PostItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        private val title = itemView.tvPostTitle
        fun bind(position: Int) {
            title.text = filteredPostList[position].title
            itemView.setOnClickListener {
                filteredPostList[position].id
                val intent = if (isCounsel) {
                    Intent(context, CounselPostActivity::class.java)
                } else {
                    Intent(context, FreePostActivity::class.java)
                }
                intent.putExtra("isCounsel", isCounsel)
                intent.putExtra("postId", filteredPostList[position].id)
                context.startActivity(intent)
            }
        }
    }

    init {
        this.filteredPostList = posts
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.post_item, viewGroup, false)
        return ViewHolder(PostItemBinding.bind(view))
    }

    override fun onBindViewHolder(viewholder: RecyclerView.ViewHolder, position: Int) {
        (viewholder as ViewHolder).bind(position)
    }

    override fun getItemCount(): Int = filteredPostList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                filteredPostList = if (charString.isEmpty()) {
                    posts
                } else {
                    val filteredList = ArrayList<Post>()
                    for (row in posts) {
                        if (row.title.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredPostList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredPostList = filterResults.values as ArrayList<Post>
                notifyDataSetChanged()
            }
        }
    }
}