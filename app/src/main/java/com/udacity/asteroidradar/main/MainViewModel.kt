package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.getSeventhDay
import com.udacity.asteroidradar.api.getToday
import com.udacity.asteroidradar.database.AsteroidDB
import com.udacity.asteroidradar.repos.AsteroidRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AsteroidDB.getInstance(application)
     val asteroidRepository = AsteroidRepo(database)

    private val _navigateToDetailFragment = MutableLiveData<Asteroid>()
    val navigateToDetailFragment: LiveData<Asteroid>
        get() = _navigateToDetailFragment

    val asteroids = asteroidRepository.asteroids


    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    private val _isloading = MutableLiveData<Boolean>()
    val isloading: LiveData<Boolean>
        get() = _isloading

    init {
        viewModelScope.launch {
            try {

                asteroidRepository.refreshAsteroids()
                getPictureOfTheDay()

            } catch (e: Exception) {
                println("Exception refreshing data: $e.message")
                _isloading.value = false
            }
        }
    }

    // val asteroids = asteroidRepository.asteroids

    fun onAsteroidClicked(asteroid: Asteroid) {
        _navigateToDetailFragment.value = asteroid
    }

    fun doneNavigating() {
        _navigateToDetailFragment.value = null
    }

    fun doneDisplayingSnackbar() {
        _isloading.value = false
    }

    fun updateFilter(filter: AsteroidRepo.AsteroidFilter) {
        asteroidRepository.applyFilter(filter)
    }


    suspend fun getPictureOfTheDay(){
        _isloading.value = true
        withContext(Dispatchers.IO) {
            val pictureOfDay = AsteroidApi.retrofitService.getPictureOfDay(
                Constants.API_KEY
            )
                .await()

            _isloading.value = false

            _pictureOfDay.value = pictureOfDay

        }
    }

}
