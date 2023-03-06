package com.udacity.project4.locationreminders.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

/**
 * Main entry point for accessing reminders data.
 */
@Dao
interface ReminderDataSource {

    @Query("SELECT * FROM reminders")
    suspend fun getReminders(): Result<List<ReminderDTO>>

    @Insert
    suspend fun saveReminder(reminder: ReminderDTO)

    @Query("SELECT * FROM reminders WHERE entry_id like :id")
    suspend fun getReminder(id: String): Result<ReminderDTO>

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

}
