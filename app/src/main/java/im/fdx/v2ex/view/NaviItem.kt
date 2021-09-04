package im.fdx.v2ex.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import im.fdx.v2ex.R

/**
 * TODO: document your custom view class.
 */
class NaviItem : FrameLayout {
    /**
     * In the example view, this drawable is drawn above the text.
     */
    var exampleDrawable: Drawable? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    var icon: Drawable? = null
    var title: String? = null

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.NaviItem, defStyle, 0
        )
        title = a.getString(
            R.styleable.NaviItem_title
        )
        icon = a.getDrawable(R.styleable.NaviItem_icon)
        a.recycle()
        inflate(context, R.layout.navi_item, this)

        findViewById<TextView>(R.id.title).text = title
        findViewById<ImageView>(R.id.icon).setImageDrawable(icon)

    }
}