package com.example.newsreportingapp

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsreportingapp.Fragments.Post
import com.example.newsreportingapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsApprovalAdapter(
    private val newsList: MutableList<Post>
) : RecyclerView.Adapter<NewsApprovalAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val newsImage: ImageView = view.findViewById(R.id.newsImages)
        val newsTitle: TextView = view.findViewById(R.id.newsTitles)
        val newsDescription: TextView = view.findViewById(R.id.newsDescriptions)
        val newsReporter: TextView = view.findViewById(R.id.newsReporters)
        val newsTimestamp: TextView = view.findViewById(R.id.newsTimestamps)
        val newsFeedbacks: TextView = view.findViewById(R.id.newsFeedbacks)
        val newsRatings: TextView = view.findViewById(R.id.newsRatings)
        val approvalStatus: TextView = view.findViewById(R.id.approvalStatuss)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_approve_list, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val post = newsList[position]

        // Decode Base64 image safely
        if (!post.image.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(post.image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Glide.with(holder.itemView.context)
                    .load(bitmap)
                    .into(holder.newsImage)
            } catch (e: Exception) {
                holder.newsImage.setImageResource(R.drawable.ic_image) // Fallback image
            }
        } else {
            holder.newsImage.setImageResource(R.drawable.ic_image) // Fallback image
        }

        // Set text values safely
        holder.newsTitle.text = post.topic ?: "No Title"
        holder.newsDescription.text = post.content ?: "No Description"
        holder.newsReporter.text = "Reporter: ${post.reporter ?: "Unknown"}"
        holder.newsFeedbacks.text = "Feedback: ${post.feedback ?: "No Feedback"}"

        // Set star rating
        val rating = post.rating?.toInt() ?: 0 // Ensure rating is an integer
        val stars = "★ ".repeat(rating) + "☆ ".repeat(5 - rating) // Generate star rating
        holder.newsRatings.text = stars

        // Set timestamp
        try {
            val timestampMillis = post.createdAt?.seconds?.times(1000) ?: 0L
            if (timestampMillis > 0) {
                val date = Date(timestampMillis)
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                holder.newsTimestamp.text = sdf.format(date)
            } else {
                holder.newsTimestamp.text = "Time Unavailable"
            }
        } catch (e: Exception) {
            holder.newsTimestamp.text = "Time Error"
        }

        holder.approvalStatus.text = post.status ?: "Pending"

        // Set text color based on status
        val color = when (post.status) {
            "Approved" -> R.color.green  // Green for Approved
            "rejected" -> R.color.red    // Red for Rejected
            else -> R.color.orange       // Default Orange for Pending
        }

        holder.approvalStatus.setTextColor(holder.itemView.context.getColor(color))
    }


    override fun getItemCount() = newsList.size

    fun updateNewsList(newList: List<Post>) {
        newsList.clear()
        newsList.addAll(newList)
        notifyDataSetChanged()
    }
}