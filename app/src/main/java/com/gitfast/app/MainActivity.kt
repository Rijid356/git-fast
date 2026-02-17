package com.gitfast.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.gitfast.app.navigation.GitFastNavGraph
import com.gitfast.app.ui.theme.GitFastTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GitFastTheme {
                val navController = rememberNavController()
                GitFastNavGraph(navController = navController)
            }
        }
    }
}
