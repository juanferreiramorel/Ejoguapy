package com.example.myapplication.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

//scope propio para refrescar el widget sin depender de la pantalla que lo pidio
//(SupervisorJob: si una actualizacion falla no cancela las siguientes)
private val scopeWidget = CoroutineScope(SupervisorJob() + Dispatchers.Default)

//vuelve a dibujar todas las instancias del widget del catalogo con los datos actuales de la base
//se llama despues de cada insercion, actualizacion, eliminacion o restauracion en las pantallas
fun actualizarWidgetCatalogo(context: Context) {
    //se usa el applicationContext para no retener la Activity mientras corre la corrutina
    val contextoApp = context.applicationContext
    scopeWidget.launch {
        try {
            CatalogoWidget().updateAll(contextoApp)
        } catch (e: Exception) {
            //si el widget no esta agregado o falla el dibujo, la app sigue funcionando igual
        }
    }
}

//accion del boton de refrescar del widget: no necesita registrarse en el manifest,
//Glance ya declara su propio receptor para ejecutar los ActionCallback
class ActualizarCatalogoAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        //provideGlance vuelve a leer la base de datos y recalcula la hora de actualizacion
        CatalogoWidget().update(context, glanceId)
    }
}
