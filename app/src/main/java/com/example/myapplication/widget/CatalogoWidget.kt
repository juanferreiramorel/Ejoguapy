package com.example.myapplication.widget

import android.content.Context
import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.MarcaDao
import com.example.myapplication.data.ProductoDao
import com.example.myapplication.data.ProveedorDao
import com.example.myapplication.ui.theme.Pink40
import com.example.myapplication.ui.theme.Pink80
import com.example.myapplication.ui.theme.Purple40
import com.example.myapplication.ui.theme.Purple80
import com.example.myapplication.ui.theme.PurpleGrey40
import com.example.myapplication.ui.theme.PurpleGrey80
import com.example.myapplication.util.formatearGuaranies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//datos que muestra el widget, leidos de la base de datos en cada actualizacion
data class ResumenCatalogo(
    val productosActivos: Int,
    val productosInactivos: Int,
    val proveedoresActivos: Int,
    val marcasActivas: Int,
    val valorCatalogo: Double,
    val horaActualizacion: String
)

//colores de respaldo para Android 11 o menor (sin Material You): los mismos del tema de la app
private val coloresRespaldo = ColorProviders(
    light = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40),
    dark = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
)

class CatalogoWidget : GlanceAppWidget() {

    companion object {
        //tamanos de referencia: el launcher elige el mas grande que entra en el espacio disponible
        private val PEQUENO = DpSize(110.dp, 110.dp)   //2x2: logo + numero principal
        private val MEDIANO = DpSize(250.dp, 110.dp)   //3x2 / 4x2: encabezado + estadisticas
        private val GRANDE = DpSize(250.dp, 220.dp)    //3x3 o mas: tarjetas + boton
    }

    //Responsive: Glance dibuja una version por cada tamano y el launcher muestra la que corresponde
    override val sizeMode = SizeMode.Responsive(setOf(PEQUENO, MEDIANO, GRANDE))

    override suspend fun provideGlance(context: Context, id: GlanceId){
        //la base de datos se lee ANTES de provideContent y fuera del hilo principal
        val resumen = cargarResumen(context)

        provideContent {
            //Android 12+: colores dinamicos del fondo de pantalla (Material You), claro u oscuro
            //Android 11 o menor: colores del tema de la app
            val colores = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors else coloresRespaldo
            GlanceTheme(colors = colores) {
                ContenidoWidget(resumen)
            }
        }
    }

    //consulta los totales con los DAO; si algo falla devuelve null y el widget muestra "--"
    private suspend fun cargarResumen(context: Context): ResumenCatalogo? = withContext(Dispatchers.IO) {
        try {
            val productoDao = ProductoDao(context)
            val activos = productoDao.contarActivos()
            val total = productoDao.contarTodos()
            ResumenCatalogo(
                productosActivos = activos,
                productosInactivos = total - activos,
                proveedoresActivos = ProveedorDao(context).contarActivos(),
                marcasActivas = MarcaDao(context).contarActivos(),
                valorCatalogo = productoDao.sumarPrecioActivos(),
                horaActualizacion = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            )
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
private fun ContenidoWidget(resumen: ResumenCatalogo?) {
    val tamano = LocalSize.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            //cornerRadius solo tiene efecto en Android 12+; en versiones anteriores el launcher usa su forma
            .cornerRadius(20.dp)
            .padding(12.dp)
            //tocar cualquier parte del widget abre la app
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        when {
            tamano.width >= 250.dp && tamano.height >= 220.dp -> VersionGrande(resumen)
            tamano.width >= 250.dp -> VersionMediana(resumen)
            else -> VersionPequena(resumen)
        }
    }
}

//2x2: logo, cantidad de productos activos y su etiqueta
@Composable
private fun VersionPequena(resumen: ResumenCatalogo?) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Logo(tamano = 32)
        Spacer(modifier = GlanceModifier.height(6.dp))
        NumeroPrincipal(resumen, tamanoLetra = 32)
        Text(
            text = "Productos activos",
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

//3x2: encabezado, numero principal con 3 estadisticas al costado, valor y hora al pie
@Composable
private fun VersionMediana(resumen: ResumenCatalogo?) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Encabezado()
        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                NumeroPrincipal(resumen, tamanoLetra = 28)
                Etiqueta("Productos activos")
            }
            EstadisticaMini(resumen?.productosInactivos, "Inactivos")
            Spacer(modifier = GlanceModifier.width(10.dp))
            EstadisticaMini(resumen?.proveedoresActivos, "Proveed.")
            Spacer(modifier = GlanceModifier.width(10.dp))
            EstadisticaMini(resumen?.marcasActivas, "Marcas")
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Valor: ${resumen?.let { formatearGuaranies(it.valorCatalogo) } ?: "--"}",
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Etiqueta(textoActualizado(resumen))
        }
    }
}

//3x3 o mas: encabezado, numero y valor, tarjetas de estadisticas y boton para abrir la app
@Composable
private fun VersionGrande(resumen: ResumenCatalogo?) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Encabezado()
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                NumeroPrincipal(resumen, tamanoLetra = 34)
                Etiqueta("Productos activos")
            }
            Column(horizontalAlignment = Alignment.End) {
                Etiqueta("Valor del catalogo")
                Text(
                    text = resumen?.let { formatearGuaranies(it.valorCatalogo) } ?: "--",
                    maxLines = 1,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            TarjetaEstadistica(resumen?.productosInactivos, "Inactivos", GlanceModifier.defaultWeight())
            Spacer(modifier = GlanceModifier.width(6.dp))
            TarjetaEstadistica(resumen?.proveedoresActivos, "Proveedores", GlanceModifier.defaultWeight())
            Spacer(modifier = GlanceModifier.width(6.dp))
            TarjetaEstadistica(resumen?.marcasActivas, "Marcas", GlanceModifier.defaultWeight())
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = GlanceModifier.defaultWeight()) {
                Etiqueta(textoActualizado(resumen))
            }
            //enlace compacto: un FilledButton mide 48dp de alto y no entra en un widget 3x2
            Text(
                text = "Abrir catalogo ›",
                maxLines = 1,
                modifier = GlanceModifier
                    .cornerRadius(12.dp)
                    .background(GlanceTheme.colors.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

//fila superior: logo, titulo y boton de refrescar
@Composable
private fun Encabezado() {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Logo(tamano = 24)
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = "Catalogo",
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        )
        //vuelve a leer la base de datos sin abrir la app
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_actualizar),
            contentDescription = "Actualizar",
            onClick = actionRunCallback<ActualizarCatalogoAction>(),
            modifier = GlanceModifier.size(32.dp),
            backgroundColor = null,
            contentColor = GlanceTheme.colors.primary
        )
    }
}

//el logo de la app (el mismo png que usa el icono del launcher)
@Composable
private fun Logo(tamano: Int) {
    Image(
        provider = ImageProvider(R.drawable.app_logo),
        contentDescription = "Logo",
        modifier = GlanceModifier.size(tamano.dp)
    )
}

@Composable
private fun NumeroPrincipal(resumen: ResumenCatalogo?, tamanoLetra: Int) {
    Text(
        text = resumen?.productosActivos?.toString() ?: "--",
        maxLines = 1,
        style = TextStyle(
            color = GlanceTheme.colors.primary,
            fontSize = tamanoLetra.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        text = texto,
        maxLines = 1,
        style = TextStyle(
            color = GlanceTheme.colors.onSurfaceVariant,
            fontSize = 11.sp
        )
    )
}

//estadistica compacta (valor arriba, etiqueta abajo) para la version mediana
@Composable
private fun EstadisticaMini(valor: Int?, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor?.toString() ?: "--",
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Etiqueta(etiqueta)
    }
}

//estadistica dentro de una tarjeta redondeada para la version grande
@Composable
private fun TarjetaEstadistica(valor: Int?, etiqueta: String, modifier: GlanceModifier) {
    Column(
        modifier = modifier
            .background(GlanceTheme.colors.secondaryContainer)
            .cornerRadius(12.dp)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valor?.toString() ?: "--",
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSecondaryContainer,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = etiqueta,
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSecondaryContainer,
                fontSize = 11.sp
            )
        )
    }
}

private fun textoActualizado(resumen: ResumenCatalogo?): String =
    resumen?.let { "Actualizado ${it.horaActualizacion}" } ?: "Sin datos"
