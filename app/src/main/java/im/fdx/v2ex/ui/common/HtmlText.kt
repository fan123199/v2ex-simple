package im.fdx.v2ex.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.view.MotionEvent
import android.text.Selection
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import android.view.View
import android.text.Spannable
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.URLSpan
import androidx.core.text.getSpans
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.*

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onLongClick: (() -> Unit)? = null,
    onMentionClick: ((String, Int?) -> Unit)? = null
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            LongPressHtmlTextView(ctx).apply {
                movementMethod = ClickableMovementMethod
                setTextColor(textColor)
                setLinkTextColor(linkColor)
                textSize = style.fontSize.value
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            textView.setLinkTextColor(linkColor)
            textView.textSize = style.fontSize.value
            textView.setOnLongClickListener(onLongClick?.let { callback ->
                View.OnLongClickListener {
                    callback()
                    true
                }
            })
            textView.isLongClickable = onLongClick != null
            textView.imageGetter?.clear()
            val imageGetter = CoilImageGetter(textView)
            textView.imageGetter = imageGetter
            val spanned = HtmlCompat.fromHtml(
                html,
                HtmlCompat.FROM_HTML_MODE_LEGACY,
                imageGetter,
                null
            )
            
            if (spanned is Spannable) {
                onMentionClick?.let { onMention ->
                    spanned.getSpans<URLSpan>().forEach { link ->
                        val start = spanned.getSpanStart(link)
                        val end = spanned.getSpanEnd(link)
                        val mention = parseReplyMention(link.url, spanned, start, end)
                            ?: return@forEach
                        spanned.removeSpan(link)
                        spanned.setSpan(object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                onMention(mention.username, mention.floor)
                            }
                        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                val imageSpans = spanned.getSpans<ImageSpan>().map { original ->
                    val start = spanned.getSpanStart(original)
                    val end = spanned.getSpanEnd(original)
                    spanned.removeSpan(original)
                    TextAlignedImageSpan(original.drawable, original.source).also {
                        spanned.setSpan(it, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                imageSpans.forEach { imageSpan ->
                    val start = spanned.getSpanStart(imageSpan)
                    val end = spanned.getSpanEnd(imageSpan)
                    // A wrapping HTML link must not override the image's own interaction policy.
                    spanned.getSpans<ClickableSpan>(start, end).forEach { spanned.removeSpan(it) }
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            // Classify using decoded dimensions, not the URL or file extension.
                            val photoSpans = imageSpans.filter {
                                (it.drawable as? CoilImageGetter.UrlDrawable)?.isPreviewable == true
                            }
                            val index = photoSpans.indexOf(imageSpan)
                            if (index < 0) return
                            val photos = photoSpans.map { it.source.orEmpty() }
                            onImageClick(photos, index)
                        }
                    }
                    spanned.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            textView.text = spanned
        },
        onRelease = { it.imageGetter?.clear() }
    )
}

/** Keeps native text/link handling while allowing the containing reply's long-press menu. */
internal class LongPressHtmlTextView(context: Context) : TextView(context) {
    private var longPressConsumed = false
    var imageGetter: CoilImageGetter? = null

    override fun performLongClick(): Boolean {
        return super.performLongClick().also { longPressConsumed = it }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            longPressConsumed = false
        }
        if (event.actionMasked == MotionEvent.ACTION_UP && longPressConsumed) {
            // LinkMovementMethod otherwise opens the link/image when the finger is released.
            val cancel = MotionEvent.obtain(event).apply { action = MotionEvent.ACTION_CANCEL }
            try {
                super.onTouchEvent(cancel)
            } finally {
                cancel.recycle()
            }
            (text as? Spannable)?.let { Selection.removeSelection(it) }
            longPressConsumed = false
            return true
        }
        if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
            longPressConsumed = false
            (text as? Spannable)?.let { Selection.removeSelection(it) }
        }
        return super.onTouchEvent(event)
    }
}

class CoilImageGetter(private val textView: TextView) : Html.ImageGetter {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun clear() = scope.cancel()

    override fun getDrawable(source: String?): Drawable {
        Log.d("HtmlText", "getDrawable: $source")
        val drawablePlaceholder = UrlDrawable()
        val context = textView.context

        if (source == null) return drawablePlaceholder

        scope.launch {
            val loader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(source)
                .build()

            Log.d("HtmlText", "Loading image: $source")
            val result = withContext(Dispatchers.IO) {
                loader.execute(request)
            }

            if (result is SuccessResult) {
                Log.d("HtmlText", "Image loaded successfully: $source")
                val bitmap = (result.drawable as BitmapDrawable).bitmap
                val res = context.resources
                val drawable = BitmapDrawable(res, bitmap)

                val width = bitmap.width
                val height = bitmap.height
                
                val maxWidth = textView.width - textView.paddingLeft - textView.paddingRight
                val metrics = textView.paint.fontMetricsInt
                val size = htmlImageSize(width, height, metrics.descent - metrics.ascent, maxWidth)
                drawable.setBounds(0, 0, size.width, size.height)
                drawablePlaceholder.setBounds(0, 0, size.width, size.height)
                drawablePlaceholder.inlineEmoji = size.inlineEmoji

                drawablePlaceholder.actualDrawable = drawable
                if (size.inlineEmoji) {
                    (textView.text as? Spannable)?.let { text ->
                        text.getSpans<ImageSpan>().filter { it.drawable === drawablePlaceholder }.forEach { span ->
                            text.getSpans<ClickableSpan>(text.getSpanStart(span), text.getSpanEnd(span))
                                .forEach { text.removeSpan(it) }
                        }
                    }
                }
                textView.text = textView.text // Trigger refresh
                textView.invalidate()
            }
        }

        return drawablePlaceholder
    }

    class UrlDrawable : BitmapDrawable() {
        var actualDrawable: Drawable? = null
        var inlineEmoji: Boolean = false
        val isPreviewable: Boolean get() = actualDrawable != null && !inlineEmoji

        override fun draw(canvas: Canvas) {
            actualDrawable?.draw(canvas)
        }
    }
}

/** Emoji align with the surrounding font instead of sitting below its baseline. */
private class TextAlignedImageSpan(drawable: Drawable, source: String?) : ImageSpan(drawable, source.orEmpty()) {
    private val inlineEmoji: Boolean
        get() = (drawable as? CoilImageGetter.UrlDrawable)?.inlineEmoji == true

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        if (!inlineEmoji) return super.getSize(paint, text, start, end, fm)
        fm?.let { paint.getFontMetricsInt(it) }
        return drawable.bounds.width()
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        if (!inlineEmoji) {
            super.draw(canvas, text, start, end, x, top, y, bottom, paint)
            return
        }
        val metrics = paint.fontMetricsInt
        canvas.save()
        canvas.translate(x, y + (metrics.ascent + metrics.descent - drawable.bounds.height()) / 2f)
        drawable.draw(canvas)
        canvas.restore()
    }
}

object ClickableMovementMethod : LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val link = buffer.getSpans(off, off, ClickableSpan::class.java)
            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget)
                } else {
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])
                    )
                }
                return true
            } else {
                Selection.removeSelection(buffer)
            }
        }
        return false
    }
}
