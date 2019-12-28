package vk.help.placepicker

import java.io.Serializable

open class AddressModel : Serializable {

    var latitude = 0.0
    var longitude = 0.0
    var postalCode = ""
    var city = ""
    var state = ""
    var countryName = ""
    var localAddress = ""
    var fullAddress = ""
}