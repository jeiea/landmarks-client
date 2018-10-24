package kr.ac.kw.coms.globealbum.game

import android.content.Context
import android.content.res.Resources
import kr.ac.kw.coms.globealbum.R
import kr.ac.kw.coms.globealbum.provider.*

interface IPictureExaminer<T : IPicture> {
  fun getRandomPictures(n: Int, prom: Promise<List<T>>)
}

class RemoteExaminer(val context: Context) : IPictureExaminer<RemotePicture> {
  override fun getRandomPictures(n: Int, prom: Promise<List<RemotePicture>>) {
    RemoteJava.getRandomPictures(n, prom)
  }
}

class ResourceExaminer(val resources: Resources) : IPictureExaminer<ResourcePicture> {

  override fun getRandomPictures(n: Int, prom: Promise<List<ResourcePicture>>) {
    val samples = mutableListOf(
      R.drawable.sample0,
      R.drawable.sample1,
      R.drawable.sample2,
      R.drawable.sample3,
      R.drawable.sample4,
      R.drawable.sample5,
      R.drawable.sample6,
      R.drawable.sample7,
      R.drawable.sample8,
      R.drawable.sample9
    )
    samples.shuffle()
    prom.resolve(samples.take(n).map { ResourcePicture(it, resources) })
  }
}