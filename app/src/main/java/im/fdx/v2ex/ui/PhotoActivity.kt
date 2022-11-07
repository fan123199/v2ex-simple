package im.fdx.v2ex.ui

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import im.fdx.v2ex.GlideApp
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.ImageUtil
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.load
import im.fdx.v2ex.utils.extensions.setUpToolbar
import im.fdx.v2ex.view.BottomSheetMenu
import kotlinx.parcelize.Parcelize
import java.util.*


class PhotoActivity : AppCompatActivity() {

    private lateinit var thelist: ArrayList<V2Photo>
    private var position: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        setUpToolbar()
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val list = intent.getParcelableArrayListExtra<V2Photo>(Keys.KEY_PHOTO)
        if (list == null) {
            finish()
            return
        }
        thelist = list
        position = intent.getIntExtra(Keys.KEY_POSITION, 0)

        toolbar.title = "${position + 1}/${thelist.size}"

        val adapter = MyViewPagerAdapter(thelist)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(position, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                toolbar.title = "${position + 1}/${thelist.size}"
            }
        })
    }


    inner class MyViewPagerAdapter(val list: MutableList<V2Photo>) : RecyclerView.Adapter<VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.pager_item, parent, false)
            return VH(itemView)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {

            val imageView = holder.itemView.findViewById(R.id.photo_view) as ImageView
            val imageUrl = list[position].url
            imageView.load(imageUrl)
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
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }


    class VH(itemView: View) : RecyclerView.ViewHolder(itemView)

    @Parcelize
    data class V2Photo(var url: String, var name: String? = null) : Parcelable
}
