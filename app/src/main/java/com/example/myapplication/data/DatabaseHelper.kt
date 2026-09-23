package com.example.myapplication.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        const val DATABASE_NAME ="sistema_compras_db"
        const val DATABASE_VERSION = 3
        const val TABLA_PRODUCTOS = "productos"
        const val COL_ID = "id"
        const val COL_DESCRIPCION = "descripcion"
        //clave foranea que apunta al id de la tabla proveedores
        const val COL_PROVEEDOR_ID = "proveedor_id"
        const val COL_PRECIO = "precio"
        const val COL_ACTIVO = "activo"

        //tabla de proveedores
        const val TABLA_PROVEEDORES = "proveedores"
        const val COL_PROV_ID = "id"
        const val COL_PROV_RAZON_SOCIAL = "razon_social"
        const val COL_PROV_RUC = "ruc"
        const val COL_PROV_DIRECCION = "direccion"
        const val COL_PROV_TELEFONO = "telefono"
        const val COL_PROV_CORREO = "correo"
        const val COL_PROV_CONTACTO = "contacto"
        const val COL_PROV_ACTIVO = "activo"
    }

    //SQLite no controla las claves foraneas por defecto, hay que activarlas en cada conexion
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        //primero se crea proveedores porque productos hace referencia a esta tabla
        val scriptCrearTablaProveedores= """
            CREATE TABLE $TABLA_PROVEEDORES(
            $COL_PROV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_PROV_RAZON_SOCIAL TEXT NOT NULL,
            $COL_PROV_RUC TEXT NOT NULL,
            $COL_PROV_DIRECCION TEXT NOT NULL,
            $COL_PROV_TELEFONO TEXT NOT NULL,
            $COL_PROV_CORREO TEXT NOT NULL,
            $COL_PROV_CONTACTO TEXT NOT NULL,
            $COL_PROV_ACTIVO INTEGER NOT NULL

            )
        """.trimIndent()
        db.execSQL(scriptCrearTablaProveedores)
        db.execSQL("INSERT INTO $TABLA_PROVEEDORES($COL_PROV_RAZON_SOCIAL,$COL_PROV_RUC, $COL_PROV_DIRECCION, $COL_PROV_TELEFONO, $COL_PROV_CORREO, $COL_PROV_CONTACTO, $COL_PROV_ACTIVO) VALUES ('Tupi S.A.','80012345-6','Av. Mcal. Lopez 1234, Asuncion','021 612 345','ventas@tupi.com.py','Carlos Benitez',1)")
        db.execSQL("INSERT INTO $TABLA_PROVEEDORES($COL_PROV_RAZON_SOCIAL,$COL_PROV_RUC, $COL_PROV_DIRECCION, $COL_PROV_TELEFONO, $COL_PROV_CORREO, $COL_PROV_CONTACTO, $COL_PROV_ACTIVO) VALUES ('Konext S.A.','80067890-1','Av. Espana 567, Asuncion','021 228 900','compras@konext.com.py','Maria Gonzalez',1)")
        db.execSQL("INSERT INTO $TABLA_PROVEEDORES($COL_PROV_RAZON_SOCIAL,$COL_PROV_RUC, $COL_PROV_DIRECCION, $COL_PROV_TELEFONO, $COL_PROV_CORREO, $COL_PROV_CONTACTO, $COL_PROV_ACTIVO) VALUES ('Compulandia S.A.','80023456-7','Av. Eusebio Ayala 2100, Asuncion','021 555 100','ventas@compulandia.com.py','Jorge Martinez',1)")
        db.execSQL("INSERT INTO $TABLA_PROVEEDORES($COL_PROV_RAZON_SOCIAL,$COL_PROV_RUC, $COL_PROV_DIRECCION, $COL_PROV_TELEFONO, $COL_PROV_CORREO, $COL_PROV_CONTACTO, $COL_PROV_ACTIVO) VALUES ('Nissei S.A.','80034567-8','Av. Mcal. Lopez 3794, Asuncion','021 618 1000','compras@nissei.com.py','Ana Rodriguez',1)")

        val scriptCrearTabla= """
            CREATE TABLE $TABLA_PRODUCTOS(
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_DESCRIPCION TEXT NOT NULL,
            $COL_PROVEEDOR_ID INTEGER NOT NULL,
            $COL_PRECIO REAL NOT NULL,
            $COL_ACTIVO INTEGER NOT NULL,
            FOREIGN KEY($COL_PROVEEDOR_ID) REFERENCES $TABLA_PROVEEDORES($COL_PROV_ID)
            )
        """.trimIndent()
        db.execSQL(scriptCrearTabla)
        //el id del proveedor se busca por su razon social con una subconsulta
        db.execSQL("INSERT INTO $TABLA_PRODUCTOS($COL_DESCRIPCION,$COL_PROVEEDOR_ID, $COL_PRECIO, $COL_ACTIVO) VALUES ('Notebook HP',(SELECT $COL_PROV_ID FROM $TABLA_PROVEEDORES WHERE $COL_PROV_RAZON_SOCIAL='Compulandia S.A.'),4500000,1)")
        db.execSQL("INSERT INTO $TABLA_PRODUCTOS($COL_DESCRIPCION,$COL_PROVEEDOR_ID, $COL_PRECIO, $COL_ACTIVO) VALUES ('Teclado Redragon',(SELECT $COL_PROV_ID FROM $TABLA_PROVEEDORES WHERE $COL_PROV_RAZON_SOCIAL='Nissei S.A.'),800000,1)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_PRODUCTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_PROVEEDORES")
        onCreate(db)
    }
}
