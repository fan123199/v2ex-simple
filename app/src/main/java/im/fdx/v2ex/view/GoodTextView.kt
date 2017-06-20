package im.fdx.v2ex.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.elvishew.xlog.XLog
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.ContentUtils
import im.fdx.v2ex.utils.ViewUtil
import im.fdx.v2ex.utils.extensions.dp2px


/**
 * Created by fdx on 2016/9/11.
 * fdx will maintain it
 */
class GoodTextView : android.support.v7.widget.AppCompatTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    @Suppress("DEPRECATION")
    fun setGoodText(text: String) {
        if (TextUtils.isEmpty(text)) {
            return
        }
        setLinkTextColor(ContextCompat.getColor(context, R.color.primary))

        val formContent = ContentUtils.format(text)
        val imageGetter = MyImageGetter()
        val spannedText = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(formContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
        } else {
            Html.fromHtml(formContent, imageGetter, null)
        }


        val htmlSpannable = SpannableStringBuilder(spannedText)
        //
        //分离 图片 span
        val imageSpans = htmlSpannable.getSpans(0, htmlSpannable.length, ImageSpan::class.java)

        val urlSpans = htmlSpannable.getSpans(0, htmlSpannable.length, URLSpan::class.java)

        for (urlSpan in urlSpans) {

            val spanStart = htmlSpannable.getSpanStart(urlSpan)
            val spanEnd = htmlSpannable.getSpanEnd(urlSpan)
            val newUrlSpan = UrlSpanNoUnderline(urlSpan.url)

            htmlSpannable.removeSpan(urlSpan)
            htmlSpannable.setSpan(newUrlSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        }


        for (imageSpan in imageSpans) {

            val imageUrl = imageSpan.source
            val start = htmlSpannable.getSpanStart(imageSpan)
            val end = htmlSpannable.getSpanEnd(imageSpan)

            //生成自定义可点击的span
            val newClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl))
                    context.startActivity(intent)
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
        movementMethod = LinkMovementMethod.getInstance()
    }

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

            //怪不得一样的图片。放在了类里。
            val bitmapHolder = BitmapHolder()
            Log.i(TAG, " Image url: " + source)

            val target = object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {

                    XLog.tag(TAG).d("bp.getByteCount() " + bitmap.byteCount
                            + "\nbp.getAllocationByteCount() = " + bitmap.allocationByteCount
                            + "\nbp.getWidth() = " + bitmap.width
                            + "\nbp.getHeight() =" + bitmap.height
                            + "\nbp.getDensity() = " + bitmap.density
                            + "\ngetWidth() = " + this@GoodTextView.width
                            + "\ngetHeight() = " + this@GoodTextView.height
                            + "\ngetMeasuredWidth()" + measuredWidth
                            + "\ngetMeasuredHeight()" + measuredHeight
                    )


                    var targetWidth = ViewUtil.screenSize[1] - 36.dp2px()
                    val targetHeight: Int

                    val minWidth = 12.dp2px()
                    when {
                        bitmap.width > targetWidth -> {
                            targetWidth = ViewUtil.screenSize[1] - 36.dp2px()
                            targetHeight = (targetWidth * bitmap.height.toDouble() / bitmap.width.toDouble()).toInt()
                        }
                        bitmap.width < minWidth -> {
                            targetWidth = minWidth
                            targetHeight = (targetWidth * bitmap.height.toDouble() / bitmap.width.toDouble()).toInt()
                        }
                        else -> {
                            targetWidth = bitmap.width
                            targetHeight = bitmap.height
                        }
                    }

                    val result = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true) //压缩
                    val drawable = BitmapDrawable(context.resources, result)
                    drawable.setBounds(0, 0, result.width, result.height)
                    bitmapHolder.setBounds(0, 0, result.width, result.height)
                    bitmapHolder.setDrawable(drawable)
                    this@GoodTextView.text = this@GoodTextView.text
                    invalidate()
                }

                override fun onBitmapFailed(errorDrawable: Drawable?) {
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                }
            }

            Picasso.with(context).load(source).into(target)
            return bitmapHolder
        }
    }


    inner class UrlSpanNoUnderline : URLSpan {
        constructor(src: URLSpan) : super(src.url) {}

        constructor(url: String) : super(url) {}

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }

        override fun onClick(widget: View) = CustomChrome(context).load(url)

    }


    /**
     * 缩放图片
     */
    private inner class ImageTransform : Transformation {

        private val key = "ImageTransform"

        override fun transform(source: Bitmap): Bitmap {
            val targetWidth = ViewUtil.screenSize[1] - 36.dp2px()
            Log.i(TAG, targetWidth.toString() + "targetWidth")
            if (source.width == 0) {
                return source
            }
            //如果图片大于设置的宽度，做处理
            if (source.width > targetWidth) {
                val targetHeight = (targetWidth * source.height.toDouble() / source.width.toDouble()).toInt()

                if (targetHeight != 0 && targetWidth != 0) {
                    val result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true) //true会压缩
                    if (result != source) {
                        // Same bitmap is returned if sizes are the same
                        source.recycle()
                    }
                    return result
                } else {
                    return source
                }
            } else {
                return source
            }
        }

        override fun key() = key
    }

    companion object {

        val REQUEST_CODE = 200
        private val TAG = GoodTextView::class.java.simpleName

        fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and width
                val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

                // Choose the smallest ratio as inSampleSize value, this will guarantee
                // a final image with both dimensions larger than or equal to the
                // requested height and width.
                inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            }

            return inSampleSize
        }

        fun decodeSampledBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap {

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeFile(path, options)
        }
    }

}

