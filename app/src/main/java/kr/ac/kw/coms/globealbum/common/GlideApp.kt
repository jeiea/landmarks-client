package kr.ac.kw.coms.globealbum.common

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kr.ac.kw.coms.globealbum.provider.*
import kr.ac.kw.coms.landmarks.client.getThumbnailLevel
import java.io.InputStream
import java.security.MessageDigest
import kotlin.coroutines.experimental.buildSequence


@GlideModule
class MyGlideModule : AppGlideModule() {
  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    registry.prepend(IPicture::class.java, InputStream::class.java, PictureModelLoaderFactory())
  }

  override fun applyOptions(context: Context, builder: GlideBuilder) {
    super.applyOptions(context, builder)
    builder.setLogLevel(Log.VERBOSE);
  }
}

/**
 *  @see <a href="https://bumptech.github.io/glide/tut/custom-modelloader.html">Glide doc</a>
 */
class PictureModelLoaderFactory : ModelLoaderFactory<IPicture, InputStream> {

  override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<IPicture, InputStream> {
    val stringLoader: ModelLoader<String, InputStream> =
      multiFactory.build(String::class.java, InputStream::class.java)
    val resourceLoader: ModelLoader<Int, InputStream> =
      multiFactory.build(Int::class.java, InputStream::class.java)
    return PictureModelLoader(stringLoader, resourceLoader)
  }

  override fun teardown() {
  }
}

/**
 * Loads an [InputStream] from a IPicture instance
 */
class PictureModelLoader(
  private val stringLoader: ModelLoader<String, InputStream>,
  private val resourceLoader: ModelLoader<Int, InputStream>
) : ModelLoader<IPicture, InputStream> {

  override fun buildLoadData(
    model: IPicture, width: Int, height: Int, options: Options
  ): LoadData<InputStream>? {

    when (model) {
      is UrlPicture -> return stringLoader.buildLoadData(
        model.url.toString(), width, height, options
      )
      is LocalPicture -> return stringLoader.buildLoadData(model.path, width, height, options)
      is ResourcePicture -> return resourceLoader.buildLoadData(model.id, width, height, options)
      is RemotePicture -> {
        val w = if (width < 0) 1920 else width
        val h = if (height < 0) 1080 else height
        val key = RemotePictureKey(model, w, h)
        val alternateKeys = buildSequence {
          for (i in 0 until key.level) {
            yield(RemotePictureKey(model.info.id, i))
          }
        }.toList()
        val fetcher = PictureDataFetcher(model, w, h)
        return LoadData(key, alternateKeys, fetcher)
      }
      else -> throw NotImplementedError()
    }
  }

  override fun handles(model: IPicture) = true
}

class RemotePictureKey(val id: Int, val level: Int) : Key {

  constructor(pic: RemotePicture, width: Int, height: Int) :
    this(pic.info.id, getThumbnailInt(pic, width, height))

  companion object {
    fun getThumbnailInt(pic: RemotePicture, width: Int, height: Int): Int {
      val fullWidth = pic.info.data.width ?: 1920
      val fullHeight = pic.info.data.height ?: 1080
      val level = getThumbnailLevel(fullWidth, fullHeight, width, height)
      return level
    }
  }

  override fun equals(other: Any?): Boolean {
    return other is RemotePictureKey
      && id == other.id
      && level == other.level
  }

  override fun hashCode(): Int {
    return id * 4 + level
  }

  override fun toString(): String {
    return "thumbnail:$id:$level"
  }

  override fun updateDiskCacheKey(messageDigest: MessageDigest) {
    for (i in 0..3) {
      messageDigest.update((id shr i and 0xff).toByte())
    }
    messageDigest.update(level.toByte())
  }
}

class PictureDataFetcher(val model: RemotePicture, val width: Int, val height: Int) :
  DataFetcher<InputStream> {

  var fetch: Job? = null

  override fun getDataClass(): Class<InputStream> {
    return InputStream::class.java
  }

  override fun getDataSource(): DataSource = model.dataSource

  override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
    fetch = GlobalScope.launch(Dispatchers.IO) {
      try {
        callback.onDataReady(RemoteJava.client.getThumbnail(model.info.id, width, height))
      }
      catch (e: Exception) {
        callback.onLoadFailed(e)
      }
    }
  }

  override fun cleanup() {
    cancel()
  }

  override fun cancel() {
    fetch?.also {
      fetch = null
      it.cancel()
    }
  }
}
