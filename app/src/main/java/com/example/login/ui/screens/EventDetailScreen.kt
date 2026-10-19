package com.example.login.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.login.data.model.Comment
import com.example.login.data.model.Event
import com.example.login.data.repository.EventRepository
import com.example.login.ui.components.EventRatingSummaryCard
import com.example.login.ui.components.MapsActivity
import com.example.login.ui.components.StudentEventRatingCard
import java.io.File
import android.util.Base64

@Composable
fun EventDetailScreen(
    event: Event,
    isEnrolled: Boolean,
    currentUserId: String?,
    onToggleEnroll: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddComment: (String, Int?, Uri?) -> Unit,
    onEditComment: (String, String, Int?) -> Unit,
    onDeleteComment: (String) -> Unit,
    onRateEvent: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newCommentText by remember { mutableStateOf("") }
    var commentToEdit by remember { mutableStateOf<Comment?>(null) }
    var editText by remember { mutableStateOf("") }
    var editRating by remember { mutableIntStateOf(0) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }

    var averageRating by remember { mutableDoubleStateOf(0.0) }
    var totalRatings by remember { mutableIntStateOf(0) }
    var userRating by remember { mutableStateOf<Int?>(null) }
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var tempUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoUri = tempUri
    }

    fun launchCamera() {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "EventImg${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        tempUri = uri
        takePictureLauncher.launch(uri)
    }

    DisposableEffect(event.id) {
        val listener = EventRepository.listenToComments(event.id) { updatedList ->
            comments = updatedList
        }
        onDispose {
            EventRepository.removeCommentsListener(event.id, listener)
        }
    }

    DisposableEffect(event.id) {
        val ratingsListener = EventRepository.listenToRatings(event.id) { avg, count, mine ->
            averageRating = avg
            totalRatings = count
            userRating = mine
        }
        onDispose {
            EventRepository.removeRatingsListener(event.id, ratingsListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Box(
            modifier = Modifier
                .background(Color(0xFFE6F4FF), RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = event.category.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F93FF)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = event.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Organizado por: ${event.organizer}",
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(context, MapsActivity::class.java).apply {
                        putExtra("event_title", event.title)
                        putExtra("event_location", event.location)
                        putExtra("event_lat", event.latitude ?: -23.550520)
                        putExtra("event_lng", event.longitude ?: -46.633308)
                    }
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF0F93FF))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = event.date, fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0F93FF))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = event.location, fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF0F93FF),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.location,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "Toque para abrir o mapa",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        EventRatingSummaryCard(
            averageRating = averageRating,
            totalCount = totalRatings
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sobre o Evento",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = event.description,
            fontSize = 15.sp,
            color = Color.DarkGray,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        val canEnroll = !event.isEnded
        Button(
            onClick = onToggleEnroll,
            enabled = canEnroll,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (event.isEnded) Color.LightGray else if (isEnrolled) Color(0xFFDC3545) else Color(0xFF0F93FF),
                disabledContainerColor = Color.LightGray
            )
        ) {
            Text(
                text = if (event.isEnded) "Evento Encerrado (Inscrições Fechadas)" else if (isEnrolled) "Cancelar Inscrição" else "Inscrever-se no Evento",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onToggleFavorite,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (EventRepository.isFavorite(eventId = event.id)) Color(0xFFDC3545) else Color(0xFF0F93FF)
            )
        ) {
            Text(
                text = if (EventRepository.isFavorite(eventId = event.id)) "Desfavoritar" else "Adicionar aos favoritos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        StudentEventRatingCard(
            isEnrolled = isEnrolled,
            isEnded = event.isEnded,
            userRating = userRating,
            onRatingSelected = { rating ->
                onRateEvent(rating)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.AddComment,
                contentDescription = null,
                tint = Color(0xFF0F93FF)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Comentários (${comments.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentUserId != null) {
            Column(modifier = Modifier.fillMaxWidth()) {
                photoUri?.let { uri ->
                    Box(modifier = Modifier.padding(bottom = 8.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Foto selecionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { photoUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .background(Color(0x99000000), RoundedCornerShape(14.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remover foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Escreva um comentário...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF0F93FF)
                        ),
                        maxLines = 3
                    )
                    IconButton(onClick = { launchCamera() }) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Adicionar foto",
                            tint = Color(0xFF0F93FF)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(newCommentText, userRating, photoUri)
                                newCommentText = ""
                                photoUri = null
                            }
                        },
                        modifier = Modifier
                            .background(Color(0xFF0F93FF), RoundedCornerShape(12.dp))
                            .size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = Color.White
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Faça login para comentar neste evento.",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (comments.isEmpty()) {
            Text(
                text = "Nenhum comentário ainda. Seja o primeiro a comentar!",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                comments.forEach { comment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF0F93FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = comment.authorName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comment.publishedAt,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )

                                    if (comment.authorId == currentUserId) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                commentToEdit = comment
                                                editText = comment.content
                                                editRating = comment.rating ?: 0
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = Color(0xFF0F93FF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { commentToDelete = comment },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir",
                                                tint = Color(0xFFDC3545),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = comment.content,
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )

                            comment.imageBase64?.let { b64 ->
                                val bytes = remember(b64) { Base64.decode(b64, Base64.DEFAULT) }
                                Spacer(modifier = Modifier.height(8.dp))
                                AsyncImage(
                                    model = bytes,
                                    contentDescription = "Foto do comentário",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            comment.rating?.let { rating ->
                                if (rating > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.Star,
                                                contentDescription = "Nota $rating de 5",
                                                tint = if (i <= rating) Color(0xFFFFC107) else Color.LightGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (commentToEdit != null) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { commentToEdit = null },
            title = { Text("Editar Comentário", color = Color(0xFF4A4A4A)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("Comentário") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Nota",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A4A4A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= editRating) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = "$i estrela(s)",
                                tint = if (i <= editRating) Color(0xFFFFC107) else Color.LightGray,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable {
                                        editRating = if (editRating == i) 0 else i
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val comment = commentToEdit
                        if (comment != null && editText.isNotBlank()) {
                            onEditComment(
                                comment.id,
                                editText,
                                if (editRating > 0) editRating else null
                            )
                        }
                        commentToEdit = null
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { commentToEdit = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { commentToDelete = null },
            title = { Text("Excluir Comentário") },
            text = { Text("Tem certeza de que deseja excluir este comentário?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val comment = commentToDelete
                        if (comment != null) {
                            onDeleteComment(comment.id)
                        }
                        commentToDelete = null
                    }
                ) {
                    Text("Excluir", color = Color(0xFFDC3545))
                }
            },
            dismissButton = {
                TextButton(onClick = { commentToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}