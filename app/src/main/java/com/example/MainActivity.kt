package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.screens.ApexSignageRootScreen
import com.example.ui.theme.ApexSignageTheme
import com.example.viewmodel.SignageViewModel

class MainActivity : ComponentActivity() {

    private val signageViewModel: SignageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sustained 1080p TV kiosk operation: keep screen awake during digital signage loops
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            ApexSignageTheme {
                ApexSignageRootScreen(
                    viewModel = signageViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    /**
     * Hardware Remote Control (D-Pad) key dispatcher for Android TV / Kiosk remotes.
     * Supports directional traversal (UP/DOWN/LEFT/RIGHT), primary selection (DPAD_CENTER/ENTER),
     * Kiosk Options override (MENU), and step-out navigation (BACK).
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    signageViewModel.onDpadLeft()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    signageViewModel.onDpadRight()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    signageViewModel.onDpadUp()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    signageViewModel.onDpadDown()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                    signageViewModel.onDpadOk()
                    return true
                }
                KeyEvent.KEYCODE_MENU,
                KeyEvent.KEYCODE_SETTINGS,
                KeyEvent.KEYCODE_M -> {
                    signageViewModel.onMenuKey()
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    signageViewModel.playerManager.stepToNextTrack()
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    signageViewModel.playerManager.stepToPreviousTrack()
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    signageViewModel.playerManager.togglePlayPause()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
