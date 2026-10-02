package com.example.login.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.data.model.Event
import com.example.login.data.repository.EventRepository
import com.example.login.ui.components.EventCard

@Composable
fun MyEventsTab(onEventClick: (Event) -> Unit, onUnenroll: (Int) -> Unit) {
    val enrolledEvents = EventRepository.allEvents.filter { EventRepository.isEnrolled(it.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Minhas Inscrições",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (enrolledEvents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Você ainda não se inscreveu em nenhum evento.",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(enrolledEvents) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event) },
                        trailingAction = {
                            IconButton(onClick = { onUnenroll(event.id) }) {
                                Icon(
                                    Icons.Default.Bookmark,
                                    contentDescription = "Cancelar inscrição",
                                    tint = Color(0xFF0F93FF)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
