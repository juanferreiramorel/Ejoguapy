package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.screens.LoginScreem
import com.example.myapplication.screens.MainDrawerScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppPrincipal()
                }

        }
    }
@Composable
fun AppPrincipal(){
    var sesionIniciada by remember { mutableStateOf(false) }
    var usuarioActual by remember { mutableStateOf("") }
    if (!sesionIniciada){
        Scaffold(modifier = Modifier.fillMaxSize()) { innerpadding ->
            LoginScreem(
                modifier = Modifier.padding(innerpadding),
                onLoginSuccess = {usuario ->
                    usuarioActual = usuario
                    sesionIniciada = true
                }
            )

        }
    }
    else{
        MainDrawerScreen(
            usuario = usuarioActual,
            onCerrarSesion = {
                sesionIniciada = false
                usuarioActual = ""
            }
        )
    }
}
@Preview(showBackground = true)
@Composable
fun LoginScreemPreviw() {
    MyApplicationTheme {
        LoginScreem()
    }
}