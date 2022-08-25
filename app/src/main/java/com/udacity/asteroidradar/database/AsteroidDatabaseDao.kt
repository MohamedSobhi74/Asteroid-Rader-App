package com.udacity.asteroidradar.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.udacity.asteroidradar.Asteroid
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface AsteroidDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: AsteroidTable)

    @Query("SELECT * FROM asteroid_table WHERE closeApproachDate <= :startDate AND closeApproachDate >= :endDate ORDER BY closeApproachDate ASC")
    fun getAsteroidsByDate(startDate: String, endDate: String): LiveData<List<Asteroid>>

    @Query("SELECT * FROM asteroid_table ORDER BY closeApproachDate ASC")
    fun getAllAsteroids(): LiveData<List<Asteroid>>

    @Query("DELETE FROM asteroid_table WHERE closeApproachDate < :today")
    fun deletePreviousDayAsteroids(today: String): Int

}

