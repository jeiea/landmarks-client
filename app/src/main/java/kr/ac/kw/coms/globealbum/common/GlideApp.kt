package kr.ac.kw.coms.globealbum.common

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kr.ac.kw.coms.globealbum.provider.IPicture
import kr.ac.kw.coms.globealbum.provider.LocalPicture
import kr.ac.kw.coms.globealbum.provider.ResourcePicture
import kr.ac.kw.coms.globealbum.provider.UrlPicture
import java.io.InputStream


@GlideModule
class MyGlideModule : AppGlideModule() {
  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    registry.prepend(IPicture::class.java, InputStream::class.java, PictureModelLoaderFactory())
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
  private val resourceLoader: ModelLoader<Int, InputStream>) : ModelLoader<IPicture, InputStream> {

  override fun buildLoadData(model: IPicture, width: Int, height: Int, options: Options): LoadData<InputStream>? {
    when (model) {
      is UrlPicture -> return stringLoader.buildLoadData(model.url.toString(), width, height, options)
      is LocalPicture -> return stringLoader.buildLoadData(model.path, width, height, options)
      is ResourcePicture -> return resourceLoader.buildLoadData(model.id, width, height, options)
      else -> {
        val key = ObjectKey(model)
        val fetcher = PictureDataFetcher(model)
        return LoadData(key, fetcher)
      }
    }
  }

  override fun handles(model: IPicture) = true
}

class PictureDataFetcher(val model: IPicture) : DataFetcher<InputStream> {

  var fetch: Job? = null

  override fun getDataClass(): Class<InputStream> {
    return InputStream::class.java
  }

  override fun getDataSource(): DataSource = model.dataSource

  override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
    fetch = GlobalScope.launch {
      try {
        callback.onDataReady(model.stream())
      } catch (e: Exception) {
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
