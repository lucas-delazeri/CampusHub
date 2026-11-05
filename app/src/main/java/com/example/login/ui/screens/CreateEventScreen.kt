package com.example.login.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.login.data.model.Event
import com.example.login.data.utils.ImageUtils
import com.example.login.ui.components.MapsActivity

@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    eventToEdit: Event? = null,
    onCreateEvent: (title: String, description: String, date: String, location: String, category: String, lat: Double?, lng: Double?, maxCapacity: Int, imageBase64: String?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(eventToEdit?.title ?: "") }
    var category by remember { mutableStateOf(eventToEdit?.category ?: "") }
    var date by remember { mutableStateOf(eventToEdit?.date ?: "") }
    var location by remember { mutableStateOf(eventToEdit?.location ?: "") }
    var description by remember { mutableStateOf(eventToEdit?.description ?: "") }
    var selectedLat by remember { mutableStateOf<Double?>(eventToEdit?.latitude) }
    var selectedLng by remember { mutableStateOf<Double?>(eventToEdit?.longitude) }
    var maxCapacity by remember { mutableStateOf(if ((eventToEdit?.maxCapacity ?: 0) > 0) eventToEdit!!.maxCapacity.toString() else "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBase64 by remember { mutableStateOf<String?>(eventToEdit?.imageBase64) }

    val isEditMode = eventToEdit != null

    val mapPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val lat = result.data?.getDoubleExtra("selected_lat", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("selected_lng", 0.0) ?: 0.0
            if (lat != 0.0 && lng != 0.0) {
                selectedLat = lat
                selectedLng = lng
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imageBase64 = ImageUtils.uriToBase64(context, uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditMode) "Editar Evento" else "Informações do Evento",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F93FF)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Evento") },
                    placeholder = { Text("Ex: Feira de Tecnologia 2026") },
                    leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF0F93FF)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0F93FF),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = maxCapacity,
                    onValueChange = { new ->
                        if (new.all { it.isDigit() }) maxCapacity = new
                    },
                    label = { Text("Capacidade Máxima") },
                    placeholder = { Text("Ex: 100") },
                    leadingIcon = { Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF0F93FF)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0F93FF),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria") },
                    placeholder = { Text("Ex: Tecnologia, Cultura, Carreira") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF0F93FF)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0F93FF),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data e Horário") },
                    placeholder = { Text("Ex: 20 de Outubro, 14:00") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF0F93FF)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0F93FF),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Nome do Local / Endereço") },
                    placeholder = { Text("Ex: Praça da Sé ou Auditório B") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0F93FF)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0F93FF),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )

                // Button to select event image
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Color(0xFF0F93FF)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedImageUri != null || imageBase64 != null) "✓ Imagem Selecionada" else "Selecionar Imagem do Evento",
                        color = Color(0xFF0F93FF),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Image preview if selected
                if (selectedImageUri != null || !imageBase64.isNullOrBlank()) {
                    val imageBitmap = remember(selectedImageUri, imageBase64) {
                        if (selectedImageUri != null) {
                            try {
                                context.contentResolver.openInputStream(selectedImageUri!!)?.use { stream ->
                                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                                }
                            } catch (_: Exception) { null }
                        } else if (!imageBase64.isNullOrBlank()) {
                            try {
                                val clean = if (imageBase64!!.contains(",")) imageBase64!!.substringAfter(",") else imageBase64!!
                                val bytes = Base64.decode(clean.trim(), Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } catch (_: Exception) { null }
                        } else null
                    }
                    if (imageBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "Pré-visualização da imagem do evento",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                // Button to open interactive map picker
                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, MapsActivity::class.java).apply {
                            putExtra("picker_mode", true)
                            if (selectedLat != null && selectedLng != null) {
                                putExtra("event_lat", selectedLat)
                                putExtra("event_lng", selectedLng)
                            }
                        }
                        mapPickerLauncher.launch(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = Color(0xFF0F93FF)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedLat != null && selectedLng != null) "✓ Ponto Selecionado no Mapa" else "Escolher Ponto no Mapa Interativo",
                        color = Color(0xFF0F93FF),
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição Completa") },
                    placeholder = { Text("Descreva as principais atrações e tópicos do evento...") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF0F93FF)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0F93FF),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    minLines = 4
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onCreateEvent(
                            title, description, date, location, category,
                            selectedLat, selectedLng,
                            maxCapacity.toIntOrNull() ?: 0,
                            imageBase64
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F93FF))
                ) {
                    Text(
                        text = if (isEditMode) "Salvar Alterações" else "Publicar Evento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
