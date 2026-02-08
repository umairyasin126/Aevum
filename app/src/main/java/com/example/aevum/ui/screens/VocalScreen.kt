package com.example.aevum.ui.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aevum.ui.theme.ElectricViolet
import com.example.aevum.ui.theme.NeonCyan
import com.example.aevum.viewmodel.VocalViewModel
import java.util.concurrent.TimeUnit

@Composable
fun VocalScreen() {
    val context = LocalContext.current
    val viewModel: VocalViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VocalViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val timeMillis by viewModel.timeMillis.collectAsState()
    val isListening by viewModel.isListening.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatTime(timeMillis),
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isListening) {
             Text(
                text = "Listening...",
                color = ElectricViolet,
                fontSize = 18.sp
            )
        } else {
            Button(
                onClick = { 
                     if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.startListening() 
                     } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                     }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("Tap to Listen")
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Commands: \"Start\", \"Stop\", \"Reset\"",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    val hundreds = (millis % 1000) / 10
    return String.format("%02d:%02d.%02d", minutes, seconds, hundreds)
}
