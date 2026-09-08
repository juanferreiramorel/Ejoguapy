package com.example.pam_sl_2026.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Producto (
    val id: Int,
    val descripcion: String,
    val proveedor: String,
    val precio: Double,
    val activo: Boolean
)




@Composable
fun listadoProductosTab (productos: List<Producto>) {
    if (productos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
            ) {
            Text(text = "No hay productos registrados aun")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(productos){ prod->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ){
                    Column( modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = prod.descripcion,
                                style = MaterialTheme.typography.titleMedium
                                )
                            Badge(
                                containerColor = if (prod.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            ){
                                Text(if (prod.activo)"Activo" else "Inactivo")
                            }
                        }
                        Spacer( modifier = Modifier.height(4.dp))
                        Text(
                            text = "Proveedor: ${prod.proveedor}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Precio: Gs. ${prod.precio.toLong()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary

                        )
                    }
                }
            }
        }
    }
}