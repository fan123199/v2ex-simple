package im.fdx.v2ex.ui.main

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.elvishew.xlog.XLog
import com.google.gson.reflect.TypeToken
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.ui.details.DetailsActivity
import im.fdx.v2ex.ui.node.NodeModel
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.t
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern


class NewTopicActivity : AppCompatActivity() {

    private lateinit var adapter: ArrayAdapter<NodeModel>
    private lateinit var mNodename: String
    private lateinit var etTitle: TextInputEditText
    private lateinit var etContent: EditText
    private lateinit var spinner: SearchableSpinner

    private var mTitle: String = ""
    private var mContent: String = ""
    private var once: String? = null
    private val nodeModels = ArrayList<NodeModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_topic)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        etTitle = findViewById(R.id.et_title)
        etContent = findViewById(R.id.et_content)


        title = getString(R.string.new_top)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener { onBackPressed() }


        spinner = findViewById(R.id.search_spinner_node)
        spinner.setTitle(getString(R.string.choose_node))
        spinner.setPositiveButton(getString(R.string.close))
        adapter = ArrayAdapter(this, R.layout.simple_list_item, nodeModels)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mNodename = (parent.getItemAtPosition(position) as NodeModel).name
                XLog.tag(TAG).d(mNodename)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        HttpHelper.OK_CLIENT
                .newCall(Request.Builder().url(NetManager.URL_ALL_NODE).build())
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        NetManager.dealError(this@NewTopicActivity)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val type = object : TypeToken<ArrayList<NodeModel>>() {}.type
                        val nodes = NetManager.myGson.fromJson<ArrayList<NodeModel>>(response.body()!!.string(), type)
                        runOnUiThread {
                            adapter.addAll(nodes)
                            adapter.notifyDataSetChanged()
                            setNode(intent)
                        }

                    }
                })

        parseIntent(intent)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        when (requestCode) {
            110 ->
                if (resultCode == RESULT_OK) {
                    try {
                        val imageUri = imageReturnedIntent.data;
                        val imageStream = contentResolver.openInputStream(imageUri);

                        val file = File("new_file")
                        val out: FileOutputStream = FileOutputStream(file);
                        out.write(imageStream.readBytes())
                        out.flush()
                        out.close()
                        uploadImage(file)
                        t(imageUri.toString())
                    } catch (e: Exception) {
                        e.printStackTrace();
                    }

                }
        }
    }

    private fun uploadImage(file: File) {

        val MEDIA_TYPE_PNG = MediaType.parse("image/*");
        val url = "https://sm.ms/api/upload"
        val body: RequestBody = RequestBody.create(MEDIA_TYPE_PNG, file)

        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .post(body)
                .url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {
                Log.d("haha", response?.body().toString())
            }
        })
    }

    private fun parseIntent(intent: Intent) {
        val action = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                etContent.setText(sharedText)
            }
        }
    }

    private fun setNode(intent: Intent) {
        if (intent.getStringExtra(Keys.KEY_NODE_NAME) != null) {
            mNodename = intent.getStringExtra(Keys.KEY_NODE_NAME)
            XLog.d(mNodename + "|" + spinner.count + "|" + adapter.count)
            val nodeTitle: String?
            for (i in 0..adapter.count - 1) {
                if (mNodename == adapter.getItem(i)?.name) {
                    XLog.d("yes, $i")
                    nodeTitle = adapter.getItem(i)?.title
                    XLog.d(nodeTitle)
                    spinner.setSelection(i)
                    XLog.d(spinner.selectedItemPosition)
                    break
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, Menu.FIRST, Menu.NONE, "send")
                .setIcon(R.drawable.ic_send_white_24dp)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == Menu.FIRST) {

            mTitle = etTitle.text.toString()
            mContent = etContent.text.toString()

            when {
                TextUtils.isEmpty(mTitle) -> t("标题和内容不能为空")
                TextUtils.isEmpty(mContent) -> t("标题和内容不能为空")
                mTitle.length > 120 -> t("标题字数超过限制")
                mContent.length > 20000 -> t("主题内容不能超过 20000 个字符")
                TextUtils.isEmpty(mNodename) -> t(getString(R.string.choose_node))
                else -> postNew()
            }
        }
        return true
    }


    private fun postNew() {

        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .url("https://www.v2ex.com/new")
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(this@NewTopicActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code() != 200) {
                    NetManager.dealError(this@NewTopicActivity, response.code())
                    return
                }
                once = getOnce(response)
                if (once == null) {
                    return
                }

                val requestBody = FormBody.Builder()
                        .add("title", mTitle)
                        .add("content", mContent)
                        .add("node_name", mNodename)
                        .add("once", once)
                        .build()

                HttpHelper.OK_CLIENT.newCall(Request.Builder()
                        .headers(HttpHelper.baseHeaders)
                        .url("https://www.v2ex.com/new")
                        .post(requestBody)
                        .build()).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        NetManager.dealError(this@NewTopicActivity)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {

                        if (response.code() == 302) {

                            val location = response.header("Location")
                            val p = Pattern.compile("(?<=/t/)(\\d+)")
                            val matcher = p.matcher(location!!)
                            val topic: String
                            if (matcher.find()) {
                                topic = matcher.group()
                                XLog.tag(TAG).d(topic)
                                val intent = Intent(this@NewTopicActivity, DetailsActivity::class.java)
                                intent.putExtra(Keys.KEY_TOPIC_ID, topic)
                                startActivity(intent)
                            }
                            finish()
                        }
                    }
                })
            }
        })
    }

    private fun getOnce(response: Response): String? {
        val p = Pattern.compile("(?<=<input type=\"hidden\" name=\"once\" value=\")(\\d+)")
        val matcher = p.matcher(response.body()!!.string())
        if (!matcher.find()) {
            runOnUiThread { t("无法发布主题，请退出后重试") }
            return null
        }
        return matcher.group()
    }

    companion object {

        private val TAG = NewTopicActivity::class.java.simpleName
    }
}
