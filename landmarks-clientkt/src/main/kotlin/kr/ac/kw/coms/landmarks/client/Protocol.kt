package kr.ac.kw.coms.landmarks.client

import com.beust.klaxon.*
import com.beust.klaxon.internal.*
import kotlinx.serialization.*
import java.util.*

@Serializable
data class ServerFault(
  val error: String
) : Exception(error)

interface IntIdentifiable {
  var id: Int
}

interface IAccountForm {
  val login: String?
  val password: String?
  val email: String?
  val nick: String?
}

@Serializable
data class AccountForm(
  override val login: String? = null,
  override val password: String? = null,
  override val email: String? = null,
  override val nick: String? = null
) : IAccountForm

@Serializable
data class IdAccountForm(
  override var id: Int,
  var data: AccountForm
) : IntIdentifiable, IAccountForm by data {

  override fun equals(other: Any?): Boolean {
    return other is IdPictureInfo && id == other.id
  }

  override fun hashCode(): Int {
    return id * 10
  }
}

interface IPictureInfo {
  var uid: Int?
  var author: String?
  var width: Int?
  var height: Int?
  var address: String?
  var lat: Double?
  var lon: Double?
  var time: Date?
  var isPublic: Boolean
}

@Serializable
data class PictureInfo(
  override var uid: Int? = null,
  override var author: String? = null,
  override var width: Int? = null,
  override var height: Int? = null,
  override var address: String? = null,
  override var lat: Double? = null,
  override var lon: Double? = null,
  @Transient
  override var time: Date? = null,
  override var isPublic: Boolean = true
) : IPictureInfo

@Serializable
data class IdPictureInfo(
  override var id: Int,
  var data: PictureInfo
) : IntIdentifiable, IPictureInfo by data {

  override fun equals(other: Any?): Boolean {
    return other is IdPictureInfo && id == other.id
  }

  override fun hashCode(): Int {
    return id * 10
  }
}

interface ICollectionInfo {
  var title: String?
  var text: String?
  var images: ArrayList<Int>?
  var previews: ArrayList<IdPictureInfo>?
  var likes: Int?
  var liking: Boolean?
  var isRoute: Boolean?
  var isPublic: Boolean?
  var parent: Int?
}

@Serializable
data class CollectionInfo(
  override var title: String? = null,
  override var text: String? = null,
  override var images: ArrayList<Int>? = null,
  override var previews: ArrayList<IdPictureInfo>? = null,
  override var likes: Int? = null,
  override var liking: Boolean? = null,
  override var isRoute: Boolean? = null,
  override var isPublic: Boolean? = null,
  override var parent: Int? = null
) : ICollectionInfo

@Serializable
data class IdCollectionInfo(
  override var id: Int,
  var data: CollectionInfo
) : IntIdentifiable, ICollectionInfo by data {

  override fun equals(other: Any?): Boolean {
    return other is IdPictureInfo && id == other.id
  }

  override fun hashCode(): Int {
    return id * 10
  }
}

@Serializable
sealed class UserFilter {
  @Serializable
  data class Include(val uid: Int) : UserFilter() {
    override fun toString() = "uid=$uid"
  }

  @Serializable
  data class Exclude(val uid: Int) : UserFilter() {
    override fun toString() = "not_uid=$uid"
  }
}

@Serializable
data class NearGeoPoint(var lat: Double, var lon: Double, var km: Double) {
  override fun toString() = "lat=$lat&lon=$lon&km=$km"
}

@Serializable
class PictureQuery {
  var userFilter: UserFilter? = null
  var geoFilter: NearGeoPoint? = null
  var offset: Int? = null
  var limit: Int? = null

  override fun toString(): String {
    val sb = StringBuilder()
    offset?.also { sb.append("&offset=$it") }
    limit?.also { sb.append("&limit=$it") }
    userFilter?.also { sb.append("&$it") }
    geoFilter?.also { sb.append("&$it") }
    return sb.toString()
  }
}

@Serializable
data class ProfileInfo(
  var collectionCount: Int,
  var pictureCount: Int,
  @Transient
  var registered: Date = Date()
)

/**
 * https://wiki.openstreetmap.org/wiki/Nominatim
 */
@Serializable
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

@Serializable
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
