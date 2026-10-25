package com.xandron.novapool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.xandron.novapool.data.ProgressionRepository
import com.xandron.novapool.data.dataStore
import com.xandron.novapool.game.TournamentManager
import com.xandron.novapool.ui.NovaPoolNavHost
import com.xandron.novapool.ui.theme.NovaPoolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = remember { ProgressionRepository(dataStore = applicationContext.dataStore) }
            val tournamentManager = remember { TournamentManager() }

            NovaPoolTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NovaPoolNavHost(
                        repository = repository,
                        tournamentManager = tournamentManager
                    )
                }
            }
        }
    }
}
