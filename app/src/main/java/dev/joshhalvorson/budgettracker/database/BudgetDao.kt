package dev.joshhalvorson.budgettracker.database

import android.database.Cursor
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

    @Query("SELECT budget FROM budget WHERE id=:id")
    fun getTotalBudget(id: String): Float

    @Query("SELECT spent FROM budget WHERE id=:id")
    fun getTotalSpent(id: String): Float

    @Query("SELECT balance FROM budget WHERE id=:id")
    fun getBalance(id: String): Float

    @Query("SELECT bills FROM budget WHERE id=:id")
    fun getBills(id: String): Float

    @Query("SELECT social FROM budget WHERE id=:id")
    fun getSocial(id: String): Float

    @Query("SELECT transportation FROM budget WHERE id=:id")
    fun getTransportation(id: String): Float

    @Query("SELECT food FROM budget WHERE id=:id")
    fun getFood(id: String): Float

    @Query("SELECT insurance FROM budget WHERE id=:id")
    fun getInsurance(id: String): Float

    @Query("SELECT entertainment FROM budget WHERE id=:id")
    fun getEntertainment(id: String): Float

    @Query("SELECT other FROM budget WHERE id=:id")
    fun getOther(id: String): Float

    @Query("SELECT * FROM Budget WHERE id=:id")
    fun getAllCursor(id: String): Cursor

}