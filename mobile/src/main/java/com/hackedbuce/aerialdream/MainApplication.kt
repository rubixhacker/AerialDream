package com.hackedbuce.aerialdream

import android.app.Application
import com.hackedbuce.aerialdream.repository.VideosRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainApplication : Application() {
    lateinit var aerialService: AerialService
    lateinit var videosRepository: VideosRepository

    override fun onCreate() {
        super.onCreate()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://a1.phobos.apple.com/us/r1000/000/Features/atv/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        aerialService = retrofit.create(AerialService::class.java)
        videosRepository = VideosRepository(aerialService)
    }
}
