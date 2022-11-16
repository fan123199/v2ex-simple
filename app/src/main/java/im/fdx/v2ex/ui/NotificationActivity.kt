package im.fdx.v2ex.ui

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import im.fdx.v2ex.R
import im.fdx.v2ex.model.NotificationModel
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.Parser
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.setUpToolbar
import im.fdx.v2ex.utils.extensions.showNoContent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jetbrains.anko.toast
import java.io.IOException

class NotificationActivity : BaseActivity() {

    private var notifications: MutableList<NotificationModel> = mutableListOf()
    private lateinit var adapter: NotificationAdapter
    private lateinit var mSwipe: SwipeRefreshLayout
    private lateinit var rvNotification: RecyclerView
    private lateinit var flContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        setUpToolbar()

        flContainer = findViewById(R.id.fl_container)
        mSwipe = findViewById(R.id.swipe_container)
        mSwipe.initTheme()
        mSwipe.setOnRefreshListener {
            adapter.number = -1
            findViewById<Toolbar>(R.id.toolbar).title = "${getString(R.string.message)} "
            fetchNotification()
        }

        rvNotification = findViewById(R.id.rv_container)
        rvNotification.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        adapter = NotificationAdapter(this, notifications)
        rvNotification.adapter = adapter
        parseIntent(intent)
    }

    private fun parseIntent(intent: Intent) {
        val numUnread = intent.getIntExtra(Keys.KEY_UNREAD_COUNT, -1)
        adapter.number = numUnread
        findViewById<Toolbar>(R.id.toolbar).title = "${getString(R.string.message)} " +
                if (numUnread != -1) "($numUnread 条未读)" else ""
        mSwipe.isRefreshing = true
        fetchNotification()
    }

    private fun fetchNotification() {
        val url = "https://www.v2ex.com/notifications"
        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(this@NotificationActivity, swipe = mSwipe)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                when (response.code) {
                    302 -> runOnUiThread {
                        toast("您未登录或登录信息已过时，请重新登录")
                    }
                    200 -> {
                        val c = Parser(response.body!!.string()).parseToNotifications()
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
                    else -> NetManager.dealError(this@NotificationActivity, response.code, mSwipe)
                }
            }
        })

    }

}
