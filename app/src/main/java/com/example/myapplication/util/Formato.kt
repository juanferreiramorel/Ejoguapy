package com.example.myapplication.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

//formatea un monto en guaranies con separador de miles '.' (ej: 4500000.0 -> "Gs. 4.500.000")
//se definen los simbolos a mano para no depender del Locale del dispositivo
fun formatearGuaranies(monto: Double): String {
    val simbolos = DecimalFormatSymbols().apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formato = DecimalFormat("#,##0", simbolos)
    return "Gs. ${formato.format(monto)}"
}
