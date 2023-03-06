package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var selectedLocationMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            longitude = location.longitude
            latitude = location.latitude
            val zoomLevel = 15f
            val myLocation = LatLng(location.longitude, location.latitude)
            selectedLocationMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoomLevel))
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {

        }

        override fun onProviderDisabled(provider: String) {}
    }
    private val REQUEST_LOCATION_PERMISSION = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.selecting_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.btnSelectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.Back
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

//        mapFragment = binding.selectingMap
//        TODO: zoom to the user location after taking his permission

//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }


    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            selectedLocationMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            selectedLocationMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            selectedLocationMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            selectedLocationMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        if (googleMap != null) {
            selectedLocationMap = googleMap
        }
        grantPermission()
        enableMyLocation()

        selectedLocationMap.setOnMyLocationClickListener {
            val zoomLevel = 15f
            val myLocation = LatLng(it.latitude, it.longitude)
            selectedLocationMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoomLevel))
            selectedLocationMap.addMarker(MarkerOptions().position(myLocation))
        }
        setPoiClick(selectedLocationMap)
        setMapStyle(selectedLocationMap)
        setMapOnClickListener(selectedLocationMap)

    }

    private fun setMapOnClickListener(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            val selectedLocation = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(_viewModel.reminderTitle.value.toString())
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            )
            selectedLocation.showInfoWindow()
            _viewModel.latitude.value = selectedLocation.position.latitude
            _viewModel.longitude.value = selectedLocation.position.longitude
            _viewModel.reminderSelectedLocationStr.value = selectedLocation.title
            Toast.makeText(
                requireContext(),
                "latitude ${selectedLocation.position.latitude} longitude ${selectedLocation.position.longitude} title ${_viewModel.reminderSelectedLocationStr.value} ",
                Toast.LENGTH_SHORT
            ).show()
            Log.d(
                "The location",
                "latitude ${selectedLocation.position.latitude} longitude ${selectedLocation.position.longitude} title ${_viewModel.reminderSelectedLocationStr.value} "
            )
        }
    }

    fun grantPermission()
    {
        requestPermissions(
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    // Places a marker on the map and displays an info window that contains POI name.
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }

        map.setOnPoiClickListener {
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latLng.latitude,
                it.latLng.longitude
            )

            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .snippet(snippet)
                    .title(it.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            )

            poiMarker.showInfoWindow()
            _viewModel.latitude.value = poiMarker.position.latitude
            _viewModel.longitude.value = poiMarker.position.longitude
            _viewModel.reminderSelectedLocationStr.value = poiMarker.title
            Log.d(
                "The location",
                "latitude ${poiMarker.position.latitude} longitude ${poiMarker.position.longitude} "
            )
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            )

        }
    }


    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )
            if (!success) {

            }
        } catch (e: Resources.NotFoundException) {

        }

    }

    //Getting The Location GPS On Or Off

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e(
                        TAG,
                        "Error geting location settings resolution: " + sendEx.message
                    )
                }
            } else {

            }

        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "GPS work successfully ")
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
            else{
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                }.show()
            }
        }
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mFusedLocationClient.lastLocation.addOnCompleteListener {
                try{
                    val location: Location? = it.result
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val list: List<Address> =
                        geocoder.getFromLocation(location!!.latitude, location.longitude, 1)
                    val zoomLevel = 15f
//            // Add a marker in Sydney and move the camera
                    val mosque = LatLng(list[0].latitude, list[0].longitude)
                    selectedLocationMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mosque, zoomLevel))
                }
                catch (exception :Exception)
                {
                    checkDeviceLocationSettingsAndStartGeofence()
                }
            }
            selectedLocationMap.setMyLocationEnabled(true)

//            LocationServices.getFusedLocationProviderClient(requireContext())
//            val latitude = 30.724645
//            val longitude = 31.457044
//            val zoomLevel = 15f
//            // Add a marker in Sydney and move the camera
//            val mosque = LatLng(selectedLocationMap.myLocation.latitude, selectedLocationMap.myLocation.longitude)
//            selectedLocationMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mosque, zoomLevel))
//            selectedLocationMap.addMarker(MarkerOptions().position(mosque).title("جامع الاربعين"))

        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    companion object {
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val TAG = "SELETLocationFragment"
    }

    // Checks that users have given permission
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

}
