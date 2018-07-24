package im.fdx.v2ex.ui.main

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.MyCallback
import im.fdx.v2ex.ui.details.DetailsActivity
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.NodeActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.load
import im.fdx.v2ex.view.GoodTextView
import org.jetbrains.anko.startActivity

/**
 * Created by a708 on 15-8-14.
 * 主页的Adapter，就一个普通的RecyclerView
 */
class TopicsRVAdapter(private val mContext: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mInflater = LayoutInflater.from(mContext)
    private var mTopicList: MutableList<Topic> = mutableListOf()

    fun updateItems(newItems: List<Topic>) {
        val callback = MyCallback()
        callback.adapter = this
        val diffResult = DiffUtil.calculateDiff(MyDiffCallback(mTopicList, newItems))
        diffResult.dispatchUpdatesTo(callback)
        mTopicList.clear()
        mTopicList.addAll(newItems)
    }

    fun clear() {
        mTopicList.clear()
        notifyDataSetChanged()
    }

    fun addAllItems(newItems: List<Topic>) {
        val old = mTopicList.toList()
        mTopicList.addAll(newItems)
        val diffResult = DiffUtil.calculateDiff(MyDiffCallback(old, mTopicList))
        diffResult.dispatchUpdatesTo(this)
    }


    //Done onCreateViewHolder一般就这样.除了layoutInflater,没有什么变动
    // 20150916,可以对View进行Layout的设置。
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(mInflater.inflate(R.layout.item_topic_view, parent, false))
    }

    //Done 对TextView进行赋值, 也就是操作
    override fun onBindViewHolder(holder2: RecyclerView.ViewHolder, position: Int) {
        val currentTopic = mTopicList[position]
        val listener = MyOnClickListener(mContext, currentTopic)
        val holder = holder2 as MainViewHolder
        holder.tvTitle.maxLines = 2
        holder.tvTitle.text = currentTopic.title
        holder.itemView.setOnClickListener(listener)
        holder.tvContent.visibility = View.GONE

        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //            holder.tvContent.setTransitionName("header");
        //        }
        holder.tvReplyNumber.text = currentTopic.replies.toString()
        holder.tvAuthor.text = currentTopic.member?.username
        holder.tvNode.text = currentTopic.node?.title
        holder.tvCreated.text = TimeUtil.getRelativeTime(currentTopic.created)
        holder.ivAvatar.load(currentTopic.member?.avatarNormalUrl)
        holder.tvNode.setOnClickListener(listener)
        holder.ivAvatar.setOnClickListener(listener)

    }

    override fun getItemCount() = mTopicList.size

    // 这是构建一个引用 到每个数据item的视图.用findViewById将视图的元素与变量对应起来,。
    // 用static就是为了复用
    open class MainViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        var tvTitle: TextView = container.findViewById(R.id.tv_title)
        var tvContent: GoodTextView = container.findViewById(R.id.tv_content)
        var tvReplyNumber: TextView = container.findViewById(R.id.tv_reply_number)
        var tvCreated: TextView = container.findViewById(R.id.tv_created)
        var tvAuthor: TextView = container.findViewById(R.id.tv_author)
        var ivAvatar: CircleImageView = container.findViewById(R.id.iv_avatar_profile)
        var tvNode: TextView = container.findViewById(R.id.tv_node)
        var view: View = container.findViewById(R.id.divider)
    }

    class MyOnClickListener(private val context: Context, private val topic: Topic) : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.iv_avatar_profile -> context.startActivity<MemberActivity>(Keys.KEY_USERNAME to topic.member?.username!!)
                R.id.tv_node -> context.startActivity<NodeActivity>(Keys.KEY_NODE_NAME to topic.node?.name!!)
                R.id.main_text_view -> context.startActivity<DetailsActivity>("model" to topic)
            }
        }
    }
}
