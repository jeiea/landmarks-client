package kr.ac.kw.coms.globealbum.common

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView


open class RecyclerItemClickListener(recyclerView: RecyclerView) {

  internal var mGestureDetector: GestureDetector

  open fun onItemClick(view: View, position: Int) {}

  open fun onLongItemClick(view: View, position: Int) {}

  init {
    mGestureDetector = GestureDetector(recyclerView.context, object : GestureDetector.SimpleOnGestureListener() {
      override fun onSingleTapUp(e: MotionEvent): Boolean {
        return true
      }

      override fun onLongPress(e: MotionEvent) {
        val child = recyclerView.findChildViewUnder(e.x, e.y)
        if (child != null) {
          onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
        }
      }
    })
  }

  val itemTouchListener
    get() = ItemTouchListener()

  inner class ItemTouchListener : RecyclerView.OnItemTouchListener {

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
      val childView = view.findChildViewUnder(e.x, e.y)
      if (childView != null && mGestureDetector.onTouchEvent(e)) {
        onItemClick(childView, view.getChildAdapterPosition(childView))
        return true
      }
      return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
  }

}