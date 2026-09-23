package com.example.myapplication.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class CatalogoWidgetReceiver : GlanceAppWidgetReceiver() {
    //asocia el BroadcastReceiver con la clase visual del widget
    override val glanceAppWidget: GlanceAppWidget= CatalogoWidget()
}
