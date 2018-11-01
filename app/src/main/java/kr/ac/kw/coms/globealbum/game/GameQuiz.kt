package kr.ac.kw.coms.globealbum.game

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.launch
import kr.ac.kw.coms.globealbum.common.GlideApp
import kr.ac.kw.coms.globealbum.provider.IPicture
import kr.ac.kw.coms.globealbum.provider.Promise
import kr.ac.kw.coms.globealbum.provider.RemoteJava
import kr.ac.kw.coms.globealbum.provider.RemotePicture
import kr.ac.kw.coms.landmarks.client.RecoverableChannel
import java.util.*


internal interface IGameQuiz {
  var msTimeLimit: Int

  val usedPictures: Collection<IPicture>
}

internal class PositionQuiz(var picture: IPicture) : IGameQuiz {
  override var msTimeLimit = 0

  override val usedPictures: Collection<IPicture>
    get() = ArrayList<IPicture>().apply { add(picture) }
}

internal class PicChoiceQuiz(val pictures: List<IPicture>, random: Random) : IGameQuiz {
  private val correctIdx: Int = random.nextInt(4)

  override var msTimeLimit = 0

  override val usedPictures: Collection<IPicture> = pictures

  val correctPicture: IPicture
    get() = pictures[correctIdx]
}

internal class GameQuizFactory(val context: Context) {

  private val random = Random()

  private val quizBuffer by RecoverableChannel {
    GlobalScope.produce(Dispatchers.Main, capacity = 2) {
      while (true) {
        send(generateQuiz())
      }
    }
  }

  val pictureBuffer by RecoverableChannel {
    GlobalScope.produce(Dispatchers.IO) {
      while (true) {
        val pics = RemoteJava.client.getRandomPictures(33).map(::RemotePicture)
        pics.forEach { send(it) }
      }
    }
  }

  init {
    quizBuffer
  }

  private suspend fun generateQuiz(): IGameQuiz {
    val (w, h) = getScreenSize()
    return if (random.nextBoolean()) {
      val pic = pictureBuffer.receive()
      GlideApp.with(context).load(pic).preload(w, h / 2)
      PositionQuiz(pic)
    }
    else {
      val pics = mutableListOf<RemotePicture>()
      for (_i in 1..10) {
        val pic = pictureBuffer.receive().takeIf { !pics.contains(it) } ?: continue
        GlideApp.with(context).load(pic).preload(w / 2, h / 4)
        pics.add(pic)
        if (pics.size >= 4) {
          break
        }
      }
      PicChoiceQuiz(pics, random)
    }
  }

  private fun getScreenSize(): Pair<Int, Int> {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val metrics = DisplayMetrics()
    wm.defaultDisplay.getMetrics(metrics)
    return metrics.run { Pair(widthPixels, heightPixels) }
  }

  fun fetchQuiz(prom: Promise<IGameQuiz>) {
    GlobalScope.launch { prom.resolve { quizBuffer.receive() } }
  }
}
