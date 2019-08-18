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
        if(vDrawable == null) {
            //todo 加入占位图
//            val d = MyApp.get().getDrawable(R.drawable.ic_github)
//            d?.draw(canvas)
        } else {
            vDrawable?.draw(canvas)
        }
    }

    fun setDrawable(drawable: Drawable) {
        this.vDrawable = drawable
    }
}