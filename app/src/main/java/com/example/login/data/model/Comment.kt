package com.example.login.data.model

data class Comment(
    val id: String = "",
    val eventId: Int = 0,
    val authorId: String = "",
    val authorName: String = "",
    val content: String = "",
    val publishedAt: String = "",
    val rating: Int? = null
)