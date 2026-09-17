package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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


@Composable
fun LoginScreem(modifier: Modifier = Modifier,
                onLoginSuccess: (String)->Unit = {}
){
    val context = LocalContext.current
    //declaracion de de los estados mutables para los estados del formulario
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val esFormularioValido = usuario.isNotBlank() && password.length>= 4

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
            value = password,
            onValueChange = {password = it},
            label = "ingrese su clave",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password
        )
        Spacer(modifier = Modifier.height(28.dp))

        CustomPrimaryButton(
            text = "iniciar sesion",
            enabled = esFormularioValido,
            onClick = {
                if (usuario.trim()=="admin" && password =="1234"){
                    Toast.makeText(context, "Acceso Concedido", Toast.LENGTH_SHORT).show()
                    onLoginSuccess(usuario)

                }
                else{
                    Toast.makeText(context, "Acceso Incorreto", Toast.LENGTH_LONG).show()
                }
            }
        )

    }
}