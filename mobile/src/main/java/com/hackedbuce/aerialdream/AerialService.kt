package com.hackedbuce.aerialdream

import com.hackedbuce.aerialdream.data.Video
import retrofit2.http.GET
import retrofit2.http.Path

interface AerialService {
    @GET("{season}/videos/entries.json")
    suspend fun getVideos(@Path("season") season: String): List<Video>
}
