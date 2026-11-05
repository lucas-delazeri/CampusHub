package com.example.login.data.model

data class Event(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val organizer: String = "",
    val category: String = "",
    val isEnded: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val maxCapacity: Int = 0,
    val imageBase64: String? = null
)
