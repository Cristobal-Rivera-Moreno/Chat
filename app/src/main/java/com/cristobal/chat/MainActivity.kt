package com.cristobal.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withResumed
import com.cristobal.chat.components.ChatRoomView
import com.cristobal.chat.storage.repositories.MessageRepository
import com.cristobal.chat.ui.theme.ChatTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == false && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == false) {
                finish()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermissions()
        enableEdgeToEdge()
        setContent{
            ChatTheme {
                Scaffold(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.ime)) { innerPadding ->
                    /*ChatRoomsScreen(modifier = Modifier.padding(innerPadding))*/
                    ChatRoomView(
                        modifier =  Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }

        lifecycleScope.launch (Dispatchers.IO) {
            lifecycle.repeatOnLifecycle( androidx.lifecycle.Lifecycle.State.RESUMED) {
                MessageRepository(
                    applicationContext
                ).deleteExpiredMessages()
            }
        }
    }

    private fun requestLocationPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

fun getRandomDateString(
    startMillis: Long = 1609459200000, // Jan 1, 2021
    endMillis: Long = System.currentTimeMillis()
): String {
    val randomMillis = Random.nextLong(startMillis, endMillis)
    val date = Date(randomMillis)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}
