package im.fdx.v2ex.ui.main

import im.fdx.v2ex.data.model.Data
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import im.fdx.v2ex.data.network.HttpHelper
import im.fdx.v2ex.data.network.NetManager
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.data.network.vCall
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class NewTopicViewModel(application: Application) : AndroidViewModel(application) {

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val _nodeName = MutableStateFlow("")
    val nodeName = _nodeName.asStateFlow()

    private val _nodeTitle = MutableStateFlow("")
    val nodeTitle = _nodeTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _sendResult = MutableStateFlow<SendResult?>(null)
    val sendResult = _sendResult.asStateFlow()

    // Used to append uploaded image to content
    fun appendContent(text: String) {
        _content.value += text
    }

    fun onTitleChange(value: String) { _title.value = value }
    fun onContentChange(value: String) { _content.value = value }
    fun onNodeNameChange(value: String) { _nodeName.value = value }
    fun onNodeTitleChange(value: String) { _nodeTitle.value = value }
    
    fun setInitialData(t: String?, c: String?, n: String?) {
        if (t != null && t != "{title}") _title.value = t
        if (c != null && c != "{content}") _content.value = c
        if (n != null && n != "{node}") {
            _nodeName.value = n
            _nodeTitle.value = n // Fallback to name if title is not provided initially
        }
        // Report logic ignored for simplicity, can be re-added
    }

    fun send() {
        if (_title.value.isEmpty() || _content.value.isEmpty()) {
            _sendResult.value = SendResult.Error(getApplication<android.app.Application>().getString(R.string.error_empty_title_content))
            return
        }
        if (_nodeName.value.isEmpty()) {
             _sendResult.value = SendResult.Error(getApplication<android.app.Application>().getString(R.string.error_choose_node))
             return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            postNew()
        }
    }

    private fun postNew() {
         try {
             val response = vCall("https://www.v2ex.com/new").execute()
             if (response.code != 200) {
                 _isLoading.value = false
                 _sendResult.value = SendResult.Error("Network Error: ${response.code}")
                 return
             }
             
             val bodyString = response.body.string()
             val once = Parser(bodyString).getOnceNum2()
             if (once == null) {
                 _isLoading.value = false
                 _sendResult.value = SendResult.Error(getApplication<android.app.Application>().getString(R.string.error_post_failed_once))
                 return
             }
             
             val requestBody = FormBody.Builder()
                .add("title", _title.value)
                .add("content", _content.value)
                .add("node_name", _nodeName.value)
                .add("once", once)
                .build()

            val postResponse = HttpHelper.OK_CLIENT.newCall(
                 Request.Builder()
                     .url("https://www.v2ex.com/new")
                     .post(requestBody)
                     .build()
            ).execute()
            
            _isLoading.value = false
            if (postResponse.code == 302) {
                // Success redirect
                 _sendResult.value = SendResult.Success
            } else {
                 val errorMsg = Parser(postResponse.body.string()).getErrorMsg()
                 _sendResult.value = SendResult.Error(errorMsg)
            }
             
         } catch (e: Exception) {
             e.printStackTrace()
             _isLoading.value = false
             _sendResult.value = SendResult.Error("Exception: ${e.message}")
         }
    }
    
    fun resetResult() {
        _sendResult.value = null
    }
}

sealed class SendResult {
    object Success : SendResult()
    data class Error(val msg: String) : SendResult()
}




