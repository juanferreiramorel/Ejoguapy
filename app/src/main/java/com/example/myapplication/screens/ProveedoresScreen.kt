package com.example.myapplication.screens


import android.database.sqlite.SQLiteConstraintException
import android.util.Patterns
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import com.example.myapplication.components.AvatarCircular
import com.example.myapplication.components.CampoBusqueda
import com.example.myapplication.components.CustomTextField
import com.example.myapplication.components.EstadoVacio
import com.example.myapplication.components.obtenerIniciales
import com.example.myapplication.data.ProveedorDao
import com.example.myapplication.data.ResultadoRuc
import com.example.myapplication.data.consultarRuc
import com.example.myapplication.data.extraerNumeroRuc

//modelo simple de datos para representar en la interfaz (id = codigo del proveedor)
data class Proveedor(
    val id: Int,
    val razonSocial: String,
    val ruc: String,
    val direccion: String,
    val telefono: String,
    val correo: String,
    val contacto: String,
    val activo: Boolean
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedoresScreen(modifier: Modifier= Modifier){
    val context= LocalContext.current
    val dao = remember { ProveedorDao(context) }
    //Estado para controlar la pestana seleccionada (0: Listado, 1: Formulario de Alta)
    var selectedTabIndex by remember { mutableStateOf(0) }
    val pestanas = listOf("Listado", "Nuevo Registro")

    //Snackbar propio de la pantalla (reemplaza a los Toast)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    //orden seleccionado en el menu del listado: null (sin orden), "ASC" o "DESC"
    var ordenSeleccionado by remember { mutableStateOf<String?>(null) }

    //Lista en memoria de proveedores
    val proveedoresRegistrados = remember {
        mutableStateListOf<Proveedor>()
    }
    //funcion auxiliar para refrescar la lista desde la base de datos (respetando el orden elegido)
    fun recargarProveedoresDesdeDb(){
        val desdeDb = dao.listarTodos()
        val ordenados = when (ordenSeleccionado){
            "ASC" -> desdeDb.sortedBy { it.razonSocial.lowercase() }
            "DESC" -> desdeDb.sortedByDescending { it.razonSocial.lowercase() }
            else -> desdeDb
        }
        proveedoresRegistrados.clear()
        proveedoresRegistrados.addAll(ordenados)
    }
    LaunchedEffect(Unit){
        recargarProveedoresDesdeDb()
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

    //habilita o deshabilita un proveedor (menu contextual o dialogo de "no se puede eliminar")
    fun cambiarEstadoProveedor(proveedor: Proveedor){
        val filas = dao.actualizar(proveedor.copy(activo = !proveedor.activo))
        if (filas>0){
            recargarProveedoresDesdeDb()
            mostrarMensajeConDeshacer(if (proveedor.activo) "Proveedor deshabilitado" else "Proveedor habilitado"){
                //se vuelve al estado anterior
                dao.actualizar(proveedor)
                recargarProveedoresDesdeDb()
            }
        }else{
            mostrarMensaje("Error al cambiar el estado del proveedor")
        }
    }

    //estado para el proveedor seleccionado a eliminar
    var proveedorAEliminar by remember { mutableStateOf<Proveedor?>(null) }
    var proveedorAEditar by remember { mutableStateOf<Proveedor?>(null) }
    //proveedor que no se pudo eliminar por tener productos asociados (proveedor, cantidad de productos)
    var proveedorADeshabilitar by remember { mutableStateOf<Pair<Proveedor, Int>?>(null) }

    //dialogo: no se puede eliminar, se ofrece deshabilitar
    proveedorADeshabilitar?.let { (proveedor, cantidadProductos) ->
        AlertDialog(
            onDismissRequest = {proveedorADeshabilitar=null},
            title = {Text("No se puede eliminar")},
            text= {
                if (proveedor.activo){
                    Text("El proveedor '${proveedor.razonSocial}' tiene $cantidadProductos productos asociados.\n\n¿Desea deshabilitarlo? Sus productos se conservan, pero ya no aparecera al registrar productos nuevos.")
                }else{
                    Text("El proveedor '${proveedor.razonSocial}' tiene $cantidadProductos productos asociados y ya se encuentra deshabilitado.")
                }
            },
            confirmButton = {
                if (proveedor.activo){
                    TextButton(
                        onClick = {
                            cambiarEstadoProveedor(proveedor)
                            proveedorADeshabilitar=null
                        }
                    ) {
                        Text("Deshabilitar", color= MaterialTheme.colorScheme.error)
                    }
                }else{
                    TextButton(onClick = {proveedorADeshabilitar=null}) {
                        Text("Entendido")
                    }
                }
            },
            dismissButton = {
                if (proveedor.activo){
                    TextButton(onClick = {proveedorADeshabilitar=null}) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    //dialogo de confirmacion
    proveedorAEliminar?.let { proveedor ->
        AlertDialog(
            onDismissRequest = {proveedorAEliminar=null},
            title = {Text("Confirmar Eliminacion")},
            text= {Text("¿Desea eliminar permanentemente al proveedor '${proveedor.razonSocial}'?")},
            confirmButton = {
                TextButton(
                    onClick = {
                        //no se puede eliminar un proveedor que tiene productos asociados
                        val cantidadProductos = dao.contarProductos(proveedor.id)
                        if (cantidadProductos>0){
                            //se ofrece deshabilitarlo en lugar de eliminarlo
                            proveedorADeshabilitar = proveedor to cantidadProductos
                        }else{
                            try {
                                val filas = dao.eliminar(proveedor.id)
                                if (filas>0){
                                    recargarProveedoresDesdeDb()
                                    //se ofrece deshacer: se vuelve a insertar con el mismo codigo
                                    mostrarMensajeConDeshacer("Proveedor eliminado"){
                                        if (dao.restaurar(proveedor) != -1L){
                                            recargarProveedoresDesdeDb()
                                        }else{
                                            mostrarMensaje("No se pudo restaurar el proveedor")
                                        }
                                    }
                                }else{
                                    mostrarMensaje("Error al eliminar el registro")
                                }
                            } catch (e: SQLiteConstraintException){
                                //por seguridad: la clave foranea impide borrar si quedo algun producto asociado
                                proveedorADeshabilitar = proveedor to dao.contarProductos(proveedor.id)
                            }
                        }
                        proveedorAEliminar=null
                    }
                ) {
                    Text("Eliminar", color= MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {proveedorAEliminar=null}) {
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
                            proveedorAEditar=null
                        }

                            selectedTabIndex = index
                                },
                        text = {Text(titulo)}
                    )
                }
            }
            //renderizado condicional segun la pestana activa
            when(selectedTabIndex){
                0-> ListadoProveedoresTab(
                    proveedores = proveedoresRegistrados,
                    ordenSeleccionado = ordenSeleccionado,
                    onOrdenarPorRazonSocialAsc = {
                        ordenSeleccionado = "ASC"
                        val ordenados = proveedoresRegistrados.sortedBy { it.razonSocial.lowercase() }
                        proveedoresRegistrados.clear()
                        proveedoresRegistrados.addAll(ordenados)
                    },
                    onOrdenarPorRazonSocialDesc = {
                        ordenSeleccionado = "DESC"
                        val ordenados = proveedoresRegistrados.sortedByDescending { it.razonSocial.lowercase() }
                        proveedoresRegistrados.clear()
                        proveedoresRegistrados.addAll(ordenados)
                    },
                    onSolicitarEliminar = {proveedor-> proveedorAEliminar=proveedor},
                    onEditar = {proveedor ->
                        proveedorAEditar=proveedor
                        selectedTabIndex=1
                    },
                    onCambiarEstado = {proveedor -> cambiarEstadoProveedor(proveedor)},
                    onRegistrarPrimero = {
                        proveedorAEditar=null
                        selectedTabIndex=1
                    }
                )
                1 -> FormularioProveedorTab(
                    proveedorInicial = proveedorAEditar,
                    onGuardarProveedor = {prov ->
                        if (proveedorAEditar==null){
                            //1-Insercion
                            val idGenerado=dao.insertar(prov)
                            if(idGenerado!=-1L){
                                mostrarMensaje("Proveedor guardado (Codigo: $idGenerado)")
                            }else{
                                mostrarMensaje("Error al guardar el proveedor")
                            }
                        }else{
                            //2-Actualizacion
                            val filas=dao.actualizar(prov)
                            if (filas>0){
                                mostrarMensaje("Registro actualizado en base de datos")
                            }else{
                                mostrarMensaje("Error al actualizar el registro")
                            }
                        }
                        proveedorAEditar=null
                        recargarProveedoresDesdeDb()
                        selectedTabIndex=0
                    },
                    onCancelarEdicion = {
                        proveedorAEditar=null
                        selectedTabIndex=0
                    },
                    onMostrarMensaje = {mensaje -> mostrarMensaje(mensaje)}
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


//Pestana 1: Listado de proveedores
@Composable
fun ListadoProveedoresTab(
    proveedores: List<Proveedor>,
    ordenSeleccionado: String?,
    onOrdenarPorRazonSocialAsc: () -> Unit,
    onOrdenarPorRazonSocialDesc: () -> Unit,
    onSolicitarEliminar: (Proveedor) -> Unit,
    onEditar: (Proveedor) -> Unit,
    onCambiarEstado: (Proveedor) -> Unit,
    onRegistrarPrimero: () -> Unit
){
    var menuOpcionesGeneralExpandido by remember { mutableStateOf(false) }
    //texto de busqueda: filtra por razon social o RUC (sin distinguir mayusculas)
    var textoBusqueda by remember { mutableStateOf("") }
    val filtrando = textoBusqueda.isNotBlank()
    val proveedoresFiltrados = if (filtrando) {
        val buscado = textoBusqueda.trim()
        proveedores.filter {
            it.razonSocial.contains(buscado, ignoreCase = true) ||
                    it.ruc.contains(buscado, ignoreCase = true)
        }
    } else proveedores

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        //cabecera con menu basico
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text= if (filtrando) "Proveedores (${proveedoresFiltrados.size}/${proveedores.size} registros)"
                      else "Proveedores (${proveedores.size} registros)",
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
                        text={Text("Ordenar por Razon Social (A-Z)")},
                        leadingIcon = {Icon(Icons.Default.ArrowUpward, contentDescription = null)},
                        trailingIcon = {
                            if (ordenSeleccionado == "ASC") Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                        },
                        onClick = {
                            onOrdenarPorRazonSocialAsc()
                            menuOpcionesGeneralExpandido=false
                        }
                    )
                    DropdownMenuItem(
                        text={Text("Ordenar por Razon Social (Z-A)")},
                        leadingIcon = {Icon(Icons.Default.ArrowDownward, contentDescription = null)},
                        trailingIcon = {
                            if (ordenSeleccionado == "DESC") Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                        },
                        onClick = {
                            onOrdenarPorRazonSocialDesc()
                            menuOpcionesGeneralExpandido=false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        //campo de busqueda (solo si hay proveedores cargados)
        if (proveedores.isNotEmpty()){
            CampoBusqueda(
                texto = textoBusqueda,
                onTextoChange = {textoBusqueda=it},
                placeholder = "Buscar por razon social o RUC"
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if(proveedores.isEmpty()){
            //estado vacio: no hay nada registrado
            EstadoVacio(
                icono = Icons.Default.Business,
                mensaje = "No hay proveedores registrados aun",
                textoBoton = "Registrar el primero",
                onAccion = onRegistrarPrimero
            )
        } else if (proveedoresFiltrados.isEmpty()){
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
                items(proveedoresFiltrados, key={it.id}){prov->
                    ProveedorItemContextual(
                        proveedor = prov,
                        onEditar={onEditar(prov)},
                        onEliminar = {onSolicitarEliminar(prov)},
                        onCambiarEstado = {onCambiarEstado(prov)}
                    )
                }
            }
        }
    }


}

//fila de dato con icono pequeno (RUC, telefono, contacto)
@Composable
private fun DatoConIcono(icono: ImageVector, descripcion: String, texto: String, destacado: Boolean = false){
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = texto,
            style = if (destacado) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            color = if (destacado) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProveedorItemContextual(
    proveedor: Proveedor,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onCambiarEstado: () -> Unit
){
    var menuContextualExpandido by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(modifier = Modifier
            .fillMaxWidth()
            //los proveedores inactivos se muestran atenuados
            .alpha(if (proveedor.activo) 1f else 0.55f)
            .combinedClickable(
                onClick = {/*Clic normal*/},
                onLongClick = {
                    //vibracion corta y despliega el menu contextual al mantener presionado
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuContextualExpandido=true
                }
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (proveedor.activo) MaterialTheme.colorScheme.surfaceVariant
                                 else MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //avatar circular con las iniciales de la razon social
                AvatarCircular(iniciales = obtenerIniciales(proveedor.razonSocial))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text="${proveedor.id}. ${proveedor.razonSocial}",
                            style= MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Badge(
                            containerColor = if(proveedor.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        ){
                            Text(if (proveedor.activo)"Activo" else "Inactivo")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    DatoConIcono(Icons.Default.Badge, "RUC", "RUC: ${proveedor.ruc}", destacado = true)
                    DatoConIcono(Icons.Default.Phone, "Telefono", proveedor.telefono.ifBlank { "Sin telefono" })
                    DatoConIcono(Icons.Default.Person, "Contacto", proveedor.contacto.ifBlank { "Sin contacto" })
                }
            }
        }
        // menu contextual anclado a la tarjeta
        DropdownMenu(
            expanded = menuContextualExpandido,
            onDismissRequest = {menuContextualExpandido=false}
        ) {
            //cabecera no clickeable con el nombre del proveedor
            Text(
                text = proveedor.razonSocial,
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
                text={Text(if (proveedor.activo) "Deshabilitar" else "Habilitar")},
                leadingIcon = {
                    Icon(
                        imageVector = if (proveedor.activo) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = if (proveedor.activo) "Deshabilitar" else "Habilitar"
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





//Pestana 2: Formulario de alta de proveedores
@Composable
fun FormularioProveedorTab(
    onGuardarProveedor: (Proveedor) -> Unit,
    proveedorInicial: Proveedor? =null,
    onCancelarEdicion:()-> Unit,
    onMostrarMensaje: (String) -> Unit = {} //usa el Snackbar de la pantalla

){
    val context = LocalContext.current
    val dao = remember { ProveedorDao(context) }
    //corrutina para consultar la API de RUC sin bloquear la interfaz
    val scope = rememberCoroutineScope()

    //Si se esta editando, los campos inician con los datos del proveedor seleccionado
    var razonSocial by remember(proveedorInicial) { mutableStateOf(proveedorInicial?.razonSocial ?: "") }
    var ruc by remember(proveedorInicial) { mutableStateOf(proveedorInicial?.ruc ?: "") }
    var direccion by remember(proveedorInicial) { mutableStateOf(proveedorInicial?.direccion ?: "") }
    var telefono by remember(proveedorInicial) { mutableStateOf(proveedorInicial?.telefono ?: "") }
    var correo by remember(proveedorInicial) { mutableStateOf(proveedorInicial?.correo ?: "") }
    var contacto by remember(proveedorInicial) { mutableStateOf(proveedorInicial?.contacto ?: "") }
    var estaActivo by remember(proveedorInicial) { mutableStateOf(proveedorInicial?.activo ?: true) }

    //los errores solo se muestran despues de que el usuario modifico el campo
    //(en modo edicion se muestran desde el inicio, para avisar si el dato guardado no es valido)
    var razonSocialTocada by remember(proveedorInicial) { mutableStateOf(proveedorInicial != null) }
    var rucTocado by remember(proveedorInicial) { mutableStateOf(proveedorInicial != null) }
    var telefonoTocado by remember(proveedorInicial) { mutableStateOf(proveedorInicial != null) }
    var correoTocado by remember(proveedorInicial) { mutableStateOf(proveedorInicial != null) }

    val esEdicion =proveedorInicial!=null

    //estado del mini buscador de RUC
    var buscandoRuc by remember { mutableStateOf(false) }
    //error propio de la busqueda (texto que no se puede interpretar como RUC)
    var errorBusquedaRuc by remember(proveedorInicial) { mutableStateOf<String?>(null) }

    //reglas de validacion
    //RUC paraguayo: 5 a 8 digitos, guion y digito verificador (ej: 80012345-6)
    val formatoRuc = remember { Regex("^\\d{5,8}-\\d$") }
    //telefono: solo digitos, espacios, + y -
    val formatoTelefono = remember { Regex("^[0-9+\\- ]+$") }
    val errorRazonSocial: String? = if (razonSocial.isBlank()) "La razon social es obligatoria" else null
    val errorRuc: String? = when {
        errorBusquedaRuc != null -> errorBusquedaRuc
        ruc.isBlank() -> "El RUC es obligatorio"
        !formatoRuc.matches(ruc.trim()) -> "Formato invalido (ej: 80012345-6)"
        else -> null
    }
    val errorTelefono: String? =
        if (telefono.isNotBlank() && !formatoTelefono.matches(telefono.trim())) "Solo numeros, espacios, + y -" else null
    val errorCorreo: String? =
        if (correo.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) "Correo electronico invalido" else null
    val formularioValido = errorRazonSocial == null && errorRuc == null && errorTelefono == null && errorCorreo == null

    //consulta el RUC en la API "people" y autocompleta los datos del proveedor
    fun buscarRuc(){
        if (buscandoRuc) return
        rucTocado=true
        val numero = extraerNumeroRuc(ruc)
        if (numero == null){
            errorBusquedaRuc = "RUC invalido"
            return
        }
        errorBusquedaRuc = null
        //aviso si ya existe otro proveedor local con el mismo RUC (no se bloquea la carga)
        val duplicado = dao.listarTodos().firstOrNull {
            it.id != proveedorInicial?.id && extraerNumeroRuc(it.ruc) == numero
        }
        //se antepone al mensaje final para que un Snackbar no tape al otro
        val avisoDuplicado = if (duplicado != null) "Ya existe un proveedor con ese RUC: ${duplicado.razonSocial}\n" else ""
        fun mostrar(mensaje: String) = onMostrarMensaje(avisoDuplicado + mensaje)
        buscandoRuc = true
        scope.launch {
            when (val resultado = consultarRuc(numero)){
                is ResultadoRuc.Encontrado -> {
                    //con DV se arma el RUC completo; sin DV se deja el numero para que la validacion pida el DV
                    ruc = if (!resultado.dv.isNullOrBlank()) "${resultado.ruc}-${resultado.dv}" else resultado.ruc
                    razonSocial = resultado.razonSocial
                    razonSocialTocada = true
                    //el estado de la SET NO cambia "activo": un proveedor nuevo siempre se crea activo
                    //y en edicion lo decide el usuario con el Switch (solo se muestra una advertencia)
                    //direccion, telefono, correo y contacto no vienen en la API: se conservan
                    val cargado = "Datos cargados desde la SET: ${resultado.razonSocial}" +
                            if (resultado.estado.isNotBlank()) " (${resultado.estado})" else ""
                    //si el RUC no esta activo se agrega la advertencia en el mismo mensaje
                    if (resultado.estado.isNotBlank() && resultado.estado != "ACTIVO"){
                        mostrar("$cargado\nAtencion: el RUC figura como ${resultado.estado}")
                    } else {
                        mostrar(cargado)
                    }
                }
                ResultadoRuc.NoEncontrado ->
                    mostrar("RUC no encontrado, complete los datos manualmente")
                is ResultadoRuc.Error ->
                    mostrar("No se pudo consultar el RUC (${resultado.mensaje}). Complete los datos manualmente")
            }
            buscandoRuc = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text= if(esEdicion)"Modificar Proveedor (Codigo: ${proveedorInicial?.id})" else "Registro en SQLite",
            style= MaterialTheme.typography.titleMedium,
            color= if(esEdicion) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        )
        //mini buscador de RUC (va primero porque con el se autocompletan los demas datos)
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Consulta en la SET",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Busque el RUC para completar la razon social automaticamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                //campo RUC + boton Buscar en la misma fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomTextField(
                        value=ruc,
                        onValueChange = {
                            ruc=it
                            rucTocado=true
                            errorBusquedaRuc=null
                        },
                        label="RUC",
                        leadingIcon = Icons.Default.Badge,
                        modifier = Modifier.weight(1f),
                        isError = rucTocado && errorRuc != null,
                        supportingText = if (rucTocado && errorRuc != null) errorRuc else "Ingrese el RUC con o sin DV y presione buscar",
                        //boton "Buscar" del teclado
                        imeAction = ImeAction.Search,
                        keyboardActions = KeyboardActions(onSearch = {buscarRuc()})
                    )
                    //boton visible para consultar; deshabilitado mientras busca o si el campo esta vacio
                    //(el padding superior lo alinea con el borde del campo, que deja lugar a la etiqueta)
                    FilledTonalButton(
                        onClick = {buscarRuc()},
                        enabled = !buscandoRuc && ruc.isNotBlank(),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .height(56.dp)
                    ) {
                        if (buscandoRuc){
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscando...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscar")
                        }
                    }
                }
            }
        }
        //campo razon social
        CustomTextField(
            value=razonSocial,
            onValueChange = {
                razonSocial=it
                razonSocialTocada=true
            },
            label="Razon Social",
            leadingIcon = Icons.Default.Business,
            isError = razonSocialTocada && errorRazonSocial != null,
            supportingText = if (razonSocialTocada) errorRazonSocial else null
        )
        //campo direccion
        CustomTextField(
            value=direccion,
            onValueChange = {direccion=it},
            label="Direccion",
            leadingIcon = Icons.Default.LocationOn
        )
        //campo telefono
        CustomTextField(
            value=telefono,
            onValueChange = {
                telefono=it
                telefonoTocado=true
            },
            label="Telefono",
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            isError = telefonoTocado && errorTelefono != null,
            supportingText = if (telefonoTocado) errorTelefono else null
        )
        //campo correo
        CustomTextField(
            value=correo,
            onValueChange = {
                correo=it
                correoTocado=true
            },
            label="Correo electronico",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            isError = correoTocado && errorCorreo != null,
            supportingText = if (correoTocado) errorCorreo else null
        )
        //campo persona de contacto
        CustomTextField(
            value=contacto,
            onValueChange = {contacto=it},
            label="Persona de contacto",
            leadingIcon = Icons.Default.Person
        )

        //control de seleccion: Switch para estado activo e inactivo
        //solo en edicion: al crear, el proveedor nuevo siempre queda activo
        if (esEdicion){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("El proveedor esta activo?")
                Switch(
                    checked = estaActivo,
                    onCheckedChange = {estaActivo=it}
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        //Boton de confirmacion (deshabilitado mientras el formulario no sea valido)
        Button(
            onClick = {
                val proveedorResultante= Proveedor(
                    id= proveedorInicial?.id?:0,
                    razonSocial=razonSocial.trim(),
                    ruc = ruc.trim(),
                    direccion = direccion.trim(),
                    telefono = telefono.trim(),
                    correo = correo.trim(),
                    contacto = contacto.trim(),
                    //alta: siempre activo; edicion: lo que indique el Switch
                    activo = if (esEdicion) estaActivo else true
                )
                onGuardarProveedor(proveedorResultante)
            },
            enabled = formularioValido,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(if(esEdicion)"Actualizar en BD" else"Guardar en BD")
        }
        //Boton para cancelar la edicion y volver al listado
        if (esEdicion){
            OutlinedButton(
                onClick = onCancelarEdicion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar Edicion")
            }
        }
    }


}
