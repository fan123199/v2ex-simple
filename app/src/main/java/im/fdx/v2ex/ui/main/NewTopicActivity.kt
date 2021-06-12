@file:Suppress("DEPRECATION")

package im.fdx.v2ex.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.esafirm.imagepicker.features.ImagePicker
import im.fdx.v2ex.R
import im.fdx.v2ex.databinding.ActivityCreateTopicBinding
import im.fdx.v2ex.network.*
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.details.TopicActivity
import im.fdx.v2ex.ui.node.AllNodesActivity
import im.fdx.v2ex.ui.node.Node
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.Keys.KEY_TO_CHOOSE_NODE
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.openImagePicker
import im.fdx.v2ex.utils.extensions.setUpToolbar
import okhttp3.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.regex.Pattern


class NewTopicActivity : BaseActivity() {

    private var mNodename: String = ""

    private var mTitle: String = ""
    private var mContent: String = ""
    private var once: String? = null
    private lateinit var binding: ActivityCreateTopicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateTopicBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setUpToolbar(getString(R.string.new_top))


        binding.searchSpinnerNode.setOnClickListener {
            startActivityForResult<AllNodesActivity>(REQUEST_NODE, KEY_TO_CHOOSE_NODE to true)
        }
        parseIntent(intent)
    }


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
                                binding.etContent.append("![image](${s?.url})\n") //todo 做到点击删除。那就完美了
                                binding.etContent.setSelection(binding.etContent.length())
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
            binding.searchSpinnerNode.text = "${nodeInfo.name} | ${nodeInfo.title}"
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun parseIntent(intent: Intent) {
        val action = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                binding.etContent.setText(sharedText)
            }
        }
    }


    fun rotate(iv: View) {
        (iv as ImageView).setImageResource(R.drawable.loading)
        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh)
        rotation.repeatCount = Animation.INFINITE
        iv.startAnimation(rotation)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val iv = layoutInflater.inflate(R.layout.iv_refresh, null) as ImageView
        iv.setImageResource(R.drawable.ic_send_primary_24dp)

        menu.add(0, MENU_ID_SEND, 1, "send")
                .setActionView(iv)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(0, MENU_ID_UPLOAD, 0, "upload")
                .setIcon(R.drawable.ic_image)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        val menuSend = menu.findItem(MENU_ID_SEND)

        menuSend?.actionView?.setOnClickListener {
            onOptionsItemSelected(menuSend)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            MENU_ID_SEND -> {

                mTitle = binding.etTitle.text.toString()
                mContent = binding.etContent.text.toString()

                when {
                    mTitle.isEmpty() -> toast("标题和内容不能为空")
                    mContent.isEmpty() -> toast("标题和内容不能为空")
                    mTitle.length > 120 -> toast("标题字数超过限制")
                    mContent.length > 20000 -> toast("主题内容不能超过 20000 个字符")
                    mNodename.isEmpty() -> toast(R.string.choose_node)
                    else -> postNew(item)
                }
            }

            MENU_ID_UPLOAD -> {
                openImagePicker(this)
            }

        }

        return true
    }


    @SuppressLint("InflateParams")
    private fun postNew(item: MenuItem) {
        rotate(item.actionView)


        vCall("https://www.v2ex.com/new").start(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                resetIcon(item)
                NetManager.dealError(this@NewTopicActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code != 200) {
                    resetIcon(item)
                    NetManager.dealError(this@NewTopicActivity, response.code)
                    return
                }
                once = Parser(response.body!!.string()).getOnceNum2()
                if (once == null) {
                    runOnUiThread {
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

                HttpHelper.OK_CLIENT.newCall(Request.Builder()
                        .url("https://www.v2ex.com/new")
                        .post(requestBody)
                        .build()).start(object : Callback {
                    override fun onFailure(call1: Call, e: IOException) {
                        resetIcon(item)
                        NetManager.dealError(this@NewTopicActivity)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call1: Call, response2: Response) {
                        resetIcon(item)
                        if (response2.code == 302) {

                            val location = response2.header("Location")
                            val p = Pattern.compile("(?<=/t/)(\\d+)")
                            val matcher = p.matcher(location!!)
                            val topic: String
                            if (matcher.find()) {
                                topic = matcher.group()
                                logd(topic)
                                val intent = Intent(this@NewTopicActivity, TopicActivity::class.java)
                                intent.putExtra(Keys.KEY_TOPIC_ID, topic)
                                startActivity(intent)
                            }
                            finish()
                        } else {
                            val errorMsg = Parser(response2.body!!.string()).getErrorMsg()
                            runOnUiThread {
                                longToast(errorMsg)
                            }
                        }
                    }
                })
            }
        })
    }

    private fun resetIcon(item: MenuItem) {
        runOnUiThread {
            (item.actionView as ImageView).clearAnimation()
            (item.actionView as ImageView).setImageResource(R.drawable.ic_send_primary_24dp)
        }
    }

    companion object {
        const val REQUEST_NODE = 123
        const val MENU_ID_SEND = 123
        const val MENU_ID_UPLOAD =124

    }
}
