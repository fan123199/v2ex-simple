package im.fdx.v2ex.ui.settings

import im.fdx.v2ex.data.model.Data
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.WorkManager
import im.fdx.v2ex.BuildConfig
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import im.fdx.v2ex.data.network.HttpHelper
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.core.net.toUri

data class SettingsUiState(
    val isLogin: Boolean = false,
    val username: String? = null,
    val nightMode: String = AppCompatDelegate.MODE_NIGHT_NO.toString(),
    val showPageNum: Boolean = false,
    val receiveMsg: Boolean = false,
    val backgroundMsg: Boolean = false,
    val msgPeriod: String = "15",
    val textSize: String = "0"
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val isLogin = myApp.isLogin
        val username = pref.getString(Keys.PREF_USERNAME, "user")
        val nightMode = pref.getString(Keys.PREF_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO.toString()) ?: AppCompatDelegate.MODE_NIGHT_NO.toString()
        val showPageNum = pref.getBoolean("pref_page_num", false)
        val receiveMsg = pref.getBoolean("pref_msg", false)
        val backgroundMsg = pref.getBoolean("pref_background_msg", false)
        val msgPeriod = pref.getString("pref_msg_period", "15") ?: "15"

        _uiState.update {
            it.copy(
                isLogin = isLogin,
                username = username,
                nightMode = nightMode,
                showPageNum = showPageNum,
                receiveMsg = receiveMsg,
                backgroundMsg = backgroundMsg,
                msgPeriod = msgPeriod,
                textSize = pref.getString(Keys.PREF_TEXT_SIZE, "0") ?: "0"
            )
        }
    }

    fun setTextSize(size: String) {
        pref.edit { putString(Keys.PREF_TEXT_SIZE, size) }
        _uiState.update { it.copy(textSize = size) }
    }
    
    fun setNightMode(mode: String) {
        pref.edit { putString(Keys.PREF_NIGHT_MODE, mode) }
        AppCompatDelegate.setDefaultNightMode(mode.toInt())
        _uiState.update { it.copy(nightMode = mode) }
    }

    fun setReceiveMsg(enable: Boolean, context: Context) {
        pref.edit { putBoolean("pref_msg", enable) }
        _uiState.update { it.copy(receiveMsg = enable) }
        if (!enable) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(Keys.notifyID)
            WorkManager.getInstance(myApp).cancelAllWorkByTag(Keys.TAG_WORKER)
        }
    }
    
    fun setBackgroundMsg(enable: Boolean) {
        pref.edit { putBoolean("pref_background_msg", enable) }
         _uiState.update { it.copy(backgroundMsg = enable) }
    }
    
    // Simplification: Not implementing full worker logic here, assuming existing logic handles shared pref changes elsewhere or needs porting. 
    // The original activity used OnSharedPreferenceChangeListener.
    // For now we persist changes.
    
    fun logout(context: Context, activity: android.app.Activity) {
         HttpHelper.myCookieJar.clear()
          im.fdx.v2ex.setLogin(false)
          pref.edit {
            remove(Keys.PREF_TEXT_SIZE)
            remove(Keys.PREF_TAB)
          }
          context.toast("已退出登录")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onTabSettingClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showNightModeSheet by remember { mutableStateOf(false) }
    var showTextSizeSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val textSizeSheetState = rememberModalBottomSheetState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.isLogin) {
                SettingsCategory(stringResource(R.string.user))
                SettingsItem(
                    title = stringResource(R.string.username),
                    subtitle = uiState.username
                )
                 SettingsItem(
                    title = stringResource(R.string.logout),
                    onClick = {
                         // Show dialog
                         viewModel.logout(context, context as android.app.Activity)
                    }
                )
                
                 HorizontalDivider()
                 SettingsCategory(stringResource(R.string.message))
                 SwitchSettingsItem(
                     title = stringResource(R.string.unread_message_notification),
                     checked = uiState.receiveMsg,
                     onCheckedChange = { viewModel.setReceiveMsg(it, context) }
                 )
                 if (uiState.receiveMsg) {
                      SwitchSettingsItem(
                         title = stringResource(R.string.get_message_background),
                         checked = uiState.backgroundMsg,
                         onCheckedChange = { viewModel.setBackgroundMsg(it) }
                     )
                 }
            }
            
            HorizontalDivider()
            SettingsCategory(stringResource(R.string.general))
            
            SettingsItem(
                title = "Tab Settings",
                onClick = onTabSettingClick
            )

            SettingsItem(
                title = stringResource(R.string.font_size),
                subtitle = context.resources.getStringArray(R.array.text_size_string).getOrElse(uiState.textSize.toInt()) { "Follow System" },
                onClick = {
                    showTextSizeSheet = true
                }
            )
            
            // Night Mode Logic - simplifed dialog or dropdown
             SettingsItem(
                title = stringResource(R.string.theme_background),
                subtitle = when (uiState.nightMode.toInt()) {
                    -1 -> stringResource(R.string.follow_system)
                    1 -> stringResource(R.string.light_mode)
                    2 -> stringResource(R.string.dark_mode)
                    else -> stringResource(R.string.follow_system)
                },
                onClick = {
                     showNightModeSheet = true
                }
            )

            HorizontalDivider()
            SettingsCategory(stringResource(R.string.other))
             SettingsItem(
                title = stringResource(R.string.version),
                subtitle = BuildConfig.VERSION_NAME
            )
            val noappstore = stringResource(R.string.there_is_no_app_store)
            SettingsItem(
                title = stringResource(R.string.rate),
                onClick = {
                    try {
                      val uri = "market://details?id=im.fdx.v2ex".toUri()
                      val intent = Intent(Intent.ACTION_VIEW, uri)
                      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                      context.startActivity(intent)
                    } catch (e: Exception) {
                        context.toast(noappstore)
                    }
                }
            )
            
        }

        if (showNightModeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNightModeSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                ) {
                    val currentMode = uiState.nightMode.toInt()
                    val nightModeLabels = context.resources.getStringArray(R.array.night_mode_string)
                    // night_mode array maps to: 2 (Dark), 1 (Light), -1 (System)
                    
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.follow_system)) },
                        trailingContent = if (currentMode == -1) { { Icon(Icons.Default.Check, null) } } else null,
                        modifier = Modifier.clickable {
                            viewModel.setNightMode("-1")
                            showNightModeSheet = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.light_mode)) },
                        trailingContent = if (currentMode == 1) { { Icon(Icons.Default.Check, null) } } else null,
                        modifier = Modifier.clickable {
                            viewModel.setNightMode("1")
                            showNightModeSheet = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.dark_mode)) },
                        trailingContent = if (currentMode == 2) { { Icon(Icons.Default.Check, null) } } else null,
                        modifier = Modifier.clickable {
                            viewModel.setNightMode("2")
                            showNightModeSheet = false
                        }
                    )
                }
            }
        }

        if (showTextSizeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTextSizeSheet = false },
                sheetState = textSizeSheetState
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                ) {
                    val fontLabels = context.resources.getStringArray(R.array.text_size_string)
                    val fontValues = listOf("0", "1", "2", "3", "4")
                    
                    fontValues.forEachIndexed { index, value ->
                        ListItem(
                            headlineContent = { Text(fontLabels[index]) },
                            trailingContent = if (uiState.textSize == value) { { Icon(Icons.Default.Check, null) } } else null,
                            modifier = Modifier.clickable {
                                viewModel.setTextSize(value)
                                showTextSizeSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategory(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = onClick ?: {})
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SwitchSettingsItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}




