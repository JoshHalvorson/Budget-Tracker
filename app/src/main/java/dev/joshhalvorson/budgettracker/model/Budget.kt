package dev.joshhalvorson.budgettracker.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Budget(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "budget") var budget: Float,
    @ColumnInfo(name = "spent") var spent: Float,
    @ColumnInfo(name = "balance") var balance: Float,
    @ColumnInfo(name = "bills") var bills: Float,
    @ColumnInfo(name = "social") var social: Float,
    @ColumnInfo(name = "transportation") var transportation: Float,
    @ColumnInfo(name = "food") var food: Float,
    @ColumnInfo(name = "insurance") var insurance: Float,
    @ColumnInfo(name = "entertainment") var entertainment: Float,
    @ColumnInfo(name = "other") var other: Float
)