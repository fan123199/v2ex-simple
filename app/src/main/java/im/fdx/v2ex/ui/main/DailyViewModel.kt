package im.fdx.v2ex.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.fdx.v2ex.data.network.NetManager
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.utils.extensions.logd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import im.fdx.v2ex.utils.extensions.await

sealed class DailyState {
    object Loading : DailyState()
    data class Ready(val statusText: String, val once: String?) : DailyState() // once is null if already checked
    data class LinkError(val msg: String) : DailyState()
    object Success : DailyState() // Successfully checked in
}

class DailyViewModel : ViewModel() {

    private val _state = MutableStateFlow<DailyState>(DailyState.Loading)
    val state: StateFlow<DailyState> = _state.asStateFlow()

    init {
        loadDaily()
    }

    fun loadDaily() {
        viewModelScope.launch {
            _state.value = DailyState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    NetManager.client.newCall(Request.Builder().url(NetManager.DAILY_CHECK).build()).await()
                }
                if (response.code == 302) {
                    _state.value = DailyState.LinkError("Need Login first")
                    return@launch
                }
                val body = response.body?.string() ?: ""
                val parser = Parser(body)
                val once = parser.parseDailyOnce()
                val status = parser.getDailyCheckStatus()
                
                logd("Daily: once=$once, status=$status")
                
                _state.value = DailyState.Ready(status, once)

            } catch (e: Exception) {
                _state.value = DailyState.LinkError(e.message ?: "Unknown error")
            }
        }
    }

    fun checkIn(once: String) {
        viewModelScope.launch {
            _state.value = DailyState.Loading
            try {
                val url = "${NetManager.DAILY_CHECK}/redeem?once=$once"
                val response = withContext(Dispatchers.IO) {
                    NetManager.client.newCall(Request.Builder().url(url)
                        .header("Referer", NetManager.DAILY_CHECK)
                        .build()).await()
                }
                
                // After check-in, reload or show success
                // Usually it redirects back to /mission/daily
                val body = response.body?.string() ?: ""
                 val parser = Parser(body)
                 val newStatus = parser.getDailyCheckStatus()
                 val newOnce = parser.parseDailyOnce()
                 
                 // If check-in successful, once should be null and status updated
                 _state.value = DailyState.Ready(newStatus, newOnce)

            } catch (e: Exception) {
                _state.value = DailyState.LinkError("Check-in failed: ${e.message}")
            }
        }
    }
}
