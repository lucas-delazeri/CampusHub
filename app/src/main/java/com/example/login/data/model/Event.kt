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
    val imageBase64: String? = null,
    val enrolledUserIds: List<String> = emptyList(),
    val waitlistUserIds: List<String> = emptyList(),
    val pendingConfirmationUserIds: List<String> = emptyList()
) {
    val currentEnrolledCount: Int
        get() = enrolledUserIds.size

    val currentOccupiedCount: Int
        get() = enrolledUserIds.size + pendingConfirmationUserIds.size

    val isFull: Boolean
        get() = maxCapacity > 0 && currentOccupiedCount >= maxCapacity

    val remainingSpots: Int
        get() = if (maxCapacity > 0) maxOf(0, maxCapacity - currentOccupiedCount) else Int.MAX_VALUE
}
