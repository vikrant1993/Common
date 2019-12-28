package vk.help.placepicker

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_place_picker.*
import vk.help.MasterActivity
import vk.help.R
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.roundToInt

class ActivityPlacePicker : MasterActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val handler = Handler()
    private val filterTaskList: ArrayList<GetAddressFromLatLng> = ArrayList()
    private var addressModel: AddressModel? = null

    private val setLocationButtonBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ImageViewCompat.setImageTintList(
                myLocation,
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        if (intent.getStringExtra(DATA)!! == CENTER_LOCATION) R.color.material_blue else R.color.material_black
                    )
                )
            )
        }
    }

    private val myLocationRunnable = object : Runnable {
        override fun run() {
            try {
                if (googleMap != null) {
                    val cameraPosition = googleMap!!.cameraPosition.target
                    val currentPosition = googleMap!!.myLocation
                    val cameraPositionLatitude =
                        DecimalFormat("##.#####").format(cameraPosition.latitude).toDouble()
                    val cameraPositionLongitude =
                        DecimalFormat("##.#####").format(cameraPosition.longitude).toDouble()
                    val currentPositionLatitude =
                        DecimalFormat("##.#####").format(currentPosition.latitude).toDouble()
                    val currentPositionLongitude =
                        DecimalFormat("##.#####").format(currentPosition.longitude).toDouble()
                    sendBroadcast(
                        Intent(CUSTOM_INTENT_FILTER).putExtra(
                            DATA,
                            if (cameraPositionLatitude == currentPositionLatitude && cameraPositionLongitude == currentPositionLongitude) CENTER_LOCATION else NOT_CENTER_LOCATION
                        )
                    )
                }
            } catch (ignored: Exception) {
            } finally {
                handler.postDelayed(this, 200)
            }
        }
    }

    fun useAnother() {
        val fusedLocationProviderClient = FusedLocationProviderClient(context)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                showToast(
                    String.format(
                        Locale.US,
                        "%s -- %s",
                        location.latitude,
                        location.longitude
                    )
                )
            }
        }

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 20 * 1000

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        showToast(
                            String.format(
                                Locale.US,
                                "%s -- %s",
                                location.latitude,
                                location.longitude
                            )
                        )
                        fusedLocationProviderClient.removeLocationUpdates(this)
                    }
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val showMyLocationRunnable = object : Runnable {
        override fun run() {
            try {
                val latLng =
                    LatLng(googleMap!!.myLocation.latitude, googleMap!!.myLocation.longitude)
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18f)
                googleMap?.animateCamera(cameraUpdate)
            } catch (e: Exception) {
                e.printStackTrace()
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_picker)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_round_arrow_white_24px)
        toolbar.setNavigationOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        try {
            MapsInitializer.initialize(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)

        myLocation.setOnClickListener {
            handler.postDelayed(showMyLocationRunnable, 1000)
            checkPermission()
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomSheetForm)

        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                layoutAddressFormExpanded.alpha = slideOffset
                layoutAddressCollapsed.alpha = 1 - slideOffset
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                if (newState == STATE_EXPANDED) {
//                    layoutAddressFormExpanded.animate().alpha(1f).duration = 200
//                    layoutAddressCollapsed.animate().alpha(0f).duration = 200
//                } else if (newState == STATE_COLLAPSED) {
//                    layoutAddressFormExpanded.animate().alpha(0f).duration = 200
//                    layoutAddressCollapsed.animate().alpha(1f).duration = 200
//                }
            }
        })

        fullAddressTemp.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        buttonDone.setOnClickListener {
            val data = Intent()
            val resultCode: Int
            resultCode = if (addressModel != null) {
                data.putExtra(DATA, getJSON(addressModel!!))
                RESULT_OK
            } else {
                data.putExtra(DATA, "")
                RESULT_NULL
            }
            setResult(resultCode, data)
            finish()
        }

        selectAddress.setOnClickListener {
            val data = Intent()
            val resultCode: Int
            resultCode = if (addressModel != null) {
                data.putExtra(DATA, getJSON(addressModel!!))
                RESULT_OK
            } else {
                data.putExtra(DATA, "")
                RESULT_NULL
            }
            setResult(resultCode, data)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(setLocationButtonBroadcastReceiver, IntentFilter(CUSTOM_INTENT_FILTER))
        handler.postDelayed(myLocationRunnable, 200)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(setLocationButtonBroadcastReceiver)
        handler.removeCallbacks(myLocationRunnable)
        handler.removeCallbacks(showMyLocationRunnable)
    }

    override fun onMapReady(mMap: GoogleMap?) {
        googleMap = mMap
        checkPermission()
        googleMap!!.setOnCameraMoveStartedListener {
            aim.animate().alpha(1f).duration = 500
            ObjectAnimator.ofFloat(centralMarker, "translationY", -100f).setDuration(250).start()
        }

        googleMap!!.setOnCameraIdleListener {
            aim.animate().alpha(0f).duration = 500
            ObjectAnimator.ofFloat(centralMarker, "translationY", 0f).setDuration(250).start()
            try {
                val nowLocation = googleMap!!.cameraPosition.target
                if (nowLocation != null) {
                    getAddressByGeoCodingLatLng(nowLocation.latitude, nowLocation.longitude)
                } else {
                    showErrorToast("can't pick this location")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkPermission() {
        try {
            val permissionList = ArrayList<String>()
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }

            if (permissionList.size > 0) {
                showToast("required location permission")
                ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 100)
            } else {
                googleMap?.isMyLocationEnabled = true
                googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                googleMap?.uiSettings?.isCompassEnabled = true
//                createLocationRequest()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createLocationRequest() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment!!.getMapAsync { mMap ->
                googleMap = mMap
                googleMap!!.isMyLocationEnabled = true
                googleMap!!.uiSettings.isMyLocationButtonEnabled = false
                googleMap!!.uiSettings.isCompassEnabled = true

                mMap.setOnCameraMoveStartedListener {
                    aim.animate().alpha(1f).duration = 500
                    ObjectAnimator.ofFloat(centralMarker, "translationY", -100f).setDuration(250)
                        .start()
                }

                mMap.setOnCameraIdleListener {
                    aim.animate().alpha(0f).duration = 500
                    ObjectAnimator.ofFloat(centralMarker, "translationY", 0f).setDuration(250)
                        .start()
                    try {
                        val nowLocation = mMap.cameraPosition.target
                        if (nowLocation != null) {
                            getAddressByGeoCodingLatLng(nowLocation.latitude, nowLocation.longitude)
                        } else {
                            showErrorToast("can't pick this location")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            myLocation.performClick()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, 3512)
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (permissions.isNotEmpty()) {
                if (grantResults.isEmpty()) {
                    showErrorToast("Permission Required")
                }
            } else {
                showErrorToast("Permission Required")
            }
        }
    }

    private fun getAddressByGeoCodingLatLng(lat: Double, lng: Double) {
        if (lat != 0.0 && lng != 0.0) {
            for (prevTask in filterTaskList) {
                prevTask.cancel(true)
            }
            filterTaskList.clear()
            val asyncTask = GetAddressFromLatLng()
            filterTaskList.add(asyncTask)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, lat, lng)
        } else {
            showToast("location not fetch")
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetAddressFromLatLng : AsyncTask<Double, Void, AddressModel?>() {
        var latitude: Double = 00.00
        var longitude: Double = 0.00

        override fun doInBackground(vararg doubles: Double?): AddressModel? {
            try {
                latitude = doubles[0]!!
                longitude = doubles[1]!!

                val addresses: List<Address>? =
                    Geocoder(context, Locale.getDefault()).getFromLocation(
                        latitude,
                        longitude,
                        1
                    )


                if (addresses != null && addresses.isNotEmpty()) {

                    val addressWeGet: Address? = addresses[0]

                    addressModel = AddressModel()
                    addressModel!!.latitude = roundAvoid(latitude)
                    addressModel!!.longitude = roundAvoid(longitude)
                    try {
                        addressModel!!.localAddress = addressWeGet!!.getAddressLine(0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        addressModel!!.localAddress = ""
                    }

                    try {
                        addressModel!!.city = addressWeGet!!.locality
                    } catch (e: Exception) {
                        e.printStackTrace()
                        addressModel!!.city = ""
                    }

                    try {
                        addressModel!!.state = addressWeGet!!.adminArea
                    } catch (e: Exception) {
                        e.printStackTrace()
                        addressModel!!.state = ""
                    }

                    try {
                        addressModel!!.countryName = addressWeGet!!.countryName
                    } catch (e: Exception) {
                        e.printStackTrace()
                        addressModel!!.countryName = ""
                    }

                    try {
                        addressModel!!.postalCode = addressWeGet!!.postalCode
                    } catch (e: Exception) {
                        e.printStackTrace()
                        addressModel!!.postalCode = ""
                    }

                    try {
                        addressModel!!.fullAddress =
                            addressModel?.localAddress + ", " + addressModel!!.city + ", " + addressModel!!.state + "(" + addressModel!!.postalCode + "), " + addressModel!!.countryName
                    } catch (e: Exception) {
                        e.printStackTrace()
                        addressModel!!.fullAddress = ""
                    }

                    return addressModel
                } else {
                    return null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(_userAddress: AddressModel?) {
            super.onPostExecute(_userAddress)
            try {
                addressModel = _userAddress
                if (_userAddress != null) {
                    fullAddressTemp.text = _userAddress.fullAddress
                    addressTitle.setText(_userAddress.fullAddress)
                    localAddress.setText(_userAddress.localAddress)
                    state.setText(_userAddress.state)
                    postalCode.setText(_userAddress.postalCode)
                    description.setText(_userAddress.countryName)
                } else {
                    fullAddressTemp.text = ""
                    addressTitle.setText("")
                    localAddress.setText("")
                    state.setText("")
                    postalCode.setText("")
                    description.setText("")
                }
            } catch (e: Exception) {
                fullAddressTemp.text = ""
                addressTitle.setText("")
                localAddress.setText("")
                state.setText("")
                postalCode.setText("")
                description.setText("")
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetLatLngFromAddress : AsyncTask<String, Void, LatLng>() {

        override fun doInBackground(vararg userAddress: String): LatLng {
            var latLng = LatLng(0.0, 0.0)

            try {

                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address>
                //get location from lat long if address string is null
                addresses = geocoder.getFromLocationName(
                    "H Block, Sector 62, Noida, Uttar Pradesh 201301",
                    1
                )
                if (addresses != null && addresses.isNotEmpty()) {
                    latLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                }
            } catch (ignored: Exception) {
                ignored.printStackTrace()
            }
            return latLng
        }

        override fun onPostExecute(latLng: LatLng) {
            super.onPostExecute(latLng)
            log(String.format("%s -- %s", latLng.latitude, latLng.longitude))
            showToast(String.format("%s -- %s", latLng.latitude, latLng.longitude))
        }
    }

    fun roundAvoid(value: Double): Double {
        val scale = 10.0.pow(6.0)
        return (value * scale).roundToInt() / scale
    }

    companion object {
        const val CENTER_LOCATION = "location.in.center"
        const val CUSTOM_INTENT_FILTER = "location.button.update"
        const val NOT_CENTER_LOCATION = "location.not.in.center"
        const val RESULT_NULL = 404
    }
}