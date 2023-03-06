/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.project4

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.runBlocking

/**
 * A Service Locator for the [TasksRepository]. This is the mock version, with a
 * [FakeTasksRemoteDataSource].
 */
object ServiceLocator {

    private val lock = Any()
    private var database: RemindersDatabase? = null
    @Volatile
    var reminderDataSource: ReminderDataSource? = null
        @VisibleForTesting set

    fun provideRemindersRepository(context: Context): ReminderDataSource {
        synchronized(this) {
            return reminderDataSource ?: reminderDataSource ?: createReminderLocalDataSource(context)
        }
    }

    private fun createReminderLocalDataSource(context: Context): ReminderDataSource {
        val database = database ?: createDataBase(context)
        return RemindersLocalRepository(database.reminderDao())
    }

    private fun createDataBase(context: Context): RemindersDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "Tasks.db"
        ).build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                reminderDataSource?.deleteAllReminders()
            }
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            reminderDataSource = null
        }
    }
}
