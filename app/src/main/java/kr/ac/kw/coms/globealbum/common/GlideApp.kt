package kr.ac.kw.coms.globealbum.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.Size
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
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kr.ac.kw.coms.globealbum.provider.*
import kr.ac.kw.coms.landmarks.client.getThumbnailLevel
import java.io.InputStream
import java.security.MessageDigest

fun clearCache(context: Context): Job = GlobalScope.launch {
  coroutineScope {
    launch(Dispatchers.IO) {
      GlideApp.get(context).clearDiskCache()
    }
    launch(Dispatchers.Main) {
      GlideApp.get(context).clearMemory()
    }
  }
}

@GlideModule
class MyGlideModule : AppGlideModule() {
  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    registry.prepend(IPicture::class.java, InputStream::class.java, PictureModelLoaderFactory())
  }

  override fun applyOptions(context: Context, builder: GlideBuilder) {
    super.applyOptions(context, builder)
    builder.setLogLevel(Log.VERBOSE)
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
        val alternateKeys = (0 until key.level).map { RemotePictureKey(model.info.id, it) }
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
      return getThumbnailLevel(fullWidth, fullHeight, width, height)
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

class PictureDataFetcher(private val model: RemotePicture, val width: Int, val height: Int) :
  DataFetcher<InputStream> {

  private var fetch: Job? = null

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

class AsyncTarget(
  val width: Int = Target.SIZE_ORIGINAL,
  val height: Int = Target.SIZE_ORIGINAL
) : Target<Drawable>, Disposable {

  private var req: Request? = null
  private val channel = Channel<Unit>()
  var drawable: Drawable? = null
  var prepared: CompletableDeferred<Drawable> = CompletableDeferred()
    private set

  constructor(size: Size) : this(size.width, size.height)

  suspend fun awaitChanges(): Drawable? {
    channel.receive()
    return drawable
  }

  private fun broadcast(placeholder: Drawable?) {
    drawable = placeholder
    channel.offer(Unit)
  }

  override fun onLoadStarted(placeholder: Drawable?) {
    broadcast(placeholder)
  }

  override fun onLoadFailed(errorDrawable: Drawable?) {
    broadcast(errorDrawable)
    prepared.cancel()
  }

  override fun onLoadCleared(placeholder: Drawable?) {
    broadcast(placeholder)
    resetPrepared()
  }

  override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
    broadcast(resource)
    prepared.complete(resource)
  }

  override fun getRequest(): Request? = req

  override fun setRequest(request: Request?) {
    req = request
  }

  private fun resetPrepared() {
    if (!prepared.isActive) {
      prepared = CompletableDeferred()
    }
  }

  override fun getSize(cb: SizeReadyCallback) {
    cb.onSizeReady(width, height)
  }

  override fun removeCallback(cb: SizeReadyCallback) {}

  override fun onStart() {}

  override fun onStop() {}

  override fun onDestroy() {
    dispose()
  }

  override fun dispose() {
    req?.clear()
    prepared.cancel()
  }
}
