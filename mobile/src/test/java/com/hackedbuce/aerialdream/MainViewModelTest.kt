package com.hackedbuce.aerialdream

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hackedbuce.aerialdream.data.Asset
import com.hackedbuce.aerialdream.data.Result
import com.hackedbuce.aerialdream.data.Video
import com.hackedbuce.aerialdream.repository.VideosRepository
import com.hackedbuce.aerialdream.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var videosRepository: VideosRepository

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel(videosRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadVideo should post video asset on success`() = runTest {
        // Given
        val asset = Asset("url", "label", "id", "day")
        val video = Video("id", listOf(asset))
        val videoList = listOf(video)
        val successResult = Result.Success(videoList)

        whenever(videosRepository.getVideos()).thenReturn(successResult)

        // When
        viewModel.loadVideo()
        advanceUntilIdle()

        // Then
        assertEquals(asset, viewModel.video.value)
    }

    @Test
    fun `loadVideo should post error message on failure`() = runTest {
        // Given
        val errorMessage = "Error fetching videos"
        val errorResult = Result.Error(Exception(errorMessage))

        whenever(videosRepository.getVideos()).thenReturn(errorResult)

        // When
        viewModel.loadVideo()
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.error.value)
    }
}
