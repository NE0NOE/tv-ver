package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.KioskScreenMode
import com.example.viewmodel.SignageViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Apex Signage", appName)
    }

    @Test
    fun `dpad navigation and modal default focus behavior`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = SignageViewModel(app)

        // Verify initial state starts on lane 0, card 0
        assertEquals(0, viewModel.uiState.value.focusedLaneIndex)
        assertEquals(0, viewModel.uiState.value.focusedCardIndex)

        // Move focus right along carousel lane
        viewModel.onDpadRight()
        assertEquals(1, viewModel.uiState.value.focusedCardIndex)

        // Open Kiosk Options modal via MENU key and verify focus defaults to non-destructive index 0
        viewModel.onMenuKey()
        assertTrue(viewModel.uiState.value.isModalOpen)
        assertEquals(0, viewModel.uiState.value.focusedModalIndex)

        // Pressing OK on default modal button closes modal safely
        viewModel.onDpadOk()
        assertFalse(viewModel.uiState.value.isModalOpen)

        // Enter Fullscreen Kiosk Video Playback Mode and step out via BACK
        viewModel.enterFullscreenPlayback()
        assertEquals(KioskScreenMode.FULLSCREEN_PLAYBACK, viewModel.uiState.value.screenMode)
        viewModel.onBackKey()
        assertEquals(KioskScreenMode.DASHBOARD_10FT, viewModel.uiState.value.screenMode)
    }
}
