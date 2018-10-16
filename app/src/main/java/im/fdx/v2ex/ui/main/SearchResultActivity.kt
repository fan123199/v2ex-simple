package im.fdx.v2ex.ui.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.start
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.member.Member
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.dealError
import im.fdx.v2ex.utils.extensions.loge
import im.fdx.v2ex.utils.extensions.setUpToolbar
import okhttp3.*
import org.jetbrains.anko.bundleOf
import java.io.IOException


class SearchResultActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_search_result)
    setUpToolbar()


    if (pref.getBoolean(Keys.KEY_WARN_SEARCH_API, true)) {
      pref.edit().putBoolean(Keys.KEY_WARN_SEARCH_API, false).apply()
      AlertDialog.Builder(this, R.style.AppTheme_Simple)
          .setPositiveButton("知道了") { _, _ ->

          }
          .setTitle("搜索API提供方")
          .setMessage("""
                    1. 本搜索api来自开源项目https://www.sov2ex.com
                    2. 默认按时间倒序
                    3. 后续版本提供条件筛选功能
                """.trimIndent()).show()
    }
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, TopicsFragment.newInstance())
        .commit()
    handleIntent(intent)
  }


  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleIntent(intent!!)
  }

  private fun handleIntent(intent: Intent) {

    if (Intent.ACTION_SEARCH == intent.action) {
      val query = intent.getStringExtra(SearchManager.QUERY)
      //use the query to search your data somehow

      val url: HttpUrl = HttpUrl.Builder()
          .scheme("https")
          .host("www.sov2ex.com")
          .addEncodedPathSegments("/api/search")
          .addEncodedQueryParameter("q", query)
          .addEncodedQueryParameter("sort", "created")
//          .addEncodedQueryParameter("from", "0") // 偏移量
//          .addEncodedQueryParameter("size", "10") //数量，默认10
//          .addEncodedQueryParameter("node") //节点名称
          .build()
      val topicsFragment = supportFragmentManager.findFragmentById(R.id.container) as TopicsFragment

      topicsFragment.showRefresh(true)
      HttpHelper.OK_CLIENT.newCall(Request.Builder()
          .addHeader("accept", "application/json")
          .url(url)
          .build())
          .start(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
              topicsFragment.showRefresh(false)
              dealError()
            }

            override fun onResponse(call: Call?, response: Response?) {
              topicsFragment.showRefresh(false)
              val body = response?.body()!!.string()
              loge(body)
              val result: SearchResult = Gson().fromJson(body, SearchResult::class.java)

              val topics = result.hits?.map {
                val topic = Topic()
                topic.id = it.id.toString()
                topic.title = it.source?.title.toString()
                topic.content = it.source?.content
                topic.created = TimeUtil.toUtcTime(it.source?.created.toString())
                topic.member = Member().apply { username = it.source?.member.toString() }
                topic.replies = it.source?.replies
                topic
              } ?: return

              loge(result)
              loge(topics)
              topicsFragment.arguments = bundleOf(Keys.KEY_TOPIC_LIST to topics)
              topicsFragment.handleIt()

            }
          })
    }
  }


  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_search, menu)
    val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
    val item = menu!!.findItem(R.id.action_search)
    item.expandActionView()
    val searchView = item.actionView as SearchView
    val et = searchView.findViewById<EditText>(R.id.search_src_text)
    et.setTextColor(ContextCompat.getColor(this, R.color.toolbar_text))
    et.setHintTextColor(ContextCompat.getColor(this, R.color.hint))
    searchView.apply {
      setSearchableInfo(searchManager.getSearchableInfo(componentName))
    }
    return true
  }
}