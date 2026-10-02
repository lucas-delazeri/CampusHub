package com.example.login.data.model

data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val organizer: String,
    val category: String,
    val isEnded: Boolean = false
)
