package im.fdx.v2ex.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.Parser
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.network.NetManager.API_HEATED
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.URL_FOLLOWING
import im.fdx.v2ex.ui.settings.NODE_TYPE
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
import im.fdx.v2ex.network.Parser.Source.*
import im.fdx.v2ex.ui.node.Node
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.fdx.v2ex.ui.main.Topic
import okhttp3.HttpUrl
import im.fdx.v2ex.ui.main.model.SearchResult
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.ui.member.Member

data class TopicListUiState(
    val topics: List<Topic> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val totalPage: Int = 0,
    val isEndless: Boolean = true,
    val node: Node? = null
)

class TopicListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TopicListUiState())
    val uiState: StateFlow<TopicListUiState> = _uiState.asStateFlow()

    private var currentMode = FROM_HOME
    private var requestUrl = ""
    private var tab: String? = null
    private var nodeName: String? = null
    private var username: String? = null

    fun init(tab: String?, type: Int?, username: String?, nodeName: String?) {
        this.tab = tab
        this.nodeName = nodeName
        this.username = username

        when {
            type == 1 -> { // Favorite Topics
                 currentMode = FROM_FAVOR
                 requestUrl = "$HTTPS_V2EX_BASE/my/topics"
            }
            type == 2 -> { // Following
                currentMode = FROM_FAVOR
                requestUrl = URL_FOLLOWING
            }
            tab == "recent" -> {
                currentMode = FROM_HOME
                requestUrl = "$HTTPS_V2EX_BASE/recent"
            }
            tab == "heated" -> {
                currentMode = FROM_HOME
                requestUrl = API_HEATED
            }
            tab != null -> {
                if (type == NODE_TYPE) {
                    currentMode = FROM_NODE
                    requestUrl = "$HTTPS_V2EX_BASE/go/$tab"
                } else {
                    currentMode = FROM_HOME
                    requestUrl = "$HTTPS_V2EX_BASE/?tab=$tab"
                }
            }
            username != null -> {
                currentMode = FROM_MEMBER
                requestUrl = "$HTTPS_V2EX_BASE/member/$username/topics"
            }
            nodeName != null -> {
                currentMode = FROM_NODE
                requestUrl = "$HTTPS_V2EX_BASE/go/$nodeName"
            }
        }
        
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, page = 1) }
        fetchTopics(1, isRefresh = true)
    }

    fun loadMore() {
        if (_uiState.value.isLoading) return
        val nextPage = _uiState.value.page + 1
        // Basic check to stop endless if we know total page
         if (_uiState.value.totalPage > 0 && nextPage > _uiState.value.totalPage) return

        _uiState.update { it.copy(isLoading = true, page = nextPage) }
        fetchTopics(nextPage, isRefresh = false)
    }

    private fun fetchTopics(page: Int, isRefresh: Boolean) {
        if (currentMode == FROM_SEARCH) {
            fetchSearch(page, isRefresh)
            return
        }
        if (requestUrl == API_HEATED) {
             fetchHeated(isRefresh)
             return
        }

        val url = if (currentMode == FROM_HOME && requestUrl != "$HTTPS_V2EX_BASE/recent") {
             requestUrl // Tabs like 'tech' don't seemingly support ?p= for endless on home, or maybe they do? 
             // Logic in Fragment: if Home and not recent -> just URL. 
             // Actually V2EX tabs on home page usually don't have pagination like /?tab=hot&p=2. 
             // But "Recent" does.
             // We will stick to Fragment logic for now.
             requestUrl
        } else {
             "$requestUrl?p=$page"
        }

        val userAgent = if(currentMode == FROM_FAVOR) "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:90.0) Gecko/20100101 Firefox/90.0" else null

        // Using OkHttp callback generic wrapper or just executing in IO scope if vCall is synchronous? 
        // vCall returns a Call. 
        
        vCall(url, userAgent).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }

            override fun onResponse(call: Call, response: Response) {
                // Parse Logic
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                     _uiState.update { it.copy(isLoading = false, error = "Error: ${response.code}") }
                     return
                }

                try {
                    val parser = Parser(body)
                    val newTopics = parser.parseTopicLists(currentMode)
                    val totalPage = parser.getTotalPageForTopics()
                    
                    val nodeInfo = if (currentMode == FROM_NODE && nodeName != null) {
                        try {
                            parser.getNodeInfo(nodeName!!)
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                    
                    _uiState.update { currentState ->
                        val combinedList = if (isRefresh) newTopics else currentState.topics + newTopics
                        currentState.copy(
                            topics = combinedList,
                            isLoading = false,
                            page = page,
                            totalPage = if(totalPage > 0) totalPage else currentState.totalPage,
                            node = nodeInfo ?: currentState.node
                        )
                    }
                } catch (e: Exception) {
                     _uiState.update { it.copy(isLoading = false, error = "Parse Error: ${e.message}") }
                }
            }
        })
    }

    private fun fetchHeated(isRefresh: Boolean) {
         vCall(API_HEATED).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }

            override fun onResponse(call: Call, response: Response) {
                 val str = response.body!!.string()
                 val type = object : TypeToken<List<Topic>>() {}.type
                 val topicList = Gson().fromJson<List<Topic>>(str, type)
                 
                 _uiState.update { 
                     it.copy(topics = topicList, isLoading = false, page = 1) 
                 }
            }
         })
    }

    // Search Logic
    private var searchOption: SearchOption? = null

    fun search(query: String) {
        currentMode = FROM_SEARCH
        searchOption = SearchOption(q = query)
        refresh()
    }

    private fun fetchSearch(page: Int, isRefresh: Boolean) {
        val option = searchOption ?: return
        val nextIndex = (page - 1) * 10
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(NetManager.API_SEARCH_HOST)
            .addEncodedPathSegments("api/search")
            .addEncodedQueryParameter("q", option.q)
            .addEncodedQueryParameter("from", nextIndex.toString())
            .addEncodedQueryParameter("size", "10")
            .addEncodedQueryParameter("sort", option.sort)
            .addEncodedQueryParameter("order", option.order)
            .build()
            .toString()

        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                 _uiState.update { it.copy(isLoading = false, error = e.message) }
            }

            override fun onResponse(call: Call, response: Response) {
                 val body = response.body?.string()
                 try {
                     // Need SearchResult model. using generic parsing for now if model is not easily accessible or use existing model
                     val result = Gson().fromJson(body, SearchResult::class.java)
                     val topics = result.hits?.map { hit ->
                         Topic().apply {
                             id = hit.id.toString()
                             title = hit.source?.title.toString()
                             content = hit.source?.content ?: ""
                             created = TimeUtil.toUtcTime(hit.source?.created.toString())
                             member = Member().apply { username = hit.source?.member.toString() }
                             replies = hit.source?.replies ?: 0
                         }
                     } ?: emptyList()

                     var totalPage = _uiState.value.totalPage
                     if (page == 1) {
                         result.total?.let {
                             totalPage = (it / 10 + 1)
                         }
                     }

                     _uiState.update { currentState ->
                        val combinedList = if (isRefresh) topics else currentState.topics + topics
                        currentState.copy(
                            topics = combinedList,
                            isLoading = false,
                            page = page,
                            totalPage = totalPage
                        )
                    }
                 } catch (e: Exception) {
                     _uiState.update { it.copy(isLoading = false, error = "Search Parse Error: ${e.message}") }
                 }
            }
        })
    }
}


