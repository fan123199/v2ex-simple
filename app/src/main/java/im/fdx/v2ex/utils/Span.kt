/*
 * Copyright (C) 2017 Simon Marquis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package span

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.MaskFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.view.View
import java.util.*

/**
 * Kotlin wrapper around SpannableStringBuilder
 * Inspired by JakeWharton's Truss and Kotlin's kotlinx.html
 * @see <a href="https://gist.github.com/JakeWharton/11274467">Truss</a>
 * @see <a href="https://github.com/Kotlin/kotlinx.html">kotlinx.html</a>
 */
@Suppress("unused")
open class Span {

    private val spans = ArrayList<Span>()

    open fun build(builder: SpannableStringBuilder = SpannableStringBuilder()): Spannable {
        spans.forEach { span -> span.build(builder) }
        return builder
    }

    fun span(what: Any, init: Node.() -> Unit): Span {
        val child = Node(what)
        child.init()
        spans.add(child)
        return this
    }

    fun text(content: String): Span {
        spans.add(Leaf(content))
        return this
    }

    operator fun String.unaryPlus() = text(this)

    fun style(style: Int, span: StyleSpan = StyleSpan(style), init: Span.() -> Unit): Span = span(span, init)
    fun normal(span: StyleSpan = StyleSpan(Typeface.NORMAL), init: Span.() -> Unit): Span = span(span, init)
    fun bold(span: StyleSpan = StyleSpan(Typeface.BOLD), init: Span.() -> Unit): Span = span(span, init)
    fun italic(span: StyleSpan = StyleSpan(Typeface.ITALIC), init: Span.() -> Unit): Span = span(span, init)
    fun boldItalic(span: StyleSpan = StyleSpan(Typeface.BOLD_ITALIC), init: Span.() -> Unit): Span = span(span, init)
    fun underline(span: UnderlineSpan = UnderlineSpan(), init: Span.() -> Unit): Span = span(span, init)

    fun typeface(family: String, span: TypefaceSpan = TypefaceSpan(family), init: Span.() -> Unit): Span = span(span, init)
    fun sansSerif(span: TypefaceSpan = TypefaceSpan("sans-serif"), init: Span.() -> Unit): Span = span(span, init)
    fun serif(span: TypefaceSpan = TypefaceSpan("serif"), init: Span.() -> Unit): Span = span(span, init)
    fun monospace(span: TypefaceSpan = TypefaceSpan("monospace"), init: Span.() -> Unit): Span = span(span, init)
    fun appearance(context: Context, appearance: Int, span: TextAppearanceSpan = TextAppearanceSpan(context, appearance), init: Span.() -> Unit): Span = span(span, init)

    fun superscript(span: SuperscriptSpan = SuperscriptSpan(), init: Span.() -> Unit): Span = span(span, init)
    fun subscript(span: SubscriptSpan = SubscriptSpan(), init: Span.() -> Unit): Span = span(span, init)

    fun strikethrough(span: StrikethroughSpan = StrikethroughSpan(), init: Span.() -> Unit): Span = span(span, init)

    fun scaleX(proportion: Float, span: ScaleXSpan = ScaleXSpan(proportion), init: Span.() -> Unit): Span = span(span, init)

    fun relativeSize(proportion: Float, span: RelativeSizeSpan = RelativeSizeSpan(proportion), init: Span.() -> Unit): Span = span(span, init)
    fun absoluteSize(size: Int, dip: Boolean = false, span: AbsoluteSizeSpan = AbsoluteSizeSpan(size, dip), init: Span.() -> Unit): Span = span(span, init)

    fun quote(color: Int = Color.BLACK, span: QuoteSpan = QuoteSpan(color), init: Span.() -> Unit): Span = span(span, init)

    fun mask(filter: MaskFilter, span: MaskFilterSpan = MaskFilterSpan(filter), init: Span.() -> Unit): Span = span(span, init)

    fun leadingMargin(every: Int = 0, span: LeadingMarginSpan = LeadingMarginSpan.Standard(every), init: Span.() -> Unit): Span = span(span, init)
    fun leadingMargin(first: Int = 0, rest: Int = 0, span: LeadingMarginSpan = LeadingMarginSpan.Standard(first, rest), init: Span.() -> Unit): Span = span(span, init)

    fun foregroundColor(color: Int = Color.BLACK, span: ForegroundColorSpan = ForegroundColorSpan(color), init: Span.() -> Unit): Span = span(span, init)
    fun backgroundColor(color: Int = Color.BLACK, span: BackgroundColorSpan = BackgroundColorSpan(color), init: Span.() -> Unit): Span = span(span, init)

    fun bullet(gapWidth: Int = 0, color: Int = Color.BLACK, span: BulletSpan = BulletSpan(gapWidth, color), init: Span.() -> Unit): Span = span(span, init)

    fun align(align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL, span: AlignmentSpan.Standard = AlignmentSpan.Standard(align), init: Span.() -> Unit): Span = span(span, init)

    fun drawableMargin(drawable: Drawable, padding: Int = 0, span: DrawableMarginSpan = DrawableMarginSpan(drawable, padding), init: Span.() -> Unit): Span = span(span, init)
    fun iconMargin(bitmap: Bitmap, padding: Int = 0, span: IconMarginSpan = IconMarginSpan(bitmap, padding), init: Span.() -> Unit): Span = span(span, init)

    fun image(context: Context, bitmap: Bitmap, verticalAlignment: Int = DynamicDrawableSpan.ALIGN_BOTTOM, span: ImageSpan = ImageSpan(context, bitmap, verticalAlignment), init: Span.() -> Unit): Span = span(span, init)
    fun image(drawable: Drawable, verticalAlignment: Int = DynamicDrawableSpan.ALIGN_BOTTOM, span: ImageSpan = ImageSpan(drawable, verticalAlignment), init: Span.() -> Unit): Span = span(span, init)
    fun image(context: Context, uri: Uri, verticalAlignment: Int = DynamicDrawableSpan.ALIGN_BOTTOM, span: ImageSpan = ImageSpan(context, uri, verticalAlignment), init: Span.() -> Unit): Span = span(span, init)
    fun image(context: Context, resourceId: Int, verticalAlignment: Int = DynamicDrawableSpan.ALIGN_BOTTOM, span: ImageSpan = ImageSpan(context, resourceId, verticalAlignment), init: Span.() -> Unit): Span = span(span, init)

    fun clickable(onClick: (ClickableSpan) -> Unit, span: ClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View?) {
            onClick(this)
        }
    }, init: Span.() -> Unit): Span = span(span, init)

    fun url(url: String, onClick: (URLSpan) -> Boolean, span: URLSpan = object : URLSpan(url) {
        override fun onClick(widget: View?) {
            if (onClick(this)) {
                super.onClick(widget)
            }
        }
    }, init: Span.() -> Unit): Span = span(span, init)

    class Node(val span: Any) : Span() {
        override fun build(builder: SpannableStringBuilder): Spannable {
            val start = builder.length
            super.build(builder)
            builder.setSpan(span, start, builder.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            return builder
        }
    }

    class Leaf(val content: Any) : Span() {
        override fun build(builder: SpannableStringBuilder): Spannable {
            builder.append(content.toString())
            return builder
        }
    }

}

fun span(init: Span.() -> Unit): Span {
    val spanWithChildren = Span()
    spanWithChildren.init()
    return spanWithChildren
}
