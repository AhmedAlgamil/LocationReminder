package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.LiveDataTestUtil
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var reminderFakeDataSource: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val reminderItem = ReminderDTO("TitleItem", "DescriptionItem", "LocationItem", 32.0, 30.0)

    @Before
    fun setupViewModel() {
        stopKoin()
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 32.0, 30.0)
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 32.0, 30.0)
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 32.0, 30.0)
        reminderFakeDataSource = FakeDataSource()
        reminderFakeDataSource.addReminders(reminder1, reminder2, reminder3)
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            reminderFakeDataSource
        )
        val testModule = module {
            viewModel {
                SaveReminderViewModel(
                    ApplicationProvider.getApplicationContext(),
                    reminderFakeDataSource
                )
            }
            single<ReminderDataSource> {
                FakeDataSource()
            }
        }

    }

    @Test
    fun saveReminder_SaveToDataBase() = runBlocking {
//        reminderFakeDataSource.saveReminder(reminderItem)
        saveReminderViewModel.saveReminder(reminderItem)
//        Truth.assertThat(LiveDataTestUtil.getValue(saveReminderViewModel.reminderTitle)).hasSize(3)
//        MatcherAssert.assertThat(
//            LiveDataTestUtil.getValue(saveReminderViewModel.reminderTitle),
//            `is`("TitleItem")
//        )
        val result = saveReminderViewModel.dataSource.getReminder(reminderItem.id) as Result.Success
        assertThat(result.data.title).isEqualTo("TitleItem")
    }


    @Test
    fun saveReminder_ShowToast() = runBlocking {
        saveReminderViewModel.saveReminder(reminderItem)
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showToast.value).isEqualTo("Reminder Saved !")
    }


    @Test
    fun validateEnteredData_ShowSnackBar_title_Empty() = runBlocking {
        val item = ReminderDTO("","describtion","location",0.0,0.0)
        saveReminderViewModel.validateEnteredData(item)
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showSnackBarInt.value).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun validateEnteredData_ShowSnackBar_locationEmpty() = runBlocking {
        val item = ReminderDTO("title","describtion","",0.0,0.0)
        saveReminderViewModel.validateEnteredData(item)
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showSnackBarInt.value).isEqualTo(R.string.err_select_location)
    }


}