package com.example.newsreportingapp.Fragments

import com.google.firebase.Timestamp

data class Post(
    val content: String = "",
    val createdAt: Timestamp? = null,
    val image: String = "",
    var status: String = "",
    val rating: Int = 0,
    val reporter: String = "",
    val feedback: String = "",
    val topic: String = ""
) {
    // Corrected no-argument constructor
    @JvmOverloads
    constructor() : this("", null, "", "", 0, "", "", "")
}