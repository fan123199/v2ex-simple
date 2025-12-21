package im.fdx.v2ex.ui.favor

import im.fdx.v2ex.data.model.Data
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.fdx.v2ex.data.network.NetManager
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.data.network.vCall
import im.fdx.v2ex.data.model.Node
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

data class FavoriteNodeUiState(
    val nodes: List<Node> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FavoriteNodeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteNodeUiState())
    val uiState: StateFlow<FavoriteNodeUiState> = _uiState.asStateFlow()

    init {
        fetchNodes()
    }

    fun fetchNodes() {
        _uiState.update { it.copy(isLoading = true) }
        val url = "https://www.v2ex.com/my/nodes"
        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }

            override fun onResponse(call: Call, response: Response) {
                 if (response.code != 200) {
                     _uiState.update { it.copy(isLoading = false, error = "Error ${response.code}") }
                 } else {
                     val body = response.body?.string() ?: ""
                     val nodes = Parser(body).parseToNode()
                     _uiState.update { it.copy(nodes = nodes, isLoading = false) }
                 }
            }
        })
    }
}




