package kr.ac.kw.coms.globealbum.album

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.activity_navigator.*
import kr.ac.kw.coms.globealbum.R
import kr.ac.kw.coms.globealbum.provider.EXIFinfo
import kr.ac.kw.coms.globealbum.provider.IPicture
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent
import java.io.File
import java.util.*

/**
 * GroupDiaryView 사용 예제 액티비티
 * recycle_gallery가 GroupDiaryView인데 저 뷰에 groups, 즉
 * ArrayList<PictureGroup>을 넣어주면 된다.
 */
class ExampleDiary : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigator)
    val idToPic: (Int) -> ResourcePicture = resPicGetter(baseContext)

    val data = arrayListOf(
      PictureGroup("group 1", arrayListOf(
        idToPic(R.drawable.sample0),
        idToPic(R.drawable.sample1),
        idToPic(R.drawable.sample2),
        idToPic(R.drawable.sample3),
        idToPic(R.drawable.sample4)
      )),
      PictureGroup("group 2", arrayListOf(
        idToPic(R.drawable.sample5),
        idToPic(R.drawable.sample6),
        idToPic(R.drawable.sample7),
        idToPic(R.drawable.sample8),
        idToPic(R.drawable.sample9)
      ))
    )

    recycle_gallery.groups = data
  }
}

data class PictureGroup(val name: String, val pics: ArrayList<IPicture>)

fun resPicGetter(context: Context): (Int) -> ResourcePicture = @DrawableRes { i ->
  ResourcePicture(context, i)
}

class UriPicture(val uri: android.net.Uri, val context: Context) : IPicture {

  override val drawable: RequestBuilder<Drawable>
    get() = Glide.with(context).load(uri)

  override var title: String
    get() = uri.lastPathSegment
    set(value) = throw NotImplementedError()

  override val time: Date
    get() = throw NotImplementedError()

  override val coords: Pair<Double, Double>
    get() = throw NotImplementedError()

  override fun delete() = throw NotImplementedError()
}

class LocalPicture(val path: String, val context: Context) : IPicture {

  override val drawable: RequestBuilder<Drawable>
    get() = Glide.with(context).load(File(path))

  override var title: String
    get() = File(path).nameWithoutExtension
    set(value) {}

  override val time: Date
    get() = Date(EXIFinfo(path).timeTaken);

  override val coords: Pair<Double, Double>
    get() = TODO("not implemented")

  override fun delete() = throw NotImplementedError()
}

class ResourcePicture(val context: Context, @DrawableRes val id: Int) : IPicture {

  override val drawable: RequestBuilder<Drawable>
    get() = Glide.with(context).load(id)

  override var title: String
    get() = "resource:$id"
    set(value) {}

  override val time: Date
    get() = Date(1000000L + 1000 * (100 - id))

  override val coords: Pair<Double, Double>
    get() = throw NotImplementedError()

  override fun delete() = throw NotImplementedError()
}

/**
 * 그룹 사진을 보여줄 수 있는 RecyclerView. 그냥 이 뷰를 집어넣고
 * groups라는 프로퍼티만 설정해주면 끝.
 */
class GroupDiaryView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
  RecyclerView(context, attrs, defStyle) {

  /**
   * 사진과 구분자 뷰 제공 어댑터
   */
  val picAdapter = GroupedPicAdapter()

  var direction: Int
    get() = layoutManager.layoutDirection
    set(@setparam:FlexDirection value) {
      layoutManager = FlexboxLayoutManager(context).apply {
        flexWrap = FlexWrap.WRAP
        flexDirection = value
        alignItems = AlignItems.STRETCH
      }
    }

  init {
    adapter = picAdapter
    direction = FlexDirection.ROW
  }

  var groups: List<PictureGroup>
    get() = picAdapter.data
    set(value) {
      picAdapter.data = value
    }

}

/***
 * RecyclerView를 제어하는 클래스. 상훈이만 알 필요 있음.
 * RecyclerView가 FlexboxLayoutManager의 제어를 받아 이 어댑터에 필요한 뷰를 요청하게 됨.
 */

class GroupedPicAdapter : RecyclerView.Adapter<GroupedPicAdapter.ElementViewHolder>() {

  // 이걸 설정하면 notifyDataSetChanged를 따로 호출할 필요 없다!
  var viewData = arrayListOf<Any>()
  var data: List<PictureGroup> = listOf()
    set(value) {
      for (g: PictureGroup in value) {
        viewData.add(g.name)
        g.pics.sortBy { it.time }
        viewData.addAll(g.pics)
      }
      notifyDataSetChanged()
    }
  var padding: Int = 0
  var nameTextSize: Int = 20
  var nameBackgroundColor: Long = 0xFFFFFFFF

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      0 -> createSeparator(parent)
      else -> createPicture(parent)
    }

  override fun getItemViewType(position: Int): Int {
    return if (viewData[position] is String) 0 else 1
  }

  override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
    holder.boundItem = viewData[position]
    when (holder) {
      is SeparatorHolder -> {
        holder.textView.text = holder.boundItem as String
        if (viewData[0].equals("")) {
          holder.textView.visibility = TextView.GONE
        }
      }
      is PictureHolder -> {
        val pic = holder.boundItem as ResourcePicture
        pic.drawable.into(holder.imageView)
        holder.imageView.scaleType = ImageView.ScaleType.FIT_XY
        holder.imageView.setPadding(padding / 2, 0, padding / 2, 0)
      }
    }
  }

  override fun getItemCount() = viewData.size

  private fun createSeparator(parent: View): SeparatorHolder {
    return SeparatorHolder(TextView(parent.context).apply {
      layoutParams = FlexboxLayoutManager.LayoutParams(matchParent, wrapContent)
      backgroundColor = nameBackgroundColor.toInt()
      textSize = nameTextSize.toFloat()
    })
  }

  private fun createPicture(parent: View): PictureHolder {
    return PictureHolder(ImageView(parent.context).apply {
      val metrics = parent.resources.displayMetrics
      val mw = metrics.widthPixels / 3
      val mh = metrics.heightPixels / 4
      scaleType = ImageView.ScaleType.CENTER_CROP
      layoutParams = FlexboxLayoutManager.LayoutParams(mw, mh).apply {
        flexGrow = 1f
      }
    })
  }

  open class ElementViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var boundItem: Any? = null
  }

  class SeparatorHolder(val textView: TextView) : ElementViewHolder(textView)
  class PictureHolder(val imageView: ImageView) : ElementViewHolder(imageView)
}
