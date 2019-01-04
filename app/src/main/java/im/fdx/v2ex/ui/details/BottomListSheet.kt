package im.fdx.v2ex.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import im.fdx.v2ex.R
import kotlinx.android.synthetic.main.bottom_list_sheet.*

class BottomListSheet(var list: List<Reply>) : BottomSheetDialogFragment(){


    companion object {
        fun newInstance(list: List<Reply>): BottomListSheet {
            return BottomListSheet(list)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.bottom_list_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv.layoutManager = LinearLayoutManager(rv.context)
        rv.adapter = RAdater(list)
    }

    inner class RAdater(var list: List<Reply>) : RecyclerView.Adapter<TopicDetailAdapter.ItemViewHolder>() {
        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: TopicDetailAdapter.ItemViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicDetailAdapter.ItemViewHolder {
            return TopicDetailAdapter.ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reply_view, parent, false))
        }

    }
}