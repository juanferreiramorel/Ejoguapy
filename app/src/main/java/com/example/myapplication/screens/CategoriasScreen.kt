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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.myapplication.data.CategoriaDao

//modelo simple de datos para representar en la interfaz (id = codigo de la categoria)
//las categorias no tienen estado (activo/inactivo), solo codigo, nombre y descripcion
data class Categoria(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

//largo maximo permitido para el nombre de la categoria
private const val LARGO_MAXIMO_NOMBRE_CATEGORIA = 50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(modifier: Modifier= Modifier){
    val context= LocalContext.current
    val dao = remember { CategoriaDao(context) }
    //Estado para controlar la pestana seleccionada (0: Listado, 1: Formulario de Alta)
    var selectedTabIndex by remember { mutableStateOf(0) }
    val pestanas = listOf("Listado", "Nuevo Registro")

    //Snackbar propio de la pantalla (reemplaza a los Toast)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    //orden seleccionado en el menu del listado: null (sin orden), "ASC" o "DESC"
    var ordenSeleccionado by remember { mutableStateOf<String?>(null) }

    //Lista en memoria de categorias
    val categoriasRegistradas = remember {
        mutableStateListOf<Categoria>()
    }
    //funcion auxiliar para refrescar la lista desde la base de datos (respetando el orden elegido)
    fun recargarCategoriasDesdeDb(){
        val desdeDb = dao.listarTodos()
        val ordenadas = when (ordenSeleccionado){
            "ASC" -> desdeDb.sortedBy { it.nombre.lowercase() }
            "DESC" -> desdeDb.sortedByDescending { it.nombre.lowercase() }
            else -> desdeDb
        }
        categoriasRegistradas.clear()
        categoriasRegistradas.addAll(ordenadas)
    }
    LaunchedEffect(Unit){
        recargarCategoriasDesdeDb()
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

    //estado para la categoria seleccionada a eliminar
    var categoriaAEliminar by remember { mutableStateOf<Categoria?>(null) }
    var categoriaAEditar by remember { mutableStateOf<Categoria?>(null) }
    //categoria que no se puede eliminar por tener productos asociados (categoria, cantidad de productos)
    var categoriaConProductos by remember { mutableStateOf<Pair<Categoria, Int>?>(null) }

    //antes de pedir confirmacion se verifica si la categoria tiene productos asociados
    fun solicitarEliminacion(categoria: Categoria){
        val cantidadProductos = dao.contarProductos(categoria.id)
        if (cantidadProductos>0){
            categoriaConProductos = categoria to cantidadProductos
        }else{
            categoriaAEliminar = categoria
        }
    }

    //dialogo: no se puede eliminar porque tiene productos asociados
    categoriaConProductos?.let { (categoria, cantidadProductos) ->
        AlertDialog(
            onDismissRequest = {categoriaConProductos=null},
            title = {Text("No se puede eliminar")},
            text= {
                Text("La categoria '${categoria.nombre}' tiene $cantidadProductos productos asociados. Reasigne esos productos a otra categoria antes de eliminarla.")
            },
            confirmButton = {
                TextButton(onClick = {categoriaConProductos=null}) {
                    Text("Entendido")
                }
            }
        )
    }

    //dialogo de confirmacion
    categoriaAEliminar?.let { categoria ->
        AlertDialog(
            onDismissRequest = {categoriaAEliminar=null},
            title = {Text("Confirmar Eliminacion")},
            text= {Text("¿Desea eliminar permanentemente la categoria '${categoria.nombre}'?")},
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val filas = dao.eliminar(categoria.id)
                            if (filas>0){
                                recargarCategoriasDesdeDb()
                                actualizarWidgetCatalogo(context)
                                //se ofrece deshacer: se vuelve a insertar con el mismo codigo
                                mostrarMensajeConDeshacer("Categoria eliminada"){
                                    if (dao.restaurar(categoria) != -1L){
                                        recargarCategoriasDesdeDb()
                                        actualizarWidgetCatalogo(context)
                                    }else{
                                        mostrarMensaje("No se pudo restaurar la categoria")
                                    }
                                }
                            }else{
                                mostrarMensaje("Error al eliminar el registro")
                            }
                        } catch (e: SQLiteConstraintException){
                            //por seguridad: la clave foranea impide borrar si quedo algun producto asociado
                            categoriaConProductos = categoria to dao.contarProductos(categoria.id)
                        }
                        categoriaAEliminar=null
                    }
                ) {
                    Text("Eliminar", color= MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {categoriaAEliminar=null}) {
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
                            categoriaAEditar=null
                        }

                            selectedTabIndex = index
                                },
                        text = {Text(titulo)}
                    )
                }
            }
            //renderizado condicional segun la pestana activa
            when(selectedTabIndex){
                0-> ListadoCategoriasTab(
                    categorias = categoriasRegistradas,
                    ordenSeleccionado = ordenSeleccionado,
                    onOrdenarPorNombreAsc = {
                        ordenSeleccionado = "ASC"
                        val ordenadas = categoriasRegistradas.sortedBy { it.nombre.lowercase() }
                        categoriasRegistradas.clear()
                        categoriasRegistradas.addAll(ordenadas)
                    },
                    onOrdenarPorNombreDesc = {
                        ordenSeleccionado = "DESC"
                        val ordenadas = categoriasRegistradas.sortedByDescending { it.nombre.lowercase() }
                        categoriasRegistradas.clear()
                        categoriasRegistradas.addAll(ordenadas)
                    },
                    onSolicitarEliminar = {categoria-> solicitarEliminacion(categoria)},
                    onEditar = {categoria ->
                        categoriaAEditar=categoria
                        selectedTabIndex=1
                    },
                    onRegistrarPrimero = {
                        categoriaAEditar=null
                        selectedTabIndex=1
                    }
                )
                1 -> FormularioCategoriaTab(
                    categoriaInicial = categoriaAEditar,
                    onGuardarCategoria = {cat ->
                        try {
                            if (categoriaAEditar==null){
                                //1-Insercion
                                val idGenerado=dao.insertar(cat)
                                if(idGenerado!=-1L){
                                    actualizarWidgetCatalogo(context)
                                    mostrarMensaje("Categoria guardada (Codigo: $idGenerado)")
                                }else{
                                    mostrarMensaje("Error al guardar la categoria")
                                }
                            }else{
                                //2-Actualizacion
                                val filas=dao.actualizar(cat)
                                if (filas>0){
                                    actualizarWidgetCatalogo(context)
                                    mostrarMensaje("Registro actualizado en base de datos")
                                }else{
                                    mostrarMensaje("Error al actualizar el registro")
                                }
                            }
                            categoriaAEditar=null
                            recargarCategoriasDesdeDb()
                            selectedTabIndex=0
                        } catch (e: SQLiteConstraintException){
                            //el nombre es UNIQUE (sin distinguir mayusculas): se queda en el formulario para corregirlo
                            mostrarMensaje("Ya existe una categoria con ese nombre")
                        }
                    },
                    onCancelarEdicion = {
                        categoriaAEditar=null
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


//Pestana 1: Listado de categorias
@Composable
fun ListadoCategoriasTab(
    categorias: List<Categoria>,
    ordenSeleccionado: String?,
    onOrdenarPorNombreAsc: () -> Unit,
    onOrdenarPorNombreDesc: () -> Unit,
    onSolicitarEliminar: (Categoria) -> Unit,
    onEditar: (Categoria) -> Unit,
    onRegistrarPrimero: () -> Unit
){
    var menuOpcionesGeneralExpandido by remember { mutableStateOf(false) }
    //texto de busqueda: filtra por nombre o descripcion (sin distinguir mayusculas)
    var textoBusqueda by remember { mutableStateOf("") }
    val filtrando = textoBusqueda.isNotBlank()
    val categoriasFiltradas = if (filtrando) {
        val buscado = textoBusqueda.trim()
        categorias.filter {
            it.nombre.contains(buscado, ignoreCase = true) ||
                    it.descripcion.contains(buscado, ignoreCase = true)
        }
    } else categorias

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        //cabecera con menu basico
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text= if (filtrando) "Categorias (${categoriasFiltradas.size}/${categorias.size} registros)"
                      else "Categorias (${categorias.size} registros)",
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
        //campo de busqueda (solo si hay categorias cargadas)
        if (categorias.isNotEmpty()){
            CampoBusqueda(
                texto = textoBusqueda,
                onTextoChange = {textoBusqueda=it},
                placeholder = "Buscar por nombre o descripcion"
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if(categorias.isEmpty()){
            //estado vacio: no hay nada registrado
            EstadoVacio(
                icono = Icons.Default.Category,
                mensaje = "No hay categorias registradas aun",
                textoBoton = "Registrar la primera",
                onAccion = onRegistrarPrimero
            )
        } else if (categoriasFiltradas.isEmpty()){
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
                items(categoriasFiltradas, key={it.id}){cat->
                    CategoriaItemContextual(
                        categoria = cat,
                        onEditar={onEditar(cat)},
                        onEliminar = {onSolicitarEliminar(cat)}
                    )
                }
            }
        }
    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoriaItemContextual(
    categoria: Categoria,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
){
    var menuContextualExpandido by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {/*Clic normal*/},
                onLongClick = {
                    //vibracion corta y despliega el menu contextual al mantener presionado
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuContextualExpandido=true
                }
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //avatar circular con las iniciales del nombre de la categoria
                AvatarCircular(iniciales = obtenerIniciales(categoria.nombre))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text="${categoria.id}. ${categoria.nombre}",
                        style= MaterialTheme.typography.titleMedium
                    )
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
                            text= categoria.descripcion.ifBlank { "Sin descripcion" },
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
            //cabecera no clickeable con el nombre de la categoria
            Text(
                text = categoria.nombre,
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





//Pestana 2: Formulario de alta de categorias
@Composable
fun FormularioCategoriaTab(
    onGuardarCategoria: (Categoria) -> Unit,
    categoriaInicial: Categoria? =null,
    onCancelarEdicion:()-> Unit

){
    //Si se esta editando, los campos inician con los datos de la categoria seleccionada
    var nombre by remember(categoriaInicial) { mutableStateOf(categoriaInicial?.nombre ?: "") }
    var descripcion by remember(categoriaInicial) { mutableStateOf(categoriaInicial?.descripcion ?: "") }

    //los errores solo se muestran despues de que el usuario modifico el campo
    //(en modo edicion se muestran desde el inicio, para avisar si el dato guardado no es valido)
    var nombreTocado by remember(categoriaInicial) { mutableStateOf(categoriaInicial != null) }

    val esEdicion =categoriaInicial!=null

    //reglas de validacion
    val errorNombre: String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length > LARGO_MAXIMO_NOMBRE_CATEGORIA -> "Maximo $LARGO_MAXIMO_NOMBRE_CATEGORIA caracteres"
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
            text= if(esEdicion)"Modificar Categoria (Codigo: ${categoriaInicial?.id})" else "Registro en SQLite",
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
            leadingIcon = Icons.Default.Category,
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
        Spacer(modifier = Modifier.height(8.dp))

        //Boton de confirmacion (deshabilitado mientras el formulario no sea valido)
        Button(
            onClick = {
                val categoriaResultante= Categoria(
                    id= categoriaInicial?.id?:0,
                    nombre=nombre.trim(),
                    //si no se carga descripcion se guarda "" (la columna es NOT NULL)
                    descripcion = descripcion.trim()
                )
                onGuardarCategoria(categoriaResultante)
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
