package im.fdx.v2ex.ui.settings

import im.fdx.v2ex.data.model.Data
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import im.fdx.v2ex.data.network.HttpHelper
import android.app.Activity
import androidx.compose.ui.res.stringResource

const val TAB_TYPE = 0
const val NODE_TYPE = 1

@Keep
data class MyTab(var title: String, var path: String, var type: Int = TAB_TYPE)

val tabPaths = listOf(
    "hot",
    "recent",
    "all",
    "heated",
    "tech",
    "creative",
    "play",
    "apple",
    "jobs",
    "deals",
    "city",
    "qna",
    "r2",
    "members"
)

fun getTabTitleRes(path: String): Int? {
    return when (path) {
        "hot" -> R.string.tab_hot
        "recent" -> R.string.tab_recent
        "all" -> R.string.tab_all
        "heated" -> R.string.tab_heated
        "tech" -> R.string.tab_tech
        "creative" -> R.string.tab_creative
        "play" -> R.string.tab_play
        "apple" -> R.string.tab_apple
        "jobs" -> R.string.tab_jobs
        "deals" -> R.string.tab_deals
        "city" -> R.string.tab_city
        "qna" -> R.string.tab_qna
        "r2" -> R.string.tab_r2
        "members" -> R.string.tab_members
        else -> null
    }
}

val tabTitles = tabPaths // We'll resolve these dynamically


class TabSettingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TabSettingUiState())
    val uiState: StateFlow<TabSettingUiState> = _uiState.asStateFlow()

    private val initMyTabs: MutableList<MyTab> = mutableListOf()
    private val initNodes: MutableList<MyTab> = mutableListOf()

    init {
        initMyTabs.addAll(tabTitles.mapIndexed { index, s ->
            MyTab(s, tabPaths[index])
        })
        loadTabs()
        if (myApp.isLogin) {
            fetchNodes()
        }
    }

    private fun loadTabs() {
        val str = pref.getString(Keys.PREF_TAB, null)
        val turnsType = object : TypeToken<List<MyTab>>() {}.type
        val savedList = Gson().fromJson<List<MyTab>>(str, turnsType)
        
        val current = mutableListOf<MyTab>()
        val remaining = mutableListOf<MyTab>()

        if (savedList.isNullOrEmpty()) {
            current.addAll(initMyTabs)
            remaining.addAll(initNodes)
        } else {
            current.addAll(savedList)
            // Note: initNodes might be empty here if not fetched yet, so we only add initMyTabs diff
             remaining.addAll(initMyTabs.filter { it.path !in current.map { t -> t.path } })
        }
        _uiState.update { it.copy(currentTabs = current, availableTabs = remaining) }
        updateRemaining()
    }

    private fun fetchNodes() {
        val request = Request.Builder().url("https://www.v2ex.com/my/nodes").build()
        HttpHelper.OK_CLIENT.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Ignore
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val body = response.body.string() ?: return
                    val nodeModels = Parser(body).parseToNode()
                    val nodes = nodeModels.map { MyTab(it.title, it.name, NODE_TYPE) }
                    initNodes.clear()
                    initNodes.addAll(nodes)
                    
                    updateRemaining() // Update UI with available nodes
                }
            }
        })
    }
    
    // Updates availableTabs based on currentTabs and all sources
    private fun updateRemaining() {
         _uiState.update { state ->
             val currentPaths = state.currentTabs.map { it.path }.toSet()
             val allPossible = initMyTabs + initNodes
             val newRemaining = allPossible.filter { it.path !in currentPaths }
             state.copy(availableTabs = newRemaining)
         }
    }

    fun addTab(tab: MyTab) {
        _uiState.update {
            it.copy(
                currentTabs = it.currentTabs + tab,
                availableTabs = it.availableTabs - tab
            )
        }
    }

    fun removeTab(tab: MyTab) {
        _uiState.update {
            it.copy(
                currentTabs = it.currentTabs - tab,
                availableTabs = it.availableTabs + tab
            )
        }
    }

    fun moveUp(index: Int) {
        if (index > 0) {
            _uiState.update {
                val list = it.currentTabs.toMutableList()
                java.util.Collections.swap(list, index, index - 1)
                it.copy(currentTabs = list)
            }
        }
    }

    fun moveDown(index: Int) {
        _uiState.update {
            val list = it.currentTabs.toMutableList()
            if (index < list.size - 1) {
                java.util.Collections.swap(list, index, index + 1)
                it.copy(currentTabs = list)
            } else {
                it
            }
        }
    }
    
    fun reset() {
        _uiState.update {
             it.copy(currentTabs = initMyTabs, availableTabs = initNodes)
        }
    }

    fun save(context: Context) {
        val current = _uiState.value.currentTabs
        if (current.isEmpty()) {
            context.toast(context.getString(R.string.need_at_least_one))
            return
        }
        val savedList = Gson().toJson(current)
        pref.edit { putString(Keys.PREF_TAB, savedList) }
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(Keys.ACTION_TAB_SETTING))
        (context as? Activity)?.finish()
    }
}

data class TabSettingUiState(
    val currentTabs: List<MyTab> = emptyList(),
    val availableTabs: List<MyTab> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSettingScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: TabSettingViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.tab_setting)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.reset() }) {
                        Text(stringResource(R.string.menu_reset))
                    }
                    TextButton(onClick = { viewModel.save(context) }) {
                        Text(stringResource(R.string.menu_save))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(stringResource(R.string.show_area), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
            
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                itemsIndexed(uiState.currentTabs) { index, tab ->
                     Row(
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(horizontal = 16.dp, vertical = 4.dp)
                             .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                             .padding(8.dp),
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         val displayTitle = getTabTitleRes(tab.path)?.let { stringResource(it) } ?: tab.title
                         Text(displayTitle, modifier = Modifier.weight(1f))
                         
                         IconButton(onClick = { viewModel.moveUp(index) }, enabled = index > 0) {
                             Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up")
                         }
                         IconButton(onClick = { viewModel.moveDown(index) }, enabled = index < uiState.currentTabs.size - 1) {
                             Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down")
                         }
                         IconButton(onClick = { viewModel.removeTab(tab) }) {
                             Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                         }
                     }
                }
            }
            
            HorizontalDivider()
            
            Text(stringResource(R.string.hide_area), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(uiState.availableTabs) { tab ->
                    Row(
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(horizontal = 16.dp, vertical = 4.dp)
                             .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.small)
                             .padding(8.dp),
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         val displayTitle = getTabTitleRes(tab.path)?.let { stringResource(it) } ?: tab.title
                         Text(displayTitle, modifier = Modifier.weight(1f))
                         IconButton(onClick = { viewModel.addTab(tab) }) {
                             Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                         }
                     }
                }
            }
        }
    }
}




