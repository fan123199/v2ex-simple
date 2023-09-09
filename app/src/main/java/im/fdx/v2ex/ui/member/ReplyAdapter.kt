package im.fdx.v2ex.ui.member

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.topic.TopicActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.view.GoodTextView
import im.fdx.v2ex.utils.extensions.startActivity

/**
 * Created by fdx on 2017/7/15.
 * fdx will maintain it
 * 在用户信息的回复页面
 */
class ReplyAdapter(val activity: Activity,
                   var list: MutableList<MemberReplyModel> = mutableListOf())
  : androidx.recyclerview.widget.RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ReplyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reply_member, parent, false))

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {

        val reply = list[position]
        holder.tvTitle.text =reply.createdOriginal +  " 回复了主题：\n" + reply.topic.title
        holder.tvContent.setGoodText(reply.content, true)
//        holder.tvTime.text = reply.createdOriginal

        holder.itemView.setOnClickListener {
            activity.startActivity<TopicActivity>(Keys.KEY_TOPIC_ID to reply.topic.id)
        }
    }


    fun updateItem(newItems: List<MemberReplyModel>) {
//        val diffResult = DiffUtil.calculateDiff(DiffReply(list, newItems))
        list.clear()
        list.addAll(newItems)
        notifyDataSetChanged()
//        diffResult.dispatchUpdatesTo(this)

    }

    fun addItems(newItems: List<MemberReplyModel>) {
        val old = list.toList()
        list.addAll(newItems)
        val diffResult = DiffUtil.calculateDiff(DiffReply(old, list))
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = list.size

  inner class ReplyViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvContent: GoodTextView = itemView.findViewById(R.id.tv_content_reply)
        val tvTime: TextView = itemView.findViewById(R.id.tv_create)
    }
}