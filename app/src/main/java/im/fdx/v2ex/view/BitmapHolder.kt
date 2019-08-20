package im.fdx.v2ex.view

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R

@Suppress("DEPRECATION")
class BitmapHolder : BitmapDrawable() {

    private var vDrawable: Drawable? = null

    override fun draw(canvas: Canvas) {
        vDrawable?.draw(canvas)
    }

   fun setDrawable(drawable: Drawable?) {
        this.vDrawable = drawable
    }
}