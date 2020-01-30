package dev.joshhalvorson.budgettracker.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Budget(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "budget") val budget: Float,
    @ColumnInfo(name = "spent") val spent: Float,
    @ColumnInfo(name = "balance") val balance: Float,
    @ColumnInfo(name = "bills") val bills: Float,
    @ColumnInfo(name = "social") val social: Float,
    @ColumnInfo(name = "transportation") val transportation: Float,
    @ColumnInfo(name = "food") val food: Float,
    @ColumnInfo(name = "insurance") val insurance: Float,
    @ColumnInfo(name = "entertainment") val entertainment: Float,
    @ColumnInfo(name = "other") val other: Float
)