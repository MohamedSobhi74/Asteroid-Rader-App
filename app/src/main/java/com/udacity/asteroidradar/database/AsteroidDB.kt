package com.udacity.asteroidradar.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.udacity.asteroidradar.Asteroid

@Database(entities = [AsteroidTable::class], version = 1, exportSchema = false)
abstract class AsteroidDB : RoomDatabase() {

    abstract val sleepDatabaseDao: AsteroidDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: AsteroidDB? = null

        fun getInstance(context: Context): AsteroidDB {

            synchronized(this) {

                var instance = INSTANCE
                // If instance is `null` make a new database instance.
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AsteroidDB::class.java,
                        "Asteroid_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
