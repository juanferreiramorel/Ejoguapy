package com.example.myapplication.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver


class CatalogoWidgetReceiver : GlanceAppWidgetReceiver(){
    override val glanceAppWidget: GlanceAppWidget = CatalogoWidget()
}