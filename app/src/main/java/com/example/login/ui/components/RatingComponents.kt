package com.example.login.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun StarRatingBar(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 20.dp,
    interactive: Boolean = false,
    onRatingSelected: ((Int) -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..5) {
            val icon = when {
                i <= rating -> Icons.Default.Star
                i - 0.5 <= rating -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Default.StarBorder
            }
            val tint = if (i <= rating || i - 0.5 <= rating) Color(0xFFFFB800) else Color(0xFFCBD5E1)

            Icon(
                imageVector = icon,
                contentDescription = "Estrela $i",
                tint = tint,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (interactive && onRatingSelected != null) {
                            Modifier.clickable { onRatingSelected(i) }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Composable
fun EventRatingSummaryCard(
    averageRating: Double,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Média de Avaliações",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (totalCount > 0) String.format(Locale.getDefault(), "%.1f", averageRating) else "—",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        StarRatingBar(rating = averageRating, starSize = 18.dp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (totalCount == 1) "1 avaliação" else if (totalCount > 1) "$totalCount avaliações" else "Sem avaliações",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentEventRatingCard(
    isEnrolled: Boolean,
    isEnded: Boolean,
    userRating: Int?,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnrolled && isEnded) Color(0xFFFFFBEB) else Color(0xFFF8FAFC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Sua Avaliação do Evento",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                !isEnrolled -> {
                    Text(
                        text = "Apenas alunos inscritos neste evento podem avaliá-lo após o encerramento.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                !isEnded -> {
                    Text(
                        text = "A avaliação estará disponível assim que o evento for encerrado.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                else -> {
                    Text(
                        text = if (userRating != null) "Toque nas estrelas para alterar sua nota:" else "Toque para dar uma nota de 1 a 5 estrelas:",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        StarRatingBar(
                            rating = (userRating ?: 0).toDouble(),
                            starSize = 36.dp,
                            interactive = true,
                            onRatingSelected = onRatingSelected
                        )
                    }
                }
            }
        }
    }
}
