package im.fdx.v2ex.ui.node

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.Parser
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

data class AllNodesUiState(
    val nodes: Map<String, List<Node>> = emptyMap(),
    val displayedNodes: Map<String, List<Node>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AllNodesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AllNodesUiState())
    val uiState: StateFlow<AllNodesUiState> = _uiState.asStateFlow()

    init {
        loadNodes()
    }

    private fun loadNodes() {
        val time = pref.getLong(Keys.PREF_ALL_NODE_DATA_TIME, 0L)
        val cache = if (System.currentTimeMillis() - time > 24 * 60 * 60 * 1000) "" else pref.getString(Keys.PREF_ALL_NODE_DATA, "") ?: ""

        if (cache.isNotEmpty()) {
            val type = object : TypeToken<List<Node>>() {}.type
            val nodeModels = Gson().fromJson<List<Node>>(cache, type)
            updateNodes(nodeModels)
        } else {
            fetchNodes()
        }
    }

    private fun fetchNodes() {
        _uiState.update { it.copy(isLoading = true) }
        vCall(NetManager.URL_ALL_NODE_WEB).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    _uiState.update { it.copy(isLoading = false, error = "Error: ${response.code}") }
                    return
                }
                val htmlStr = response.body?.string() ?: ""
                val nodes = Parser(htmlStr).getAllNode()
                if (nodes.isNotEmpty()) {
                    pref.edit().putString(Keys.PREF_ALL_NODE_DATA, Gson().toJson(nodes))
                        .putLong(Keys.PREF_ALL_NODE_DATA_TIME, System.currentTimeMillis()).apply()
                    updateNodes(nodes)
                } else {
                     _uiState.update { it.copy(isLoading = false, error = "No nodes found") }
                }
            }
        })
    }

    private fun updateNodes(nodes: List<Node>) {
        val grouped = nodes.groupBy { it.category ?: "Other" }
        _uiState.update { 
            it.copy(nodes = grouped, displayedNodes = grouped, isLoading = false) 
        }
    }

    fun search(query: String) {
        val all = _uiState.value.nodes
        if (query.isBlank()) {
            _uiState.update { it.copy(displayedNodes = all) }
        } else {
            val filtered = all.mapValues { entry ->
                entry.value.filter { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.name.contains(query, ignoreCase = true) 
                }
            }.filterValues { it.isNotEmpty() }
            _uiState.update { it.copy(displayedNodes = filtered) }
        }
    }
}
