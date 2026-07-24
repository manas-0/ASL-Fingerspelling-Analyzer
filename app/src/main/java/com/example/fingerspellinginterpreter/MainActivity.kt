package com.example.fingerspellinginterpreter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.ui.Modifier

import com.example.fingerspellinginterpreter.ui.theme.FingerSpellingInterpreterTheme


import android.Manifest
import android.content.pm.PackageManager

import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.runtime.*
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    private var hasCameraPermission by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hasCameraPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        setContent {
            FingerSpellingInterpreterTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (hasCameraPermission) {
                        CameraPreviewScreen(modifier = Modifier.fillMaxSize().padding(innerPadding))
                    } else {
                        Text(
                            "Camera permission is required to use this app.",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}