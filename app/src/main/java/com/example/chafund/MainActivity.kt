package com.example.chafund

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.chafund.navigation.AppNavHost
import com.example.chafund.navigation.Navigator
import com.example.chafund.ui.theme.ChaFundTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val navigator: Navigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            // Theme always follows the system setting.
            ChaFundTheme {
                AppNavHost(navigator = navigator)
            }
        }
    }
}
