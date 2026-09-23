package com.example.myapplication.screens


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import com.example.myapplication.components.AvatarCircular
import com.example.myapplication.components.CampoBusqueda
import com.example.myapplication.components.CustomTextField
import com.example.myapplication.components.EstadoVacio
import com.example.myapplication.data.ProductoDao
import com.example.myapplication.data.ProveedorDao
import com.example.myapplication.util.formatearGuaranies

//modelo simple de datos para representar en la interfaz
data class Producto(
    val id: Int,
    val descripcion: String,
    val proveedorId: Int, //clave foranea: id del proveedor en la base de datos
    val proveedor: String, //razon social del proveedor (se obtiene con el JOIN, solo para mostrar)
    val precio: Double,
    val activo: Boolean
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(modifier: Modifier= Modifier){
    val context= LocalContext.current
    val dao = remember { ProductoDao(context) }
    //Estado para controlar la pestana seleccionada (0: Listado, 1: Formulario de Alta)
    var selectedTabIndex by remember { mutableStateOf(0) }
    val pestanas = listOf("Listado", "Nuevo Registro")

    //Snackbar propio de la pantalla (reemplaza a los Toast)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    //orden seleccionado en el menu del listado: null (sin orden), "ASC" o "DESC"
    var ordenSeleccionado by remember { mutableStateOf<String?>(null) }

    //Lista en memoria de productos
    val productosRegistrados = remember {
        mutableStateListOf<Producto>()
    }
    //funcion auxiliar para refrescar la lista desde la base de datos (respetando el orden elegido)
    fun recargarProductosDesdeDb(){
        val desdeDb = dao.listarTodos()
        val ordenados = when (ordenSeleccionado){
            "ASC" -> desdeDb.sortedBy { it.precio }
            "DESC" -> desdeDb.sortedByDescending { it.precio }
            else -> desdeDb
        }
        productosRegistrados.clear()
        productosRegistrados.addAll(ordenados)
    }
    LaunchedEffect(Unit){
        recargarProductosDesdeDb()
    }

    //muestra un mensaje simple en el Snackbar
    fun mostrarMensaje(mensaje: String){
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(mensaje)
        }
    }
    //muestra un mensaje con la accion "Deshacer"
    fun mostrarMensajeConDeshacer(mensaje: String, onDeshacer: () -> Unit){
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val resultado = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = "Deshacer",
                duration = SnackbarDuration.Short
            )
            if (resultado == SnackbarResult.ActionPerformed){
                onDeshacer()
            }
        }
    }

    //habilita o deshabilita un producto desde el menu contextual
    fun cambiarEstadoProducto(producto: Producto){
        val filas = dao.actualizar(producto.copy(activo = !producto.activo))
        if (filas>0){
            recargarProductosDesdeDb()
            mostrarMensajeConDeshacer(if (producto.activo) "Producto deshabilitado" else "Producto habilitado"){
                //se vuelve al estado anterior
                dao.actualizar(producto)
                recargarProductosDesdeDb()
            }
        }else{
            mostrarMensaje("Error al cambiar el estado del producto")
        }
    }

    //estado para el producto seleccionado a eliminar
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    var productoAEditar by remember { mutableStateOf<Producto?>(null) }

    //dialogo de confirmacion
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = {productoAEliminar=null},
            title = {Text("Confirmar Eliminacion")},
            text= {Text("¿Desea eliminar permanentemente '${producto.descripcion}' del catalogo?")},
            confirmButton = {
                TextButton(
                    onClick = {
                        val filas = dao.eliminar(producto.id)
                        if (filas>0){
                            recargarProductosDesdeDb()
                            //se ofrece deshacer: se vuelve a insertar con el mismo id y proveedor
                            mostrarMensajeConDeshacer("Producto eliminado"){
                                if (dao.restaurar(producto) != -1L){
                                    recargarProductosDesdeDb()
                                }else{
                                    mostrarMensaje("No se pudo restaurar el producto")
                                }
                            }
                        }else{
                            mostrarMensaje("Error al eliminar el registro")
                        }
                        productoAEliminar=null
                    }
                ) {
                    Text("Eliminar", color= MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {productoAEliminar=null}) {
                    Text("Cancelar")
                }
            }
        )
    }

    //Box para poder ubicar el Snackbar sobre el contenido, abajo al centro
    Box(modifier=modifier.fillMaxSize()) {
        Column(modifier=Modifier.fillMaxSize()) {
            //Barra de pestanas superior
            TabRow(selectedTabIndex=selectedTabIndex) {
                pestanas.forEachIndexed { index, titulo ->
                    Tab(
                        selected = selectedTabIndex== index,
                        onClick = {if (index== 1 && selectedTabIndex==0){
                            productoAEditar=null
                        }

                            selectedTabIndex = index
                                },
                        text = {Text(titulo)}
                    )
                }
            }
            //renderizado condicional segun la pestana activa
            when(selectedTabIndex){
                0-> ListadoProductosTab(
                    productos = productosRegistrados,
                    ordenSeleccionado = ordenSeleccionado,
                    onOrdenarPorPrecioAsc = {
                        ordenSeleccionado = "ASC"
                        val ordenados = productosRegistrados.sortedBy { it.precio }
                        productosRegistrados.clear()
                        productosRegistrados.addAll(ordenados)
                    },
                    onOrdenarPorPrecioDesc = {
                        ordenSeleccionado = "DESC"
                        val ordenados = productosRegistrados.sortedByDescending { it.precio }
                        productosRegistrados.clear()
                        productosRegistrados.addAll(ordenados)
                    },
                    onSolicitarEliminar = {producto-> productoAEliminar=producto},
                    onEditar = {producto ->
                        productoAEditar=producto
                        selectedTabIndex=1
                    },
                    onCambiarEstado = {producto -> cambiarEstadoProducto(producto)},
                    onRegistrarPrimero = {
                        productoAEditar=null
                        selectedTabIndex=1
                    }
                )
                1 -> FormularioProductoTab(
                    productoInicial = productoAEditar,
                    onGuardarProducto = {prod ->
                        if (productoAEditar==null){
                            //1-Insercion
                            val idGenerado=dao.insertar(prod)
                            if(idGenerado!=-1L){
                                mostrarMensaje("Producto guardado (ID: $idGenerado)")
                            }else{
                                mostrarMensaje("Error al guardar el producto")
                            }
                        }else{
                            //2-Actualizacion
                            val filas=dao.actualizar(prod)
                            if (filas>0){
                                mostrarMensaje("Registro actualizado en base de datos")
                            }else{
                                mostrarMensaje("Error al actualizar el registro")
                            }
                        }
                        productoAEditar=null
                        recargarProductosDesdeDb()
                        selectedTabIndex=0
                    },
                    onCancelarEdicion = {
                        productoAEditar=null
                        selectedTabIndex=0
                    }
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


//Pestana 1: Listado de productos
@Composable
fun ListadoProductosTab(
    productos: List<Producto>,
    ordenSeleccionado: String?,
    onOrdenarPorPrecioAsc: () -> Unit,
    onOrdenarPorPrecioDesc: () -> Unit,
    onSolicitarEliminar: (Producto) -> Unit,
    onEditar: (Producto) -> Unit,
    onCambiarEstado: (Producto) -> Unit,
    onRegistrarPrimero: () -> Unit
){
    var menuOpcionesGeneralExpandido by remember { mutableStateOf(false) }
    //texto de busqueda: filtra por descripcion o proveedor (sin distinguir mayusculas)
    var textoBusqueda by remember { mutableStateOf("") }
    val filtrando = textoBusqueda.isNotBlank()
    val productosFiltrados = if (filtrando) {
        val buscado = textoBusqueda.trim()
        productos.filter {
            it.descripcion.contains(buscado, ignoreCase = true) ||
                    it.proveedor.contains(buscado, ignoreCase = true)
        }
    } else productos

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        //cabecera con menu basico
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text= if (filtrando) "Catalogo (${productosFiltrados.size}/${productos.size} items)"
                      else "Catalogo (${productos.size} items)",
                style = MaterialTheme.typography.titleMedium
            )
            //disparador del menu basico general
            Box{
                IconButton(onClick = {menuOpcionesGeneralExpandido=true}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones de listado"
                    )
                }
                DropdownMenu(
                    expanded = menuOpcionesGeneralExpandido,
                    onDismissRequest = {menuOpcionesGeneralExpandido=false}
                ) {
                    DropdownMenuItem(
                        text={Text("Ordenar de Menor a Mayor Precio")},
                        leadingIcon = {Icon(Icons.Default.ArrowUpward, contentDescription = null)},
                        trailingIcon = {
                            if (ordenSeleccionado == "ASC") Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                        },
                        onClick = {
                            onOrdenarPorPrecioAsc()
                            menuOpcionesGeneralExpandido=false
                        }
                    )
                    DropdownMenuItem(
                        text={Text("Ordenar de Mayor a Menor Precio")},
                        leadingIcon = {Icon(Icons.Default.ArrowDownward, contentDescription = null)},
                        trailingIcon = {
                            if (ordenSeleccionado == "DESC") Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                        },
                        onClick = {
                            onOrdenarPorPrecioDesc()
                            menuOpcionesGeneralExpandido=false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        //campo de busqueda (solo si hay productos cargados)
        if (productos.isNotEmpty()){
            CampoBusqueda(
                texto = textoBusqueda,
                onTextoChange = {textoBusqueda=it},
                placeholder = "Buscar por descripcion o proveedor"
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if(productos.isEmpty()){
            //estado vacio: no hay nada registrado
            EstadoVacio(
                icono = Icons.Default.Inventory2,
                mensaje = "No hay productos registrados aun",
                textoBoton = "Registrar el primero",
                onAccion = onRegistrarPrimero
            )
        } else if (productosFiltrados.isEmpty()){
            //la busqueda no encontro coincidencias
            EstadoVacio(
                icono = Icons.Default.SearchOff,
                mensaje = "Sin resultados para '${textoBusqueda.trim()}'"
            )
        } else{
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productosFiltrados, key={it.id}){prod->
                    ProductoItemContextual(
                        producto = prod,
                        onEditar={onEditar(prod)},
                        onEliminar = {onSolicitarEliminar(prod)},
                        onCambiarEstado = {onCambiarEstado(prod)}
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
    onEliminar: () -> Unit,
    onCambiarEstado: () -> Unit
){
    var menuContextualExpandido by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(modifier = Modifier
            .fillMaxWidth()
            //los productos inactivos se muestran atenuados
            .alpha(if (producto.activo) 1f else 0.55f)
            .combinedClickable(
                onClick = {/*Clic normal*/},
                onLongClick = {
                    //vibracion corta y despliega el menu contextual al mantener presionado
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuContextualExpandido=true
                }
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (producto.activo) MaterialTheme.colorScheme.surfaceVariant
                                 else MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //avatar circular con icono de producto
                AvatarCircular(icono = Icons.Default.Inventory2)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text="${producto.id}. ${producto.descripcion}",
                            style= MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Badge(
                            containerColor = if(producto.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        ){
                            Text(if (producto.activo)"Activo" else "Inactivo")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Proveedor",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text= producto.proveedor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text= formatearGuaranies(producto.precio),
                        style = MaterialTheme.typography.bodyMedium,
                        color= MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        // menu contextual anclado a la tarjeta
        DropdownMenu(
            expanded = menuContextualExpandido,
            onDismissRequest = {menuContextualExpandido=false}
        ) {
            //cabecera no clickeable con el nombre del producto
            Text(
                text = producto.descripcion,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider()
            DropdownMenuItem(
                text={Text("Modificar Registro")},
                leadingIcon = {Icon(Icons.Default.Edit, contentDescription = "Editar")},
                onClick = {
                    menuContextualExpandido=false
                    onEditar()
                }
            )
            DropdownMenuItem(
                text={Text(if (producto.activo) "Deshabilitar" else "Habilitar")},
                leadingIcon = {
                    Icon(
                        imageVector = if (producto.activo) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = if (producto.activo) "Deshabilitar" else "Habilitar"
                    )
                },
                onClick = {
                    menuContextualExpandido=false
                    onCambiarEstado()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text={Text("Eliminar Registro", color = MaterialTheme.colorScheme.error)},
                leadingIcon = {Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)},
                onClick = {
                    menuContextualExpandido=false
                    onEliminar()
                }
            )
        }
    }
}





//Pestana 2: Formulario de alta (controles de seleccion)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProductoTab(
    onGuardarProducto: (Producto) -> Unit,
    productoInicial: Producto? =null,
    onCancelarEdicion:()-> Unit

){
    //Si se esta editando, los campos inician con los datos del producto seleccionado
    var descripcion by remember(productoInicial) { mutableStateOf(productoInicial?.descripcion ?: "") }
    var precioTexto by remember(productoInicial) { mutableStateOf(productoInicial?.precio?.toLong()?.toString() ?: "") }
    var estaActivo by remember(productoInicial) { mutableStateOf(productoInicial?.activo ?: true) }

    //los errores solo se muestran despues de que el usuario modifico el campo
    //(en modo edicion se muestran desde el inicio, para avisar si el dato guardado no es valido)
    var descripcionTocada by remember(productoInicial) { mutableStateOf(productoInicial != null) }
    var precioTocado by remember(productoInicial) { mutableStateOf(productoInicial != null) }

    //Opciones para el menu de seleccion referencial (Proveedores)
    //Se cargan desde la tabla de proveedores (solo los activos)
    //Si se edita un producto cuyo proveedor esta inactivo, igual se incluye para no perder el dato
    val context = LocalContext.current
    val proveedoresDisponibles = remember(productoInicial) {
        ProveedorDao(context).listarTodos().filter { it.activo || it.id == productoInicial?.proveedorId }
    }
    //se guarda el id del proveedor seleccionado (se muestra su razon social)
    var proveedorSeleccionadoId by remember(productoInicial) {mutableStateOf(productoInicial?.proveedorId ?: proveedoresDisponibles.firstOrNull()?.id)}
    val proveedorSeleccionado = proveedoresDisponibles.find { it.id == proveedorSeleccionadoId }
    var menuExpandido by remember { mutableStateOf(false) }
    val esEdicion =productoInicial!=null

    //reglas de validacion
    val errorDescripcion: String? = if (descripcion.isBlank()) "La descripcion es obligatoria" else null
    val precioNumero = precioTexto.trim().toDoubleOrNull()
    val errorPrecio: String? = when {
        precioTexto.isBlank() -> "El precio es obligatorio"
        precioNumero == null -> "Ingrese solo numeros"
        precioNumero <= 0 -> "El precio debe ser mayor a 0"
        else -> null
    }
    val formularioValido = errorDescripcion == null && errorPrecio == null && proveedorSeleccionado != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text= if(esEdicion)"Modificar Producto (ID: ${productoInicial?.id})" else "Registro en SQLite",
            style= MaterialTheme.typography.titleMedium,
            color= if(esEdicion) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        )
        //campo descriptivo
        CustomTextField(
            value=descripcion,
            onValueChange = {
                descripcion=it
                descripcionTocada=true
            },
            label="Descripcion del producto",
            leadingIcon = Icons.Default.Inventory2,
            isError = descripcionTocada && errorDescripcion != null,
            supportingText = if (descripcionTocada) errorDescripcion else null
        )
        //campo numerico
        CustomTextField(
            value=precioTexto,
            onValueChange = {
                precioTexto=it
                precioTocado=true
            },
            label="Precio Unitario (Gs.)",
            leadingIcon = Icons.Default.Payments,
            keyboardType = KeyboardType.Number,
            isError = precioTocado && errorPrecio != null,
            supportingText = if (precioTocado) errorPrecio else null
        )

        //Control de Seleccion 1: menu desplegable para proveedores
        ExposedDropdownMenuBox(
            expanded=menuExpandido,
            onExpandedChange = {menuExpandido = !menuExpandido}
        ) {
            OutlinedTextField(
                value=proveedorSeleccionado?.razonSocial ?: "",
                onValueChange = {},
                readOnly = true,
                label= {Text("Proveedor Referencial")},
                leadingIcon = {Icon(Icons.Default.Store, contentDescription = "Proveedor")},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido)},
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = menuExpandido,
                onDismissRequest = {menuExpandido=false}
            ) {
                proveedoresDisponibles.forEach { proveedor->
                    DropdownMenuItem(
                        text= {Text(proveedor.razonSocial)},
                        onClick = {
                            proveedorSeleccionadoId= proveedor.id
                            menuExpandido=false
                        }
                    )
                }
            }
        }
        //aviso cuando no hay proveedores cargados
        if (proveedoresDisponibles.isEmpty()){
            Text(
                text= "Registre un proveedor activo primero",
                style = MaterialTheme.typography.bodySmall,
                color= MaterialTheme.colorScheme.error
            )
        }

        //control de seleccion 2: Switch para estado activo e inactivo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("El producto esta disponible?")
            Switch(
                checked = estaActivo,
                onCheckedChange = {estaActivo=it}
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        //Boton de confirmacion (deshabilitado mientras el formulario no sea valido)
        Button(
            onClick = {
                val productoResultante= Producto(
                    id= productoInicial?.id?:0,
                    descripcion=descripcion.trim(),
                    proveedorId = proveedorSeleccionado?.id ?: 0,
                    proveedor = proveedorSeleccionado?.razonSocial ?: "",
                    precio = precioNumero ?: 0.0,
                    activo = estaActivo
                )
                onGuardarProducto(productoResultante)
            },
            enabled = formularioValido,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(if(esEdicion)"Actualizar en BD" else"Guardar en BD")
        }
    }


}
