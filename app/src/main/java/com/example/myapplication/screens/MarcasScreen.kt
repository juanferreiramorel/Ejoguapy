package com.example.myapplication.screens


import android.database.sqlite.SQLiteConstraintException
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.myapplication.widget.actualizarWidgetCatalogo
import kotlinx.coroutines.launch

import com.example.myapplication.components.AvatarCircular
import com.example.myapplication.components.CampoBusqueda
import com.example.myapplication.components.CustomTextField
import com.example.myapplication.components.EstadoVacio
import com.example.myapplication.components.obtenerIniciales
import com.example.myapplication.data.MarcaDao

//modelo simple de datos para representar en la interfaz (id = codigo de la marca)
data class Marca(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val activo: Boolean
)

//largo maximo permitido para el nombre de la marca
private const val LARGO_MAXIMO_NOMBRE_MARCA = 50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarcasScreen(modifier: Modifier= Modifier){
    val context= LocalContext.current
    val dao = remember { MarcaDao(context) }
    //Estado para controlar la pestana seleccionada (0: Listado, 1: Formulario de Alta)
    var selectedTabIndex by remember { mutableStateOf(0) }
    val pestanas = listOf("Listado", "Nuevo Registro")

    //Snackbar propio de la pantalla (reemplaza a los Toast)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    //orden seleccionado en el menu del listado: null (sin orden), "ASC" o "DESC"
    var ordenSeleccionado by remember { mutableStateOf<String?>(null) }

    //Lista en memoria de marcas
    val marcasRegistradas = remember {
        mutableStateListOf<Marca>()
    }
    //funcion auxiliar para refrescar la lista desde la base de datos (respetando el orden elegido)
    fun recargarMarcasDesdeDb(){
        val desdeDb = dao.listarTodos()
        val ordenadas = when (ordenSeleccionado){
            "ASC" -> desdeDb.sortedBy { it.nombre.lowercase() }
            "DESC" -> desdeDb.sortedByDescending { it.nombre.lowercase() }
            else -> desdeDb
        }
        marcasRegistradas.clear()
        marcasRegistradas.addAll(ordenadas)
    }
    LaunchedEffect(Unit){
        recargarMarcasDesdeDb()
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

    //habilita o deshabilita una marca (menu contextual o dialogo de "no se puede eliminar")
    fun cambiarEstadoMarca(marca: Marca){
        val filas = dao.actualizar(marca.copy(activo = !marca.activo))
        if (filas>0){
            recargarMarcasDesdeDb()
            actualizarWidgetCatalogo(context)
            mostrarMensajeConDeshacer(if (marca.activo) "Marca deshabilitada" else "Marca habilitada"){
                //se vuelve al estado anterior
                dao.actualizar(marca)
                recargarMarcasDesdeDb()
                actualizarWidgetCatalogo(context)
            }
        }else{
            mostrarMensaje("Error al cambiar el estado de la marca")
        }
    }

    //estado para la marca seleccionada a eliminar
    var marcaAEliminar by remember { mutableStateOf<Marca?>(null) }
    var marcaAEditar by remember { mutableStateOf<Marca?>(null) }
    //marca que no se pudo eliminar por tener productos asociados (marca, cantidad de productos)
    var marcaADeshabilitar by remember { mutableStateOf<Pair<Marca, Int>?>(null) }

    //dialogo: no se puede eliminar, se ofrece deshabilitar
    marcaADeshabilitar?.let { (marca, cantidadProductos) ->
        AlertDialog(
            onDismissRequest = {marcaADeshabilitar=null},
            title = {Text("No se puede eliminar")},
            text= {
                if (marca.activo){
                    Text("La marca '${marca.nombre}' tiene $cantidadProductos productos asociados.\n\n¿Desea deshabilitarla? Sus productos se conservan, pero ya no aparecera al registrar productos nuevos.")
                }else{
                    Text("La marca '${marca.nombre}' tiene $cantidadProductos productos asociados y ya se encuentra deshabilitada.")
                }
            },
            confirmButton = {
                if (marca.activo){
                    TextButton(
                        onClick = {
                            cambiarEstadoMarca(marca)
                            marcaADeshabilitar=null
                        }
                    ) {
                        Text("Deshabilitar", color= MaterialTheme.colorScheme.error)
                    }
                }else{
                    TextButton(onClick = {marcaADeshabilitar=null}) {
                        Text("Entendido")
                    }
                }
            },
            dismissButton = {
                if (marca.activo){
                    TextButton(onClick = {marcaADeshabilitar=null}) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    //dialogo de confirmacion
    marcaAEliminar?.let { marca ->
        AlertDialog(
            onDismissRequest = {marcaAEliminar=null},
            title = {Text("Confirmar Eliminacion")},
            text= {Text("¿Desea eliminar permanentemente la marca '${marca.nombre}'?")},
            confirmButton = {
                TextButton(
                    onClick = {
                        //no se puede eliminar una marca que tiene productos asociados
                        val cantidadProductos = dao.contarProductos(marca.id)
                        if (cantidadProductos>0){
                            //se ofrece deshabilitarla en lugar de eliminarla
                            marcaADeshabilitar = marca to cantidadProductos
                        }else{
                            try {
                                val filas = dao.eliminar(marca.id)
                                if (filas>0){
                                    recargarMarcasDesdeDb()
                                    actualizarWidgetCatalogo(context)
                                    //se ofrece deshacer: se vuelve a insertar con el mismo codigo
                                    mostrarMensajeConDeshacer("Marca eliminada"){
                                        if (dao.restaurar(marca) != -1L){
                                            recargarMarcasDesdeDb()
                                            actualizarWidgetCatalogo(context)
                                        }else{
                                            mostrarMensaje("No se pudo restaurar la marca")
                                        }
                                    }
                                }else{
                                    mostrarMensaje("Error al eliminar el registro")
                                }
                            } catch (e: SQLiteConstraintException){
                                //por seguridad: la clave foranea impide borrar si quedo algun producto asociado
                                marcaADeshabilitar = marca to dao.contarProductos(marca.id)
                            }
                        }
                        marcaAEliminar=null
                    }
                ) {
                    Text("Eliminar", color= MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {marcaAEliminar=null}) {
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
                            marcaAEditar=null
                        }

                            selectedTabIndex = index
                                },
                        text = {Text(titulo)}
                    )
                }
            }
            //renderizado condicional segun la pestana activa
            when(selectedTabIndex){
                0-> ListadoMarcasTab(
                    marcas = marcasRegistradas,
                    ordenSeleccionado = ordenSeleccionado,
                    onOrdenarPorNombreAsc = {
                        ordenSeleccionado = "ASC"
                        val ordenadas = marcasRegistradas.sortedBy { it.nombre.lowercase() }
                        marcasRegistradas.clear()
                        marcasRegistradas.addAll(ordenadas)
                    },
                    onOrdenarPorNombreDesc = {
                        ordenSeleccionado = "DESC"
                        val ordenadas = marcasRegistradas.sortedByDescending { it.nombre.lowercase() }
                        marcasRegistradas.clear()
                        marcasRegistradas.addAll(ordenadas)
                    },
                    onSolicitarEliminar = {marca-> marcaAEliminar=marca},
                    onEditar = {marca ->
                        marcaAEditar=marca
                        selectedTabIndex=1
                    },
                    onCambiarEstado = {marca -> cambiarEstadoMarca(marca)},
                    onRegistrarPrimero = {
                        marcaAEditar=null
                        selectedTabIndex=1
                    }
                )
                1 -> FormularioMarcaTab(
                    marcaInicial = marcaAEditar,
                    onGuardarMarca = {marc ->
                        try {
                            if (marcaAEditar==null){
                                //1-Insercion
                                val idGenerado=dao.insertar(marc)
                                if(idGenerado!=-1L){
                                    actualizarWidgetCatalogo(context)
                                    mostrarMensaje("Marca guardada (Codigo: $idGenerado)")
                                }else{
                                    mostrarMensaje("Error al guardar la marca")
                                }
                            }else{
                                //2-Actualizacion
                                val filas=dao.actualizar(marc)
                                if (filas>0){
                                    actualizarWidgetCatalogo(context)
                                    mostrarMensaje("Registro actualizado en base de datos")
                                }else{
                                    mostrarMensaje("Error al actualizar el registro")
                                }
                            }
                            marcaAEditar=null
                            recargarMarcasDesdeDb()
                            selectedTabIndex=0
                        } catch (e: SQLiteConstraintException){
                            //el nombre es UNIQUE: se queda en el formulario para que el usuario lo corrija
                            mostrarMensaje("Ya existe una marca con ese nombre")
                        }
                    },
                    onCancelarEdicion = {
                        marcaAEditar=null
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


//Pestana 1: Listado de marcas
@Composable
fun ListadoMarcasTab(
    marcas: List<Marca>,
    ordenSeleccionado: String?,
    onOrdenarPorNombreAsc: () -> Unit,
    onOrdenarPorNombreDesc: () -> Unit,
    onSolicitarEliminar: (Marca) -> Unit,
    onEditar: (Marca) -> Unit,
    onCambiarEstado: (Marca) -> Unit,
    onRegistrarPrimero: () -> Unit
){
    var menuOpcionesGeneralExpandido by remember { mutableStateOf(false) }
    //texto de busqueda: filtra por nombre o descripcion (sin distinguir mayusculas)
    var textoBusqueda by remember { mutableStateOf("") }
    val filtrando = textoBusqueda.isNotBlank()
    val marcasFiltradas = if (filtrando) {
        val buscado = textoBusqueda.trim()
        marcas.filter {
            it.nombre.contains(buscado, ignoreCase = true) ||
                    it.descripcion.contains(buscado, ignoreCase = true)
        }
    } else marcas

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        //cabecera con menu basico
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text= if (filtrando) "Marcas (${marcasFiltradas.size}/${marcas.size} registros)"
                      else "Marcas (${marcas.size} registros)",
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
                        text={Text("Ordenar por Nombre (A-Z)")},
                        leadingIcon = {Icon(Icons.Default.ArrowUpward, contentDescription = null)},
                        trailingIcon = {
                            if (ordenSeleccionado == "ASC") Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                        },
                        onClick = {
                            onOrdenarPorNombreAsc()
                            menuOpcionesGeneralExpandido=false
                        }
                    )
                    DropdownMenuItem(
                        text={Text("Ordenar por Nombre (Z-A)")},
                        leadingIcon = {Icon(Icons.Default.ArrowDownward, contentDescription = null)},
                        trailingIcon = {
                            if (ordenSeleccionado == "DESC") Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                        },
                        onClick = {
                            onOrdenarPorNombreDesc()
                            menuOpcionesGeneralExpandido=false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        //campo de busqueda (solo si hay marcas cargadas)
        if (marcas.isNotEmpty()){
            CampoBusqueda(
                texto = textoBusqueda,
                onTextoChange = {textoBusqueda=it},
                placeholder = "Buscar por nombre o descripcion"
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if(marcas.isEmpty()){
            //estado vacio: no hay nada registrado
            EstadoVacio(
                icono = Icons.Default.Sell,
                mensaje = "No hay marcas registradas aun",
                textoBoton = "Registrar la primera",
                onAccion = onRegistrarPrimero
            )
        } else if (marcasFiltradas.isEmpty()){
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
                items(marcasFiltradas, key={it.id}){marc->
                    MarcaItemContextual(
                        marca = marc,
                        onEditar={onEditar(marc)},
                        onEliminar = {onSolicitarEliminar(marc)},
                        onCambiarEstado = {onCambiarEstado(marc)}
                    )
                }
            }
        }
    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarcaItemContextual(
    marca: Marca,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onCambiarEstado: () -> Unit
){
    var menuContextualExpandido by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(modifier = Modifier
            .fillMaxWidth()
            //las marcas inactivas se muestran atenuadas
            .alpha(if (marca.activo) 1f else 0.55f)
            .combinedClickable(
                onClick = {/*Clic normal*/},
                onLongClick = {
                    //vibracion corta y despliega el menu contextual al mantener presionado
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuContextualExpandido=true
                }
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (marca.activo) MaterialTheme.colorScheme.surfaceVariant
                                 else MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //avatar circular con las iniciales del nombre de la marca
                AvatarCircular(iniciales = obtenerIniciales(marca.nombre))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text="${marca.id}. ${marca.nombre}",
                            style= MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Badge(
                            containerColor = if(marca.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        ){
                            Text(if (marca.activo)"Activo" else "Inactivo")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Descripcion",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text= marca.descripcion.ifBlank { "Sin descripcion" },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        // menu contextual anclado a la tarjeta
        DropdownMenu(
            expanded = menuContextualExpandido,
            onDismissRequest = {menuContextualExpandido=false}
        ) {
            //cabecera no clickeable con el nombre de la marca
            Text(
                text = marca.nombre,
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
                text={Text(if (marca.activo) "Deshabilitar" else "Habilitar")},
                leadingIcon = {
                    Icon(
                        imageVector = if (marca.activo) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = if (marca.activo) "Deshabilitar" else "Habilitar"
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





//Pestana 2: Formulario de alta de marcas
@Composable
fun FormularioMarcaTab(
    onGuardarMarca: (Marca) -> Unit,
    marcaInicial: Marca? =null,
    onCancelarEdicion:()-> Unit

){
    //Si se esta editando, los campos inician con los datos de la marca seleccionada
    var nombre by remember(marcaInicial) { mutableStateOf(marcaInicial?.nombre ?: "") }
    var descripcion by remember(marcaInicial) { mutableStateOf(marcaInicial?.descripcion ?: "") }
    var estaActivo by remember(marcaInicial) { mutableStateOf(marcaInicial?.activo ?: true) }

    //los errores solo se muestran despues de que el usuario modifico el campo
    //(en modo edicion se muestran desde el inicio, para avisar si el dato guardado no es valido)
    var nombreTocado by remember(marcaInicial) { mutableStateOf(marcaInicial != null) }

    val esEdicion =marcaInicial!=null

    //reglas de validacion
    val errorNombre: String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length > LARGO_MAXIMO_NOMBRE_MARCA -> "Maximo $LARGO_MAXIMO_NOMBRE_MARCA caracteres"
        else -> null
    }
    val formularioValido = errorNombre == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text= if(esEdicion)"Modificar Marca (Codigo: ${marcaInicial?.id})" else "Registro en SQLite",
            style= MaterialTheme.typography.titleMedium,
            color= if(esEdicion) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        )
        //campo nombre
        CustomTextField(
            value=nombre,
            onValueChange = {
                nombre=it
                nombreTocado=true
            },
            label="Nombre",
            leadingIcon = Icons.Default.Sell,
            isError = nombreTocado && errorNombre != null,
            supportingText = if (nombreTocado) errorNombre else null
        )
        //campo descripcion (opcional)
        CustomTextField(
            value=descripcion,
            onValueChange = {descripcion=it},
            label="Descripcion (opcional)",
            leadingIcon = Icons.Default.Description
        )

        //control de seleccion: Switch para estado activo e inactivo
        //solo en edicion: al crear, la marca nueva siempre queda activa
        if (esEdicion){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("La marca esta activa?")
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
                val marcaResultante= Marca(
                    id= marcaInicial?.id?:0,
                    nombre=nombre.trim(),
                    //si no se carga descripcion se guarda "" (la columna es NOT NULL)
                    descripcion = descripcion.trim(),
                    //alta: siempre activa; edicion: lo que indique el Switch
                    activo = if (esEdicion) estaActivo else true
                )
                onGuardarMarca(marcaResultante)
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
