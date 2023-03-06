package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService.Companion.enqueueWork
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {

//TODO: implement the onReceive method to receive the geofencing events at the background
    override fun onReceive(context: Context, intent: Intent) {
        // TODO: Step 11 implement the onReceive method
        enqueueWork(context, intent)

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            Log.e("triggeringGeofences", triggeringGeofences.size.toString())

            // Send notification and log the transition details.
//            sendNotification(geofenceTransitionDetails)
//            Log.i(TAG, geofenceTransitionDetails)
            
        } else {
            // Log the error.
//            Log.e(TAG, getString(
//                R.string.geofence_transition_invalid_type,
//                geofenceTransition))
        }
    }
}

private const val TAG = "GeofenceReceiver"