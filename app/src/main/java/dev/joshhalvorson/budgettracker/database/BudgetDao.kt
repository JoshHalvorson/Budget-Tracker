package dev.joshhalvorson.budgettracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.joshhalvorson.budgettracker.model.Budget

@Dao
interface BudgetDao {
    @Insert
    fun insert(budget: Budget)

    @Update
    fun update(budget: Budget)

    @Query("SELECT * FROM budget")
    fun getBudget(): List<Budget>

    @Query("SELECT budget FROM budget WHERE id=1")
    fun getTotalBudget(): Float

    @Query("SELECT spent FROM budget WHERE id=1")
    fun getTotalSpent(): Float

    @Query("SELECT balance FROM budget WHERE id=1")
    fun getBalance(): Float

    @Query("SELECT bills FROM budget WHERE id=1")
    fun getBills(): Float

    @Query("SELECT social FROM budget WHERE id=1")
    fun getSocial(): Float

    @Query("SELECT transportation FROM budget WHERE id=1")
    fun getTransportation(): Float

    @Query("SELECT food FROM budget WHERE id=1")
    fun getFood(): Float

    @Query("SELECT insurance FROM budget WHERE id=1")
    fun getInsurance(): Float

    @Query("SELECT entertainment FROM budget WHERE id=1")
    fun getEntertainment(): Float

    @Query("SELECT other FROM budget WHERE id=1")
    fun getOther(): Float

}