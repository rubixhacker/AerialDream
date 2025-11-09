package com.hackedbuce.aerialdream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.service.dreams.DreamService
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.hackedbuce.aerialdream.data.Asset
import com.hackedbuce.aerialdream.databinding.DreamAerialBinding
import com.hackedbuce.aerialdream.ui.MainViewModel
import com.hackedbuce.aerialdream.ui.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AerialDream : DreamService(), ViewModelStoreOwner {

    override val viewModelStore = ViewModelStore()

    private val lifecycleOwner = DreamLifecycleOwner()
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: DreamAerialBinding
    private val dateFormat = SimpleDateFormat("h:mm", Locale.getDefault())

    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateTime()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = true
        isFullscreen = true
        binding = DreamAerialBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        val application = application as MainApplication
        val viewModelFactory = ViewModelFactory(application.videosRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        observeViewModel()
        lifecycleOwner.handleCreate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleOwner.handleDestroy()
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        registerReceiver(timeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        viewModel.loadVideo()
        lifecycleOwner.handleStart()
        lifecycleOwner.handleResume()
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        unregisterReceiver(timeTickReceiver)
        lifecycleOwner.handlePause()
        lifecycleOwner.handleStop()
    }

    private fun observeViewModel() {
        viewModel.video.observe(lifecycleOwner) { video ->
            playVideo(video)
        }
        viewModel.error.observe(lifecycleOwner) { error ->
            showError(error)
        }
    }

    private fun playVideo(asset: Asset) {
        binding.videoView.player?.release()
        val player = androidx.media3.exoplayer.ExoPlayer.Builder(this).build()
        binding.videoView.player = player
        val mediaItem = androidx.media3.common.MediaItem.fromUri(Uri.parse(asset.url))
        player.setMediaItem(mediaItem)
        player.repeatMode = androidx.media3.exoplayer.ExoPlayer.REPEAT_MODE_ONE
        player.playWhenReady = true
        player.prepare()
        binding.location.text = asset.accessibilityLabel
    }

    private fun showError(error: String) {
        binding.location.text = error
    }

    private fun updateTime() {
        binding.time.text = dateFormat.format(Date())
    }
}
