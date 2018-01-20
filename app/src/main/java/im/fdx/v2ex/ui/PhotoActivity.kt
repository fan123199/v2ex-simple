package im.fdx.v2ex.ui

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.load
import im.fdx.v2ex.utils.extensions.setUpToolbar
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_photo.*
import kotlinx.android.synthetic.main.app_toolbar.*
import java.util.*


class PhotoActivity : AppCompatActivity() {

    private lateinit var thelist: ArrayList<AirPhoto>
    private var position: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        setUpToolbar()
        thelist = intent.getParcelableArrayListExtra<AirPhoto>(Keys.KEY_PHOTO)
        position = intent.getIntExtra(Keys.KEY_POSITION, 0)

        toolbar.title = "${position + 1}/${thelist.size}"

        val adapter = MyViewPagerAdapter(thelist)
        viewPager.adapter = adapter
        viewPager.currentItem = position
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                toolbar.title = "${position + 1}/${thelist.size}"
            }
        })
    }


    inner class MyViewPagerAdapter(val list: MutableList<AirPhoto>) : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object` as ViewGroup
        }

        override fun getCount(): Int {
            return list.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val itemView = LayoutInflater.from(container.context).inflate(R.layout.pager_item, container, false)

            val imageView = itemView.findViewById(R.id.photo_view) as ImageView
            imageView.load(list[position].url)
            container.addView(itemView)
            return itemView

        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as ViewGroup)
        }
    }

    @Parcelize
    data class AirPhoto(var url: String, var name: String? = null) : Parcelable
}
