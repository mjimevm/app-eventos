package plat.proyecto.guatevivo.core

import androidx.annotation.DrawableRes
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.paginas.home.ui.home.CarouselItem
import plat.proyecto.guatevivo.paginas.home.ui.home.ProximamenteItem
import plat.proyecto.guatevivo.paginas.event.ui.info.EventInfoState

data class EventEntity(
    val id: Int,
    val title: String,
    val fecha: String,
    val dia: Int,
    val ubicacion: String,
    @field:DrawableRes val imageResId: Int,
    val cantidadAmigos: Int = 0,
    val miniDescripcion: String,
    val precio: String,
    val descripcionLarga: String
) {
    val titulo: String get() = title

    fun toProximamenteItem(): ProximamenteItem {
        return ProximamenteItem(
            title = title,
            fecha = fecha,
            dia = dia,
            ubicacion = ubicacion,
            imageResId = imageResId,
            cantidadAmigos = cantidadAmigos,
            id = id
        )
    }

    fun toCarouselItem(): CarouselItem {
        return CarouselItem(
            id = id,
            imageResId = imageResId,
            titulo = title,
            ubicacion = ubicacion
        )
    }

    fun toEventInfoState(): EventInfoState {
        return EventInfoState(
            titulo = title,
            miniDescripcion = miniDescripcion,
            dia = dia.toString(),
            mes = fecha,
            ubicacion = ubicacion,
            precio = precio,
            descripcionLarga = descripcionLarga,
            imageResId = imageResId
        )
    }
}

object EventosDb {
    val eventosList = listOf(
        EventEntity(
            id = 0,
            title = "Festival de las Flores",
            fecha = "NOV",
            dia = 1,
            ubicacion = "Antigua Guatemala",
            imageResId = R.drawable.festival,
            cantidadAmigos = 2,
            miniDescripcion = "La celebración anual más grande del arte y la música en la ciudad colonial.",
            precio = "Entrada Gratis",
            descripcionLarga = "El Festival de las Flores es un evento cultural y artístico anual que transforma por completo las calles coloniales de La Antigua Guatemala en un gigantesco lienzo botánico. Durante un fin de semana, la arquitectura barroca de la ciudad se fusiona con miles de colores y aromas naturales, atrayendo a miles de visitantes locales e internacionales."
        ),
        EventEntity(
            id = 1,
            title = "Feria de Noviembre",
            fecha = "NOV",
            dia = 20,
            ubicacion = "Ciudad de Guatemala",
            imageResId = R.drawable.feria,
            cantidadAmigos = 1,
            miniDescripcion = "Tradicional feria llena de juegos mecánicos, comida típica y ambiente familiar.",
            precio = "Q20.00 por persona",
            descripcionLarga = "La Feria de Noviembre reúne lo mejor de las tradiciones guatemaltecas en la capital. Disfruta de atol de elote, churros, juegos mecánicos y espectáculos musicales para toda la familia."
        ),
        EventEntity(
            id = 2,
            title = "Cena de Café en Atitlán",
            fecha = "SEP",
            dia = 23,
            ubicacion = "Panajachel, Atitlán",
            imageResId = R.drawable.panajachel,
            cantidadAmigos = 2,
            miniDescripcion = "Experiencia culinaria con cata de los mejores cafés de la región junto al lago.",
            precio = "Q150.00",
            descripcionLarga = "Una cena exclusiva maridada con los mejores granos de café de altura producidos alrededor del Lago de Atitlán. Guiada por baristas expertos y chefs locales en un entorno mágico frente al lago."
        )
    )

    val proximamenteItems: List<ProximamenteItem>
        get() = eventosList.map { it.toProximamenteItem() }

    val destacadosList: List<CarouselItem>
        get() = eventosList.map { it.toCarouselItem() }

    fun getEventInfoState(id: Int): EventInfoState {
        val event = eventosList.find { it.id == id } ?: eventosList.first()
        return event.toEventInfoState()
    }
}
