package com.hackedbuce.aerialdream.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hackedbuce.aerialdream.repository.VideosRepository

class ViewModelFactory(private val videosRepository: VideosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(videosRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
