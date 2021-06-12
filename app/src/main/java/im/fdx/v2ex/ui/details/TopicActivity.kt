package im.fdx.v2ex.ui.details

import android.content.*
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import im.fdx.v2ex.databinding.ActivityDetailsBinding
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.view.ZoomOutPageTransform
import org.jetbrains.anko.toast



class TopicActivity : BaseActivity() {

  private lateinit var binding: ActivityDetailsBinding
  private lateinit var vpAdapter: VpAdapter

  private lateinit var mTopicId :String

  private var position = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDetailsBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    vpAdapter = VpAdapter(supportFragmentManager)
    parseIntent(intent)
  }


  private fun parseIntent(intent: Intent) {
    val data = intent.data
    val topicModel = intent.getParcelableExtra<Topic>(Keys.KEY_TOPIC_MODEL)
    val topicId = intent.getStringExtra(Keys.KEY_TOPIC_ID)
    val list = intent.getParcelableArrayListExtra<Topic>(Keys.KEY_TOPIC_LIST)
    val pos = intent.getIntExtra(Keys.KEY_POSITION, 0)
    mTopicId = when {
      data != null -> {
        data.pathSegments[1]
      }
      topicModel != null -> {
        topicModel.id
      }
      topicId != null -> {
        topicId
      }
      else -> {
        ""
      }
    }

    if(list!=null) {
      position = pos
    }

    if (mTopicId.isEmpty()) {
      toast("主题打开失败")
      finish()
      return
    }

    //如果是从首页打开，那么会有所有列表信息，那么就可以获取到列表信息，达到左右滑动
    val out2 = list?.map { topic ->
      TopicFragment().apply {
        arguments = bundleOf(Keys.KEY_TOPIC_MODEL to topic, Keys.KEY_TOPIC_ID to topic.id)
      }
    } ?:
      mutableListOf(TopicFragment().apply {
        arguments = bundleOf(Keys.KEY_TOPIC_MODEL to topicModel, Keys.KEY_TOPIC_ID to mTopicId)
      })

    val out: List<TopicFragment>
    if (pref.getBoolean("pref_viewpager", true)) {
      out = out2
    } else {
      out = mutableListOf(TopicFragment().apply {
        arguments = bundleOf(Keys.KEY_TOPIC_MODEL to topicModel, Keys.KEY_TOPIC_ID to mTopicId)
      })
    }



    vpAdapter.initList(out)
    binding.viewPagerDetail.run {
      adapter = vpAdapter
      setCurrentItem(position, false)
      setPageTransformer(true, ZoomOutPageTransform())
      addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
        }
      })
    }

  }
}


/**
 * 管理topicfragment， 存储当前位置等信息。
 *
 * todo 需要加入和endlessScrollListener的联动，不然这里的list没法增加。
 */
class VpAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

  private val fgList : MutableList<TopicFragment> = mutableListOf()


  fun initList(list: List<TopicFragment>) {
    fgList.addAll(list)
  }

  fun addList(list: List<TopicFragment>) {
    fgList.addAll(list)
  }

  override fun getItem(position: Int): Fragment {
    return fgList[position]
  }

  override fun getCount(): Int {
    return fgList.size
  }

}