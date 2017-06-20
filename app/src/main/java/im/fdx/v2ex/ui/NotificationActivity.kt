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
import im.fdx.v2ex.utils.ViewUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

class NotificationActivity : AppCompatActivity() {

    private var notifications: MutableList<NotificationModel> = ArrayList()
    private var adapter: NotificationAdapter? = null
    private var mSwipe: SwipeRefreshLayout? = null
    private lateinit var rvNotification: RecyclerView
    private lateinit var flContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.notification)

        flContainer = findViewById(R.id.fl_container)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        mSwipe = findViewById(R.id.swipe_container)
        mSwipe!!.setOnRefreshListener { fetchNotification() }

        rvNotification = findViewById(R.id.rv_container)
        rvNotification.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(this, notifications)
        rvNotification.adapter = adapter
        parseIntent(intent)
    }

    private fun parseIntent(intent: Intent) {
        adapter!!.number = intent.getIntExtra("number", -1)
        fetchNotification()
    }

    private fun fetchNotification() {
        val url = "https://www.v2ex.com/notifications"
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .url(url)
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code() == 200) {
                    val html = Jsoup.parse(response.body()?.string())
                    val c = NetManager.parseToNotifications(html)
                    if (c.isEmpty()) {
                        runOnUiThread {
                            mSwipe?.isRefreshing = false
                            ViewUtil.showNoContent(this@NotificationActivity, flContainer)
                        }
                        return
                    }
                    notifications.addAll(c)
                    runOnUiThread {
                        adapter?.notifyDataSetChanged()
                        mSwipe?.isRefreshing = false
                    }
                }
            }
        })

    }

}
