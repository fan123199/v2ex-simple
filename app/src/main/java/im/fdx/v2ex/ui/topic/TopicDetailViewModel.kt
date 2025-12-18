package im.fdx.v2ex.ui.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.Parser
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.ui.main.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

data class TopicDetailUiState(
    val topic: Topic? = null,
    val replies: List<Reply> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val totalPage: Int = 1
)

class TopicDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TopicDetailUiState())
    val uiState: StateFlow<TopicDetailUiState> = _uiState.asStateFlow()

    private var topicId: String = ""

    fun init(id: String, initialTopic: Topic?) {
        this.topicId = id
        if (initialTopic != null) {
            _uiState.update { it.copy(topic = initialTopic) }
        }
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, page = 1) }
        fetchReplies(1, isRefresh = true)
    }
    
    fun loadMore() {
        if (_uiState.value.isLoading) return
        val nextPage = _uiState.value.page + 1
        if (nextPage > _uiState.value.totalPage) return
         _uiState.update { it.copy(isLoading = true, page = nextPage) }
        fetchReplies(nextPage, isRefresh = false)
    }

    fun postReply(content: String) {
        // Pseudo logic for now, as we need 'once' token and full network implementation
        // similar to fetchReplies but using POST
        // For migration safety, we should implement the full network call using NetManager
        // But for this phase, we ensure the UI hook exists.
        
        // TODO: Implement actual POST call
    }

    private fun fetchReplies(page: Int, isRefresh: Boolean) {
        val url = "${NetManager.HTTPS_V2EX_BASE}/t/$topicId?p=$page"
        
        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }

            override fun onResponse(call: Call, response: Response) {
                 val body = response.body?.string() ?: ""
                 if (!response.isSuccessful) {
                     _uiState.update { it.copy(isLoading = false, error = "Error: ${response.code}") }
                     return
                 }

                 try {
                     val parser = Parser(body)
                     val topicHeader = parser.parseResponseToTopic(topicId)
                     val replies = parser.getReplies()
                     val totalPageArr = parser.getPageValue()
                     val totalPage = if(totalPageArr.size >= 2) totalPageArr[1] else 1

                     _uiState.update { currentState ->
                         val combinedReplies = if (isRefresh) replies else currentState.replies + replies
                         currentState.copy(
                             topic = topicHeader ?: currentState.topic, // Update header if parsed
                             replies = combinedReplies,
                             isLoading = false,
                             page = page,
                             totalPage = totalPage
                         )
                     }
                 } catch (e: Exception) {
                      _uiState.update { it.copy(isLoading = false, error = "Parse Error: ${e.message}") }
                 }
            }
        })
    }
}
