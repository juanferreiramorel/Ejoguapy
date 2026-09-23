package com.example.myapplication.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

//campo de busqueda para filtrar los listados (con boton X para limpiar)
@Composable
fun CampoBusqueda(
    texto: String,
    onTextoChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
){
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
        trailingIcon = {
            if (texto.isNotEmpty()){
                IconButton(onClick = { onTextoChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpiar busqueda")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

//estado vacio: icono grande, mensaje y boton opcional
@Composable
fun EstadoVacio(
    icono: ImageVector,
    mensaje: String,
    modifier: Modifier = Modifier,
    textoBoton: String? = null,
    onAccion: (() -> Unit)? = null
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (textoBoton != null && onAccion != null){
            Button(onClick = onAccion) {
                Text(textoBoton)
            }
        }
    }
}
