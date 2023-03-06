package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        //      TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //TODO: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //TODO call @sendNotification
        if (intent.action == SaveReminderFragment.ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent!!.hasError()) {
                val errorMessage = errorMessage(this, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                geofencingEvent.triggeringGeofences?.let { sendNotification(it) }
            }
        }
    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            val remindersLocalRepository: ReminderDataSource by inject()
            for (item in triggeringGeofences) {
                val result = remindersLocalRepository.getReminder(item.requestId)

                if (result is Result.Success<ReminderDTO>) {
                    //send a notification to the user with the reminder details
                    val reminder = result.data
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                    )
                } else if (result is Result.Error) {
                    Log.e(TAG, "${result.message.toString()}")
                    Log.e(TAG, "status ${result.statusCode.toString()}")

                }
            }

        }
//        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
//            val remindersLocalRepository: ReminderDataSource by inject()
//            for (item in triggeringGeofences) {
//                val result = remindersLocalRepository.getReminders()
//                if (result is Result.Success<List<ReminderDTO>>) {
//                    //send a notification to the user with the reminder details
//                    val reminders = result.data
//                    for (item in reminders)
//                    sendNotification(
//                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
//                            item.title,
//                            item.description,
//                            item.location,
//                            item.latitude,
//                            item.longitude,
//                            item.id
//                        )
//                    )
//                } else if (result is Result.Error) {
//                    Log.e(TAG, "${result.message.toString()}")
//                    Log.e(TAG, "status ${result.statusCode.toString()}")
//
//                }
//            }
//
//        }
    }
}

private const val TAG = "GeofenceReceiver"