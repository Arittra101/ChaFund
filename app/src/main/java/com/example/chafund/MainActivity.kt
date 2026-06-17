package com.example.chafund

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.chafund.ui.theme.ChaFundTheme
import com.example.chafund.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // ThemeMode will be driven by LocalStorage (CHF-18 / CHF-47)
            ChaFundTheme(themeMode = ThemeMode.SYSTEM) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "Cha Fund")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
private fun PreviewLight() {
    ChaFundTheme(themeMode = ThemeMode.LIGHT) {
        Surface { Text("Cha Fund") }
    }
}

@Preview(showBackground = true, name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDark() {
    ChaFundTheme(themeMode = ThemeMode.DARK) {
        Surface { Text("Cha Fund") }
    }
}
