package com.example.myapplication.integrations

import com.example.myapplication.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

//resultado posible de una consulta de RUC a la API "people"
sealed class ResultadoRuc {
    //la API devolvio los datos del contribuyente
    data class Encontrado(
        val ruc: String,
        val razonSocial: String,
        val dv: String?,
        val estado: String,
        val tipo: String
    ) : ResultadoRuc()

    //la API respondio 404: el RUC no existe en el padron
    object NoEncontrado : ResultadoRuc()

    //problema de red, timeout o respuesta inesperada
    data class Error(val mensaje: String) : ResultadoRuc()
}

//tiempo maximo de espera para conectar y para leer (en milisegundos)
private const val TIEMPO_ESPERA_MS = 5000

//obtiene solo el numero del RUC (sin digito verificador, puntos ni espacios)
//ej: "80009735-1" -> "80009735", "80.009.735" -> "80009735"
//devuelve null si el texto no es un numero de RUC valido
fun extraerNumeroRuc(texto: String): String? {
    var limpio = texto.trim()
    //si tiene guion, lo que sigue es el digito verificador y se descarta
    if (limpio.contains('-')) {
        limpio = limpio.substringBefore('-')
    }
    //se quitan puntos, espacios y cualquier otro caracter que no sea digito
    limpio = limpio.filter { !it.isWhitespace() && it != '.' }
    if (limpio.isEmpty() || !limpio.all { it.isDigit() } || limpio.length > 9) {
        return null
    }
    return limpio
}

//consulta la API "people" con el RUC ingresado (se ejecuta en un hilo de entrada/salida)
suspend fun consultarRuc(texto: String): ResultadoRuc = withContext(Dispatchers.IO) {
    val numero = extraerNumeroRuc(texto) ?: return@withContext ResultadoRuc.Error("RUC invalido")
    var conexion: HttpURLConnection? = null
    try {
        val direccion = "${BuildConfig.PEOPLE_API_URL.trimEnd('/')}/?ruc=${URLEncoder.encode(numero, "UTF-8")}"
        conexion = URL(direccion).openConnection() as HttpURLConnection
        conexion.requestMethod = "GET"
        conexion.connectTimeout = TIEMPO_ESPERA_MS
        conexion.readTimeout = TIEMPO_ESPERA_MS
        conexion.setRequestProperty("Accept", "application/json")
        //el token solo se envia si esta configurado (people.api.token en local.properties; JWT en produccion)
        val token = BuildConfig.PEOPLE_API_TOKEN.trim()
        if (token.isNotBlank()) {
            conexion.setRequestProperty("Authorization", "Bearer $token")
        }

        when (val codigo = conexion.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val cuerpo = conexion.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(cuerpo)
                //optString devuelve "null" como texto si el valor es null, por eso se controla aparte
                fun texto(campo: String): String =
                    if (json.isNull(campo)) "" else json.optString(campo, "").trim()
                ResultadoRuc.Encontrado(
                    ruc = texto("ruc").ifBlank { numero },
                    razonSocial = texto("razonsocial"),
                    dv = texto("dv").ifBlank { null },
                    estado = texto("estado"),
                    tipo = texto("tipo")
                )
            }
            HttpURLConnection.HTTP_NOT_FOUND -> ResultadoRuc.NoEncontrado
            //401: falta el token o no es valido (se configura en local.properties)
            HttpURLConnection.HTTP_UNAUTHORIZED -> ResultadoRuc.Error("sin autorizacion: revise el token de la API")
            //503: la API no tiene configurado el origen de datos del RUC
            HttpURLConnection.HTTP_UNAVAILABLE -> ResultadoRuc.Error("servicio de RUC sin configurar")
            else -> ResultadoRuc.Error("HTTP $codigo")
        }
    } catch (e: Exception) {
        ResultadoRuc.Error(e.message ?: e.javaClass.simpleName)
    } finally {
        conexion?.disconnect()
    }
}
