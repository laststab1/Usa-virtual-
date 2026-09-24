package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC LIMIT 100")
    fun getAllCalculations(): Flow<List<CalculationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(record: CalculationRecord): Long

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteCalculation(id: Long)

    @Query("DELETE FROM calculations")
    suspend fun clearAll()
}
