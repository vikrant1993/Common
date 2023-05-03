package vk.help.placepicker

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import vk.help.CommonTask
import vk.help.R
import vk.help.databinding.ActivityPlacePickerBinding
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

class ActivityPlacePicker : AppCompatActivity(), OnMapReadyCallback, CommonTask {

    val context: Context by lazy {
        this
    }

    private var googleMap: GoogleMap? = null
    private val filterTaskList: ArrayList<GetAddressFromLatLng> = ArrayList()
    private var addressModel: AddressModel? = null

    private val setLocationButtonBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ImageViewCompat.setImageTintList(
                binding.myLocation,
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
                String.format(
                    Locale.US,
                    "%s -- %s",
                    location.latitude,
                    location.longitude
                ).toToast()
            }
        }

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 20 * 1000

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        String.format(
                            Locale.US,
                            "%s -- %s",
                            location.latitude,
                            location.longitude
                        ).toToast()
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
                googleMap?.let {
                    val latLng = LatLng(it.myLocation.latitude, it.myLocation.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18f)
                    it.animateCamera(cameraUpdate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val binding: ActivityPlacePickerBinding by lazy {
        ActivityPlacePickerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_round_arrow_white_24px)
        binding.toolbar.setNavigationOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        try {
            MapsInitializer.initialize(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)

        binding.myLocation.setOnClickListener {
            handler.postDelayed(showMyLocationRunnable, 1000)
            checkPermission()
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutBottomSheetForm)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.layoutAddressFormExpanded.alpha = slideOffset
                binding.layoutAddressCollapsed.alpha = 1 - slideOffset
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

        binding.fullAddressTemp.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.buttonDone.setOnClickListener {
            val data = Intent()
            val resultCode: Int = addressModel?.let {
                data.putExtra(DATA, addressModel?.toJSON())
                RESULT_OK
            } ?: run {
                data.putExtra(DATA, "")
                RESULT_NULL
            }
            setResult(resultCode, data)
            finish()
        }

        binding.selectAddress.setOnClickListener {
            binding.buttonDone.performClick()
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

    override fun onMapReady(mMap: GoogleMap) {
        googleMap = mMap
        checkPermission()
        googleMap?.setOnCameraMoveStartedListener {
            binding.aim.animate().alpha(1f).duration = 500
            ObjectAnimator.ofFloat(binding.centralMarker, "translationY", -100f).setDuration(250)
                .start()
        }

        googleMap?.setOnCameraIdleListener {
            binding.aim.animate().alpha(0f).duration = 500
            ObjectAnimator.ofFloat(binding.centralMarker, "translationY", 0f).setDuration(250)
                .start()
            try {
                googleMap?.cameraPosition?.target?.let { nowLocation ->
                    getAddressByGeoCodingLatLng(nowLocation.latitude, nowLocation.longitude)
                } ?: "can't pick this location".log()
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
                "required location permission".toToast()
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
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync { mMap ->
                googleMap = mMap
                googleMap?.isMyLocationEnabled = true
                googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                googleMap?.uiSettings?.isCompassEnabled = true

                mMap.setOnCameraMoveStartedListener {
                    binding.aim.animate().alpha(1f).duration = 500
                    ObjectAnimator.ofFloat(binding.centralMarker, "translationY", -100f)
                        .setDuration(250).start()
                }

                mMap.setOnCameraIdleListener {
                    binding.aim.animate().alpha(0f).duration = 500
                    ObjectAnimator.ofFloat(binding.centralMarker, "translationY", 0f)
                        .setDuration(250)
                        .start()
                    try {
                        val nowLocation = mMap.cameraPosition.target
                        getAddressByGeoCodingLatLng(nowLocation.latitude, nowLocation.longitude)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            binding.myLocation.performClick()
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
                    "Permission Required".toToast()
                }
            } else {
                "Permission Required".toToast()
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
            "location not fetch".toToast()
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetAddressFromLatLng : AsyncTask<Double, Void, AddressModel?>() {
        var tempLatitude: Double = 0.0
        var tempLongitude: Double = 0.0

        override fun doInBackground(vararg doubles: Double?): AddressModel? {
            try {
                tempLatitude = doubles[0]!!
                tempLongitude = doubles[1]!!

                "lat $tempLatitude lng $tempLongitude".log()

                return Geocoder(context, Locale.getDefault()).getFromLocation(
                    tempLatitude, tempLongitude, 1
                )?.firstOrNull()?.let { addressWeGet ->
                    AddressModel().apply {
                        latitude = roundAvoid(tempLatitude)
                        longitude = roundAvoid(tempLongitude)

                        localAddress = try {
                            addressWeGet.getAddressLine(0)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ""
                        }

                        city = addressWeGet.locality ?: ""
                        state = addressWeGet.adminArea ?: ""
                        countryName = addressWeGet.countryName ?: ""
                        postalCode = addressWeGet.postalCode ?: ""

                        fullAddress = try {
                            String.format(
                                "%s, %s, %s(%s), %s",
                                localAddress,
                                city,
                                state,
                                postalCode,
                                countryName
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ""
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(_userAddress: AddressModel?) {
            super.onPostExecute(_userAddress)
            addressModel = _userAddress
            _userAddress?.let {
                binding.fullAddressTemp.text = it.fullAddress
                binding.addressTitle.text = it.fullAddress
                binding.localAddress.text = it.localAddress
                binding.state.text = it.state
                binding.postalCode.text = it.postalCode
                binding.description.text = it.countryName
            } ?: run {
                binding.fullAddressTemp.text = ""
                binding.addressTitle.text = ""
                binding.localAddress.text = ""
                binding.state.text = ""
                binding.postalCode.text = ""
                binding.description.text = ""
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetLatLngFromAddress : AsyncTask<String, Void, LatLng>() {

        override fun doInBackground(vararg userAddress: String): LatLng {
            var latLng = LatLng(0.0, 0.0)

            try {

                val geocoder = Geocoder(context, Locale.getDefault())
                //get location from lat long if address string is null
                geocoder.getFromLocationName(
                    "H Block, Sector 62, Noida, Uttar Pradesh 201301",
                    1
                )?.firstOrNull()?.let {
                    latLng = LatLng(it.latitude, it.longitude)
                }
            } catch (ignored: Exception) {
                ignored.printStackTrace()
            }
            return latLng
        }

        override fun onPostExecute(latLng: LatLng) {
            super.onPostExecute(latLng)
            String.format("%s -- %s", latLng.latitude, latLng.longitude).log()
            String.format("%s -- %s", latLng.latitude, latLng.longitude).toToast()
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