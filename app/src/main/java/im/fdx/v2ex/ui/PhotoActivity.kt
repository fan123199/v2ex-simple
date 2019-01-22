package im.fdx.v2ex.ui

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import im.fdx.v2ex.GlideApp
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.ImageUtil
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.setUpToolbar
import im.fdx.v2ex.view.BottomSheetMenu
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_photo.*
import kotlinx.android.synthetic.main.app_toolbar.*
import java.util.*


class PhotoActivity : AppCompatActivity() {

    private lateinit var thelist: ArrayList<V2Photo>
    private var position: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        setUpToolbar()
        thelist = intent.getParcelableArrayListExtra<V2Photo>(Keys.KEY_PHOTO)
        position = intent.getIntExtra(Keys.KEY_POSITION, 0)

        toolbar.title = "${position + 1}/${thelist.size}"

        val adapter = MyViewPagerAdapter(thelist)
        viewPager.adapter = adapter
        viewPager.currentItem = position
      viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                toolbar.title = "${position + 1}/${thelist.size}"
            }
        })
    }


  inner class MyViewPagerAdapter(val list: MutableList<V2Photo>) : androidx.viewpager.widget.PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object` as ViewGroup
        }

        override fun getCount(): Int {
            return list.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val itemView = LayoutInflater.from(container.context).inflate(R.layout.pager_item, container, false)
            val imageView = itemView.findViewById(R.id.photo_view) as ImageView
            val imageUrl = list[position].url
            GlideApp.with(imageView.context)
                .load(imageUrl)
                .into(imageView)
            container.addView(itemView)

            imageView.setOnLongClickListener {
                BottomSheetMenu(this@PhotoActivity)
                        .addItem("下载图片") {
                            ImageUtil.downloadImage(this@PhotoActivity, imageUrl)
                        }
                        .addItem("分享图片") {
                            ImageUtil.shareImage(this@PhotoActivity, imageUrl)
                        }
                        .show()
                true
            }


            return itemView

        }



      override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as ViewGroup)
        }
    }

    @Parcelize
    data class V2Photo(var url: String, var name: String? = null) : Parcelable
}
