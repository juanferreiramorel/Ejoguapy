package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.components.CustomPrimaryButton
import com.example.myapplication.components.CustomTextField
import com.example.myapplication.data.PreferenciasManager


@Composable
fun LoginScreen(modifier: Modifier = Modifier,
                onLoginSuccess: (String)->Unit = {}
){
    val context = LocalContext.current
    //declaracion de de los estados mutables para los estados del formulario
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    //validacion en tiempo real
    val esFormularioValido =usuario.isNotBlank() && password.length>=4
    var recordarSesion by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //encabezado
        Text(
            text = "Aplicacion de Compras",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "ingrese sus credenciales de usuario",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        //uso del control personalizado del usuario
        CustomTextField(
            value = usuario,
            onValueChange = {usuario = it},
            label = "usuario o correo",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value=password,
            onValueChange = {password=it},
            label = "Contraseña",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password
        )
        Row(
            modifier=Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = recordarSesion,
                onCheckedChange = {recordarSesion=it}
            )
            Text(text="Recordar mis credenciales", style= MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(28.dp))

        CustomPrimaryButton(
            text= "Iniciar Sesion",
            enabled= esFormularioValido,
            onClick = {
                if (usuario.trim()== "admin" && password== "1234"){
                    val prefs = PreferenciasManager(context)
                    prefs.guardarSesion(usuario=usuario, recordar = recordarSesion)
                    Toast.makeText(context, "Acceso correcto", Toast.LENGTH_SHORT).show()
                    onLoginSuccess(usuario)
                }else{
                    Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

}
