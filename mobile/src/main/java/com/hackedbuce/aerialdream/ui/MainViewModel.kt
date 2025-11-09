package com.hackedbuce.aerialdream.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackedbuce.aerialdream.data.Asset
import com.hackedbuce.aerialdream.data.Result
import com.hackedbuce.aerialdream.repository.VideosRepository
import kotlinx.coroutines.launch

class MainViewModel(private val videosRepository: VideosRepository) : ViewModel() {

    private val _video = MutableLiveData<Asset>()
    val video: LiveData<Asset> = _video

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadVideo() {
        viewModelScope.launch {
            when (val result = videosRepository.getVideos()) {
                is Result.Success -> {
                    val randomVideo = result.data.random()
                    val randomAsset = randomVideo.assets.random()
                    _video.value = randomAsset
                }
                is Result.Error -> {
                    _error.value = result.exception.message
                }
            }
        }
    }
}
