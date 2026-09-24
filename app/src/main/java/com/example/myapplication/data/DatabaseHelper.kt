package com.example.myapplication.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        const val DATABASE_NAME ="sistema_compras_db"
        const val DATABASE_VERSION = 6
        const val TABLA_PRODUCTOS = "productos"
        const val COL_ID = "id"
        const val COL_DESCRIPCION = "descripcion"
        //clave foranea que apunta al id de la tabla proveedores
        const val COL_PROVEEDOR_ID = "proveedor_id"
        //clave foranea que apunta al id de la tabla categorias
        const val COL_CATEGORIA_ID = "categoria_id"
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

        //tabla de categorias (clasificacion de los productos)
        const val TABLA_CATEGORIAS = "categorias"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NOMBRE = "nombre"
        const val COL_CAT_DESCRIPCION = "descripcion"
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
            $COL_PROV_RUC TEXT NOT NULL UNIQUE,
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

        //categorias tambien se crea antes que productos porque productos hace referencia a esta tabla
        //el nombre es UNIQUE: no puede haber dos categorias con el mismo nombre
        //COLLATE NOCASE hace que la comparacion ignore mayusculas ("Pinturas" y "pinturas" son iguales)
        val scriptCrearTablaCategorias= """
            CREATE TABLE $TABLA_CATEGORIAS(
            $COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_CAT_NOMBRE TEXT NOT NULL UNIQUE COLLATE NOCASE,
            $COL_CAT_DESCRIPCION TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(scriptCrearTablaCategorias)
        db.execSQL("INSERT INTO $TABLA_CATEGORIAS($COL_CAT_NOMBRE, $COL_CAT_DESCRIPCION) VALUES ('Herramientas','Herramientas manuales y electricas')")
        db.execSQL("INSERT INTO $TABLA_CATEGORIAS($COL_CAT_NOMBRE, $COL_CAT_DESCRIPCION) VALUES ('Pinturas','Pinturas, esmaltes, rodillos y pinceles')")
        db.execSQL("INSERT INTO $TABLA_CATEGORIAS($COL_CAT_NOMBRE, $COL_CAT_DESCRIPCION) VALUES ('Plomeria','Canos, conexiones, griferia y accesorios')")
        db.execSQL("INSERT INTO $TABLA_CATEGORIAS($COL_CAT_NOMBRE, $COL_CAT_DESCRIPCION) VALUES ('Electricidad','Cables, tomas, llaves e iluminacion')")

        val scriptCrearTabla= """
            CREATE TABLE $TABLA_PRODUCTOS(
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_DESCRIPCION TEXT NOT NULL,
            $COL_PROVEEDOR_ID INTEGER NOT NULL,
            $COL_CATEGORIA_ID INTEGER NOT NULL,
            $COL_PRECIO REAL NOT NULL,
            $COL_ACTIVO INTEGER NOT NULL,
            FOREIGN KEY($COL_PROVEEDOR_ID) REFERENCES $TABLA_PROVEEDORES($COL_PROV_ID),
            FOREIGN KEY($COL_CATEGORIA_ID) REFERENCES $TABLA_CATEGORIAS($COL_CAT_ID)
            )
        """.trimIndent()
        db.execSQL(scriptCrearTabla)
        //el id del proveedor se busca por su razon social y el de la categoria por su nombre, con subconsultas
        db.execSQL("INSERT INTO $TABLA_PRODUCTOS($COL_DESCRIPCION,$COL_PROVEEDOR_ID, $COL_CATEGORIA_ID, $COL_PRECIO, $COL_ACTIVO) VALUES ('Taladro percutor 650W',(SELECT $COL_PROV_ID FROM $TABLA_PROVEEDORES WHERE $COL_PROV_RAZON_SOCIAL='Tupi S.A.'),(SELECT $COL_CAT_ID FROM $TABLA_CATEGORIAS WHERE $COL_CAT_NOMBRE='Herramientas'),450000,1)")
        db.execSQL("INSERT INTO $TABLA_PRODUCTOS($COL_DESCRIPCION,$COL_PROVEEDOR_ID, $COL_CATEGORIA_ID, $COL_PRECIO, $COL_ACTIVO) VALUES ('Pintura latex blanca 18L',(SELECT $COL_PROV_ID FROM $TABLA_PROVEEDORES WHERE $COL_PROV_RAZON_SOCIAL='Konext S.A.'),(SELECT $COL_CAT_ID FROM $TABLA_CATEGORIAS WHERE $COL_CAT_NOMBRE='Pinturas'),380000,1)")
        db.execSQL("INSERT INTO $TABLA_PRODUCTOS($COL_DESCRIPCION,$COL_PROVEEDOR_ID, $COL_CATEGORIA_ID, $COL_PRECIO, $COL_ACTIVO) VALUES ('Cano PVC 1/2 pulgada 6m',(SELECT $COL_PROV_ID FROM $TABLA_PROVEEDORES WHERE $COL_PROV_RAZON_SOCIAL='Compulandia S.A.'),(SELECT $COL_CAT_ID FROM $TABLA_CATEGORIAS WHERE $COL_CAT_NOMBRE='Plomeria'),25000,1)")
        db.execSQL("INSERT INTO $TABLA_PRODUCTOS($COL_DESCRIPCION,$COL_PROVEEDOR_ID, $COL_CATEGORIA_ID, $COL_PRECIO, $COL_ACTIVO) VALUES ('Cable unipolar 2mm rollo 100m',(SELECT $COL_PROV_ID FROM $TABLA_PROVEEDORES WHERE $COL_PROV_RAZON_SOCIAL='Nissei S.A.'),(SELECT $COL_CAT_ID FROM $TABLA_CATEGORIAS WHERE $COL_CAT_NOMBRE='Electricidad'),320000,1)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //primero se borra productos porque hace referencia a categorias y proveedores
        //tambien se borra la tabla vieja de marcas si existe (versiones anteriores de la base)
        db.execSQL("DROP TABLE IF EXISTS $TABLA_PRODUCTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_CATEGORIAS")
        db.execSQL("DROP TABLE IF EXISTS marcas")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_PROVEEDORES")
        onCreate(db)
    }
}
