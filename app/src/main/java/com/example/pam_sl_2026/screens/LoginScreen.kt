package com.example.pam_sl_2026.screens

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
import com.example.pam_sl_2026.components.CustomPrimaryButton
import com.example.pam_sl_2026.components.CustomTextField

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String) -> Unit = {}
){
    val context = LocalContext.current

    //Declarar los estados mutables para los campos del formulario

    var usuario by remember { mutableStateOf(value = "") }
    var password by remember { mutableStateOf(value = "") }

    val esFormularioValido = usuario.isNotBlank() && password.length>= 4
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //Encabezado
        Text(
            text= "Aplicación de Compras",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "Ingrese sus credenciales de usuario",
            style = MaterialTheme.typography.bodyMedium,
            color= MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top=4.dp, bottom = 32.dp)
        )

        //Uso del control personalizado para el usuario

        CustomTextField(
            value = usuario,
            onValueChange = {usuario = it},
            label = "Usuario o Correo",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))


        CustomTextField(
            value = password,
            onValueChange = {password = it},
            label = "Contraseña",
            leadingIcon = Icons.Default.Lock,
            isPassword= true,
            keyboardType = KeyboardType.Password
        )

        Spacer(modifier = Modifier.height(28.dp))




        CustomPrimaryButton(
            text= "Iniciar Sesion",
            enabled = esFormularioValido,
            onClick = {
                if (usuario.trim() == "admin" && password == "1234") {
                    Toast.makeText(context, "Acceso Concedido", Toast.LENGTH_SHORT).show()
                    onLoginSuccess(usuario)
                }else {
                    Toast.makeText(context,  "Acceso Denegado", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }


}