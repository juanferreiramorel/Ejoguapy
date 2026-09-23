package com.example.myapplication

import com.example.myapplication.data.extraerNumeroRuc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

//pruebas del helper que quita el digito verificador y los separadores del RUC
class ExtraerNumeroRucTest {
    @Test
    fun conDigitoVerificador() {
        assertEquals("80009735", extraerNumeroRuc("80009735-1"))
    }

    @Test
    fun conPuntosYEspacios() {
        assertEquals("80009735", extraerNumeroRuc("80.009.735"))
        assertEquals("80009735", extraerNumeroRuc("  80 009 735  "))
    }

    @Test
    fun soloNumero() {
        assertEquals("4567890", extraerNumeroRuc("4567890"))
    }

    @Test
    fun textoInvalido() {
        assertNull(extraerNumeroRuc(""))
        assertNull(extraerNumeroRuc("   "))
        assertNull(extraerNumeroRuc("-1"))
        assertNull(extraerNumeroRuc("abc"))
        assertNull(extraerNumeroRuc("1234567890"))
    }
}
