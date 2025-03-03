package com.example.newsreportingapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.newsreportingapp.R
import java.text.SimpleDateFormat
import java.util.*

class NewsDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_news_details)

        val newsImage: ImageView = findViewById(R.id.newsImageFull)
        val newsTitle: TextView = findViewById(R.id.newsTitleFull)
        val newsTimestamp: TextView = findViewById(R.id.newsTimestampFull)
        val newsReporter: TextView = findViewById(R.id.newsReporterFull)
        val newsContent: TextView = findViewById(R.id.newsContentFull)

        // Get data from intent using corrected keys
        val title = intent.getStringExtra("title") ?: "No Title"
        val content = intent.getStringExtra("content") ?: "No Content"
        val reporter = intent.getStringExtra("reporter") ?: "Unknown"
        val timestampMillis = intent.getLongExtra("timestamp", 0L)
        val imageBase64 = intent.getStringExtra("image")

        newsTitle.text = title
        newsContent.text = content
        newsReporter.text = "Reporter: $reporter"

        // Convert timestamp to date format
        val formattedDate = if (timestampMillis > 0) {
            val date = Date(timestampMillis)
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(date)
        } else {
            "Time Unavailable"
        }
        newsTimestamp.text = "Published: $formattedDate"

        // Load Image
        if (!imageBase64.isNullOrEmpty()) {
            val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            Glide.with(this).load(bitmap).into(newsImage)
        } else {
            Glide.with(this).load(R.drawable.ic_image).into(newsImage)
        }

        val backButton: ImageView = findViewById(R.id.toolbar_back_button)
        backButton.setOnClickListener {
            finish() // Finishes the current activity and returns to the previous one
        }

    }
}