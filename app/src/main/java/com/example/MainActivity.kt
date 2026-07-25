package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.FahheemViewModel
import com.example.ui.navigation.FahheemNavGraph
import com.example.ui.theme.FahheemTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FahheemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userState by viewModel.userState.collectAsState()
            val commitmentPercentage = userState?.commitmentPercentage ?: 85

            FahheemTheme(commitmentPercentage = commitmentPercentage) {
                FahheemNavGraph(viewModel = viewModel)
            }
        }
    }
}

