package com.udacity.project4.locationreminders

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.fragment.NavHostFragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.utils.createChanel
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        createChanel(this)
    }

}
