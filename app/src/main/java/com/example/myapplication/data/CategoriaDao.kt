package com.example.myapplication.data

import android.content.ContentValues
import android.content.Context
import com.example.myapplication.screens.Categoria

class CategoriaDao (context: Context) {
    private val dbHelper= DatabaseHelper (context)

    // 1-Inserción
    //el nombre es UNIQUE: si ya existe una categoria con ese nombre se lanza SQLiteConstraintException
    //(se usa insertOrThrow para que la pantalla pueda avisar "Ya existe una categoria con ese nombre")
    fun insertar(categoria: Categoria): Long {
        val db=dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_CAT_NOMBRE,categoria.nombre)
            put(DatabaseHelper.COL_CAT_DESCRIPCION,categoria.descripcion)
        }
        try {
            //devolver el id generado por autoincrement
            return db.insertOrThrow(DatabaseHelper.TABLA_CATEGORIAS, null, valores)
        } finally {
            db.close()
        }
    }

    //2- Consulta y recuperacion de registros
    fun listarTodos():List<Categoria>{
        val lista =mutableListOf<Categoria>()
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLA_CATEGORIAS} ORDER BY ${DatabaseHelper.COL_CAT_ID} DESC", null
        )
        if (cursor.moveToFirst()){
            do{
                val id= cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_ID))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_NOMBRE))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_DESCRIPCION))
                lista.add(Categoria(id, nombre, descripcion))

            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }


    //3- Actualización de registros
    //si el nuevo nombre ya pertenece a otra categoria se lanza SQLiteConstraintException
    fun actualizar(categoria: Categoria):Int{
        val db =dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_CAT_NOMBRE,categoria.nombre)
            put(DatabaseHelper.COL_CAT_DESCRIPCION,categoria.descripcion)
        }
        try {
            return db.update(
                DatabaseHelper.TABLA_CATEGORIAS,
                valores,
                "${DatabaseHelper.COL_CAT_ID}=?",
                arrayOf(categoria.id.toString())
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
                DatabaseHelper.TABLA_CATEGORIAS,
                "${DatabaseHelper.COL_CAT_ID}=?",
                arrayOf(id.toString())
            )
        } finally {
            db.close()
        }
    }

    //5- Cantidad de productos asociados a una categoria (para no eliminarla si tiene productos)
    fun contarProductos(idCategoria: Int): Int{
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLA_PRODUCTOS} WHERE ${DatabaseHelper.COL_CATEGORIA_ID}=?",
            arrayOf(idCategoria.toString())
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
    fun restaurar(categoria: Categoria): Long {
        val db=dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_CAT_ID,categoria.id)
            put(DatabaseHelper.COL_CAT_NOMBRE,categoria.nombre)
            put(DatabaseHelper.COL_CAT_DESCRIPCION,categoria.descripcion)
        }
        //insert devuelve -1 si falla (ej: mientras tanto se creo otra categoria con el mismo nombre)
        val idRestaurado = db.insert(DatabaseHelper.TABLA_CATEGORIAS, null, valores)
        db.close()
        return idRestaurado
    }

    //7- Cantidad total de categorias (resumen para el widget)
    //las categorias no tienen estado, por eso se cuentan todas
    fun contarTodos(): Int{
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLA_CATEGORIAS}", null
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
