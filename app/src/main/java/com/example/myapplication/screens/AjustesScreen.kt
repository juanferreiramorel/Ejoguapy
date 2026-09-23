package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.PreferenciasManager

@Composable
fun AjustesScreen(
    modifier: Modifier= Modifier,
    modoOscuro: Boolean = false,
    onCambiarModoOscuro: (Boolean) -> Unit = {}
){
    val context = LocalContext.current
    val prefs = remember { PreferenciasManager(context) }
    //el estado del tema vive en MainActivity, aca solo se muestra y se cambia
    val usuarioActual = remember{prefs.obtenerUsuarioLogueado()}

    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text= "Configuracion del Sistema",
            style = MaterialTheme.typography.titleMedium
        )
        Card(modifier=Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier=Modifier.padding(16.dp)){
                Text(
                    text="Preferencias del usuario",
                    style= MaterialTheme.typography.titleMedium
                )

                Spacer(modifier=Modifier.height(8.dp))
                Text(
                    text= "Usuario recordado en SharedPreferences: ${if(usuarioActual.isNotBlank())usuarioActual else "Ninguno"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Row(
            modifier=Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tema Oscuro", style= MaterialTheme.typography.titleMedium)
                Text("Ajusta la apariencia visual de la app", style= MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = modoOscuro,
                onCheckedChange = {
                    prefs.guardarModoOscuro(it)
                    onCambiarModoOscuro(it)
                    Toast.makeText(context, "Preferencia guardada", Toast.LENGTH_SHORT).show()
                }
            )
        }
        HorizontalDivider()
        Button(
            onClick = {
                prefs.limpiarSesion()
                Toast.makeText(context, "Se borraron las credenciales locales", Toast.LENGTH_SHORT).show()
            },
            colors= ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ){
            Text("Limpiar Preferencias de Sesion")
        }
    }

}
