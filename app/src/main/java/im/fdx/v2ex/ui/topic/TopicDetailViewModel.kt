package im.fdx.v2ex.ui.topic

import im.fdx.v2ex.data.model.Reply
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.fdx.v2ex.data.network.NetManager
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.data.network.vCall
import im.fdx.v2ex.data.model.Topic
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val isEnd: Boolean = false,
    val filterType: FilterType = FilterType.None,
    val filterTarget: String? = null,
    val filteredReplies: List<Reply> = emptyList()
)

enum class FilterType {
    None, User, Conversation
}

sealed class TopicDetailEvent {
    object PostReplySuccess : TopicDetailEvent()
    object FavorTopicSuccess : TopicDetailEvent()
    object UnfavorTopicSuccess : TopicDetailEvent()
    object ThankTopicSuccess : TopicDetailEvent()
    object IgnoreTopicSuccess : TopicDetailEvent()
    object IgnoreReplySuccess : TopicDetailEvent()
    object ThankReplySuccess : TopicDetailEvent()
    data class ShowErrorMessage(val error: String) : TopicDetailEvent()
    data class RequestReportTopic(val reason: String) : TopicDetailEvent()
    data class RequestReportReply(val reply: Reply) : TopicDetailEvent()
}

class TopicDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TopicDetailUiState())
    val uiState: StateFlow<TopicDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TopicDetailEvent>()
    val events = _events.asSharedFlow()

    private val allReplies = mutableListOf<Reply>()

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
                     viewModelScope.launch { _events.emit(TopicDetailEvent.PostReplySuccess) }
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
                         viewModelScope.launch { _events.emit(TopicDetailEvent.ThankReplySuccess) }
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
                    viewModelScope.launch { 
                        _events.emit(if (isFavored) TopicDetailEvent.UnfavorTopicSuccess else TopicDetailEvent.FavorTopicSuccess) 
                    }
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
                    viewModelScope.launch { _events.emit(TopicDetailEvent.ThankTopicSuccess) }
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
                    viewModelScope.launch { _events.emit(TopicDetailEvent.IgnoreTopicSuccess) }
                    _uiState.update { it.copy(isIgnored = true) }
                }
            }
        })
    }

    fun reportTopic(reason: String) {
        viewModelScope.launch {
            _events.emit(TopicDetailEvent.RequestReportTopic(reason))
        }
    }

    fun reportReply(reply: Reply) {
        viewModelScope.launch {
            _events.emit(TopicDetailEvent.RequestReportReply(reply))
        }
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
                 val body = response.body.string()
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
                          if (isRefresh) {
                              allReplies.clear()
                          }
                          
                          replies.forEach { r ->
                              r.isLouzu = r.member?.username == topicHeader.member?.username
                          }
                          
                          // Deduplicate based on ID when adding to allReplies
                          val existingIds = allReplies.map { it.id }.toSet()
                          allReplies.addAll(replies.filter { it.id !in existingIds })

                          currentState.copy(
                              topic = topicHeader,
                              replies = allReplies.toList(),
                              isLoading = false,
                              page = page,
                              totalPage = totalPage,
                              token = token,
                              isFavored = isFavored,
                              isThanked = isThanked,
                              isIgnored = isIgnored,
                              isEnd = page >= totalPage || (replies.isEmpty() && !isRefresh),
                              filteredReplies = when (currentState.filterType) {
                                  FilterType.User -> allReplies.filter { it.member?.username == currentState.filterTarget }
                                  FilterType.Conversation -> currentState.filteredReplies
                                  else -> emptyList()
                              }
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
        val replies = allReplies
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

    fun ignoreReply(replyId: String) {
        allReplies.removeAll { it.id == replyId }
        viewModelScope.launch { _events.emit(TopicDetailEvent.IgnoreReplySuccess) }
        applyCurrentFilter()
    }

    fun filterByUser(username: String) {
        _uiState.update { it.copy(filterType = FilterType.User, filterTarget = username) }
        applyCurrentFilter()
    }

    fun showConversation(replyId: String) {
        val conversationIds = mutableSetOf<String>()
        var current: Reply? = allReplies.find { it.id == replyId }
        
        while (current != null) {
            conversationIds.add(current.id)
            val content = current.content
            // Simple logic: if content contains @username #floor, try to find that reply
            val match = """@(\w+)\s*#(\d+)""".toRegex().find(content)
            if (match != null) {
                val username = match.groupValues[1]
                val floor = match.groupValues[2].toIntOrNull() ?: -1
                current = findReplyByFloor(username, floor)
                if (current != null && conversationIds.contains(current.id)) break // Prevent loops
            } else {
                // If it only mentions @username, try to find the most recent reply by that user
                val mentionMatch = """@(\w+)""".toRegex().find(content)
                if (mentionMatch != null) {
                    val username = mentionMatch.groupValues[1]
                    current = findRecentReplyByUsername(username, current.id)
                    if (current != null && conversationIds.contains(current.id)) break
                } else {
                    current = null
                }
            }
        }
        
        _uiState.update { it.copy(filterType = FilterType.Conversation, filterTarget = replyId) }
        _uiState.update { currentState ->
            currentState.copy(
                filteredReplies = allReplies.filter { it.id in conversationIds }.sortedBy { it.getRowNum() }
            )
        }
    }

    fun clearFilter() {
        _uiState.update { it.copy(filterType = FilterType.None, filterTarget = null) }
        applyCurrentFilter()
    }

    private fun applyCurrentFilter() {
        _uiState.update { currentState ->
            val filtered = when (currentState.filterType) {
                FilterType.None -> emptyList()
                FilterType.User -> allReplies.filter { it.member?.username == currentState.filterTarget }
                FilterType.Conversation -> currentState.filteredReplies
            }
            currentState.copy(filteredReplies = filtered)
        }
    }
}
