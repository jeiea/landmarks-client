package kr.ac.kw.coms.globealbum.game

import android.content.Context
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.awaitAll
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.launch
import kr.ac.kw.coms.globealbum.common.AsyncTarget
import kr.ac.kw.coms.globealbum.common.GlideApp
import kr.ac.kw.coms.globealbum.map.Disposable
import kr.ac.kw.coms.globealbum.provider.IPicture
import kr.ac.kw.coms.globealbum.provider.Promise
import kr.ac.kw.coms.globealbum.provider.RemoteJava
import kr.ac.kw.coms.globealbum.provider.RemotePicture
import kr.ac.kw.coms.landmarks.client.RecoverableChannel
import java.util.*


internal interface IGameQuiz : Disposable {
  var msTimeLimit: Int

  val usedPictures: Collection<IPicture>

  val glideTargets: List<AsyncTarget>

  override fun dispose() {
    glideTargets.forEach(Disposable::dispose)
  }
}

internal class PositionQuiz(var picture: IPicture, drawable: AsyncTarget) : IGameQuiz {
  override var msTimeLimit = 0

  override val usedPictures: Collection<IPicture>
    get() = ArrayList<IPicture>().apply { add(picture) }

  override val glideTargets = listOf(drawable)
}

internal class PicChoiceQuiz(
  val pictures: List<IPicture>,
  override val glideTargets: List<AsyncTarget>,
  random: Random
) : IGameQuiz {
  private val correctIdx: Int = random.nextInt(pictures.size)

  override var msTimeLimit = 0

  override val usedPictures: Collection<IPicture> = pictures

  val correctPicture: IPicture
    get() = pictures[correctIdx]
}

internal class GameQuizFactory(val context: Context) : View.OnLayoutChangeListener {

  var smallPicSize: Size
  var largePicSize: Size

  private val random = Random()

  private val pictureBuffer by RecoverableChannel {
    GlobalScope.produce(Dispatchers.IO) {
      while (true) {
        val pics = RemoteJava.client.getRandomPictures(33).map(::RemotePicture)
        pics.forEach { send(it) }
      }
    }
  }

  private val quizBuffer by RecoverableChannel {
    GlobalScope.produce(Dispatchers.Main, capacity = 2) {
      while (true) {
        send(generateQuiz())
      }
    }
  }

  init {
    quizBuffer
    val (w, h) = getScreenSize()
    smallPicSize = Size(w / 2, h / 4)
    largePicSize = Size(w, h / 2)
  }

  override fun onLayoutChange(
    v: View, l: Int, t: Int, r: Int, b: Int, _ol: Int, _ot: Int, _or: Int, _ob: Int
  ) {
    val (ew, _) = getScreenSize()
    val w = r - l
    val h = b - t
    if (w < ew / 2) {
      smallPicSize = Size(w, h)
    }
    else {
      largePicSize = Size(w, h)
    }
    v.removeOnLayoutChangeListener(this)
  }

  fun fetchQuiz(prom: Promise<IGameQuiz>) = GlobalScope.launch {
    prom.resolve {
      val quiz = quizBuffer.receive()
      quiz.glideTargets.map { it.prepared }.awaitAll()
      quiz
    }
  }

  private suspend fun generateQuiz(): IGameQuiz {
    return if (random.nextBoolean()) {
      val pic = pictureBuffer.receive()
      val target = GlideApp.with(context).load(pic).into(AsyncTarget(largePicSize))
      PositionQuiz(pic, target)
    }
    else {
      val pics = mutableListOf<RemotePicture>()
      for (_i in 1..10) {
        val pic = pictureBuffer.receive().takeIf { !pics.contains(it) } ?: continue
        pics.add(pic)
        if (pics.size >= 4) {
          break
        }
      }
      val targets = pics.map { GlideApp.with(context).load(it).into(AsyncTarget(smallPicSize)) }
      GlideApp.with(context).load(pics[0]).preload()
      PicChoiceQuiz(pics, targets, random)
    }
  }

  private fun getScreenSize(): Pair<Int, Int> {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val metrics = DisplayMetrics()
    wm.defaultDisplay.getMetrics(metrics)
    return metrics.run { Pair(widthPixels, heightPixels) }
  }
}


