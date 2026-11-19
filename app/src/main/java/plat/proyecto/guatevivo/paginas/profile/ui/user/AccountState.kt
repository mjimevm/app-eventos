package plat.proyecto.guatevivo.paginas.profile.ui.user

import plat.proyecto.guatevivo.paginas.home.ui.home.ProximamenteItem

data class AccountState(
    val usuario: String = "",
    val correo: String = "",
    val imagenPerfil: Int? = null,
    val listaAsistidos: List<ProximamenteItem> = emptyList(),
    val listaCreados: List<ProximamenteItem> = emptyList(),
    val listaCompartidos: List<ProximamenteItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
