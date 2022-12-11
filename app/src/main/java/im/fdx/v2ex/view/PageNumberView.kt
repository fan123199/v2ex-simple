package im.fdx.v2ex.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import kotlin.math.min

/**
 * TODO: document your custom view class.
 */
class PageNumberView : FrameLayout {

    private var _totalNum: Int = 1 // TODO: use a default from R.string...
    private var _currentNum: Int = 1 // TODO: use a default from R.string...
    private var _highlightColor: Int = 1 // TODO: use a default from R.string...
    private val itemSize = 7
    private val theAdapter = TheAdapter(itemSize) // 在这里切换形式
    private var et : EditText? = null
    /**
     * The text to draw
     */
    var totalNum: Int
        get() = _totalNum
        set(value) {
            _totalNum = value
            theAdapter.setTotalNum(_totalNum)
            if(totalNum> itemSize) {
                et?.isVisible = true
            }
        }

    var currentNum: Int
        get() = _currentNum
        set(value) {
            _currentNum = value
            theAdapter.setCurrentNum(_currentNum)
        }


    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }



    fun setSelectNumListener(action: (Int) -> Unit) {
        theAdapter.setAction(action)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

        inflate(context, R.layout.view_page_number, this)

        val rv = findViewById<RecyclerView>(R.id.rv)
        rv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rv.overScrollMode = OVER_SCROLL_NEVER
        rv.adapter = theAdapter

        et = findViewById<EditText>(R.id.et_num)

        et?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == IME_ACTION_DONE){
                theAdapter.setCurrentNum (v.text.toString().toInt())
            }
            true
        }

        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.PageNumberView, defStyle, 0
        )

        totalNum = a.getInt(
            R.styleable.PageNumberView_totalNum, 1
        )

        currentNum = a.getInt(
            R.styleable.PageNumberView_currentNum, 1
        )

        _highlightColor = a.getColor(
            R.styleable.PageNumberView_highlightColor,
            Color.GRAY
        )
        a.recycle()
    }
}

class TheViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tv: TextView = itemView.findViewById(R.id.tv_num)
}

// size 必须是奇数， 不然效果很奇怪。
class TheAdapter(val size: Int) : RecyclerView.Adapter<TheViewHolder>() {
    private var  act : ((Int) -> Unit)? = null
    private var currentNum = 1

    private var totalNum = 1

    fun setCurrentNum(value:Int) {
        currentNum = value.coerceIn(1, totalNum)
        notifyDataSetChanged()
        act?.invoke(currentNum)
    }

    fun setTotalNum(value: Int) {
        totalNum = value
        notifyDataSetChanged()
    }


    private val showDatas = (1..size).map { it.toString() }.toMutableList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TheViewHolder {
        return TheViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.page_item, parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: TheViewHolder, position: Int) {
        Log.d("number", "$currentNum : $totalNum")
        holder.tv.setOnClickListener {
            currentNum = showDatas[position].toInt()
            notifyDataSetChanged()
            act?.invoke(currentNum)
        }
        showDatas[0] = "1"
        showDatas[size - 1] = totalNum.toString()

        if (totalNum > size) {
            if (currentNum > size / 2 + 1 && currentNum < totalNum - size / 2) {
                showDatas[1] = "..."
                showDatas.forEachIndexed { index, s ->
                    if(index > 1 && index < size - 2)
                        showDatas[index] = (currentNum + index - size / 2).toString()
                }
                showDatas[size - 2] = "..."

            } else if (currentNum <= size / 2 + 1) {
                showDatas.forEachIndexed { index, s ->
                    if(index >= 1 && index < size - 2)
                        showDatas[index] = (index + 1).toString()
                }
                showDatas[size - 2] = "..."
            } else if(currentNum >= totalNum - size / 2) {
                showDatas[1] = "..."
                showDatas.forEachIndexed { index, s ->
                    if(index > 1 && index < size - 1)
                        showDatas[index] = (totalNum - size + 1 + index).toString()
                }
            }
        }
        holder.tv.text = showDatas[position]
        holder.tv.isSelected = currentNum.toString() == holder.tv.text.toString()
        if (holder.tv.isSelected) {
            holder.tv.setTextColor(ContextCompat.getColor(myApp, R.color.primary))
        } else {
            holder.tv.setTextColor(ContextCompat.getColor(myApp, R.color.hint))
        }
        holder.tv.isEnabled = "..." != holder.tv.text.toString()
    }

    override fun getItemCount(): Int {
        return  min(totalNum,  showDatas.size)
    }

    fun setAction(action: (Int) -> Unit) {
        act = action
    }



}


//不带省略号版本， 但是实际体验不是很好，有滑动冲突，以及无法快速点击到最开始
class SimpleAdapter() : RecyclerView.Adapter<TheViewHolder>() {
    private var  act : ((Int) -> Unit)? = null
    private var currentNum = 1

    private var totalNum = 1
    fun setCurrentNum(value:Int) {
        currentNum = value.coerceIn(1, totalNum)
        notifyDataSetChanged()
        act?.invoke(currentNum)
    }

    fun setTotalNum(value: Int) {
        totalNum = value
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TheViewHolder {
        return TheViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.page_item, parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: TheViewHolder, position: Int) {
        Log.d("number", "$currentNum : $totalNum")
        holder.tv.setOnClickListener {
            currentNum = (position + 1)
            notifyDataSetChanged()
            act?.invoke(currentNum)
        }
        holder.tv.text = (position + 1).toString()
        holder.tv.isSelected = currentNum.toString() == holder.tv.text.toString()
        if (holder.tv.isSelected) {
            holder.tv.setTextColor(ContextCompat.getColor(myApp, R.color.primary))
        } else {
            holder.tv.setTextColor(ContextCompat.getColor(myApp, R.color.hint))
        }
        holder.tv.isEnabled = "..." != holder.tv.text.toString()
    }

    override fun getItemCount(): Int {
        return  totalNum
    }

    fun setAction(action: (Int) -> Unit) {
        act = action
    }



}

