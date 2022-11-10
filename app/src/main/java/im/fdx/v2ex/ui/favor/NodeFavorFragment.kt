package im.fdx.v2ex.ui.favor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.elvishew.xlog.XLog
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import im.fdx.v2ex.R
import im.fdx.v2ex.network.*
import im.fdx.v2ex.ui.node.AllNodesAdapter
import im.fdx.v2ex.utils.extensions.hideNoContent
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.showNoContent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


/**
 * 在主题收藏下的节点页面。 图标+文字的图标形式
 */
class NodeFavorFragment : Fragment() {

  private lateinit var swipe: SwipeRefreshLayout
    private lateinit var adapter: AllNodesAdapter
    private lateinit var flContainer: FrameLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        XLog.d("NodeFavorFragment onCreateView")
        val view = inflater.inflate(R.layout.fragment_tab_article, container, false)
      val recyclerView: RecyclerView = view.findViewById(R.id.rv_container)
        adapter = AllNodesAdapter(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context, 4)

//            FlexboxLayoutManager(activity, FlexDirection.ROW).apply {
//            justifyContent = JustifyContent.SPACE_BETWEEN
//        }

        swipe = view.findViewById(R.id.swipe_container)
        swipe.initTheme()
        swipe.setOnRefreshListener { getNode() }

        flContainer = view.findViewById(R.id.fl_container)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe.isRefreshing = true
        getNode()
    }

    private fun getNode() {
        vCall("https://www.v2ex.com/my/nodes").start(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(activity, swipe = swipe)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code != 200) {
                    NetManager.dealError(activity, response.code, swipe)
                    return
                }
                val nodeModels = Parser(response.body?.string()!!).parseToNode()
                if (nodeModels.isEmpty()) {
                    activity?.runOnUiThread {
                        adapter.clear()
                        adapter.notifyDataSetChanged()
                      flContainer.showNoContent()
                        swipe.isRefreshing = false
                    }
                    return
                }
                adapter.clear()
                adapter.addAll(nodeModels)
                activity?.runOnUiThread {
                    flContainer.hideNoContent()
                    adapter.notifyDataSetChanged()
                    swipe.isRefreshing = false
                }
            }
        })
    }

}