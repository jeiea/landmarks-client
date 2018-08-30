package kr.ac.kw.coms.globealbum.common

import android.graphics.*
import android.graphics.Shader.TileMode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import kotlin.math.min


fun getCircularBitmap(drawable: Drawable, side: Int): Bitmap {
  return drawable.toBitmap().toCircularBitmap(side)!!
}

fun getCircularBorderBitmap(drawable: Drawable, side: Int): Bitmap {
  return drawable.toBitmap().toCircularBitmap(side, Canvas::drawCircleStroke)!!
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

fun Bitmap.toCircularBitmap(side: Int, block: Canvas.(Float) -> Unit = {}): Bitmap? {
  if (isRecycled) {
    return null
  }

  val paint = Paint()
  paint.isAntiAlias = true
  paint.shader = BitmapShader(this, TileMode.CLAMP, TileMode.CLAMP).apply {
    setLocalMatrix(getStretchMatrix(side))
  }

  val canvasBitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
  val r: Float = side / 2f
  Canvas(canvasBitmap).apply {
    drawCircle(r, r, r, paint)
    block(r)
  }

  return canvasBitmap
}

private fun Canvas.drawCircleStroke(r: Float) {
  val thickness = r * 0.1f
  val radius = r - thickness * 0.5f
  drawCircle(r, r, radius, Paint().apply {
    style = Paint.Style.STROKE
    color = Color.WHITE
    strokeWidth = thickness
  })
}

private fun Bitmap.getCenterMatrix(side: Int): Matrix {
  val short = min(width, height)
  val dx = (width - short) * -0.5f
  val dy = (height - short) * -0.5f
  val scale = side.toFloat() / short
  return Matrix().apply {
    setTranslate(dx, dy)
    postScale(scale, scale)
  }
}

private fun Bitmap.getStretchMatrix(side: Int): Matrix {
  val s = side.toFloat()
  return Matrix().apply {
    postScale(s / width, s / height)
  }
}
