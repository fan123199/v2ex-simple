package im.fdx.v2ex.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.PhotoActivity
import im.fdx.v2ex.ui.node.NodeActivity
import im.fdx.v2ex.ui.topic.TopicActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.ViewUtil
import im.fdx.v2ex.utils.extensions.dp2px
import im.fdx.v2ex.utils.extensions.startActivity
import org.xml.sax.Attributes
import org.xml.sax.XMLReader
import java.util.*


typealias TextType = Int

const val typeTopic: TextType = 1
const val typeComment: TextType = 2
const val typeReply: TextType = 3

/**
 * Created by fdx on 2016/9/11.
 * fdx will maintain it
 *
 *  这里非常难实现显示textView， 因为tagHandler的使用，是要对<code> <pre>等标签进行处理，但是后续没有好用的背景绘制方法。
 *  总的来说就是，在textView搞这些超纲的多样式，是不科学的。 所以请放弃念想。优化下图片显示还是可以考虑的
 *
 *  IMPORTANT!  除了换RN技术栈，不要再花时间去优化或替换这个view了，基本没有好方法了。
 *
 */
class GoodTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    var popupListener: Popup.PopupListener? = null

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        imageGetter?.clear()
    }

    private var disableTouch = false
    var imageGetter: MyImageGetter? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (disableTouch) false
        else super.onTouchEvent(event)
    }

    fun disableTouch() {
        this.disableTouch = true
    }

    @Suppress("DEPRECATION")
    fun setGoodText(text: String?, removeClick: Boolean? = false, type: TextType = typeTopic) {
        if (text.isNullOrEmpty()) {
            return
        }
        setLinkTextColor(ContextCompat.getColor(context, im.fdx.v2ex.R.color.mode))
        imageGetter = MyImageGetter(this, type)
        val spannedText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
        } else {
            Html.fromHtml(text, imageGetter, null)
        }


        val htmlSpannable = SpannableStringBuilder(spannedText)

        //移除url的下划线
        val urlSpans = htmlSpannable.getSpans(0, htmlSpannable.length, URLSpan::class.java)
        for (urlSpan in urlSpans) {
            val spanStart = htmlSpannable.getSpanStart(urlSpan)
            val spanEnd = htmlSpannable.getSpanEnd(urlSpan)
            val newUrlSpan = UrlSpanNoUnderline(urlSpan.url) { widget ->
                // url =  https://www.v2ex.com/member/fan123199
                when {
                    urlSpan.url.contains("v2ex.com/member/") -> {
                        popupListener?.onClick(widget, urlSpan.url)
                    }
                    urlSpan.url.contains("v2ex.com/t/") -> {
                        Regex("v2ex.com/t/(\\d+)").find(urlSpan.url)?.let {
                            val (topicId) = it.destructured
                            context.startActivity<TopicActivity>(Keys.KEY_TOPIC_ID to topicId)
                        }
                    }

                    urlSpan.url.contains("v2ex.com/go/") -> {
                        Regex("v2ex.com/go/(\\w+)").find(urlSpan.url)?.let {
                            val (name) = it.destructured
                            context.startActivity<NodeActivity>(Keys.KEY_NODE_NAME to name)
                        }
                    }

                    else -> CustomChrome(context).load(urlSpan.url)
                }
            }
            htmlSpannable.removeSpan(urlSpan)
            htmlSpannable.setSpan(newUrlSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        //分离 图片 span
        val imageSpans = htmlSpannable.getSpans(0, htmlSpannable.length, ImageSpan::class.java)

        val photos = imageSpans.map { it.source ?: "" }

        imageSpans.forEachIndexed { index, imageSpan ->
            //            val imageUrl = imageSpan.source
            val start = htmlSpannable.getSpanStart(imageSpan)
            val end = htmlSpannable.getSpanEnd(imageSpan)

            //生成自定义可点击的span
            val newClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    widget.context.startActivity<PhotoActivity>(
                        Keys.KEY_PHOTO to photos,
                        Keys.KEY_POSITION to index
                    )
                }
            }

            //移除 ImageSpan 中的可能存在的clickspan
            val clickableSpans = htmlSpannable.getSpans(start, end, ClickableSpan::class.java)

            if (clickableSpans != null && clickableSpans.isNotEmpty()) {
                for (span2 in clickableSpans) {
                    htmlSpannable.removeSpan(span2)
                }
            }
            htmlSpannable.setSpan(newClickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        setText(htmlSpannable)
        //不设置这一句，点击图片会跑动。
        when (removeClick) {
            false -> movementMethod = LinkMovementMethod.getInstance()
            else -> {}
        }
    }
}

class MyImageGetter(val tv: GoodTextView, val type: TextType) : Html.ImageGetter {
    //防止 Glide，将target gc了， 导致图片无法显示
    private var targetList: MutableList<CustomTarget<Bitmap>> = mutableListOf()

    override fun getDrawable(source: String?): Drawable {
        val bitmapHolder = BitmapHolder()
        if(source == null) return bitmapHolder // 某些机型 source可能为空

//        val d =  ContextCompat.getDrawable(tv.context, R.drawable.loading_image)
//        bitmapHolder.setDrawable(d)
        Log.i("GoodTextView", " begin getDrawable, Image url: $source")

        val smallestWidth = 12.dp2px()
        val bestWidth = when (type) {
            typeTopic -> ViewUtil.screenWidth - (16 * 2).dp2px()   //主题
            typeComment -> ViewUtil.screenWidth - (16 * 2 + 8 * 2).dp2px()  //附言
            typeReply -> ViewUtil.screenWidth - (16 * 2 + 26 + 8).dp2px() //评论
            else -> 0
        }

        val target = object : CustomTarget<Bitmap>() {


            override fun onLoadCleared(placeholder: Drawable?) {
            }

            override fun onLoadStarted(placeholder: Drawable?) {
                placeholder?.let {
                    val rect = Rect(0, 0, 200.dp2px(), 200.dp2px() * placeholder.intrinsicHeight /placeholder.intrinsicWidth)
                    bitmapHolder.bounds = rect
                    placeholder.bounds = rect
                    bitmapHolder.setDrawable(placeholder)
                }
            }

            override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                val targetWidth: Int = when {
                    //超小图
                    bitmap.width < smallestWidth -> {
                        smallestWidth
                    }
                    //中小图
                    bitmap.width >= smallestWidth && bitmap.width <= bestWidth * 0.3 -> {
                        bitmap.width
                    }
                    // 中图，大图，特大图
                    else -> {
                        bestWidth
                    }
                }
                val targetHeight =
                    (targetWidth * (bitmap.height.toDouble() / bitmap.width.toDouble())).toInt()
                val rectHolder = Rect(0, 0, targetWidth, targetHeight)
                val newBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false)
                bitmapHolder.bounds = rectHolder
                val drawable = newBitmap.toDrawable(tv.context.resources)
                drawable.bounds = rectHolder
                bitmapHolder.setDrawable(drawable)
                tv.text = tv.text
                tv.invalidate()
                Log.i(
                    "GoodTextView", " end getDrawable, Image height: " +
                            "${bitmapHolder.bounds}," +
                            " ${bitmap.width},${bitmap.height}"
                )
            }
        }
        Glide.with(tv)
                .asBitmap()
            .placeholder(R.drawable.loading_image_4_3)
                .load(source)
                .into(target)
        targetList.add(target)
        return bitmapHolder
    }

    fun clear() {
        targetList.clear()
    }
}


class UrlSpanNoUnderline(url: String, var clickListener: (View) -> Unit) : URLSpan(url) {

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }

    override fun onClick(widget: View) {
        clickListener(widget)
    }
}


/**
 * 有bug， 会在某些文本中崩溃，看了Html.java也没用
 */
class CodeTagHandler : Html.TagHandler {

    class Code

    override fun handleTag(
        opening: Boolean,
        tag: String?,
        output: Editable,
        xmlReader: XMLReader?
    ) {
        if (opening) {
            if (tag.equals("pre", true)) {
                start(output, Code())
            }
        } else {

            if (tag.equals("pre", true)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    end(output, Code::class.java, QuoteSpan(Color.LTGRAY, 4, 8))
                } else {
                    end(output, Code::class.java, QuoteSpan(Color.LTGRAY))

                }
            }
        }
    }

    fun start(output: Spannable, mark: Any) {
        val len = output.length
        output.setSpan(mark, len, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }


    fun end(text: Spannable, kind: Class<Code>, repl: Any) {
        val mark = getLast(text, kind)

        mark?.let {
            // start of the tag
            val where = text.getSpanStart(mark)
            text.removeSpan(mark)
            // end of the tag
            val len = text.length

            if (where != len) {
                text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

        }

    }

    private fun getLast(text: Spannable, kind: Class<Code>): Any? {
        val objs = text.getSpans(0, text.length, kind)
        return if (objs.isEmpty()) {
            null
        } else {
            objs[objs.size - 1]
        }
    }

}

