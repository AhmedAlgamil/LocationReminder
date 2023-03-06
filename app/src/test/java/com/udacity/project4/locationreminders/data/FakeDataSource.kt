package com.udacity.project4.locationreminders.data

import androidx.annotation.VisibleForTesting
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.util.LinkedHashMap

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource() : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source
    var reminders: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test Error")
        }
        return Result.Success(reminders.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        reminders[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Could not find task")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    @VisibleForTesting
    fun addReminders(vararg remindersList: ReminderDTO) {
        for (reminder in remindersList) {
            reminders[reminder.id] = reminder
        }
    }

}