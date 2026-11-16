package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.ArduLinkApp
import com.example.ui.ArduLinkViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ArduLinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArduLinkApp(viewModel = viewModel)
        }
    }
}
