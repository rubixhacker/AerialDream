package com.hackedbuce.aerialdream.repository

import com.hackedbuce.aerialdream.AerialService
import com.hackedbuce.aerialdream.data.Video
import com.hackedbuce.aerialdream.data.Result

class VideosRepository(private val aerialService: AerialService) {

    suspend fun getVideos(): Result<List<Video>> {
        return try {
            val videos = aerialService.getVideos("AutumnResources")
            Result.Success(videos)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
