package im.fdx.v2ex.ui.member

import android.app.Activity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.details.DetailsActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.view.GoodTextView
import org.jetbrains.anko.startActivity

/**
 * Created by fdx on 2017/7/15.
 * fdx will maintain it
 */
class ReplyAdapter(val activity: Activity,
                   var list: MutableList<MemberReplyModel> = mutableListOf())
    : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ReplyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reply_member, parent, false))

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {

        val reply = list[position]
        holder.tvTitle.text = reply.topic.title
        holder.tvContent.setGoodText(reply.content, true)
        holder.tvTime.text = TimeUtil.getRelativeTime(reply.create)

        holder.itemView.setOnClickListener {
            activity.startActivity<DetailsActivity>(Keys.KEY_TOPIC_ID to reply.topic.id)
        }
    }


    fun firstLoadItems(newItems: List<MemberReplyModel>) {
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

    inner class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvContent: GoodTextView = itemView.findViewById(R.id.tv_content_reply)
        val tvTime: TextView = itemView.findViewById(R.id.tv_create)
    }
}