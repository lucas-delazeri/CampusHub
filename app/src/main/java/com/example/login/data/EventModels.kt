package com.example.login.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val organizer: String,
    val category: String
)

object EventRepository {
    val allEvents = listOf(
        Event(
            id = 1,
            title = "II Feira de Profissões e Carreiras",
            description = "Conecte-se com grandes empresas do mercado, participe de workshops de currículo e descubra novas oportunidades de estágio e emprego no ecossistema universitário.",
            date = "24 de Setembro, 14:00",
            location = "Auditório Central - Bloco A",
            organizer = "Diretório Acadêmico",
            category = "Carreira"
        ),
        Event(
            id = 2,
            title = "Hackathon CampusHub 2026",
            description = "Uma maratona de 48 horas de programação, ideação e design. Venha desenvolver soluções tecnológicas inovadoras para melhorar a vida estudantil no campus.",
            date = "02 a 04 de Outubro, 18:00",
            location = "Laboratório de Informática Avançada",
            organizer = "Faculdade de Computação",
            category = "Tecnologia"
        ),
        Event(
            id = 3,
            title = "Workshop de Introdução à Inteligência Artificial",
            description = "Aprenda os conceitos fundamentais de Machine Learning e Redes Neurais na prática. Traga seu notebook e prepare-se para codificar seus primeiros modelos.",
            date = "10 de Outubro, 09:00",
            location = "Sala 102 - Bloco de Engenharia",
            organizer = "IEEE Student Branch",
            category = "Tecnologia"
        ),
        Event(
            id = 4,
            title = "Festival Universitário de Música e Cultura",
            description = "Apresentações musicais com bandas formadas por alunos, praça de alimentação com food trucks, exposições de artes visuais e muito mais para integrar a comunidade.",
            date = "15 de Outubro, 17:00",
            location = "Praça de Convivência Externa",
            organizer = "Secretaria de Cultura Estudantil",
            category = "Cultura"
        ),
        Event(
            id = 5,
            title = "Palestra: Saúde Mental na Vida Acadêmica",
            description = "Estratégias fundamentais de mindfulness, gestão de tempo e inteligência emotional para lidar com o estresse dos exames e manter o equilíbrio na rotina universitária.",
            date = "22 de Outubro, 10:30",
            location = "Anfiteatro da Biblioteca Central",
            organizer = "Departamento de Psicologia",
            category = "Saúde"
        )
    )

    var enrolledEventIds by mutableStateOf(setOf<Int>())
        private set

    fun enroll(eventId: Int) {
        enrolledEventIds = enrolledEventIds + eventId
    }

    fun unenroll(eventId: Int) {
        enrolledEventIds = enrolledEventIds - eventId
    }

    fun isEnrolled(eventId: Int): Boolean {
        return enrolledEventIds.contains(eventId)
    }
}
