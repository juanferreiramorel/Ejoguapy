package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.PreferenciasManager
import com.example.myapplication.screens.LoginScreen
import com.example.myapplication.screens.MainDrawerScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferenciasManager(this)
        setContent {
            //estado global del tema, se inicia con la preferencia guardada
            var modoOscuro by remember { mutableStateOf(prefs.obtenerModoOscuro()) }
            //los iconos de la barra de estado siguen el tema de la app (no el del sistema)
            DisposableEffect(modoOscuro) {
                val estiloBarras = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { modoOscuro }
                enableEdgeToEdge(statusBarStyle = estiloBarras, navigationBarStyle = estiloBarras)
                onDispose { }
            }
            MyApplicationTheme(darkTheme = modoOscuro) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppPrincipal(
                        modoOscuro = modoOscuro,
                        onCambiarModoOscuro = { modoOscuro = it }
                    )
                }
            }
        }
    }
}
//Enrutador raiz del sistema
@Composable
fun AppPrincipal(
    modoOscuro: Boolean = false,
    onCambiarModoOscuro: (Boolean) -> Unit = {}
){
    val context= LocalContext.current
    val prefs = remember{ PreferenciasManager(context) }
    //estado que determinara si el usuario inició sesión
    var sesionIniciada by remember { mutableStateOf(prefs.estaSesionGuardada()) }
    var usuarioActual by remember { mutableStateOf(prefs.obtenerUsuarioLogueado()) }
    if(!sesionIniciada){
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            LoginScreen(
                modifier = Modifier.padding(innerPadding),
                onLoginSuccess = {usuario ->
                    usuarioActual=usuario
                    sesionIniciada= true
                }
            )
        }
    } else{
        MainDrawerScreen(
            usuario = usuarioActual,
            modoOscuro = modoOscuro,
            onCambiarModoOscuro = onCambiarModoOscuro,
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
        LoginScreen()
    }
}
