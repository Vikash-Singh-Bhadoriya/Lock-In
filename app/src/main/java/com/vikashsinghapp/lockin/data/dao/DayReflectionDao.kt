package com.vikashsinghapp.lockin.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vikashsinghapp.lockin.data.entity.DayReflection
import kotlinx.coroutines.flow.Flow

@Dao
interface DayReflectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reflection: DayReflection)

    @Query("SELECT * FROM day_reflections WHERE dayStartMillis = :dayStart")
    suspend fun getForDay(dayStart: Long): DayReflection?

    @Query("SELECT * FROM day_reflections ORDER BY dayStartMillis DESC")
    fun getAll(): Flow<List<DayReflection>>
}
