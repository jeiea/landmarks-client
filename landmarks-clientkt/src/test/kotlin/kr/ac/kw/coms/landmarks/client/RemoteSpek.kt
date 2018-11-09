package kr.ac.kw.coms.landmarks.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*
import java.util.concurrent.TimeUnit

@RunWith(JUnitPlatform::class)
class RemoteSpek : Spek({
  describe("landmarks server single user") {

    val client = newClient()

    xblit("does reverse geocoding") {
      val res: ReverseGeocodeResult = client.reverseGeocode(37.54567, 126.9944)
      res.country!! `should be equal to` "대한민국"
      res.detail!! `should be equal to` "서울특별시"
    }

    blit("checks server health") {
      client.checkAlive().`should be true`()
    }

    blit("reset all DB") {
      client.resetAllDatabase()
    }

    val ident = getRandomString(8)
    val pass = "pasowo"
    val email = "$ident@b.c"
    blit("registers a user") {
      client.register(ident, pass, email, ident)
    }

    blit("does login") {
      val p = client.login(ident, "pasowo").data
      p.login!! `should be equal to` ident
      p.email!! `should be equal to` email
      p.nick!! `should be equal to` ident
    }

    val pics = mutableListOf<IdPictureInfo>()
    blit("uploads picture") {
      val jpgs = File("../../landmarks-data/archive0").listFiles()
      for ((idx, f) in jpgs.withIndex()) {
        val gps = idx * 3.0
        val info = PictureInfo(lat = gps, lon = gps, address = f.nameWithoutExtension)
        val pic = client.uploadPicture(info, f)
        pics.add(pic)
      }
    }

    blit("download picture") {
      client.getPicture(pics[0].id).readBytes().size `should be greater than` 3000
    }

    var replaced = PictureInfo()
    blit("modify picture info") {
      replaced = pics[0].data.copy(address = "Manhatan?", lat = 20.0, lon = 110.0)
      client.modifyPictureInfo(pics[0].id, replaced)
    }

    blit("receive picture info") {
      val modified: PictureInfo = client.getPictureInfo(pics[0].id)
      val rep = replaced
      modified.address!! `should be equal to` rep.address!!
      modified.lat!! `should be equal to` rep.lat!!
      modified.lon!! `should be equal to` rep.lon!!
    }

    blit("query my pictures") {
      client.getPictures(PictureQuery().apply {
        userFilter = UserFilter.Include(client.profile!!.id)
      }).size `should be equal to` 3
    }

    blit("receives quiz info") {
      val quizs = mutableListOf<IdPictureInfo>()
      quizs.addAll(client.getRandomPictures(2))
      quizs[0].id `should not be equal to` quizs[1].id
    }

    blit("query thumbnail") {
      val stream = client.getThumbnail(pics[0].id, 400, 200)
      stream.readBytes().size `should be less than` 200000
    }

    blit("query around pictures") {
      val ps = client.getPictures(PictureQuery().apply {
        geoFilter = NearGeoPoint(21.0, 111.0, 300.0)
      })
      ps.size `should be greater than` 0
    }

    blit("delete my picture") {
      client.deletePicture(pics.last().id)
      pics.remove(pics.last())
    }

    val collection = CollectionInfo(
      title = "first diary",
      text = "just first"
    )
    var coll: IdCollectionInfo? = null
    blit("upload collections") {
      coll = client.uploadCollection(collection)
    }

    blit("modify collections") {
      collection.images = ArrayList(pics.map { it.id })
      coll = client.modifyCollection(coll!!.id, collection)
      coll!!.data.previews!!.size `should be equal to` collection.images!!.size
      collection.images!!.removeAt(0)
      coll = client.modifyCollection(coll!!.id, collection)
      coll!!.data.images!!.size `should be equal to` collection.images!!.size
    }

    lateinit var colls: List<IdCollectionInfo>
    blit("query my collections") {
      colls = client.getMyCollections()
      colls.size `should be equal to` 1

      val collGot: CollectionInfo = colls[0].data
      collGot.images!! `should equal` collection.images!!
      collGot.previews!!.size `should be equal to` collection.images!!.size
    }

    blit("query collections by a picture") {
      val zero = client.getCollectionsContainPicture(pics[0].id)
      zero.size `should be equal to` 0
      val one = client.getCollectionsContainPicture(pics[1].id)
      one.size `should be equal to` 1
    }

    blit("get random collections") {
      val queried = client.getRandomCollections()
      // it filters myself one
      queried.size `should be equal to` 0
    }

    blit("delete my collection") {
      client.deleteCollection(colls[0].id)
    }

    blit("get profile") {
      client.uploadCollection(CollectionInfo())
      val profile = client.getProfile()
      profile.collectionCount `should be equal to` 1
    }
  }
})

fun TestContainer.blit(description: String, body: suspend TestBody.() -> Unit) {
  it(description) {
    runBlocking { body() }
  }
}

fun TestContainer.xblit(description: String, body: suspend TestBody.() -> Unit) {
  xit(description) {
    runBlocking { body() }
  }
}

fun newClient(): Remote {
  val basePath = if (System.getProperty("heroku") != null)
    "http://landmarks-coms.herokuapp.com"
  else
    "http://localhost:8080"
  val engine = getTestEngine()
  return Remote(engine, basePath)
}

fun getTestEngine(): HttpClient {
  return HttpClient(OkHttp.create {
    config {
      if (System.getProperty("useDirect") == null) {
        proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("localhost", 8888)))
      }
      connectTimeout(1, TimeUnit.MINUTES)
      writeTimeout(1, TimeUnit.MINUTES)
      readTimeout(1, TimeUnit.MINUTES)
    }
  })
}

fun getRandomString(length: Long): String {
  val source = "abcdefghijklmnopqrstuvwxyz0123456789"
  return Random()
    .ints(length, 0, source.length)
    .mapToObj(source::get)
    .toArray()
    .joinToString("")
}