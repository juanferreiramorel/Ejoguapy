package com.example.myapplication.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.components.AvatarCircular
import com.example.myapplication.components.obtenerIniciales
import kotlinx.coroutines.launch
import java.text.Normalizer

//Modelo de cada opcion del menu: nombre, icono y si ya tiene pantalla propia
data class ModuloMenu(
    val nombre: String,
    val icono: ImageVector,
    val implementado: Boolean
)

//Seccion del menu: titulo (null = sin titulo) y sus modulos
data class SeccionMenu(
    val titulo: String?,
    val modulos: List<ModuloMenu>
)

//Fila que se dibuja en la lista del drawer: un titulo de seccion o un modulo
sealed class FilaMenu {
    data class Titulo(val texto: String) : FilaMenu()
    data class Item(val modulo: ModuloMenu) : FilaMenu()
}

//Secciones del menu SEGUN LO DEFINIDO EN LA ACTIVIDAD 1 (agrupadas)
val seccionesMenu = listOf(
    SeccionMenu(
        titulo = null,
        modulos = listOf(ModuloMenu("Inicio", Icons.Filled.Home, implementado = false))
    ),
    SeccionMenu(
        titulo = "Catalogo",
        modulos = listOf(
            ModuloMenu("Productos", Icons.Filled.Inventory2, implementado = true),
            ModuloMenu("Proveedores", Icons.Filled.Business, implementado = true),
            ModuloMenu("Marcas", Icons.Filled.Sell, implementado = true)
        )
    ),
    SeccionMenu(
        titulo = "Operaciones",
        modulos = listOf(
            ModuloMenu("Pedidos de Compra", Icons.Filled.ShoppingCart, implementado = false),
            ModuloMenu("Presupuestos", Icons.Filled.RequestQuote, implementado = false)
        )
    ),
    SeccionMenu(
        titulo = "Sistema",
        modulos = listOf(
            ModuloMenu("Usuarios", Icons.Filled.People, implementado = false),
            ModuloMenu("Ajustes", Icons.Filled.Settings, implementado = true)
        )
    )
)

//quita tildes y pasa a minusculas para buscar sin importar acentos (ej: "Catálogo" -> "catalogo")
fun normalizarTexto(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()

//arma las filas del drawer filtrando por el texto buscado
//solo se muestran los titulos de las secciones que tienen coincidencias
fun construirFilas(busqueda: String): List<FilaMenu> {
    val filtro = normalizarTexto(busqueda.trim())
    val filas = mutableListOf<FilaMenu>()
    seccionesMenu.forEach { seccion ->
        val coincidencias = seccion.modulos.filter {
            filtro.isEmpty() || normalizarTexto(it.nombre).contains(filtro)
        }
        if (coincidencias.isNotEmpty()) {
            if (seccion.titulo != null) filas.add(FilaMenu.Titulo(seccion.titulo))
            coincidencias.forEach { filas.add(FilaMenu.Item(it)) }
        }
    }
    return filas
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDrawerScreen(
    usuario: String,
    onCerrarSesion: () -> Unit,
    modoOscuro: Boolean = false,
    onCambiarModoOscuro: (Boolean) -> Unit = {}
) {
    //Estado de panel lateral y cortina para animaciones
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    //rememberSaveable: el modulo y la busqueda sobreviven a la rotacion de pantalla
    var moduloSeleccionado by rememberSaveable { mutableStateOf("Inicio") }
    var busqueda by rememberSaveable { mutableStateOf("") }
    var mostrarDialogoSalir by rememberSaveable { mutableStateOf(false) }

    //estado de la lista del drawer para poder hacer scroll por codigo
    val listState = rememberLazyListState()
    val filas = construirFilas(busqueda)

    //version de la app leida del PackageManager (si falla se muestra 1.0)
    val versionApp = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    //cuando el drawer se abre, hace scroll hasta el modulo seleccionado
    //cuando se cierra, limpia la busqueda
    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.isOpen) {
            val indice = filas.indexOfFirst {
                it is FilaMenu.Item && it.modulo.nombre == moduloSeleccionado
            }
            if (indice >= 0) listState.animateScrollToItem(indice)
        } else {
            busqueda = ""
            focusManager.clearFocus()
        }
    }

    //boton atras: cierra el drawer, o vuelve a Inicio; si ya esta en Inicio lo maneja el sistema
    BackHandler(enabled = drawerState.isOpen || moduloSeleccionado != "Inicio") {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            moduloSeleccionado = "Inicio"
        }
    }

    //dialogo de confirmacion antes de cerrar sesion
    if (mostrarDialogoSalir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSalir = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null
                )
            },
            title = { Text(text = "¿Desea cerrar sesion?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoSalir = false
                    onCerrarSesion()
                }) {
                    Text(
                        text = "Cerrar Sesion",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSalir = false }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet() {
                //Encabezado con fondo de color, avatar e informacion del operador
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Aplicacion de compras",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircular(
                                iniciales = obtenerIniciales(usuario),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = usuario,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Operador",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                //Buscador de modulos
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text(text = "Buscar modulo") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Limpiar busqueda"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )

                //Lista de modulos agrupados por seccion (con scroll)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (filas.isEmpty()) {
                        item {
                            Text(
                                text = "Sin resultados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                            )
                        }
                    }
                    items(filas) { fila ->
                        when (fila) {
                            is FilaMenu.Titulo -> {
                                Text(
                                    text = fila.texto,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        start = 28.dp, end = 28.dp, top = 16.dp, bottom = 8.dp
                                    )
                                )
                            }
                            is FilaMenu.Item -> {
                                val modulo = fila.modulo
                                NavigationDrawerItem(
                                    icon = {
                                        Icon(imageVector = modulo.icono, contentDescription = null)
                                    },
                                    label = { Text(text = modulo.nombre) },
                                    badge = {
                                        if (!modulo.implementado) {
                                            Text(
                                                text = "Próximamente",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    selected = modulo.nombre == moduloSeleccionado,
                                    onClick = {
                                        moduloSeleccionado = modulo.nombre
                                        busqueda = ""
                                        focusManager.clearFocus()
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }
                        }
                    }
                }

                //Cerrar sesion al final del drawer
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = "Cerrar Sesion") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mostrarDialogoSalir = true
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.error,
                        unselectedTextColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                //Pie con la version de la app
                Text(
                    text = "Version $versionApp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = moduloSeleccionado,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir Menu"
                            )
                        }
                    },
                    actions = {
                        //tambien pasa por el dialogo de confirmacion
                        IconButton(onClick = { mostrarDialogoSalir = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar Sesion"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        ) { innerPadding ->
            ContenidoModuloActual(
                modulo = moduloSeleccionado,
                modifier = Modifier.padding(innerPadding),
                modoOscuro = modoOscuro,
                onCambiarModoOscuro = onCambiarModoOscuro
            )
        }
    }
}
//Contenedor de contenido segun el modulo seleccionado
@Composable
fun ContenidoModuloActual(
    modulo: String,
    modifier: Modifier= Modifier,
    modoOscuro: Boolean = false,
    onCambiarModoOscuro: (Boolean) -> Unit = {}
){
    when(modulo){
        "Productos" ->{
            ProductosScreen(modifier = modifier)
        }
        "Proveedores" ->{
            ProveedoresScreen(modifier = modifier)
        }
        "Marcas" ->{
            MarcasScreen(modifier = modifier)
        }
        "Ajustes" ->{
            AjustesScreen(
                modifier = modifier,
                modoOscuro = modoOscuro,
                onCambiarModoOscuro = onCambiarModoOscuro
            )
        }
        else -> {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Area de Trabajo",
                style =  MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aqui se integra la interfaz de: $modulo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        }
    }

}
