package com.udacity.project4.locationreminders.reminderslist

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofencingConstants
import com.udacity.project4.locationreminders.geofence.LandmarkDataObject
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding

    lateinit var sharedPreference: SharedPreferences
    lateinit var editor: Editor

    public val geofencePendingIntent : PendingIntent by lazy{
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = SaveReminderFragment.ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )

        sharedPreference =
            requireActivity().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        editor = sharedPreference.edit()

        binding.viewModel = _viewModel
        SaveReminderFragment.geofencingClient = LocationServices.getGeofencingClient(requireContext())

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)

        setTitle(getString(R.string.app_name))
        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
    }

    @SuppressLint("FragmentLiveDataObserve")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }


    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }
//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
//                TODO: add the logout implementation
                editor.remove("Success")
                editor.commit()
                requireActivity().finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public fun addingGeoFences(reminderDataItem: ReminderDataItem) {
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

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
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
        }
        else
        {
            SaveReminderFragment.geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    Log.d("Add Geofence", "the request id is ${geofence.requestId}")
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


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }
    companion object {


    }
}
