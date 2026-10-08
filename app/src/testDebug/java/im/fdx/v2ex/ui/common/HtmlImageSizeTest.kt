package im.fdx.v2ex.ui.common

import org.junit.Assert.*
import org.junit.Test

class HtmlImageSizeTest {
    @Test
    fun tinyEmojiUsesTextPixelsOnHighDensityScreens() {
        val size = htmlImageSize(16, 16, textHeight = 49, availableWidth = 800)
        assertEquals(HtmlImageSize(49, 49, true), size)
    }

    @Test
    fun emojiFollowsFontSizeAndKeepsAspectRatio() {
        assertEquals(HtmlImageSize(32, 48, true), htmlImageSize(16, 24, 48, 800))
        assertEquals(HtmlImageSize(48, 72, true), htmlImageSize(16, 24, 72, 800))
    }

    @Test
    fun photosAndWideBadgesKeepPhotoBehavior() {
        assertEquals(HtmlImageSize(600, 450, false), htmlImageSize(1200, 900, 48, 600))
        assertEquals(HtmlImageSize(200, 200, false), htmlImageSize(200, 200, 48, 600))
        assertEquals(HtmlImageSize(64, 12, false), htmlImageSize(64, 12, 48, 600))
    }

    @Test
    fun invalidOrUnmeasuredSizesDoNotProduceZeroBounds() {
        val invalid = htmlImageSize(0, 0, 48, 0)
        assertFalse(invalid.inlineEmoji)
        assertTrue(invalid.width > 0 && invalid.height > 0)
        assertEquals(HtmlImageSize(900, 600, false), htmlImageSize(900, 600, 48, 0))
    }
}
