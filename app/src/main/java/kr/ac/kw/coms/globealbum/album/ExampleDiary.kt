package kr.ac.kw.coms.globealbum.album

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
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
import com.drew.lang.ByteConvert
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.activity_navigator.*
import kr.ac.kw.coms.globealbum.R
import kr.ac.kw.coms.globealbum.provider.PictureProvider
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.util.BufferedImages
import net.coobird.thumbnailator.util.ThumbnailatorUtils
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.image
import org.jetbrains.anko.matchParent
import java.io.InputStream
import java.time.LocalDateTime
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

        val idToPic: (Int) -> ResourcePicture = resPicGetter(resources)

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

data class PictureGroup(val name: String, val pics: ArrayList<PictureProvider.Picture>)

fun resPicGetter(resources: Resources): (Int) -> ResourcePicture = { i ->
    ResourcePicture(resources, i, null)
}

class ResourcePicture(val resources: Resources, @DrawableRes val id: Int, val clickListener: View.OnClickListener?) : PictureProvider.Picture {

    override fun getDrawable(): Drawable {
        val image: BitmapDrawable = BitmapDrawable(resources, ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(resources, id), 256, 256))
        //return resources.getDrawable(id, null)
        return image
    }

    fun getEventListener(): View.OnClickListener? = clickListener

    override fun getTitle(): String {
        TODO("not implemented")
    }

    override fun setTitle(title: String?) {
        TODO("not implemented")
    }

    override fun getTime(): LocalDateTime {
        TODO("not implemented")
    }

    override fun getCoords(): Pair<Double, Double> {
        TODO("not implemented")
    }

    override fun delete() {
        TODO("not implemented")
    }
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
    internal val picAdapter = GroupedPicAdapter()

    init {
        adapter = picAdapter
        layoutManager = FlexboxLayoutManager(context).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.STRETCH
        }
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
internal class GroupedPicAdapter : RecyclerView.Adapter<GroupedPicAdapter.ElementViewHolder>() {

    // 이걸 설정하면 notifyDataSetChanged를 따로 호출할 필요 없다!
    var viewData = arrayListOf<Any>()
    var data: List<PictureGroup> = listOf()
        set(value) {
            for (g: PictureGroup in value) {
                viewData.add(g.name)
                viewData.addAll(g.pics)
            }
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                0 -> SeparatorHolder(TextView(parent.context).apply {
                    layoutParams = FlexboxLayoutManager.LayoutParams(matchParent, 40)
                    backgroundColor = 0xffffff88.toInt()
                })
                else -> PictureHolder(ImageView(parent.context).apply {
                    val metrics = parent.resources.displayMetrics
                    val mw = metrics.widthPixels / 3
                    val mh = metrics.heightPixels / 4
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    layoutParams = FlexboxLayoutManager.LayoutParams(mw, mh).apply {
                        flexGrow = 1f
                    }
                })
            }

    override fun getItemViewType(position: Int): Int {
        return if (viewData[position] is String) 0 else 1
    }

    override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
        when (holder) {
            is SeparatorHolder -> {
                holder.textView.text = viewData[position] as String
            }
            is PictureHolder -> {
                val pic = viewData[position] as ResourcePicture
                holder.imageView.image = pic.drawable
                holder.imageView.setOnClickListener(pic.clickListener)
            }
        }
    }

    override fun getItemCount() = viewData.size

    open class ElementViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    class SeparatorHolder(val textView: TextView) : ElementViewHolder(textView)
    class PictureHolder(val imageView: ImageView) : ElementViewHolder(imageView)
}
