package im.fdx.v2ex.utils

import im.fdx.v2ex.data.model.Member
import im.fdx.v2ex.pref
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserStore {
    private val _user = MutableStateFlow<Member?>(null)
    val user = _user.asStateFlow()

    init {
        // Load from Prefs
        val username = pref.getString(Keys.PREF_USERNAME, "")
        val avatar = pref.getString(Keys.PREF_AVATAR, "")
        if (!username.isNullOrEmpty()) {
             _user.value = Member().apply { 
                 this.username = username
                 if (avatar != null) {
                     this.avatar_normal = avatar
                 }
             }
        }
    }
    
    fun updateUser(member: Member) {
        pref.edit {
            putString(Keys.PREF_USERNAME, member.username)
            putString(Keys.PREF_AVATAR, member.avatar_normal)
        }
        _user.value = member
    }

    fun logout() {
        _user.value = null
    }
}
