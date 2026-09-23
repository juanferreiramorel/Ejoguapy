package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

class PreferenciasManager (context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferencias", Context.MODE_PRIVATE)

    companion object{
        private const val KEY_RECORDAR_SESION = "recordar_sesion"
        private const val KEY_USUARIO_LOGUEADO = "usuario_logueado"
        private const val KEY_MODO_OSCURO = "modo_oscuro"
    }
    // guardar el estado de sesion
    fun guardarSesion(usuario: String, recordar: Boolean){
        sharedPreferences.edit().apply {
            putBoolean(KEY_RECORDAR_SESION, recordar)
            if(recordar){
                putString(KEY_USUARIO_LOGUEADO, usuario)
            }else{
                remove(KEY_USUARIO_LOGUEADO)
            }
            apply()
        }
    }
    //consultar el estado de sesion
    fun estaSesionGuardada(): Boolean{
        return sharedPreferences.getBoolean(KEY_RECORDAR_SESION, false)
    }
    fun obtenerUsuarioLogueado(): String{
        return sharedPreferences.getString(KEY_USUARIO_LOGUEADO, "")?:""

    }
    //cerrar sesion
    fun limpiarSesion(){
        sharedPreferences.edit().apply {
            putBoolean(KEY_RECORDAR_SESION, false)
            remove(KEY_USUARIO_LOGUEADO)
            apply()
        }
    }
    //configuracion general del modo oscuro
    fun guardarModoOscuro(activado: Boolean){
        sharedPreferences.edit().putBoolean(KEY_MODO_OSCURO, activado).apply()
    }
    fun obtenerModoOscuro(): Boolean{
        return sharedPreferences.getBoolean(KEY_MODO_OSCURO, false)
    }
}
