package com.example.chafund

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.chafund.navigation.AppNavHost
import com.example.chafund.ui.theme.ChaFundTheme
import com.example.chafund.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // ThemeMode driven by LocalStorage in CHF-47
            ChaFundTheme(themeMode = ThemeMode.SYSTEM) {
                AppNavHost()
            }
        }
    }
}
