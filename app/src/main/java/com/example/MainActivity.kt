package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainDashboard
import com.example.ui.theme.DopaTapTheme
import com.example.ui.viewmodel.DopaViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: DopaViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themePref by viewModel.themePreference.collectAsState()
      val isDark = when (themePref) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
      }

      DopaTapTheme(darkTheme = isDark) {
        Surface(
          modifier = Modifier.fillMaxSize()
        ) {
          MainDashboard(viewModel)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.updateServiceStatus()
  }
}

