package kr.ac.kw.coms.globealbum.common

import android.graphics.*
import android.graphics.Shader.TileMode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import kotlin.math.min

fun getCircularBitmap(drawable: Drawable, side: Int,mode: Int): Bitmap {
  return drawable.toBitmap().toCircularBitmap(side,mode)!!
}


fun Drawable.toBitmap(): Bitmap {
  val config = Bitmap.Config.ARGB_8888
  val bitmap = when (this) {
    is BitmapDrawable -> return bitmap
    is ColorDrawable -> Bitmap.createBitmap(2, 2, config)
    else -> Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, config)
  }

  val canvas = Canvas(bitmap)
  val bound = bounds
  bounds = Rect(0, 0, canvas.width, canvas.height)
  draw(canvas)
  bounds = bound

  return bitmap
}

fun Bitmap.toCircularBitmap(side: Int, mode: Int): Bitmap? {
  if (isRecycled) {
    return null
  }

  val canvasBitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
  val paint = Paint()
  paint.isAntiAlias = true

  val mat = getStretchMatrix(side)
  val shader = BitmapShader(this, TileMode.CLAMP, TileMode.CLAMP)
  shader.setLocalMatrix(mat)
  paint.shader = shader

  val canvas = Canvas(canvasBitmap)
  val r: Float = side / 2f
  canvas.drawCircle(r, r, r, paint)

  if (mode == 1){
    paint.colorFilter
    paint.shader = null
    paint.style = Paint.Style.STROKE
    paint.color = Color.RED;
    paint.strokeWidth = r * 0.1f
    canvas.drawCircle(r, r, r, paint)
  }

  return canvasBitmap
}

private fun Bitmap.getCenterMatrix(side: Int): Matrix {
  val mat = Matrix()
  val short = min(width, height)
  val dx = (width - short) * -0.5f
  val dy = (height - short) * -0.5f
  mat.setTranslate(dx, dy)
  val scale = side.toFloat() / short
  mat.postScale(scale, scale)
  return mat
}

private fun Bitmap.getStretchMatrix(side: Int): Matrix {
  val s = side.toFloat()
  return Matrix().apply {
    postScale(s / width, s / height)
  }
}
