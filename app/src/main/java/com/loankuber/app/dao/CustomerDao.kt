package com.loankuber.app.dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "customer")
data class Customer(
    @PrimaryKey val loanNumber: String,
    val name: String,
    val searchText: String
)

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<Customer>)

    @Query("SELECT * FROM customer")
    suspend fun getAllCustomers(): List<Customer>

    @Query("SELECT searchText FROM customer WHERE searchText LIKE '%' || :query || '%' LIMIT 10")
    suspend fun searchCustomers(query: String): List<String>

    @Query("SELECT loanNumber FROM customer ORDER BY loanNumber DESC LIMIT 1")
    suspend fun getLargestLoanNumber(): String?

    @Query("SELECT searchText FROM customer ORDER BY loanNumber ASC LIMIT :limit")
    suspend fun searchCustomers(limit: Int): List<String>
}