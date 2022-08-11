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
import androidx.core.os.bundleOf
import im.fdx.v2ex.R
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.setUpToolbar


class SearchActivity : BaseActivity() {


  lateinit var fra: TopicsFragment
  lateinit var query: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_search_result)
    setUpToolbar(getString(R.string.search))


    if (pref.getBoolean(Keys.KEY_WARN_SEARCH_API, true)) {
      AlertDialog.Builder(this, R.style.AppTheme_Simple)
          .setPositiveButton(R.string.iknow) { _, _ ->
              pref.edit().putBoolean(Keys.KEY_WARN_SEARCH_API, false).apply()
          }
          .setTitle(getString(R.string.search_api_tips))
          .setMessage("""
                    1. 本搜索api来自开源项目https://www.sov2ex.com
                    2. 默认按时间倒序
                """.trimIndent()).show()
    }
    fra = TopicsFragment()
    fra.arguments = bundleOf("search" to true)
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, fra)
        .commit()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    if (Intent.ACTION_SEARCH == intent?.action) {
      query = intent.getStringExtra(SearchManager.QUERY)?:""
      fra.startQuery(query)
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
    searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
    return true
  }
}