package im.fdx.v2ex.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import im.fdx.v2ex.GlideApp
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.PhotoActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.dp2px
import org.jetbrains.anko.startActivity


/**
 * Created by fdx on 2016/9/11.
 * fdx will maintain it
 *
 *  这里非常难实现显示textView， 因为tagHandler的使用，是要对<code> <pre>等标签进行处理，但是后续没有好用的背景绘制方法。
 *  总的来说就是，在textView搞这些超纲的多样式，是不科学的。 所以请放弃念想。优化下图片显示还是可以考虑的
 *
 */
class GoodTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    var bestWidth = 0

    var popupListener: Popup.PopupListener? = null

    //防止 Glide，将target gc了， 导致图片无法显示
    var targetList: MutableList<SimpleTarget<Drawable>> = mutableListOf()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
//        XLog.tag(TAG).e("view width: $width")
        if (bestWidth == 0)
            bestWidth = width
    }

    @Suppress("DEPRECATION")
    fun setGoodText(text: String?, removeClick: Boolean? = false) {
        targetList.clear()
        if (text.isNullOrEmpty()) {
            return
        }
        setLinkTextColor(ContextCompat.getColor(context, R.color.mode))

        val imageGetter = MyImageGetter()
        val spannedText = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
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
            val newUrlSpan = UrlSpanNoUnderline(urlSpan.url)
            htmlSpannable.removeSpan(urlSpan)
            htmlSpannable.setSpan(newUrlSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        //分离 图片 span
        val imageSpans = htmlSpannable.getSpans(0, htmlSpannable.length, ImageSpan::class.java)

        val photos = ArrayList(imageSpans.map { PhotoActivity.AirPhoto(it.source ?: "") })

        imageSpans.forEachIndexed { index, imageSpan ->
            //            val imageUrl = imageSpan.source
            val start = htmlSpannable.getSpanStart(imageSpan)
            val end = htmlSpannable.getSpanEnd(imageSpan)

            //生成自定义可点击的span
            val newClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    widget.context.startActivity<PhotoActivity>(Keys.KEY_PHOTO to photos,
                            Keys.KEY_POSITION to index)
                }
            }

            //替换原来的ImageSpan
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
        }
    }

    @Suppress("DEPRECATION")
    private inner class BitmapHolder : BitmapDrawable() {

        private var vDrawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            vDrawable?.draw(canvas)
        }

        fun setDrawable(drawable: Drawable) {
            this.vDrawable = drawable
        }
    }


    private inner class MyImageGetter : Html.ImageGetter {

        override fun getDrawable(source: String): Drawable {
            val bitmapHolder = BitmapHolder()
            Log.i(TAG, " begin getDrawable, Image url: " + source)

            val target = object : SimpleTarget<Drawable>() {
                override fun onResourceReady(drawable: Drawable, transition: Transition<in Drawable>?) {

                    val targetWidth: Int
                    val targetHeight: Int

                    when {
                        drawable.intrinsicWidth > bestWidth || drawable.intrinsicWidth > bestWidth * 0.3 -> {
                            targetWidth = bestWidth
                            targetHeight = (targetWidth * drawable.intrinsicHeight.toDouble() / drawable.intrinsicWidth.toDouble()).toInt()
                            drawable.setBounds(0, 0, targetWidth, targetHeight)
                            bitmapHolder.setBounds(0, 0, targetWidth, targetHeight)
                        }


                        drawable.intrinsicWidth >= smallestWidth && drawable.intrinsicWidth <= bestWidth * 0.3 -> {
                            targetWidth = bestWidth
                            targetHeight = (targetWidth * drawable.intrinsicHeight.toDouble() / drawable.intrinsicWidth.toDouble()).toInt()
                            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                            bitmapHolder.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                        }

                        drawable.intrinsicWidth < smallestWidth -> {
                            targetWidth = smallestWidth
                            targetHeight = (targetWidth * drawable.intrinsicHeight.toDouble() / drawable.intrinsicWidth.toDouble()).toInt()
                            drawable.setBounds(0, 0, targetWidth, targetHeight)
                            bitmapHolder.setBounds(0, 0, targetWidth, targetHeight)
                        }

                        else -> {
                            targetWidth = drawable.intrinsicWidth
                            targetHeight = drawable.intrinsicHeight
                            drawable.setBounds((bestWidth - targetWidth) / 2, 0, (targetWidth + bestWidth) / 2, targetHeight)
                            bitmapHolder.setBounds(0, 0, bestWidth, targetHeight)
                        }
                    }

                    bitmapHolder.setDrawable(drawable)
                    this@GoodTextView.text = this@GoodTextView.text
                    Log.i(TAG, " end getDrawable, Image height: ${drawable.intrinsicHeight}, ${drawable.intrinsicWidth}, $targetHeight,$targetWidth")
                }
            }
            targetList.add(target)
            GlideApp.with(context).load(source).into(target)
            return bitmapHolder
        }
    }


    inner class UrlSpanNoUnderline : URLSpan {
        constructor(src: URLSpan) : super(src.url)
        constructor(url: String) : super(url)

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }

        override fun onClick(widget: View) {
            // url =  https://www.v2ex.com/member/fan123199
            when {
                url.contains("v2ex.com/member/") -> {
                    popupListener?.onClick(widget, url)
                }
                else -> CustomChrome(context).load(url)
            }
        }
    }


    companion object {
        private val TAG = GoodTextView::class.java.simpleName
        val smallestWidth = 12.dp2px()
    }

}



