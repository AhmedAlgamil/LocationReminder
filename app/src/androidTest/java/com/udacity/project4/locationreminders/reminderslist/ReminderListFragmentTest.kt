package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
//import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode

@RunWith(AndroidJUnit4::class)
@MediumTest
@LooperMode(LooperMode.Mode.PAUSED)
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
@ExperimentalCoroutinesApi
class ReminderListFragmentTest {

    //    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.
    val reminder = ReminderDTO("Title1", "Description1", "Location1", 32.0, 30.0)
    private lateinit var repository: FakeDataSource

    @Before
    fun initRepository() {
        repository = FakeDataSource()
        ServiceLocator.reminderDataSource = repository
    }

    @After
    fun cleanupDb() = runBlockingTest {
        ServiceLocator.resetRepository()
    }



    @Test
    fun showAllReminders() {
        runBlocking {
            // Add one active task and one completed task
            launchActivity()
        }
    }

    private fun launchActivity(): ActivityScenario<RemindersActivity>? {
        val activityScenario = launch(RemindersActivity::class.java)
        activityScenario.onActivity { activity ->
            // Disable animations in RecyclerView
            (activity.findViewById(R.id.reminderssRecyclerView) as RecyclerView).itemAnimator = null
        }
        return activityScenario
    }

    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment(): Unit {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // THEN - Verify that we navigate to the add screen
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

}