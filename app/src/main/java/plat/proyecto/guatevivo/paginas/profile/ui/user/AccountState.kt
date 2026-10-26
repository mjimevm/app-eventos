package plat.proyecto.guatevivo.paginas.profile.ui.user

import plat.proyecto.guatevivo.R

data class AccountState(
    val usuario: String = "Alejandro Morales",
    val correo: String = "alejandro.morales@guatevivo.plat",
    val imagenPerfil: Int = R.drawable.account_circle,
    val listaAsistidos: List<String> = listOf("Festival de Barriletes Gigantes", "Cena de Café en Atitlán"),
    val listaCreados: List<String> = listOf("Concierto Acústico Antigua"),
    val listaCompartidos: List<String> = listOf("Feria Gastronómica Chapina"),
    val isLoading: Boolean = false
)
