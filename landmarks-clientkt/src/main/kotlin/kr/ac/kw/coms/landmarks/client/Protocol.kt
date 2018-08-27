package kr.ac.kw.coms.landmarks.client

import com.beust.klaxon.Json

data class ErrorJson(val error: String)
data class SuccessJson(val msg: String)

data class LoginRep(
  val id: Int? = null,
  val login: String? = null,
  val password: String? = null,
  val email: String? = null,
  val nick: String? = null
)

data class PictureRep(
  val id: Int,
  val owner: Int? = null,
  val address: String,
  val lat: Float,
  val lon: Float,
  var file: ByteArray? = null
)

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