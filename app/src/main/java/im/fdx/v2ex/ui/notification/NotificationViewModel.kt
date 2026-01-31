package im.fdx.v2ex.ui.notification

import im.fdx.v2ex.data.model.Data
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.fdx.v2ex.data.model.NotificationModel
import im.fdx.v2ex.data.network.NetManager
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.data.network.vCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

data class NotificationUiState(
    val notifications: List<NotificationModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        _uiState.update { it.copy(isLoading = true) }
        val url = "https://www.v2ex.com/notifications"
        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }

            override fun onResponse(call: Call, response: Response) {
                 if (response.code == 302) {
                     _uiState.update { it.copy(isLoading = false, error = "Login Required") }
                 } else if (response.code == 200) {
                     val body = response.body.string()
                     val list = Parser(body).parseToNotifications()
                     _uiState.update { it.copy(notifications = list, isLoading = false) }
                 } else {
                     _uiState.update { it.copy(isLoading = false, error = "Error ${response.code}") }
                 }
            }
        })
    }
}




