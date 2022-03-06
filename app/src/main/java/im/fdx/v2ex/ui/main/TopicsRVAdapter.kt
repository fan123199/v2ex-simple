package im.fdx.v2ex.ui.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import im.fdx.v2ex.ui.MyCallback
import im.fdx.v2ex.ui.topic.TopicActivity
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.NodeActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.load
import im.fdx.v2ex.view.GoodTextView

/**
 * Created by a708 on 15-8-14.
 * 主页的Adapter，就一个普通的RecyclerView
 */
class TopicsRVAdapter(private val fragment: Fragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var mTopicList: MutableList<Topic> = mutableListOf()

  fun updateItems(newItems: List<Topic>) {
    val callback = MyCallback(this)
    val diffResult = DiffUtil.calculateDiff(MyDiffCallback(mTopicList, newItems))
    mTopicList.clear()
    mTopicList.addAll(newItems)
    diffResult.dispatchUpdatesTo(callback)
  }

  fun clearAndNotify() {
    mTopicList.clear()
    notifyDataSetChanged()
  }

  fun getList(): MutableList<Topic> {
    return mTopicList
  }

  fun clear() {
    mTopicList.clear()
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
    return MainViewHolder(LayoutInflater.from(fragment.activity).inflate(R.layout.item_topic_view, parent, false))
  }

  //Done 对TextView进行赋值, 也就是操作
  override fun onBindViewHolder(holder2: RecyclerView.ViewHolder, position: Int) {
    val currentTopic = mTopicList[holder2.bindingAdapterPosition]
    val holder = holder2 as MainViewHolder
    holder.tvTitle.maxLines = 2
    holder.tvTitle.text = currentTopic.title
    holder.itemView.setOnClickListener{

      fragment.startActivity(Intent(myApp, TopicActivity::class.java)
              .apply {
                putExtras(bundleOf(
                        Keys.KEY_TOPIC_MODEL to currentTopic,
                        Keys.KEY_TOPIC_LIST to mTopicList,
                        Keys.KEY_POSITION to holder.bindingAdapterPosition)) })
    }
    holder.tvContent.isGone = true

    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    //            holder.tvContent.setTransitionName("header");
    //        }
    if (currentTopic.replies == null) {
      holder.tvReplyNumber.isGone = true
    } else {
      holder.tvReplyNumber.isGone = false
      holder.tvReplyNumber.text = currentTopic.replies.toString()
    }
    holder.tvAuthor.text = currentTopic.member?.username
    holder.tvNode.text = currentTopic.node?.title
    holder.tvCreated.text = TimeUtil.getRelativeTime(currentTopic.created)
    holder.ivAvatar.load(currentTopic.member?.avatarNormalUrl)


    holder.tvNode.setOnClickListener{
      fragment.startActivity(Intent(myApp, NodeActivity::class.java).apply { putExtras(bundleOf(Keys.KEY_NODE_NAME to currentTopic.node?.name!!)) })
    }
    holder.ivAvatar.setOnClickListener{
      fragment.startActivity(Intent(myApp, MemberActivity::class.java).apply { putExtras(bundleOf(Keys.KEY_USERNAME to currentTopic.member?.username!!)) })
    }

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
    var divider: View = container.findViewById(R.id.divider)
  }
}
