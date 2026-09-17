package com.example.myapplication.screens


import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext

import com.example.myapplication.components.CustomTextField

data class Producto(
    val id: Int,
    val descripcion: String,
    val proveedor: String,
    val precio: Double,
    val activo: Boolean
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  ProductosScreen(modifier: Modifier= Modifier){
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    val pestana = listOf("listado", "nuevo registro")

    val productosRegistrados= remember { mutableStateListOf(
        Producto(1,"Noteboock HP", "tupi S.A", 400000.0, true)
    )
    }
    // estado para el producto seleccionado a eliminar
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

// dialogo de confirmacion
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Confirmar Eliminacion") },
            text = {
                Text("¿Desea eliminar permanentemente '${producto.descripcion}' del catalogo?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        productosRegistrados.remove(producto)
                        productoAEliminar = null
                        Toast.makeText(
                            context,
                            "Producto eliminado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
    Column(modifier=modifier.fillMaxSize()){
        TabRow(selectedTabIndex=selectedTabIndex){
            pestana.forEachIndexed { index, titulo ->
                Tab(
                    selected = selectedTabIndex==index,
                    onClick = {selectedTabIndex=index},
                    text = {Text(titulo)}

                )
            }
        }
        when (selectedTabIndex){
            0 -> ListadoProductosTab(
                productos = productosRegistrados,
                onOrdenarPorPrecioAsc = {
                    val ordenados = productosRegistrados.sortedBy { it.precio }
                    productosRegistrados.clear()
                    productosRegistrados.addAll(ordenados)
                },
                onOrdenarPorPrecioDesc = {
                    val ordenados = productosRegistrados.sortedByDescending { it.precio }
                    productosRegistrados.clear()
                    productosRegistrados.addAll(ordenados)
                },
                onSolicitarEliminar = { producto ->
                    productoAEliminar = producto
                },
                onEditar = { producto ->
                    Toast.makeText(
                        context,
                        "Edicion pendiente: ${producto.descripcion}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            1 -> FormularioProducto(
                onGuardarProfucto = {nuevoProducto->
                    productosRegistrados.add(nuevoProducto)
                    Toast.makeText(context,"Producto Registrado", Toast.LENGTH_SHORT).show()
                        selectedTabIndex= 0

                }
            )
        }
    }
}

@Composable
fun ListadoProductosTab (
    productos: List<Producto>,
    onOrdenarPorPrecioAsc: () -> Unit,
    onOrdenarPorPrecioDesc: () -> Unit,
    onSolicitarEliminar: (Producto) -> Unit,
    onEditar: (Producto) -> Unit,
){
    var menuOpcionesGeneralExpandido by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // cabecera con menu basico
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Catalogo (${productos.size} items)",
                style = MaterialTheme.typography.titleMedium
            )

            // disparador del menu basico general
            Box {
                IconButton(
                    onClick = { menuOpcionesGeneralExpandido = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones de listado"
                    )
                }

                DropdownMenu(
                    expanded = menuOpcionesGeneralExpandido,
                    onDismissRequest = {
                        menuOpcionesGeneralExpandido = false
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ordenar de Menor a Mayor Precio") },
                        onClick = {
                            onOrdenarPorPrecioAsc()
                            menuOpcionesGeneralExpandido = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Ordenar de Mayor a Menor Precio") },
                        onClick = {
                            onOrdenarPorPrecioDesc()
                            menuOpcionesGeneralExpandido = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (productos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No hay productos reistrados aun")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productos, key={it.id}) { prod ->
                    ProductoItemContextual(
                        producto = prod,
                        onEditar = { onEditar(prod) },
                        onEliminar = { onSolicitarEliminar(prod) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductoItemContextual(
    producto: Producto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
){
    var menuContextualExpandido by remember { mutableStateOf(value = false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { /*Clic normal*/ },
                    onLongClick = {
                        //Despliega el menu contextual al mantener presionado
                        menuContextualExpandido = true
                    }
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = producto.descripcion,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Badge(
                        containerColor = if (producto.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    ) {
                        Text(if (producto.activo) "Activo" else "Inactivo")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "proveedor:${producto.proveedor}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Precio: Gs. ${producto.precio.toLong()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        // menu contextual anclado a la tarjeta
        DropdownMenu(
            expanded = menuContextualExpandido,
            onDismissRequest = { menuContextualExpandido = false }
        ) {
            DropdownMenuItem(
                text = { Text("Modificar Registro") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar"
                    )
                },
                onClick = {
                    menuContextualExpandido = false
                    onEditar()
                }
            )
            DropdownMenuItem(
                text = { Text("Eliminar Registro") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    menuContextualExpandido = false
                    onEliminar()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProducto (onGuardarProfucto:(Producto)->Unit){
    var descripcion by remember {mutableStateOf("")}
    var precioTexto by remember {mutableStateOf("")}
    var estaActivo by remember {mutableStateOf(true)}

    val proveedoresDisponible = listOf("tupi","Electroban","Bristol")
    var proveedorSeleccionado by remember (){mutableStateOf(proveedoresDisponible[0])}
    var menuExtendido by remember{mutableStateOf(false)}
    val formularioValido = descripcion.isNotBlank()&&precioTexto.toDoubleOrNull() !=null
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Registro de nuevo producto",
            style = MaterialTheme.typography.titleMedium
        )
        CustomTextField(
            value =  descripcion,
            onValueChange = {descripcion= it},
            label = "Descripcion de Producto",
            leadingIcon = Icons.Default.Info
        )
        CustomTextField(
            value =  precioTexto,
            onValueChange = {precioTexto= it},
            label = "Precio unitario(Gs.)",
            leadingIcon = Icons.Default.Info
        )
        ExposedDropdownMenuBox(
            expanded = menuExtendido,
            onExpandedChange = {menuExtendido = !menuExtendido}
        ){
            OutlinedTextField(
                value = proveedorSeleccionado,
                onValueChange = {},
                readOnly = true,
                label = {Text("Proveedor")},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExtendido)},
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = menuExtendido,
                onDismissRequest = {menuExtendido=false}
            ){
                proveedoresDisponible.forEach { proveedor ->
                DropdownMenuItem(
                    text = {Text(proveedor)},
                    onClick = {
                        proveedorSeleccionado=proveedor
                        menuExtendido=false
                    }
                )
                }
            }
        }
        //control de seleccion: switch para estado activo/innactivo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("producto disponible")
            Switch(
                checked = estaActivo,
                onCheckedChange = {estaActivo=it}
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        //boton de confirmacion
        Button(
            onClick = {
                val nuevo = Producto(
                    id = (100..999).random(),
                    descripcion=descripcion.trim(),
                    proveedor=proveedorSeleccionado,
                    precio = precioTexto.toDoubleOrNull()?: 0.0,
                    activo = estaActivo
                )
                onGuardarProfucto(nuevo)
            },
            enabled = formularioValido,
            modifier = Modifier.fillMaxSize()
        ){
            Text(text = "Registrar Producto")
        }
    }
}