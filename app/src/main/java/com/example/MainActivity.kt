package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.QuitSmokingRepository
import com.example.ui.QuitSmokingDashboard
import com.example.ui.QuitSmokingViewModel
import com.example.ui.QuitSmokingViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val repository by lazy { QuitSmokingRepository(database.quitSmokingDao()) }
    private val viewModel: QuitSmokingViewModel by viewModels {
        QuitSmokingViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    QuitSmokingDashboard(viewModel = viewModel)
                }
            }
        }
    }
}
