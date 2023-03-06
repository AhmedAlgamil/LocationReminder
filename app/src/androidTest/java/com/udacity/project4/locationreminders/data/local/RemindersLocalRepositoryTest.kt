package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.runner.RunWith
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt


    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrivesReminder() = runBlockingTest {
        // GIVEN - a new task saved in the database
        val reminder = ReminderDTO("title", "description",
            "Location",0.0,0.0)
        localDataSource.saveReminder(reminder)

        // WHEN  - Task retrieved by ID
        val result = localDataSource.getReminder(reminder.id)

        // THEN - Same task is returned
        Assert.assertThat(result.succeeded, `is`(true))
        result as Result.Success
        Assert.assertThat(result.data.title, `is`("title"))
        Assert.assertThat(result.data.description, `is`("description"))
        Assert.assertThat(result.data.location, `is`("Location"))
        Assert.assertThat(result.data.latitude, `is`(0.0))
        Assert.assertThat(result.data.longitude, `is`(0.0))
    }

    @Test
    fun deleteAllReminde_emptyListOfRetrievedReminder() = runBlockingTest {
        // Given a new task in the persistent repository and a mocked callback
        val reminder = ReminderDTO("title", "description",
            "Location",0.0,0.0)

        localDataSource.saveReminder(reminder)

        // When all tasks are deleted
        localDataSource.deleteAllReminders()

        // Then the retrieved tasks is an empty list
        val result = localDataSource.getReminders() as Result.Success
        Assert.assertThat(result.data.isEmpty(), `is`(true))

    }

    @Test
    fun getReminder_NotFound() = runBlockingTest {
        // Given 2 new completed tasks and 1 active task in the persistent repository
        val reminder1 = ReminderDTO("title", "description",
            "Location",0.0,0.0,"Id1")
        val reminder2 = ReminderDTO("title", "description",
            "Location",0.0,0.0,"Id2")
        localDataSource.saveReminder(reminder1)
        localDataSource.saveReminder(reminder1)
        localDataSource.saveReminder(reminder2)
        // When completed tasks are cleared in the repository
//        localDataSource.deleteAllReminders()

        // Then the completed tasks cannot be retrieved and the active one can
        Assert.assertThat(localDataSource.getReminder("Id1").succeeded, `is`(true))
        Assert.assertThat(localDataSource.getReminder("Id2").succeeded, `is`(true))

    }

    @Test
    fun getReminder_ReminderNotFound() = runBlockingTest {
        // Given 2 new tasks in the persistent repository
        val reminder1 = ReminderDTO("title", "description",
            "Location",0.0,0.0,"Id1")

        localDataSource.saveReminder(reminder1)
        // Then the tasks can be retrieved from the persistent repository
        val results = localDataSource.getReminder("Id") as Result<ReminderDTO>
        val reminderNotFound = results as Result.Error
        Assert.assertThat(reminderNotFound.message, `is`("Reminder not found!"))
    }

    @Test
    fun getReminders_retrieveSavedReminders() = runBlockingTest {
        // Given 2 new tasks in the persistent repository
        val reminder1 = ReminderDTO("title", "description",
            "Location",0.0,0.0)
        val reminder2 = ReminderDTO("title", "description",
            "Location",0.0,0.0)

        localDataSource.saveReminder(reminder1)
        localDataSource.saveReminder(reminder2)
        // Then the tasks can be retrieved from the persistent repository
        val results = localDataSource.getReminders() as Result.Success<List<ReminderDTO>>
        val reminders = results.data
        Assert.assertThat(reminders.size, `is`(2))
    }

}