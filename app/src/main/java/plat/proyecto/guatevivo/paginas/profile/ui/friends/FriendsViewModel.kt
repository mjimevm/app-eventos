package plat.proyecto.guatevivo.paginas.profile.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.proyecto.guatevivo.GuateVivoApp
import plat.proyecto.guatevivo.core.repository.FriendsRepository
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*

class FriendsViewModel(
    private val repository: FriendsRepository,
    private val profileUid: String? = null
) : ViewModel() {
    companion object {
        fun factory(profileUid: String? = null) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as GuateVivoApp
                return FriendsViewModel(app.friendsRepository, profileUid) as T
            }
        }
    }
    private val _state = MutableStateFlow(FriendsState())
    val state = _state.asStateFlow()
    private val isBusy get() = _state.value.let { it.isLoading || it.isSending || it.processingUid != null }
    private var hasResumed = false
    private var refreshPending = false

    init { refresh() }

    fun onResume() {
        if (hasResumed) {
            if (isBusy) refreshPending = true else refresh()
        }
        hasResumed = true
    }

    private suspend fun loadOverview() {
        val data = repository.load()
        _state.update { it.copy(uid = data.uid, relationships = data.relationships, profiles = data.profiles,
            error = null) }
    }

    fun refresh() {
        if (isBusy) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                if (profileUid != null) {
                    val profile = repository.getProfile(profileUid) ?: error("Este perfil ya no está disponible")
                    _state.update { it.copy(openedProfile = profile) }
                } else loadOverview()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update { it.copy(relationships = emptyList(), profiles = emptyMap(), openedProfile = null,
                    error = (if (profileUid == null) "No se pudieron cargar los amigos. " else "No se pudo cargar el perfil. ") + friendsFailureMessage(e)) }
            } finally { _state.update { it.copy(isLoading = false) } }
            finishPendingRefresh()
        }
    }

    private fun finishPendingRefresh() {
        if (refreshPending) { refreshPending = false; refresh() }
    }

    private fun sendRequest() {
        if (isBusy) return
        val email = normalizeFriendEmail(_state.value.email)
        if (!isValidFriendEmail(email)) {
            _state.update { it.copy(sendOutcome = null, sendError = "Introduce un correo electrónico válido") }
            return
        }
        _state.update { it.copy(email = email, isSending = true, sendOutcome = null, sendError = null) }
        viewModelScope.launch {
            var serverConfirmed = false
            try {
                val outcome = repository.send(email)
                serverConfirmed = true
                loadOverview()
                _state.update { it.copy(sendOutcome = outcome) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val context = if (serverConfirmed) "El servidor respondió, pero no se pudo actualizar la lista. " else "No se pudo enviar la solicitud. "
                _state.update { it.copy(sendOutcome = null, sendError = context + friendsFailureMessage(e)) }
            } finally { _state.update { it.copy(isSending = false) } }
            finishPendingRefresh()
        }
    }

    private fun mutate(uid: String, operation: suspend () -> Unit) {
        if (isBusy) return
        _state.update { it.copy(processingUid = uid, error = null) }
        viewModelScope.launch {
            try {
                operation()
                _state.update { it.copy(removeUid = null) }
                loadOverview()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update { it.copy(error = "No se pudo completar la acción. ${friendsFailureMessage(e)}") }
            } finally { _state.update { it.copy(processingUid = null) } }
            finishPendingRefresh()
        }
    }

    fun onEvent(event: FriendsEvent) {
        when (event) {
            is FriendsEvent.UpdateEmail -> if (!_state.value.isSending) {
                _state.update { it.copy(email = event.email.take(320), sendOutcome = null, sendError = null) }
            }
            FriendsEvent.SendRequest -> sendRequest()
            FriendsEvent.Refresh -> refresh()
            is FriendsEvent.SelectTab -> _state.update { it.copy(selectedTab = event.index.coerceIn(0, 1)) }
            is FriendsEvent.Accept, is FriendsEvent.Reject, is FriendsEvent.Cancel -> {
                val id = when (event) { is FriendsEvent.Accept -> event.requestId; is FriendsEvent.Reject -> event.requestId; is FriendsEvent.Cancel -> event.requestId }
                val request = _state.value.relationships.firstOrNull { it.id == id && it.status == "PENDING" } ?: return
                if (event is FriendsEvent.Cancel && request.senderUid != _state.value.uid) return
                if (event !is FriendsEvent.Cancel && request.receiverUid != _state.value.uid) return
                mutate(_state.value.otherUid(request)) {
                    when (event) { is FriendsEvent.Accept -> repository.accept(id); is FriendsEvent.Reject -> repository.reject(id); else -> repository.cancel(id) }
                }
            }
            is FriendsEvent.Remove -> if (_state.value.relationship(event.uid)?.status == "ACCEPTED") _state.update { it.copy(removeUid = event.uid, error = null) }
            FriendsEvent.CancelRemove -> if (_state.value.processingUid == null) _state.update { it.copy(removeUid = null) }
            FriendsEvent.ConfirmRemove -> _state.value.removeUid?.let { uid -> mutate(uid) { repository.remove(uid) } }
            FriendsEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }
}
