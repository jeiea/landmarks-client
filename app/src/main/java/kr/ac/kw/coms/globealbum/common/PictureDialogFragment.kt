package kr.ac.kw.coms.globealbum.common

import android.graphics.drawable.*
import android.os.*
import android.view.*
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.*
import androidx.fragment.app.*
import kotlinx.android.synthetic.main.fragment_picture_dialog.*
import kr.ac.kw.coms.globealbum.*
import org.jetbrains.anko.sdk27.coroutines.*


/**
 *  사진의 가로 길이를 화면 전체에 맞춰 띄우는 대화상자.
 *  때문에 가로 화면에서는 불상사가 일어남.
 */
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

  override fun onResume() {
    super.onResume()
    dialog!!.window!!.setLayout(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.WRAP_CONTENT)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View {
    dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
    val view = inflater.inflate(R.layout.fragment_picture_dialog, container)
    iv_popup.apply {
      scaleType = ImageView.ScaleType.CENTER
      adjustViewBounds = true
      onTouch { _, _ -> dismiss() }
    }
    return view
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
