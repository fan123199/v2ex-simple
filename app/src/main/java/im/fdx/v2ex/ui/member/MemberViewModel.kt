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
    val isRepliesEnd: Boolean = false,
    val repliesPage: Int = 1,
    val repliesTotalPage: Int = 1,
    val onceToken: String? = null,
    val isFollowed: Boolean = false,
    val isBlocked: Boolean = false,
    val topicCount: Int? = null,
    val replyCount: Int? = null
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
                     val body = response.body.string()
                     try {
                         val member = NetManager.myGson.fromJson(body, Member::class.java)
                         _uiState.update { it.copy(member = member) }
                         getMemberInfoHtml()
                     } catch (e: Exception) {
                          _uiState.update { it.copy(isLoadingProfile = false, error = "Parse Error") }
                     }
                 } else {
                     _uiState.update { it.copy(isLoadingProfile = false, error = "Error ${response.code}") }
                 }
            }
        })
    }

    private fun getMemberInfoHtml() {
        val url = "${NetManager.HTTPS_V2EX_BASE}/member/$username"
        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _uiState.update { it.copy(isLoadingProfile = false) }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body.string()
                    val parser = Parser(body)
                    val enrichedMember = parser.parseMemberProfile(username)
                    val (isFollowed, isBlocked, once) = parser.getMemberStatus()
                    
                    _uiState.update { currentState ->
                        val finalMember = currentState.member?.copy(
                            id = if (enrichedMember.id.isNotEmpty()) enrichedMember.id else currentState.member.id,
                            created = if (enrichedMember.created.isNotEmpty()) enrichedMember.created else currentState.member.created,
                            github = enrichedMember.github ?: currentState.member.github,
                            twitter = enrichedMember.twitter ?: currentState.member.twitter,
                            website = enrichedMember.website ?: currentState.member.website,
                            location = enrichedMember.location ?: currentState.member.location,
                            bio = enrichedMember.bio ?: currentState.member.bio,
                            tagline = enrichedMember.tagline ?: currentState.member.tagline,
                            avatar_normal = if (enrichedMember.avatar_normal.isNotEmpty()) enrichedMember.avatar_normal else currentState.member.avatar_normal
                        )
                        currentState.copy(
                            member = finalMember,
                            isLoadingProfile = false,
                            isFollowed = isFollowed,
                            isBlocked = isBlocked,
                            onceToken = once
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingProfile = false) }
                }
            }
        })
    }
    
    fun toggleFollow() {
        val once = _uiState.value.onceToken ?: return
        val id = _uiState.value.member?.id ?: return
        if (id.isEmpty()) return
        
        val isFollowed = _uiState.value.isFollowed
        val url = "${NetManager.HTTPS_V2EX_BASE}/${if (isFollowed) "unfollow" else "follow"}/$id?once=$once"
        
        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    getMemberInfoHtml() // Refresh status
                }
            }
        })
    }

    fun toggleBlock() {
        val once = _uiState.value.onceToken ?: return
        val id = _uiState.value.member?.id ?: return
        if (id.isEmpty()) return

        val isBlocked = _uiState.value.isBlocked
        val url = "${NetManager.HTTPS_V2EX_BASE}/${if (isBlocked) "unblock" else "block"}/$id?once=$once"

        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    getMemberInfoHtml() // Refresh status
                }
            }
        })
    }
    
    fun loadMoreReplies() {
        if (_uiState.value.isRepliesLoading || _uiState.value.isRepliesEnd) return
        val nextPage = _uiState.value.repliesPage + 1
        if(nextPage > _uiState.value.repliesTotalPage) {
            _uiState.update { it.copy(isRepliesEnd = true) }
            return
        }
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
                 val body = response.body.string()
                 val parser = Parser(body)
                 val replies = parser.getUserReplies()
                  val (totalPage, repliesNum) = parser.getTotalPageInMember()
                 
                 _uiState.update { state ->
                     val newReplies = if(page == 1) replies else state.replies + replies
                     state.copy(
                         replies = newReplies,
                         isRepliesLoading = false,
                         isRepliesEnd = page >= totalPage || (page > 1 && replies.isEmpty()),
                         repliesPage = page,
                         repliesTotalPage = totalPage,
                         replyCount = if (page == 1) repliesNum else state.replyCount
                     )
                 }
             }
         })
    }

    fun updateTopicCount(count: Int) {
        _uiState.update { it.copy(topicCount = count) }
    }
}




