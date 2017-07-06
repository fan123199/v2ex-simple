package im.fdx.v2ex.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.FrameLayout
import im.fdx.v2ex.R
import im.fdx.v2ex.model.NotificationModel
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.showNoContent
import im.fdx.v2ex.utils.extensions.toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException

class NotificationActivity : AppCompatActivity() {

    private var notifications: MutableList<NotificationModel> = mutableListOf()
    private lateinit var adapter: NotificationAdapter
    private lateinit var mSwipe: SwipeRefreshLayout
    private lateinit var rvNotification: RecyclerView
    private lateinit var flContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        flContainer = findViewById(R.id.fl_container)
        mSwipe = findViewById(R.id.swipe_container)
        mSwipe.setColorSchemeResources(R.color.primary)
        mSwipe.setOnRefreshListener { fetchNotification() }

        rvNotification = findViewById(R.id.rv_container)
        rvNotification.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(this, notifications)
        rvNotification.adapter = adapter
        parseIntent(intent)
    }

    private fun parseIntent(intent: Intent) {
        val numUnread = intent.getIntExtra(Keys.KEY_UNREAD_COUNT, -1)
        adapter.number = numUnread
        supportActionBar?.title = "${getString(R.string.notification)} ${when {
            numUnread != -1 -> "($numUnread 条未读)"
            else -> ""
        }}"
        mSwipe.isRefreshing = true
        fetchNotification()
    }

    private fun fetchNotification() {
        val url = "https://www.v2ex.com/notifications"
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .url(url)
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                NetManager.dealError(this@NotificationActivity, swipe = mSwipe)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                when (response.code()) {
                    302 -> runOnUiThread {
                        toast("您未登录或登录信息已过时，请重新登录")
                    }
                    200 -> {
                        val html = Jsoup.parse(response.body()?.string())
                        val c = NetManager.parseToNotifications(html)
                        if (c.isEmpty()) {
                            runOnUiThread {
                                mSwipe.isRefreshing = false
                                flContainer.showNoContent()
                            }
                            return
                        }
                        notifications.clear()
                        notifications.addAll(c)
                        runOnUiThread {
                            adapter.notifyDataSetChanged()
                            mSwipe.isRefreshing = false
                        }
                    }
                    else -> NetManager.dealError(this@NotificationActivity, response.code(), mSwipe)
                }
            }
        })

    }

}
