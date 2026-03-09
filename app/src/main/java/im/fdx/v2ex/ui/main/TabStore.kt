package im.fdx.v2ex.ui.main

import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.settings.MyTab
import im.fdx.v2ex.ui.settings.tabPaths
import im.fdx.v2ex.ui.settings.tabTitles
import im.fdx.v2ex.utils.Keys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TabStore {
    private val initMyTabs: List<MyTab> = tabTitles.mapIndexed { index, s ->
        MyTab(s, tabPaths[index])
    }
    
    private val _tabs = MutableStateFlow(loadFromPrefs())
    val tabs: StateFlow<List<MyTab>> = _tabs.asStateFlow()

    private fun loadFromPrefs(): List<MyTab> {
        val str = pref.getString(Keys.PREF_TAB, null)
        val turnsType = object : TypeToken<List<MyTab>>() {}.type
        val savedList: List<MyTab>? = Gson().fromJson(str, turnsType)
        return if (savedList.isNullOrEmpty()) initMyTabs else savedList
    }

    fun setTabs(newTabs: List<MyTab>) {
        val savedList = Gson().toJson(newTabs)
        pref.edit { putString(Keys.PREF_TAB, savedList) }
        _tabs.value = newTabs
    }
}
