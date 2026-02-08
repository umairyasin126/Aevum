package com.example.aevum.ui.screens

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aevum.ui.theme.ElectricViolet
import com.example.aevum.ui.theme.NeonCyan
import com.example.aevum.viewmodel.OriginViewModel

@Composable
fun OriginScreen() {
    val context = LocalContext.current
    val viewModel: OriginViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OriginViewModel(context.applicationContext as Application) as T
            }
        }
    )
    
    val days by viewModel.daysCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$days",
            fontSize = 120.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
        )
        Text(
            text = "DAYS",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        Button(
            onClick = {
                viewModel.onReset()
                vibrate(context)
            },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
        ) {
            Text("Reset to Today")
        }
    }
}

private fun vibrate(context: android.content.Context) {
    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(100)
    }
}
