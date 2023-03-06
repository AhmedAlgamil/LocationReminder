package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofencingConstants
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private val REQUEST_LOCATION_PERMISSION = 1

    lateinit var sharedPreference: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var reminderDTO: ReminderDTO
    var isOpened: Boolean = false

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    public val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = SaveReminderFragment.ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel

        sharedPreference =
            requireActivity().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        editor = sharedPreference.edit()

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }


//    private fun checkPermissionsAndStartGeofencing() :Boolean {
//        if (foregroundAndBackgroundLocationPermissionApproved()) {
//            checkDeviceLocationSettingsAndStartGeofence()
//            return true
//        } else {
//            requestForegroundAndBackgroundLocationPermissions()
//
//            return false
//        }
//    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun chckingPermission() {
//        requestPermissions(
//            arrayOf<String>(
//                android.Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ),
//            REQUEST_LOCATION_PERMISSION
//        )
//    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        when (requestCode) {
//            REQUEST_LOCATION_PERMISSION -> {
//                if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//                    requireActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                ) {
//                    checkDeviceLocationSettingsAndStartGeofence()
//                } else if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED &&
//                    requireActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
//                ) {
//                    if (sharedPreference.getBoolean("isCanceled", false)) {
//
//                    } else {
//                        Snackbar.make(
//                            binding.root,
//                            R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
//                        ).setAction(android.R.string.ok) {
//                            editor.putBoolean("isCanceled", true)
//                            editor.commit()
//                        }.show()
//                    }
//
//                }
//            }
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            // Permission denied.
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
//            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        Log.d(TAG, "Request foreground only location permission")
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        binding.saveReminder.setOnClickListener {
            val locationManger: LocationManager
            locationManger = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                isOpened = locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {

            }
            if (foregroundAndBackgroundLocationPermissionApproved()) {
                checkDeviceLocationSettingsAndStartGeofence()
            } else {
                requestForegroundAndBackgroundLocationPermissions()
            }

        }
    }

    public fun removeGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                Log.d("SaveReminderFragment", "removed")
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d("SaveReminderFragment", "Not removed")
            }
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
                try {
//                    exception.startResolutionForResult(requireActivity(),
//                        REQUEST_TURN_DEVICE_LOCATION_ON)
                    activity?.startIntentSenderForResult(
                        exception.getResolution().getIntentSender(),
                        REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {

            }

        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                val title = _viewModel.reminderTitle.value
                val description = _viewModel.reminderDescription.value
                val location = _viewModel.reminderSelectedLocationStr.value
                val latitude = _viewModel.latitude.value
                val longitude = _viewModel.longitude.value
                reminderDTO = ReminderDTO(
                    title,
                    description,
                    location,
                    latitude,
                    longitude
                )
                if (_viewModel.validateAndSaveReminder(
                        reminderDTO
                    )
                ) {
                    addingGeoFences(
                        reminderDTO
                    )
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    private fun addingGeoFences(reminderDataItem: ReminderDTO) {
        val geofence = Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(reminderDataItem.id)

            // Set the circular region of this geofence.
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )

            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(Geofence.NEVER_EXPIRE)

            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

            // Create the geofence.
            .build()

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()

        if (checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        } else {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    Log.d("Add Geofence", "the id is ${reminderDataItem.id}")
                }
                addOnFailureListener {
                    if ((it.message != null)) {
                        Log.w("SaveReminderFragment", it.message.toString())
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }


    override fun onStart() {
        super.onStart()
    }

    companion object {
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val TAG = "SELETLocationFragment"
        lateinit var geofencingClient: GeofencingClient
        internal const val ACTION_GEOFENCE_EVENT =
            "ReminderActivity.project4.action.ACTION_GEOFENCE_EVENT"
        // A PendingIntent for the Broadcast Receiver that handles geofence transitions.

    }

}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "HuntMainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

