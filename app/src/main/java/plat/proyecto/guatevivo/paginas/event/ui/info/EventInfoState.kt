package plat.proyecto.guatevivo.paginas.event.ui.info

import androidx.annotation.DrawableRes
import plat.proyecto.guatevivo.R

data class EventInfoState(
    val titulo: String = "Festival de las Flores",
    val miniDescripcion: String = "La celebración anual más grande del arte y la música en la ciudad colonial.",
    val dia: String = "14 y 15",
    val mes: String = "Noviembre",
    val ubicacion: String = "Calle del Arco, Antigua Guatemala",
    val precio: String = "Entrada Gratis",
    val descripcionLarga: String = "El Festival de las Flores es un evento cultural y artístico anual que transforma por completo las calles coloniales de La Antigua Guatemala en un gigantesco lienzo botánico. Durante un fin de semana, la arquitectura barroca de la ciudad se fusiona con miles de colores y aromas naturales, atrayendo a miles de visitantes locales e internacionales.",
    @field:DrawableRes val imageResId: Int = R.drawable.festival,
    val isAttending: Boolean = false,
    val amigosAsistiran: List<Int> = listOf(R.drawable.account_circle, R.drawable.account_circle, R.drawable.account_circle),
    val isLoading: Boolean = false,
    val successMessage: String? = null
)
