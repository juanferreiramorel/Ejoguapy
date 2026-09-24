package com.example.myapplication.data

import android.content.ContentValues
import android.content.Context
import com.example.myapplication.screens.Marca

class MarcaDao (context: Context) {
    private val dbHelper= DatabaseHelper (context)

    // 1-Inserción
    //el nombre es UNIQUE: si ya existe una marca con ese nombre se lanza SQLiteConstraintException
    //(se usa insertOrThrow para que la pantalla pueda avisar "Ya existe una marca con ese nombre")
    fun insertar(marca: Marca): Long {
        val db=dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_MARC_NOMBRE,marca.nombre)
            put(DatabaseHelper.COL_MARC_DESCRIPCION,marca.descripcion)
            put(DatabaseHelper.COL_MARC_ACTIVO,if (marca.activo) 1 else 0)
        }
        try {
            //devolver el id generado por autoincrement
            return db.insertOrThrow(DatabaseHelper.TABLA_MARCAS, null, valores)
        } finally {
            db.close()
        }
    }

    //2- Consulta y recuperacion de registros
    fun listarTodos():List<Marca>{
        val lista =mutableListOf<Marca>()
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLA_MARCAS} ORDER BY ${DatabaseHelper.COL_MARC_ID} DESC", null
        )
        if (cursor.moveToFirst()){
            do{
                val id= cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MARC_ID))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MARC_NOMBRE))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MARC_DESCRIPCION))
                val activo= cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MARC_ACTIVO)) == 1
                lista.add(Marca(id, nombre, descripcion, activo))

            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }


    //3- Actualización de registros
    //si el nuevo nombre ya pertenece a otra marca se lanza SQLiteConstraintException
    fun actualizar(marca: Marca):Int{
        val db =dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_MARC_NOMBRE,marca.nombre)
            put(DatabaseHelper.COL_MARC_DESCRIPCION,marca.descripcion)
            put(DatabaseHelper.COL_MARC_ACTIVO,if (marca.activo) 1 else 0)
        }
        try {
            return db.update(
                DatabaseHelper.TABLA_MARCAS,
                valores,
                "${DatabaseHelper.COL_MARC_ID}=?",
                arrayOf(marca.id.toString())
            )
        } finally {
            db.close()
        }
    }

    //4- Eliminación de registros
    fun eliminar(id:Int): Int{
        val db= dbHelper.writableDatabase
        try {
            return db.delete(
                DatabaseHelper.TABLA_MARCAS,
                "${DatabaseHelper.COL_MARC_ID}=?",
                arrayOf(id.toString())
            )
        } finally {
            db.close()
        }
    }

    //5- Cantidad de productos asociados a una marca (para no eliminarla si tiene productos)
    fun contarProductos(idMarca: Int): Int{
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLA_PRODUCTOS} WHERE ${DatabaseHelper.COL_MARCA_ID}=?",
            arrayOf(idMarca.toString())
        )
        var cantidad = 0
        if (cursor.moveToFirst()){
            cantidad = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return cantidad
    }

    //6- Restauracion de un registro eliminado (Deshacer): se inserta con el mismo codigo
    fun restaurar(marca: Marca): Long {
        val db=dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_MARC_ID,marca.id)
            put(DatabaseHelper.COL_MARC_NOMBRE,marca.nombre)
            put(DatabaseHelper.COL_MARC_DESCRIPCION,marca.descripcion)
            put(DatabaseHelper.COL_MARC_ACTIVO,if (marca.activo) 1 else 0)
        }
        //insert devuelve -1 si falla (ej: mientras tanto se creo otra marca con el mismo nombre)
        val idRestaurado = db.insert(DatabaseHelper.TABLA_MARCAS, null, valores)
        db.close()
        return idRestaurado
    }

    //7- Cantidad de marcas activas (resumen para el widget)
    fun contarActivos(): Int{
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLA_MARCAS} WHERE ${DatabaseHelper.COL_MARC_ACTIVO}=1", null
        )
        var cantidad = 0
        if (cursor.moveToFirst()){
            cantidad = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return cantidad
    }
}
