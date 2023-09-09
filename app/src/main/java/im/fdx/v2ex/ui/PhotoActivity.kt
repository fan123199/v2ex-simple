package im.fdx.v2ex.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.ImageUtil
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.setUpToolbar


//todo 不用长按的方式，因为这个根本没人会用。 直接加按钮才是现在操作的王道
class PhotoActivity : AppCompatActivity() {

    private lateinit var thelist: List<String>
    private var position: Int = 0
    lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        setUpToolbar()
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        viewPager = findViewById(R.id.viewPager)
        val list = intent.getStringArrayListExtra(Keys.KEY_PHOTO)
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


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        if (ev.action == MotionEvent.ACTION_MOVE) {
            viewPager.isUserInputEnabled = ev.pointerCount <= 1
        }
        if (ev.action == MotionEvent.ACTION_UP) {
            viewPager.isUserInputEnabled = true
        }
        return super.dispatchTouchEvent(ev)
    }


    inner class MyViewPagerAdapter(val list: List<String>) : RecyclerView.Adapter<VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.pager_item, parent, false)
            return VH(itemView)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {

            val imageView = holder.itemView.findViewById(R.id.photo_view) as ImageView
            val imageUrl = list[position]
            Glide.with(imageView)
                .load(imageUrl)
                .fitCenter()
                .into(imageView)
            holder.itemView.findViewById<ImageView>(R.id.ivShare).setOnClickListener {
                ImageUtil.shareImage(this@PhotoActivity, imageUrl)
            }
            holder.itemView.findViewById<ImageView>(R.id.ivSave).setOnClickListener {
                ImageUtil.downloadImage(this@PhotoActivity, imageUrl)
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }


    class VH(itemView: View) : RecyclerView.ViewHolder(itemView)

}
