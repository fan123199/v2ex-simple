@file:Suppress("DEPRECATION")

package im.fdx.v2ex.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import com.esafirm.imagepicker.features.ImagePicker
import im.fdx.v2ex.R
import im.fdx.v2ex.network.*
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.node.AllNodesActivity
import im.fdx.v2ex.ui.node.Node
import im.fdx.v2ex.ui.topic.TopicActivity
import im.fdx.v2ex.ui.theme.V2ExTheme
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.Keys.KEY_TO_CHOOSE_NODE
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.openImagePicker
import im.fdx.v2ex.utils.extensions.toast
import okhttp3.*
import java.io.IOException

class NewTopicActivity : BaseActivity() {

    private var mNodename by mutableStateOf("")
    private var mNodeTitle by mutableStateOf("")

    private var mTitle by mutableStateOf("")
    private var mContent by mutableStateOf("")
    private var isLoading by mutableStateOf(false)
    private var once: String? = null

    // Disable node selection if reporting
    private var isNodeSelectable by mutableStateOf(true)
    // Disable upload if reporting
    private var isUploadEnabled by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        parseIntent(intent)

        setContent {
            V2ExTheme {
                NewTopicScreen(
                    title = mTitle,
                    onTitleChange = { mTitle = it },
                    content = mContent,
                    onContentChange = { mContent = it },
                    nodeName = mNodename,
                    nodeTitle = mNodeTitle,
                    onNodeClick = {
                        if(isNodeSelectable) {
                            startActivityForResult(
                                Intent(this@NewTopicActivity, AllNodesActivity::class.java).apply {
                                    putExtras(bundleOf(KEY_TO_CHOOSE_NODE to true))
                                }, 
                                REQUEST_NODE
                            )
                        }
                    },
                    isLoading = isLoading,
                    onSendClick = { performSend() },
                    onUploadClick = if(isUploadEnabled) { { openImagePicker(this) } } else null,
                    onBackClick = { finish() }
                )
            }
        }
    }


    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            val images = ImagePicker.getImages(data)

            images.forEach { image ->
                Api.uploadImage(image.path, image.name) { s, i ->
                    runOnUiThread {
                        when (i) {
                            0 -> {
                                mContent += "![image](${s?.url})\n"
                            }
                            1 -> toast("网络错误")
                            2 -> toast(s?.msg ?: "上传失败")
                        }
                    }
                }
            }

        } else if (requestCode == REQUEST_NODE && resultCode == Activity.RESULT_OK && data != null) {
            val nodeInfo = data.getParcelableExtra<Node>("extra_node")!!
            mNodename = nodeInfo.name
            mNodeTitle = nodeInfo.title
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun parseIntent(intent: Intent) {
        val action = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                mContent = sharedText
            }
        } else if (action == Keys.ACTION_V2EX_REPORT) {
            val title = intent.getStringExtra(Intent.EXTRA_TITLE) ?: ""
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            mTitle = title
            mContent = sharedText
            mNodename = "feedback"
            mNodeTitle = "反馈"
            isNodeSelectable = false
            isUploadEnabled = false 
        }
    }

    private fun performSend() {

        when {
            mTitle.isEmpty() -> toast("标题和内容不能为空")
            mContent.isEmpty() -> toast("标题和内容不能为空")
            mTitle.length > 120 -> toast("标题字数超过限制")
            mContent.length > 20000 -> toast("主题内容不能超过 20000 个字符")
            mNodename.isEmpty() -> toast(this.getString(R.string.choose_node))
            else -> postNew()
        }
    }


    private fun postNew() {
        isLoading = true
        vCall("https://www.v2ex.com/new").start(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { isLoading = false }
                NetManager.dealError(this@NewTopicActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code != 200) {
                    runOnUiThread { isLoading = false }
                    NetManager.dealError(this@NewTopicActivity, response.code)
                    return
                }
                once = Parser(response.body!!.string()).getOnceNum2()
                if (once == null) {
                    runOnUiThread {
                        isLoading = false
                        toast("发布主题失败，请退出app重试")
                    }
                    return
                }

                val requestBody = FormBody.Builder()
                    .add("title", mTitle)
                    .add("content", mContent)
                    .add("node_name", mNodename)
                    .add("once", once!!)
                    .build()

                HttpHelper.OK_CLIENT.newCall(
                    Request.Builder()
                        .url("https://www.v2ex.com/new")
                        .post(requestBody)
                        .build()
                ).start(object : Callback {
                    override fun onFailure(call1: Call, e: IOException) {
                        runOnUiThread { isLoading = false }
                        NetManager.dealError(this@NewTopicActivity)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call1: Call, response2: Response) {
                        runOnUiThread { isLoading = false }
                        if (response2.code == 302) {
                            if (intent.action == Keys.ACTION_V2EX_REPORT) {
                                runOnUiThread {
                                    toast("用户报告已提交到反馈节点")
                                    finish()
                                }
                                return
                            }

                            val location = response2.header("Location")?:""
                            val p = Regex("(?<=/t/)(\\d+)")
                            val matcher = p.find(location)?.value
                            matcher?.let {
                                val topic: String = it
                                logd(topic)
                                val intent = Intent(this@NewTopicActivity, TopicActivity::class.java)
                                intent.putExtra(Keys.KEY_TOPIC_ID, topic)
                                startActivity(intent)
                            }
                            finish()

                        } else {
                            val errorMsg = Parser(response2.body!!.string()).getErrorMsg()
                            runOnUiThread {
                                toast(errorMsg)
                            }
                        }
                    }
                })
            }
        })
    }

    companion object {
        const val REQUEST_NODE = 123
        const val MENU_ID_SEND = 123
        const val MENU_ID_UPLOAD = 124

    }
}
