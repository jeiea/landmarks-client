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
import kr.ac.kw.coms.landmarks.client.RecoverableChannel
import java.util.*


internal interface IGameQuiz {
  val msTimeLimit: Int

  val description: String

  val usedPictures: Collection<IPicture>
}

internal class PositionQuiz(var picture: IPicture) : IGameQuiz {

  override val msTimeLimit = 0

  override val description = ""

  override val usedPictures: Collection<IPicture>
    get() = ArrayList<IPicture>().apply { add(picture) }
}

internal class PicChoiceQuiz(val pictures: List<IPicture>, random: Random) : IGameQuiz {
  private val correctIdx: Int

  override val msTimeLimit = 0

  override val description = ""

  override val usedPictures: Collection<IPicture> = pictures

  val correctPicture: IPicture
    get() = pictures[correctIdx]

  init {
    correctIdx = random.nextInt(4)
  }
}

internal class GameQuizFactory(val context: Context) {

  private val random = Random()

  private val quizBuffer by RecoverableChannel {
    GlobalScope.produce(Dispatchers.Main, capacity = 1) {
      while (true) {
        send(generateQuiz())
      }
    }
  }

  init {
    quizBuffer
  }

  private suspend fun generateQuiz(): IGameQuiz {
    val (w, h) = getScreenSize()
    if (random.nextBoolean()) {
      val pic = RemoteJava.pictureBuffer.receive()
      GlideApp.with(context).load(pic).preload(w, h / 2)
      return PositionQuiz(pic)
    }
    else {
      val pics = (1..4).map { RemoteJava.pictureBuffer.receive() }
      pics.forEach {
        GlideApp.with(context).load(it).preload(w / 2, h / 4)
      }
      return PicChoiceQuiz(pics, random)
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
