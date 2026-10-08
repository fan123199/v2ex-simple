package im.fdx.v2ex.ui.common

import kotlin.math.roundToInt

internal data class HtmlImageSize(val width: Int, val height: Int, val inlineEmoji: Boolean)

internal fun htmlImageSize(width: Int, height: Int, textHeight: Int, availableWidth: Int): HtmlImageSize {
    val safeWidth = width.coerceAtLeast(1)
    val safeHeight = height.coerceAtLeast(1)
    // Small, roughly square inline images are commonly used as emoji in replies.
    val inlineEmoji = width in 1..64 && height in 1..64 &&
        width.toFloat() / height in 0.5f..2f
    if (inlineEmoji) {
        val targetHeight = textHeight.coerceAtLeast(1)
        return HtmlImageSize(
            (targetHeight.toFloat() * safeWidth / safeHeight).roundToInt().coerceAtLeast(1),
            targetHeight,
            true
        )
    }
    val targetWidth = if (availableWidth > 0) minOf(safeWidth, availableWidth) else safeWidth
    return HtmlImageSize(
        targetWidth,
        (targetWidth.toFloat() * safeHeight / safeWidth).roundToInt().coerceAtLeast(1),
        false
    )
}
