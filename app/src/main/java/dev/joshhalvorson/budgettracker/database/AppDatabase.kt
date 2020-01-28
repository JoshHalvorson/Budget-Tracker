package dev.joshhalvorson.budgettracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.joshhalvorson.budgettracker.model.Budget

@Database(entities = arrayOf(Budget::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
}