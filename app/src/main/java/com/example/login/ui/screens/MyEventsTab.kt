package com.example.login.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun MyEventsTab(
    onEventClick: (Event) -> Unit,
    onUnenroll: (Int) -> Unit,
    onUnfavorite: (Int) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("inscritos") }
    val enrolledEvents = EventRepository.allEvents.filter { EventRepository.isEnrolled(it.id) }
    val favoriteEvents = EventRepository.allEvents.filter { EventRepository.isFavorite(it.id) }
    val currentEvents = if (selectedTab == "inscritos") enrolledEvents else favoriteEvents

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Meus Eventos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = { selectedTab = "inscritos" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == "inscritos") Color(0xFF0F93FF) else Color(0xFFF0F0F0)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Inscrições (${enrolledEvents.size})",
                    color = if (selectedTab == "inscritos") Color.White else Color.DarkGray
                )
            }

            Button(
                onClick = { selectedTab = "favoritos" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == "favoritos") Color(0xFF0F93FF) else Color(0xFFF0F0F0)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Favoritos (${favoriteEvents.size})",
                    color = if (selectedTab == "favoritos") Color.White else Color.DarkGray
                )
            }
        }

        if (currentEvents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == "inscritos") {
                        "Você ainda não se inscreveu em nenhum evento."
                    } else {
                        "Você ainda não favoritou nenhum evento."
                    },
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
                items(currentEvents) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event) },
                        trailingAction = {
                            if (selectedTab == "inscritos") {
                                IconButton(onClick = { onUnenroll(event.id) }) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Cancelar inscrição",
                                        tint = Color(0xFF0F93FF)
                                    )
                                }
                            } else {
                                IconButton(onClick = {
                                    EventRepository.unfavorite(event.id)
                                    onUnfavorite(event.id)
                                }) {
                                    Icon(
                                        Icons.Default.Bookmark,
                                        contentDescription = "Remover dos favoritos",
                                        tint = Color(0xFF0F93FF)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
