package com.example.myapplication.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.myapplication.MainActivity

class CatalogoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId){
        // en fases posteriores se leera directamente de la base de datos
        val totalProductos = 5

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E2E))
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Catalogo de productos",
                        style= TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "$totalProductos",
                        style= TextStyle(
                            color = ColorProvider(Color(0xFF64B5F6)),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Productos activos",
                        style= TextStyle(
                            color = ColorProvider(Color.LightGray),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
