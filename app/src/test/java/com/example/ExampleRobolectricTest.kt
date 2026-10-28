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
    fun `pure 4-direction dpad navigation and fullscreen mode`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = SignageViewModel(app)

        // Starts on Row 1 (Hero Stage), Col 0 (Fullscreen Trigger)
        assertEquals(1, viewModel.uiState.value.focusRow)
        assertEquals(0, viewModel.uiState.value.focusCol)

        // Pressing UP moves to Row 0 (Minimalist Top Icon Bar)
        viewModel.onDpadUp()
        assertEquals(0, viewModel.uiState.value.focusRow)

        // Pressing RIGHT moves across all top icons (0..4)
        viewModel.onDpadRight()
        assertEquals(1, viewModel.uiState.value.focusCol)

        // Pressing DOWN twice moves to Row 2 (First Carousel Lane)
        viewModel.onDpadDown()
        viewModel.onDpadDown()
        assertEquals(2, viewModel.uiState.value.focusRow)

        // Enter Fullscreen Mode and navigate purely with UP/DOWN/LEFT/RIGHT
        viewModel.enterFullscreenPlayback()
        assertEquals(KioskScreenMode.FULLSCREEN_PLAYBACK, viewModel.uiState.value.screenMode)
        assertFalse(viewModel.uiState.value.isFullscreenHudVisible)

        // Pressing UP in Fullscreen reveals the minimalist icon HUD
        viewModel.onDpadUp()
        assertTrue(viewModel.uiState.value.isFullscreenHudVisible)

        // Pressing UP again exits Fullscreen back to the Dashboard
        viewModel.onDpadUp()
        assertEquals(KioskScreenMode.DASHBOARD_10FT, viewModel.uiState.value.screenMode)
    }
}
