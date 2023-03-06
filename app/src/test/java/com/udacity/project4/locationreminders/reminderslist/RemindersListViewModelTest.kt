package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.LiveDataTestUtil
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.locationreminders.data.dto.Result
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    // Subject under test
    private lateinit var reminderViewModel: RemindersListViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var reminderFakeDataSource: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val reminderItem = ReminderDTO("Title1", "Description1","Location",32.0,30.0)

    @Before
    fun setupViewModel() {
        stopKoin()
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 32.0, 30.0)
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 32.0, 30.0)
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 32.0, 30.0)
        reminderFakeDataSource = FakeDataSource()
        reminderFakeDataSource.addReminders(reminder1, reminder2, reminder3)
        reminderViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            reminderFakeDataSource
        )
        val testModule = module {
            viewModel {
                RemindersListViewModel(
                    ApplicationProvider.getApplicationContext(),
                    reminderFakeDataSource
                )
            }
            single<ReminderDataSource> {
                FakeDataSource()
            }
        }

        startKoin {
            modules(listOf(testModule))
        }
    }

    @Test
    fun loadReminders_fromDataSource()
    {
        reminderViewModel.loadReminders()
        // And data correctly loaded
        assertThat(LiveDataTestUtil.getValue(reminderViewModel.remindersList)).hasSize(3)
        assertThat(LiveDataTestUtil.getValue(reminderViewModel.remindersList)).isNotNull()
    }

    @Test
    fun loadReminders_Error() = mainCoroutineRule.runBlockingTest {
        reminderFakeDataSource.setReturnError(true)
        reminderViewModel.loadReminders()
//         THEN - Verify that an error occurred.
        val result = reminderViewModel.remindersList.value
        assertThat(LiveDataTestUtil.getValue(reminderViewModel.showSnackBar)).isEqualTo("Test Error")
    }

    @Test
    fun loadReminder_ShowAllData()
    {
        reminderViewModel.loadReminders()

        assertThat(reminderViewModel.remindersList.value!!.get(0).title).isEqualTo("Title1")
        assertThat(reminderViewModel.remindersList.value!!.get(0).description).isEqualTo("Description1")
        assertThat(reminderViewModel.remindersList.value!!.get(0).location).isEqualTo("Location1")
        assertThat(reminderViewModel.remindersList.value!!.get(1).title).isEqualTo("Title2")
        assertThat(reminderViewModel.remindersList.value!!.get(1).description).isEqualTo("Description2")
        assertThat(reminderViewModel.remindersList.value!!.get(1).location).isEqualTo("Location2")
    }

    @Test
    fun loadReminders_Null() = runBlocking()
    {
        reminderFakeDataSource.deleteAllReminders()
        assertThat(LiveDataTestUtil.getValue(reminderViewModel.remindersList)).isNull()
    }

    @Test
    fun loadReminders_ShowLoadingValue()
    {
        mainCoroutineRule.pauseDispatcher()
        reminderViewModel.loadReminders()
        assertThat(LiveDataTestUtil.getValue(reminderViewModel.showLoading)).isEqualTo(true)
        mainCoroutineRule.resumeDispatcher()
        assertThat(LiveDataTestUtil.getValue(reminderViewModel.showLoading)).isEqualTo(false)
    }



}