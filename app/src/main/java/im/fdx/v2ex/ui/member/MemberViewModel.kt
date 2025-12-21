package im.fdx.v2ex.ui.member

import im.fdx.v2ex.data.model.Member
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.fdx.v2ex.data.model.MemberReplyModel
import im.fdx.v2ex.data.network.NetManager
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.data.network.NetManager.API_USER
import im.fdx.v2ex.data.network.vCall
import im.fdx.v2ex.data.model.Reply
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

data class MemberUiState(
    val member: Member? = null,
    val isLoadingProfile: Boolean = false,
    val error: String? = null,
    // For Replies Tab
    val replies: List<MemberReplyModel> = emptyList(),
    val isRepliesLoading: Boolean = false,
    val repliesPage: Int = 1,
    val repliesTotalPage: Int = 1
)

class MemberViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MemberUiState())
    val uiState: StateFlow<MemberUiState> = _uiState.asStateFlow()

    private var username: String = ""

    fun init(username: String) {
        this.username = username
        getMemberInfo()
        getMemberReplies(1)
    }

    private fun getMemberInfo() {
        _uiState.update { it.copy(isLoadingProfile = true) }
        val url = "$API_USER?username=$username"
        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                 _uiState.update { it.copy(isLoadingProfile = false, error = e.message) }
            }

            override fun onResponse(call: Call, response: Response) {
                 if(response.isSuccessful) {
                     val body = response.body?.string() ?: ""
                     try {
                         val member = NetManager.myGson.fromJson(body, Member::class.java)
                         // Fetch detailed info via HTML for block/follow status if needed, 
                         // but for now API covers basic info. 
                         // MemberActivity did getByHtml as fallback or for extra tokens.
                         // For Phase 4, we stick to API where possible for profile.
                         _uiState.update { it.copy(member = member, isLoadingProfile = false) }
                     } catch (e: Exception) {
                          _uiState.update { it.copy(isLoadingProfile = false, error = "Parse Error") }
                     }
                 } else {
                     _uiState.update { it.copy(isLoadingProfile = false, error = "Error ${response.code}") }
                 }
            }
        })
    }
    
    fun loadMoreReplies() {
        if (_uiState.value.isRepliesLoading) return
        val nextPage = _uiState.value.repliesPage + 1
        if(nextPage > _uiState.value.repliesTotalPage) return
         getMemberReplies(nextPage)
    }

    private fun getMemberReplies(page: Int) {
         _uiState.update { it.copy(isRepliesLoading = true) }
         val url = "${NetManager.HTTPS_V2EX_BASE}/member/$username/replies?p=$page"
         vCall(url).enqueue(object : Callback {
             override fun onFailure(call: Call, e: IOException) {
                  _uiState.update { it.copy(isRepliesLoading = false) }
             }

             override fun onResponse(call: Call, response: Response) {
                 val body = response.body?.string() ?: ""
                 val parser = Parser(body)
                 val replies = parser.getUserReplies()
                  val (totalPage, _) = parser.getTotalPageInMember()
                 
                 _uiState.update { state ->
                     val newReplies = if(page == 1) replies else state.replies + replies
                     state.copy(
                         replies = newReplies,
                         isRepliesLoading = false,
                         repliesPage = page,
                         repliesTotalPage = totalPage
                     )
                 }
             }
         })
    }
}




