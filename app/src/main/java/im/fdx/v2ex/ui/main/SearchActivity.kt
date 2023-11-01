package im.fdx.v2ex.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.SearchManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.esafirm.imagepicker.features.ImagePicker
import im.fdx.v2ex.R
import im.fdx.v2ex.databinding.ActivityMemberBinding
import im.fdx.v2ex.databinding.ActivitySearchResultBinding
import im.fdx.v2ex.network.Api
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.node.AllNodesActivity
import im.fdx.v2ex.ui.node.Node
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.setUpToolbar
import im.fdx.v2ex.utils.extensions.showNoContent
import im.fdx.v2ex.utils.extensions.toast
import java.util.Calendar


class SearchActivity : BaseActivity() {


    lateinit var fra: TopicsFragment
    var query: SearchOption = SearchOption("")

    private lateinit var binding: ActivitySearchResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpToolbar(getString(R.string.search))


        if (pref.getBoolean(Keys.KEY_WARN_SEARCH_API, true)) {
            AlertDialog.Builder(this, R.style.AppTheme_Simple)
                .setPositiveButton(R.string.iknow) { _, _ ->
                    pref.edit().putBoolean(Keys.KEY_WARN_SEARCH_API, false).apply()
                }
                .setTitle(getString(R.string.search_api_tips))
                .setMessage(
                    """
                    1. 本搜索api来自开源项目https://www.sov2ex.com
                    2. 默认按时间倒序
                """.trimIndent()
                ).show()
        }

        (binding.searchSpinnerNode.parent as View).setOnClickListener {
            startActivityForResult(Intent(this, AllNodesActivity::class.java)
                .apply {
                    putExtras(bundleOf(Keys.KEY_TO_CHOOSE_NODE to true))
                }, NewTopicActivity.REQUEST_NODE
            )

        }
        binding.ivNode.setOnClickListener {
            if (binding.searchSpinnerNode.text != getString(R.string.node)) {
                query = query.copy(node = null)
                binding.ivNode.setImageResource(R.drawable.baseline_arrow_drop_down_24)
            }
        }

        (binding.tvGte.parent as View).setOnClickListener {
            showTimePickerDialog(it) { date ->
                query = query.copy(gte = (date.timeInMillis/1000).toString())
                binding.tvGte.text = TimeUtil.toDisplay(date)
                binding.ivGte.setImageResource(R.drawable.rounded_close_24)
            }
        }
        (binding.tvLte.parent as View).setOnClickListener {
            showTimePickerDialog(it) { date ->
                query = query.copy(lte = (date.timeInMillis/1000).toString())
                binding.tvLte.text = TimeUtil.toDisplay(date)
                binding.ivLte.setImageResource(R.drawable.rounded_close_24)
            }
        }

        binding.ivGte.setOnClickListener {
            if (binding.tvGte.text != getString(R.string.gte)) {
                query = query.copy(gte =null)
                binding.tvGte.text = getString(R.string.gte)
                binding.ivGte.setImageResource(R.drawable.baseline_arrow_drop_down_24)
            }
        }

        binding.ivLte.setOnClickListener {
            if (binding.tvLte.text != getString(R.string.lte)) {
                query = query.copy(lte = null)
                binding.tvLte.text = getString(R.string.lte)
                binding.ivLte.setImageResource(R.drawable.baseline_arrow_drop_down_24)
            }
        }






//        binding.tvSort.setOnClickListener {
//            query = query.copy(sort = if(query.sort == SUMUP) CREATED else SUMUP)
//
//        }

        ArrayAdapter.createFromResource(this, R.array.search_option, R.layout.text_view_spinner).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.tvSort.adapter = it
        }
        binding.tvSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        query = query.copy(sort = CREATED, order = NEW_FIRST)
                    }
                    1 -> {
                        query = query.copy(sort = CREATED, order = OLD_FIRST)
                    }
                    2 -> {
                        query = query.copy(sort = SUMUP)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }


        fra = TopicsFragment()
        fra.arguments = bundleOf("search" to true)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fra)
            .commit()
    }


    fun showTimePickerDialog(v: View,onSet: (Calendar)->Unit) {
        TimePickerFragment {
            onSet(it)
        }
            .show(supportFragmentManager, "datePicker")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent?.action) {
            query = query.copy(q = intent.getStringExtra(SearchManager.QUERY) ?: "")
            fra.startQuery(query)
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NewTopicActivity.REQUEST_NODE && resultCode == Activity.RESULT_OK && data != null) {
            val nodeInfo = data.getParcelableExtra<Node>("extra_node")!!
            query = query.copy(node = nodeInfo.name)
            binding.searchSpinnerNode.text = "${nodeInfo.name} | ${nodeInfo.title}"
            binding.ivNode.setImageResource(R.drawable.rounded_close_24)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val item = menu!!.findItem(R.id.action_search)
        item.expandActionView()
        val searchView = item.actionView as SearchView
        searchView.isSubmitButtonEnabled = true
        val et = searchView.findViewById<EditText>(R.id.search_src_text)
        et.setTextColor(ContextCompat.getColor(this, R.color.toolbar_text))
        et.setHintTextColor(ContextCompat.getColor(this, R.color.hint))
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return true
    }

    class TimePickerFragment(val onSet: (Calendar)->Unit) : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()

            return DatePickerDialog(requireActivity(), this, c.get(Calendar.YEAR), c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH) )
        }

        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            val c = Calendar.getInstance().apply {
                set(year, month,dayOfMonth)
            }
            onSet(c)
        }
    }
}

