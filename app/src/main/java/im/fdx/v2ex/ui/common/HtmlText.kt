package im.fdx.v2ex.ui.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.TextUtils
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
import androidx.core.text.getSpans
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import im.fdx.v2ex.utils.extensions.logi
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.*

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                movementMethod = ClickableMovementMethod
                setTextColor(textColor)
                setLinkTextColor(linkColor)
                textSize = style.fontSize.value
            }
        },
        update = { textView ->
            val imageGetter = CoilImageGetter(textView)
            val spanned = HtmlCompat.fromHtml(
                html,
                HtmlCompat.FROM_HTML_MODE_LEGACY,
                imageGetter,
                null
            )
            
            if (spanned is Spannable) {
                val imageSpans = spanned.getSpans<ImageSpan>()
                val photos = imageSpans.map { it.source ?: "" }
                imageSpans.forEachIndexed { index, imageSpan ->
                    val start = spanned.getSpanStart(imageSpan)
                    val end = spanned.getSpanEnd(imageSpan)
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            onImageClick(photos, index)
                        }
                    }
                    spanned.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            textView.text = spanned
        }
    )
}

class CoilImageGetter(private val textView: TextView) : Html.ImageGetter {

    override fun getDrawable(source: String?): Drawable {
        Log.d("HtmlText", "getDrawable: $source")
        val drawablePlaceholder = UrlDrawable()
        val context = textView.context

        if (source == null) return drawablePlaceholder

        CoroutineScope(Dispatchers.Main).launch {
            val loader = ImageLoader(context)
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
                
                // Scale to fit width if necessary
                val maxWidth = textView.width - textView.paddingLeft - textView.paddingRight
                if (maxWidth > 0 && width > maxWidth) {
                    val scale = maxWidth.toFloat() / width.toFloat()
                    val newHeight = (height * scale).toInt()
                    drawable.setBounds(0, 0, maxWidth, newHeight)
                    drawablePlaceholder.setBounds(0, 0, maxWidth, newHeight)
                } else {
                    drawable.setBounds(0, 0, width, height)
                    drawablePlaceholder.setBounds(0, 0, width, height)
                }

                drawablePlaceholder.actualDrawable = drawable
                textView.text = textView.text // Trigger refresh
                textView.invalidate()
            }
        }

        return drawablePlaceholder
    }

    class UrlDrawable : BitmapDrawable() {
        var actualDrawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            actualDrawable?.draw(canvas)
        }
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
