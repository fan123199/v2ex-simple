package im.fdx.v2ex.ui.topic

import im.fdx.v2ex.data.model.Reply
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.fdx.v2ex.data.network.NetManager
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.data.network.vCall
import im.fdx.v2ex.data.model.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import okhttp3.FormBody
import okhttp3.Request
import im.fdx.v2ex.data.network.HttpHelper
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.loge
import org.json.JSONObject

data class TopicDetailUiState(
    val topic: Topic? = null,
    val replies: List<Reply> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val totalPage: Int = 1,
    val token: String? = null,
    val isFavored: Boolean = false,
    val isThanked: Boolean = false,
    val isIgnored: Boolean = false,
    val isEnd: Boolean = false
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
        if (_uiState.value.isLoading || _uiState.value.isEnd) return
        val nextPage = _uiState.value.page + 1
        if (nextPage > _uiState.value.totalPage) {
            _uiState.update { it.copy(isEnd = true) }
            return
        }
        _uiState.update { it.copy(isLoading = true, page = nextPage) }
        fetchReplies(nextPage, isRefresh = false)
    }

    fun postReply(content: String) {
        val token = _uiState.value.token
        if (token == null) {
            _uiState.update { it.copy(error = "No token found, please refresh") }
            return
        }
        val requestBody = FormBody.Builder()
            .add("content", content)
            .add("once", token)
            .build()

        val url = "${NetManager.HTTPS_V2EX_BASE}/t/$topicId"
        HttpHelper.OK_CLIENT.newCall(Request.Builder().url(url).post(requestBody).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                 _uiState.update { it.copy(error = "Failed to post reply: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                 if (response.isSuccessful) {
                     // Refresh to show new reply
                     refresh()
                 } else {
                     _uiState.update { it.copy(error = "Failed to post reply: ${response.code}") }
                 }
            }
        })
    }

    fun thankReply(replyId: String) {
        val token = _uiState.value.token
        if (token == null) {
             _uiState.update { it.copy(error = "No token found, please refresh") }
            return
        }
         val url = "${NetManager.HTTPS_V2EX_BASE}/thank/reply/$replyId?once=$token"
         HttpHelper.OK_CLIENT.newCall(Request.Builder().url(url).post(FormBody.Builder().build()).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                 _uiState.update { it.copy(error = "Failed to thank: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                 if (response.isSuccessful) {
                     val body = response.body.string()
                     val json = JSONObject(body)
                     if(json.optBoolean("success")) {
                         refresh()
                     }
                 } else {
                     _uiState.update { it.copy(error = "Failed to thank: ${response.code}") }
                 }
            }
         })
    }

    fun favorTopic() {
        val token = _uiState.value.token
        if (token == null) return
        val isFavored = _uiState.value.isFavored
        val action = if (isFavored) "unfavorite" else "favorite"
        val url = "${NetManager.HTTPS_V2EX_BASE}/$action/topic/$topicId?once=$token"
        
        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isFavored = !isFavored) }
                }
            }
        })
    }

    fun thankTopic() {
        if (_uiState.value.isThanked) return
        val token = _uiState.value.token
        if (token == null) return
        val url = "${NetManager.HTTPS_V2EX_BASE}/thank/topic/$topicId?once=$token"
        
        HttpHelper.OK_CLIENT.newCall(Request.Builder().url(url).post(FormBody.Builder().build()).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isThanked = true) }
                }
            }
        })
    }

    fun ignoreTopic() {
        val token = _uiState.value.token
        if (token == null) return
        val url = "${NetManager.HTTPS_V2EX_BASE}/ignore/topic/$topicId?once=$token"
        
        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isIgnored = true) }
                }
            }
        })
    }

    private fun fetchReplies(page: Int, isRefresh: Boolean) {
        val url = "${NetManager.HTTPS_V2EX_BASE}/t/$topicId?p=$page"
        logd("fetchReplies: $url")
        
        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                loge("fetchReplies onFailure: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }

            override fun onResponse(call: Call, response: Response) {
                 val body = response.body.string() ?: ""
                 logd("fetchReplies onResponse: code=${response.code}, bodyLength=${body.length}")
                 if (!response.isSuccessful) {
                     _uiState.update { it.copy(isLoading = false, error = "Error: ${response.code}") }
                     return
                 }

                 try {
                     val parser = Parser(body)
                     val topicHeader = parser.parseResponseToTopic(topicId)
                     logd("fetchReplies parsed: title=${topicHeader.title}, contentLen=${topicHeader.content?.length}, replies=${topicHeader.replies}")
                     
                     val replies = parser.getReplies()
                     val totalPageArr = parser.getPageValue()
                     val totalPage = if(totalPageArr.size >= 2) totalPageArr[1] else 1
                     val token = parser.getOnceNum()
                     
                     val isFavored = parser.isTopicFavored()
                     val isThanked = parser.isTopicThanked()
                     val isIgnored = parser.isIgnored()

                     _uiState.update { currentState ->
                         val combinedReplies = if (isRefresh) replies else currentState.replies + replies
                         currentState.copy(
                             topic = topicHeader, // Update header if parsed
                             replies = combinedReplies,
                             isLoading = false,
                             page = page,
                             totalPage = totalPage,
                             token = token,
                             isFavored = isFavored,
                             isThanked = isThanked,
                             isIgnored = isIgnored,
                             isEnd = page >= totalPage || (replies.isEmpty() && !isRefresh)
                         )
                     }
                 } catch (e: Exception) {
                      loge("fetchReplies parse error: ${e.message}")
                      e.printStackTrace()
                      _uiState.update { it.copy(isLoading = false, error = "Parse Error: ${e.message}") }
                 }
            }
        })
    }

    fun findReplyByFloor(username: String, floor: Int): Reply? {
        logd("Searching for floor $floor by user $username")
        val found = _uiState.value.replies.find { r ->
            r.member?.username?.equals(username, ignoreCase = true) == true && r.getRowNum() == floor
        }
        logd("Found floor reply: ${found?.id}")
        return found
    }

    fun findRecentReplyByUsername(username: String, beforeReplyId: String? = null): Reply? {
        logd("Searching for recent reply by $username before $beforeReplyId")
        val replies = _uiState.value.replies
        val found = if (beforeReplyId == null) {
            replies.findLast { it.member?.username?.equals(username, ignoreCase = true) == true }
        } else {
            val index = replies.indexOfFirst { it.id == beforeReplyId }
            if (index == -1) {
                replies.findLast { it.member?.username?.equals(username, ignoreCase = true) == true }
            } else {
                replies.take(index).findLast { it.member?.username?.equals(username, ignoreCase = true) == true }
            }
        }
        logd("Found recent reply: ${found?.id}")
        return found
    }
}



