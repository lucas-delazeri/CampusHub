package com.example.login.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.data.model.Event
import com.example.login.data.repository.EventRepository
import com.example.login.ui.components.EventCard

@Composable
fun EventsListTab(onEventClick: (Event) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("todos") }
    val filteredEvents = EventRepository.allEvents.filter { event ->
        val matchesSearch = searchQuery.isEmpty() ||
                event.title.contains(searchQuery, ignoreCase = true) ||
                event.description.contains(searchQuery, ignoreCase = true) ||
                event.category.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "proximos" -> !event.isEnded
            "encerrados" -> event.isEnded
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Pesquisar eventos, categorias...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF0F93FF)
            ),
            singleLine = true
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = { selectedFilter = "todos" },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFilter == "todos") Color(0xFF0F93FF) else Color.LightGray
                )
            ) {
                Text(
                    text = "Todos",
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Button(
                onClick = { selectedFilter = "proximos" },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFilter == "proximos") Color(0xFF0F93FF) else Color.LightGray
                )
            ) {
                Text(
                    text = "Próximos",
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Button(
                onClick = { selectedFilter = "encerrados" },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFilter == "encerrados") Color(0xFF0F93FF) else Color.LightGray
                )
            ) {
                Text(
                    text = "Encerrados",
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }

        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum evento encontrado.", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEvents) { event ->
                    EventCard(event = event, onClick = { onEventClick(event) })
                }
            }
        }
    }
}
