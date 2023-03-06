package com.udacity.project4.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources.Theme
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat.ThemeCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import kotlin.random.Random

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var customLayout: AuthMethodPickerLayout
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var loginSuccessfulIntent: Intent
    private lateinit var sharedPreference: SharedPreferences

    companion object {
        const val TAG = "AuthActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        loginSuccessfulIntent = Intent(applicationContext, RemindersActivity::class.java)
        sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        if(sharedPreference.getLong("Success",0L) == 100L)
        {
            startActivity(loginSuccessfulIntent)
        }
        else{

        }
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        binding.btnLogin.setOnClickListener {
            launchSignInFlow()
        }
//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }


    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
             AuthUI.IdpConfig.GoogleBuilder().build(),AuthUI.IdpConfig.EmailBuilder().build()
        )
        customLayout = AuthMethodPickerLayout.Builder(R.layout.firebase_authentication)
            .setEmailButtonId(R.id.btn_email_login)
            .setGoogleButtonId(R.id.btn_google_login)
            .build()

        val signIntent: Intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAlwaysShowSignInMethodScreen(true)
            .setAuthMethodPickerLayout(customLayout)
            .setAvailableProviders(providers)
            .build()
        startActivityForResult(
            signIntent, SIGN_IN_RESULT_CODE
        )
        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.
//        startActivityForResult(
//            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
//                providers
//            ).build(), SIGN_IN_RESULT_CODE
//        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
                var editor = sharedPreference.edit()
                editor.putString("username",FirebaseAuth.getInstance().currentUser?.displayName)
                editor.putLong("Success",100L)
                editor.commit()
                startActivity(loginSuccessfulIntent)

            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
                Toast.makeText(
                    applicationContext,
                    "Sign in unsuccessful ${response?.error?.errorCode}",
                    Toast.LENGTH_LONG
                ).show()
                var editor = sharedPreference.edit()
                editor.putLong("Success",0L)
            }
        }
    }

}
