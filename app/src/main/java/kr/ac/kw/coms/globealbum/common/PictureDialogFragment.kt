package kr.ac.kw.coms.globealbum.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import kr.ac.kw.coms.globealbum.R
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.image
import org.jetbrains.anko.imageView
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent


class PictureDialogFragment : DialogFragment() {

  var drawable: Drawable? = null

  companion object {
    /***
     *  안드로이드가 프래그먼트를 생성할 때 생성자 말고 이 메소드를 쓰도록 강제함.
     *  @param drawable 보여줄 이미지
     */
    fun newInstance(drawable: Drawable): PictureDialogFragment {
      return PictureDialogFragment().apply { this.drawable = drawable }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View {
    dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
    return inflater.context.constraintLayout {
      imageView {
        image = this@PictureDialogFragment.drawable
      }.lparams(wrapContent, wrapContent) {
        leftToLeft = PARENT_ID
        rightToRight = PARENT_ID
        topToTop = PARENT_ID
        bottomToBottom = PARENT_ID
        background = ColorDrawable(Color.GREEN)
      }
      textView { text = "You can put caption" }
      background = ColorDrawable(Color.GRAY)
    }
  }
}

// from java, call this function like this.
// kr.ac.kw.coms.globealbum.common.PictureDialogFragmentKt.usageExample(activity);
fun AppCompatActivity.usageExample() {
  /* In java,
  Drawable pic = getResources().getDrawable(R.drawable.blank);
  PictureDialogFragment pdf = PictureDialogFragment.Companion.newInstance(pic);
  pdf.show(getSupportFragmentManager(), "wow");
  */
  val pic = resources.getDrawable(R.drawable.blank, theme)
  val pdf = PictureDialogFragment.newInstance(pic)
  pdf.show(supportFragmentManager, "wow")
}
