package com.udacity.project4

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.AdditionalMatchers.not
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
//    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

//    @Before
//    fun registerIdlingResources() {
//        IdlingRegistry.getInstance().apply {
//            register(EspressoIdlingResource.countingIdlingResource)
//            register(dataBindingIdlingResource)
//        }
//    }
//
//    @After
//    fun unregisterIdlingResources() {
//        IdlingRegistry.getInstance().apply {
//            unregister(EspressoIdlingResource.countingIdlingResource)
//            unregister(dataBindingIdlingResource)
//        }
//    }

//    TODO: add End to End testing to the app

    @Test
    fun showSnackBar_Text()
    {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

//        onView(withId(R.id.title)).perform(ViewActions.typeText(""))

        onView(withId(com.udacity.project4.R.id.saveReminder)).perform(ViewActions.click())

        // Compare with the text message of snackbar
        onView(withText(R.string.err_enter_title))
            .check(matches(isDisplayed()));
    }

    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment():Unit {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), com.udacity.project4.R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "+" button
        onView(withId(com.udacity.project4.R.id.saveReminder)).perform(ViewActions.click())

        // THEN - Verify that we navigate to the add screen
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun showToast_Text()
    {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("title"))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("Ahmed Abd Elkareem"))
//        onView(withId(R.id.selectedLocation)).perform(ViewActions.("test"))
        onView(allOf(withId(R.id.selectedLocation), withText("Hello!")))
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

//        // THEN - Verify that we navigate to the add screen
        Mockito.verify(navController).navigate(
            SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment()
        )
        // Compare with the text message of snackbar
        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                not(
                    `is`(
                        getActivity(appContext)!!.getWindow().getDecorView()
                    )
                )
            )
        ).check(
            matches(
                isDisplayed()
            )
        )
    }

}
