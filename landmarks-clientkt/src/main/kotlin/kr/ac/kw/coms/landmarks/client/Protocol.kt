package kr.ac.kw.coms.landmarks.client

import com.beust.klaxon.Json
import com.beust.klaxon.JsonObject
import com.beust.klaxon.internal.firstNotNullResult
import java.util.*

data class ServerFault(
  val error: String,
  val stacktrace: String? = null
) : Throwable(error)

data class ServerOK(val msg: String)

data class WithIntId<T>(val id: Int, val value: T)

data class AccountForm(
  val login: String? = null,
  val password: String? = null,
  val email: String? = null,
  val nick: String? = null
)

interface IPictureInfo {
  var uid: Int?
  var author: String?
  var address: String?
  var lat: Float?
  var lon: Float?
  var time: Date?
  var isPublic: Boolean
}

data class PictureInfo(
  override var uid: Int? = null,
  override var author: String? = null,
  override var address: String? = null,
  override var lat: Float? = null,
  override var lon: Float? = null,
  override var time: Date? = null,
  override var isPublic: Boolean = true
) : IPictureInfo

interface ICollectionInfo {
  var title: String?
  var text: String?
  var images: ArrayList<Int>?
  var previews: ArrayList<WithIntId<PictureInfo>>?
  var likes: Int?
  var liking: Boolean?
  var isRoute: Boolean?
  var isPublic: Boolean?
  var parent: Int?
}

data class CollectionInfo(
  override var title: String? = null,
  override var text: String? = null,
  override var images: ArrayList<Int>? = null,
  override var previews: ArrayList<WithIntId<PictureInfo>>? = null,
  override var likes: Int? = null,
  override var liking: Boolean? = null,
  override var isRoute: Boolean? = null,
  override var isPublic: Boolean? = null,
  override var parent: Int? = null
) : ICollectionInfo

/**
 * https://wiki.openstreetmap.org/wiki/Nominatim
 */
class NominatimReverseGeocodeJsonV2 {
  @Json("place_id")
  var placeId: Int? = null
  var licence: String? = null
  @Json("osm_type")
  var osmType: String? = null
  @Json("osm_id")
  var osmId: String? = null
  @Json("lat")
  var latitude: Double? = null
  @Json("lon")
  var longitude: Double? = null
  @Json("place_rank")
  var placeRank: Int? = null
  var category: String? = null
  var type: String? = null
  var importance: Float? = null
  @Json("addresstype")
  var addressType: String? = null
  @Json("display_name")
  var displayName: String? = null
  var name: String? = null
  var address: NominatimAddressJson? = null
}

class NominatimAddressJson {
  // different by type
  var attraction: String? = null
  var parking: String? = null
  //

  var city: String? = null
  @Json("city_district")
  var cityDistrict: String? = null
  var construction: String? = null
  var continent: String? = null
  var country: String? = null
  @Json("country_code")
  var countryCode: String? = null
  @Json("house_number")
  var houseNumber: Int? = null
  var neighbourhood: String? = null
  var postcode: String? = null
  @Json("public_building")
  var publicBuilding: String? = null
  var road: String? = null
  var state: String? = null
  @Json("state_district")
  var stateDistrict: String? = null
  var suburb: String? = null
  var village: String? = null
}

class ReverseGeocodeResult(val json: JsonObject) {
  val addr: JsonObject?
    get() = json.obj("address")
  val country: String?
    get() = addr?.string("country")
  val detail: String?
    get() = addr?.let { adr ->
      val prio = listOf("city", "county", "town", "attraction")
      prio.map(adr::string).firstNotNullResult { it }
    }
}
