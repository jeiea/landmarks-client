package kr.ac.kw.coms.globealbum.common

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File


fun getAllDrives(): MutableList<File> {
  val ret = mutableListOf<File>()
  // https://stackoverflow.com/q/39850004
  Environment.getExternalStorageDirectory()?.also { builtin ->
    ret.add(builtin)
  }
  System.getenv("SECONDARY_STORAGE")?.also { externals ->
    externals.split(':').forEach {
      ret.add(File(it))
    }
  }
  return ret
}

/**
 * 모든 드라이브의 DCIM, Pictures 폴더 .jpg 파일을 스캔한다
 */
fun Context.mediaScan() = mediaScan(null)

/**
 * 모든 드라이브의 DCIM, Pictures 폴더 .jpg 파일을 스캔한다
 */
fun Context.mediaScan(onComplete: Runnable?) {
  val drives: MutableList<File> = getAllDrives()

  lateinit var scannerConn: MediaScannerConnection
  scannerConn = MediaScannerConnection(this, object :
    MediaScannerConnection.MediaScannerConnectionClient {

    val filter = Regex(".*\\.(?:jpe?g|jfif)$")

    override fun onMediaScannerConnected() {
      val existing = getStoredImages()
      existing.forEach { scannerConn.scanFile(it, null) }

      drives.forEach { drive ->
        val scanIfPicture: (File) -> Unit = {
          when {
            it.isHidden || it.isDirectory -> {
            }
            it.isFile && it.absolutePath !in existing && filter.matches(it.name) ->
              scannerConn.scanFile(it.absolutePath, null)
          }
        }
        drive.resolve(Environment.DIRECTORY_DCIM).walkTopDown().forEach(scanIfPicture)
        drive.resolve(Environment.DIRECTORY_PICTURES).walkTopDown().forEach(scanIfPicture)
      }
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
      onComplete?.run()
    }
  })
  scannerConn.connect()
}

fun Context.getStoredImages(): MutableList<String> {
  val imagePaths = mutableListOf<String>()
  val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
  val projection = arrayOf(MediaStore.MediaColumns.DATA)
  contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
    val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
    while (cursor.moveToNext()) {
      val absolute = cursor.getString(columnIndexData)
      imagePaths.add(absolute)
    }
  }
  return imagePaths
}
