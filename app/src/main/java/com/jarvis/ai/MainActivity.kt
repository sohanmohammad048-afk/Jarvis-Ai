package com.jarvis.ai

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import com.jarvis.ai.ui.JarvisScreen
import com.jarvis.ai.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {
    private val vm: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
                LaunchedEffect(Unit) { askMic.launch(Manifest.permission.RECORD_AUDIO) }
                JarvisScreen(vm)
            }
        }
    }
}
