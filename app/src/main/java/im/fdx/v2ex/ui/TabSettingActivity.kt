package im.fdx.v2ex.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import im.fdx.v2ex.network.Parser
import im.fdx.v2ex.network.start
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.Keys.PREF_TAB
import im.fdx.v2ex.utils.extensions.setUpToolbar
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*


class TabSettingActivity : BaseActivity() {


    private val curList: MutableList<Tab> = mutableListOf()
    private val remainList: MutableList<Tab> = mutableListOf()
    private lateinit var adapter: DefaultAdapter
    private lateinit var adapter2: DefaultAdapter

    private var initTabs: MutableList<Tab> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_setting)

        setUpToolbar("栏目设置")
        initTab()

    }


    private fun initTab() {
        initTabs.clear()
        initTabs.addAll(tabTitles.mapIndexed { index, s ->
            Tab(s, tabPaths[index])
        })
        if (myApp.isLogin) {
            getNode()
        } else {
            initAdapter()
        }
    }

    private fun initAdapter() {
        val rvLeft = findViewById<RecyclerView>(R.id.rvLeft)
        val rvRight = findViewById<RecyclerView>(R.id.rvRight)
        rvLeft.layoutManager = LinearLayoutManager(this)
        rvRight.layoutManager = LinearLayoutManager(this)
        val str = pref.getString(PREF_TAB, null)
        val turnsType = object : TypeToken<List<Tab>>() {}.type
        val savedList = Gson().fromJson<List<Tab>>(str, turnsType)
        if (savedList != null) {
            curList.addAll(savedList)
            remainList.addAll(initTabs - curList)
        } else {
            curList.addAll(initTabs)
            remainList.clear()
        }
        adapter = DefaultAdapter(curList)
        adapter2 = DefaultAdapter(remainList)

        adapter.registerListener { i, tab ->
            curList.remove(tab)
            remainList.add(tab)
            adapter.notifyItemRemoved(i)
            adapter2.notifyItemInserted(remainList.size - 1)
        }

        adapter2.registerListener { i, tab ->
            curList.add(tab)
            remainList.remove(tab)
            adapter.notifyItemInserted(curList.size - 1)
            adapter2.notifyItemRemoved(i)
        }
        rvLeft.adapter = adapter
        rvRight.adapter = adapter2
        val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                //首先回调的方法 返回int表示是否监听该方向
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT//侧滑删除
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                //滑动事件
                Collections.swap(
                    curList,
                    viewHolder.bindingAdapterPosition,
                    target.bindingAdapterPosition
                )
                adapter.notifyItemMoved(
                    viewHolder.bindingAdapterPosition,
                    target.bindingAdapterPosition
                )
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //侧滑事件
            }
        })
        helper.attachToRecyclerView(rvLeft)
    }

    private fun getNode() {
        vCall("https://www.v2ex.com/my/nodes").start(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code != 200) {
                    return
                }
                val nodeModels = Parser(response.body?.string()!!).parseToNode()
                if (nodeModels.isEmpty()) {
                    return
                }
                nodeModels.forEach {
                    initTabs.add(Tab(it.title, it.name, NODE_TYPE))
                }

                runOnUiThread {
                    initAdapter()
                }
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tab_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_reset -> reset()
            R.id.menu_save -> save()
        }

        return true
    }


    private fun reset() {
        curList.clear()
        curList.addAll(initTabs)
        remainList.clear()
        adapter.notifyDataSetChanged()
        adapter2.notifyDataSetChanged()
    }


    private fun save() {
        if (curList.isEmpty()) {
            toast("至少保留一个")
            return
        }
        val savedList = Gson().toJson(curList)

        pref.edit {
            putString(PREF_TAB, savedList)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Keys.ACTION_TAB_SETTING))

        finish()

    }


}


typealias TopicType = Int

const val TAB_TYPE: TopicType = 0
const val NODE_TYPE: TopicType = 1

data class Tab(var title: String, var path: String, var type: Int = TAB_TYPE)

class DefaultAdapter(val list: MutableList<Tab>) : RecyclerView.Adapter<DefaultAdapter.VH>() {

    var listener: ((Int, Tab) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_text_switch, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun registerListener(listener: ((Int, Tab) -> Unit)?) {
        this.listener = listener
    }

    override fun onBindViewHolder(vh: VH, position: Int) {
        vh.tv.text = list[vh.bindingAdapterPosition].title

        vh.ivDelete.setOnClickListener {
            listener?.invoke(vh.bindingAdapterPosition, list[vh.bindingAdapterPosition])
        }
    }

    class VH(containerView: View) : RecyclerView.ViewHolder(containerView) {
        val tv = containerView.findViewById<TextView>(R.id.tv)
        val ivDelete = containerView.findViewById<View>(R.id.ivDelete)
    }

}

val tabTitles = listOf(
    "最热", "全部", "热议", "最近", "技术", "创意", "好玩", "Apple", "酷工作", "交易", "城市", "问与答", "R2", "关注"
)
val tabPaths = listOf(
    "hot",
    "all",
    "heated",
    "recent",
    "tech",
    "creative",
    "play",
    "apple",
    "jobs",
    "deals",
    "city",
    "qna",
    "r2",
    "members"
)