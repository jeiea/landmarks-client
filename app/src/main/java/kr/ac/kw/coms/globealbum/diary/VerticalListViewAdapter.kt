package kr.ac.kw.coms.globealbum.diary

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import kr.ac.kw.coms.globealbum.album.ResourcePicture
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent

class VerticalListView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
  : RecyclerView(context, attrs, defStyle) {
  internal val Adapter = VerticalListViewAdapter()

  init {
    adapter = Adapter
  }

  var groups: List<ResourcePicture>
    get() = Adapter.data
    set(value) {
      Adapter.data = value
    }
}

internal class VerticalListViewAdapter : RecyclerView.Adapter<VerticalListViewAdapter.LayoutHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayoutHolder {
    var layout: InnerLayout = InnerLayout(parent.context)
    return LayoutHolder(layout)
  }

  override fun getItemCount(): Int {
    return viewData.size
  }

  override fun onBindViewHolder(holder: LayoutHolder, position: Int) {
    val pic: ResourcePicture = viewData[position]
    pic.drawable?.into(holder.layout.imageView)
  }

  var viewData = arrayListOf<ResourcePicture>()
  var data: List<ResourcePicture> = listOf()
    set(value) {
      viewData.addAll(value)
      notifyDataSetChanged()
    }

  open class LayoutHolder(val layout: InnerLayout) : RecyclerView.ViewHolder(layout)
  open class InnerLayout(context: Context) : ConstraintLayout(context) {
    init {
      layoutParams = ConstraintLayout.LayoutParams(matchParent, wrapContent)
      backgroundColor = Color.WHITE
    }

    var imageView: ImageView = ImageView(context)
      set(value) {
        value.apply {
          scaleType = ImageView.ScaleType.CENTER_CROP
          layoutParams = LayoutParams(50, 50)
        }
        var constraintset = ConstraintSet()
        constraintset.clone(this)
        constraintset.connect(value.id, ConstraintSet.TOP, this.id, ConstraintSet.TOP)
        constraintset.connect(value.id, ConstraintSet.LEFT, this.id, ConstraintSet.LEFT)
        constraintset.applyTo(this)
      }

  }
}