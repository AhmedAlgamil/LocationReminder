package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt
private lateinit var database: RemindersDatabase

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertreminderAndGetById() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder = ReminderDTO("title", "description",
        "Location",0.0,0.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminderAndGetTasks() = runBlockingTest {
        // GIVEN - insert a task
        val reminder = ReminderDTO("title", "description",
            "Location",0.0,0.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get tasks from the database
        val tasks = database.reminderDao().getReminders()

        // THEN - There is only 1 task in the database, and contains the expected values
        assertThat(tasks.size, `is`(1))
        assertThat(tasks[0].id, `is`(reminder.id))
        assertThat(tasks[0].title, `is`(reminder.title))
        assertThat(tasks[0].description, `is`(reminder.description))
        assertThat(tasks[0].location, `is`(reminder.location))
        assertThat(tasks[0].latitude, `is`(reminder.latitude))
        assertThat(tasks[0].longitude, `is`(reminder.longitude))
    }

}