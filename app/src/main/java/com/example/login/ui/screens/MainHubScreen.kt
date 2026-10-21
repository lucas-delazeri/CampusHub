package com.example.login.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.data.utils.ImageUtils
import com.example.login.data.repository.EventRepository
import com.example.login.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHubScreen(
    viewModel: HomeViewModel,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val selectedEvent = viewModel.selectedEvent

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (selectedEvent != null || viewModel.isCreatingEvent) {
                        IconButton(onClick = {
                            if (selectedEvent != null) {
                                viewModel.selectedEvent = null
                            } else {
                                viewModel.isCreatingEvent = false
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color.White
                            )
                        }
                    }
                },
                title = {
                    val title = when {
                        selectedEvent != null -> "Detalhes do Evento"
                        viewModel.isCreatingEvent -> "Novo Evento"
                        viewModel.currentTab == 0 -> "CampusHub"
                        viewModel.currentTab == 1 -> "Meus Eventos"
                        else -> "Perfil"
                    }

                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.signOut(onNavigateToLogin)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sair da conta",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF22396F)
                )
            )
        },
        bottomBar = {
            if (selectedEvent == null && !viewModel.isCreatingEvent) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = viewModel.currentTab == 0,
                        onClick = { viewModel.changeTab(0) },
                        icon = { Icon(Icons.Default.Event, contentDescription = "Eventos") },
                        label = { Text("Eventos") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0F93FF),
                            selectedTextColor = Color(0xFF0F93FF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE6F4FF)
                        )
                    )
                    NavigationBarItem(
                        selected = viewModel.currentTab == 1,
                        onClick = { viewModel.changeTab(1) },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Meus Eventos") },
                        label = { Text("Meus Eventos") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0F93FF),
                            selectedTextColor = Color(0xFF0F93FF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE6F4FF)
                        )
                    )
                    NavigationBarItem(
                        selected = viewModel.currentTab == 2,
                        onClick = { viewModel.changeTab(2) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0F93FF),
                            selectedTextColor = Color(0xFF0F93FF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE6F4FF)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7F8FA))
        ) {
            if (selectedEvent != null) {
                EventDetailScreen(
                    event = selectedEvent,
                    onBack = { viewModel.selectedEvent = null },
                    isEnrolled = viewModel.isEventEnrolled(selectedEvent.id),
                    currentUserId = viewModel.currentUserId,
                    onToggleEnroll = {
                        viewModel.toggleEnrollment(selectedEvent) { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onToggleFavorite = {
                        viewModel.toggleFavorite(selectedEvent) { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onAddComment = { content, rating, photoUri, audioUri, videoUri ->
                        val imageBase64 = photoUri?.let { ImageUtils.uriToBase64(context, it) }
                        viewModel.addComment(selectedEvent.id, content, rating, imageBase64, audioUri, videoUri) { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onEditComment = { commentId, newContent, newRating, newAudioUri, newImageUri, newVideoUri ->
                        val imageBase64 = newImageUri?.let { ImageUtils.uriToBase64(context, it) }
                        viewModel.editComment(selectedEvent.id, commentId, newContent, newRating, newAudioUri, imageBase64, newVideoUri) { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDeleteComment = { commentId ->
                        viewModel.deleteComment(selectedEvent.id, commentId) { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRateEvent = { rating ->
                        viewModel.rateEvent(selectedEvent.id, rating) { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else if (viewModel.isCreatingEvent) {
                CreateEventScreen(
                    onBack = { viewModel.isCreatingEvent = false },
                    onCreateEvent = { title, description, date, location, category, lat, lng ->
                        viewModel.createEvent(title, description, date, location, category, lat, lng) { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (message.contains("sucesso", ignoreCase = true)) {
                                viewModel.isCreatingEvent = false
                            }
                        }
                    }
                )
            } else {
                when (viewModel.currentTab) {
                    0 -> EventsListTab(
                        onEventClick = { viewModel.selectedEvent = it },
                        isTeacher = viewModel.isTeacher,
                        onOpenCreateScreen = { viewModel.isCreatingEvent = true }
                    )
                    1 -> MyEventsTab(
                        onEventClick = { viewModel.selectedEvent = it },
                        onUnenroll = { id ->
                            EventRepository.unenroll(id)
                            Toast.makeText(context, "Inscrição cancelada!", Toast.LENGTH_SHORT).show()
                        },
                        onUnfavorite = { _ ->
                            Toast.makeText(context, "Removido dos favoritos!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    2 -> ProfileTab(
                        name = viewModel.userName,
                        email = viewModel.email,
                        userRole = viewModel.userRole,
                        onNameChange = { viewModel.userName = it },
                        onSaveProfile = {
                            viewModel.updateProfile(viewModel.userName) {
                                Toast.makeText(context, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}
