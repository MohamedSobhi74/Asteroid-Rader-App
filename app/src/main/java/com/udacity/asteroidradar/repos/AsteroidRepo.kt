package com.udacity.asteroidradar.repos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi.retrofitService
import com.udacity.asteroidradar.api.getSeventhDay
import com.udacity.asteroidradar.api.getToday
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDB
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject

class AsteroidRepo(val database: AsteroidDB) {

    enum class AsteroidFilter(val value: String) { SAVED("saved"), TODAY("today"), WEEK("week") }

    private val _asteroidType: MutableLiveData<AsteroidFilter> =
        MutableLiveData(AsteroidFilter.WEEK)
    private val asteroidType: LiveData<AsteroidFilter>
        get() = _asteroidType


    val asteroids: LiveData<List<Asteroid>> =
        Transformations.switchMap(asteroidType) { type ->
            when (type) {
                AsteroidFilter.SAVED -> database.sleepDatabaseDao.getAllAsteroids()
                AsteroidFilter.TODAY -> database.sleepDatabaseDao.getAsteroidsByDate(
                    getToday(),
                    getToday()
                )
                AsteroidFilter.WEEK -> database.sleepDatabaseDao.getAsteroidsByDate(
                    getSeventhDay(),
                    getToday()
                )
                else -> throw IllegalArgumentException(" Invalid type !")
            }
        }

    suspend fun refreshAsteroids(
        startDate: String = "",
        endDate: String = ""
    ) {
        var asteroidList: ArrayList<Asteroid>
        withContext(Dispatchers.IO) {
            val asteroidResponseBody: ResponseBody = retrofitService.getAsteroids(
                startDate, endDate,
                Constants.API_KEY
            )
                .await()
            asteroidList = parseAsteroidsJsonResult(JSONObject(asteroidResponseBody.string()))
            database.sleepDatabaseDao.insertAll(*asteroidList.asDomainModel())
        }
    }

    suspend fun deletePreviousDayAsteroids() {
        withContext(Dispatchers.IO) {
            database.sleepDatabaseDao.deletePreviousDayAsteroids(getToday())
        }
    }

    fun applyFilter(filter: AsteroidFilter) {

        _asteroidType.value = filter
    }

}