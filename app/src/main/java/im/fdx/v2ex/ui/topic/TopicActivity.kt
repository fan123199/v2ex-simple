package im.fdx.v2ex.ui.topic

import android.content.*
import android.os.Bundle
import android.view.MotionEvent
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import im.fdx.v2ex.databinding.ActivityDetailsBinding
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.view.ViewPagerHelper
import im.fdx.v2ex.view.ZoomOutPageTransform
import im.fdx.v2ex.utils.extensions.toast
import kotlin.math.abs


/**
 *  这个仅是容器，因为加入了左右滑动， 于是就又用Fragment来做内容
 */
class TopicActivity : BaseActivity() {

  private var helper: ViewPagerHelper? = null
  private lateinit var binding: ActivityDetailsBinding
  private lateinit var vpAdapter: VpAdapter

  private lateinit var mTopicId :String

  val isUsePager by lazy {  pref.getBoolean("pref_viewpager", true) }
  private var position = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDetailsBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    vpAdapter = VpAdapter(this)
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
        data.pathSegments.getOrNull(1)?:""
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

    if (isUsePager) {
      out = out2
    } else {
      out = mutableListOf(TopicFragment().apply {
        arguments = bundleOf(Keys.KEY_TOPIC_MODEL to topicModel, Keys.KEY_TOPIC_ID to mTopicId)
      })
    }



    vpAdapter.initList(out)
    if(!isUsePager) {
      binding.viewPagerDetail.isUserInputEnabled = false
    }
    binding.viewPagerDetail.run {
      adapter = vpAdapter
      setCurrentItem(position, false)
      setPageTransformer(ZoomOutPageTransform())
    }
    helper = ViewPagerHelper(binding.viewPagerDetail)
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    if (isUsePager) {
      helper?.dispatchTouchEvent(ev)
    }
    return super.dispatchTouchEvent(ev)

  }

}


/**
 * 管理topicfragment， 存储当前位置等信息。
 *
 * todo 需要加入和endlessScrollListener的联动，不然这里的list没法增加。
 */
class VpAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

  private val fgList : MutableList<TopicFragment> = mutableListOf()


  fun initList(list: List<TopicFragment>) {
    fgList.addAll(list)
  }

  fun addList(list: List<TopicFragment>) {
    fgList.addAll(list)
  }

  override fun createFragment(position: Int): Fragment {
    return fgList[position]
  }

  override fun getItemCount(): Int {
    return fgList.size
  }

}