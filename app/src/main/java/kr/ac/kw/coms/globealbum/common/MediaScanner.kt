package kr.ac.kw.coms.globealbum.common

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import java.io.File


fun getAllDrives(): ArrayList<File> {
  val ret = ArrayList<File>()
  // https://stackoverflow.com/q/39850004
  Environment.getExternalStorageDirectory()?.also { sdcard ->
    ret.add(sdcard)
  }
  System.getenv("SECONDARY_STORAGE")?.also {
    it.split(':').forEach {
      ret.add(File(it))
    }
  }
  return ret
}

/**
 * 모든 드라이브의 DCIM, Pictures 폴더 .jpg 파일을 스캔한다
 */
fun Context.mediaScan() {
  val drives: ArrayList<File> = getAllDrives()

  lateinit var scannerConn: MediaScannerConnection
  scannerConn = MediaScannerConnection(this, object :
    MediaScannerConnection.MediaScannerConnectionClient {

    val filter = Regex(".*\\.(?:jpe?g|png|jfif)$")

    override fun onMediaScannerConnected() {
      drives.forEach {
        val scanIfPicture: (File) -> Unit = {
          when {
            it.isHidden || it.isDirectory -> {
            }
            it.isFile && filter.matches(it.name) ->
              scannerConn.scanFile(it.absolutePath, null)
          }
        }
        it.resolve(Environment.DIRECTORY_DCIM).walkTopDown().forEach(scanIfPicture)
        it.resolve(Environment.DIRECTORY_PICTURES).walkTopDown().forEach(scanIfPicture)
      }
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {}
  })
  scannerConn.connect()
}
