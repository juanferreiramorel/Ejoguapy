package com.example.myapplication.data

import android.content.ContentValues
import android.content.Context
import com.example.myapplication.screens.Producto

class ProductoDao (context: Context) {
    private val dbHelper= DatabaseHelper (context)

    // 1-Inserción
    fun insertar(producto: Producto): Long {
        val db=dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_DESCRIPCION,producto.descripcion)
            put(DatabaseHelper.COL_PROVEEDOR_ID,producto.proveedorId)
            put(DatabaseHelper.COL_CATEGORIA_ID,producto.categoriaId)
            put(DatabaseHelper.COL_PRECIO,producto.precio)
            put(DatabaseHelper.COL_ACTIVO,if (producto.activo) 1 else 0)
        }
        //devolver el id generado por autoincrement o -1 si falla
        val idGenerado = db.insert(DatabaseHelper.TABLA_PRODUCTOS, null, valores)
        db.close()
        return idGenerado
    }

    //2- Consulta y recuperacion de registros
    fun listarTodos():List<Producto>{
        val lista =mutableListOf<Producto>()
        val db =dbHelper.readableDatabase
        //JOIN con proveedores y categorias para traer la razon social del proveedor y el nombre de la categoria
        //se usan alias (p, pr y c) porque las tres tablas tienen las columnas id y activo
        //(productos y categorias tambien comparten "descripcion", por eso se califica con p.)
        val cursor = db.rawQuery(
            "SELECT p.${DatabaseHelper.COL_ID}, p.${DatabaseHelper.COL_DESCRIPCION}, p.${DatabaseHelper.COL_PROVEEDOR_ID}, " +
                    "pr.${DatabaseHelper.COL_PROV_RAZON_SOCIAL}, p.${DatabaseHelper.COL_CATEGORIA_ID}, c.${DatabaseHelper.COL_CAT_NOMBRE}, p.${DatabaseHelper.COL_PRECIO}, p.${DatabaseHelper.COL_ACTIVO} " +
                    "FROM ${DatabaseHelper.TABLA_PRODUCTOS} p " +
                    "INNER JOIN ${DatabaseHelper.TABLA_PROVEEDORES} pr ON p.${DatabaseHelper.COL_PROVEEDOR_ID} = pr.${DatabaseHelper.COL_PROV_ID} " +
                    "INNER JOIN ${DatabaseHelper.TABLA_CATEGORIAS} c ON p.${DatabaseHelper.COL_CATEGORIA_ID} = c.${DatabaseHelper.COL_CAT_ID} " +
                    "ORDER BY p.${DatabaseHelper.COL_ID} DESC", null
        )
        if (cursor.moveToFirst()){
            do{
                val id= cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPCION))
                val proveedorId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROVEEDOR_ID))
                val proveedor = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROV_RAZON_SOCIAL))
                val categoriaId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORIA_ID))
                val categoria = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_NOMBRE))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRECIO))
                val activo= cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACTIVO)) == 1
                lista.add(Producto(id, descripcion, proveedorId, proveedor, categoriaId, categoria, precio, activo))

            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }


    //3- Actualización de registros
    fun actualizar(producto: Producto):Int{
        val db =dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_DESCRIPCION,producto.descripcion)
            put(DatabaseHelper.COL_PROVEEDOR_ID,producto.proveedorId)
            put(DatabaseHelper.COL_CATEGORIA_ID,producto.categoriaId)
            put(DatabaseHelper.COL_PRECIO,producto.precio)
            put(DatabaseHelper.COL_ACTIVO,if (producto.activo) 1 else 0)
        }
        val filasAfectadas = db.update(
            DatabaseHelper.TABLA_PRODUCTOS,
            valores,
            "${DatabaseHelper.COL_ID}=?",
            arrayOf(producto.id.toString())
        )
        db.close()
        return filasAfectadas
    }

    //4- Eliminación de registros
    fun eliminar(id:Int): Int{
        val db= dbHelper.writableDatabase
        val filasEliminadas = db.delete(
            DatabaseHelper.TABLA_PRODUCTOS,
            "${DatabaseHelper.COL_ID}=?",
            arrayOf(id.toString())
        )
        db.close()
        return filasEliminadas
    }

    //5- Restauracion de un registro eliminado (Deshacer): se inserta con el mismo id
    fun restaurar(producto: Producto): Long {
        val db=dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_ID,producto.id)
            put(DatabaseHelper.COL_DESCRIPCION,producto.descripcion)
            put(DatabaseHelper.COL_PROVEEDOR_ID,producto.proveedorId)
            put(DatabaseHelper.COL_CATEGORIA_ID,producto.categoriaId)
            put(DatabaseHelper.COL_PRECIO,producto.precio)
            put(DatabaseHelper.COL_ACTIVO,if (producto.activo) 1 else 0)
        }
        val idRestaurado = db.insert(DatabaseHelper.TABLA_PRODUCTOS, null, valores)
        db.close()
        return idRestaurado
    }

    //6- Cantidad de productos activos (resumen para el widget)
    fun contarActivos(): Int{
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLA_PRODUCTOS} WHERE ${DatabaseHelper.COL_ACTIVO}=1", null
        )
        var cantidad = 0
        if (cursor.moveToFirst()){
            cantidad = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return cantidad
    }

    //7- Cantidad total de productos, activos e inactivos (resumen para el widget)
    fun contarTodos(): Int{
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLA_PRODUCTOS}", null
        )
        var cantidad = 0
        if (cursor.moveToFirst()){
            cantidad = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return cantidad
    }

    //8- Suma de precios de los productos activos = valor del catalogo (resumen para el widget)
    //TOTAL() devuelve 0.0 cuando no hay filas (SUM() devolveria NULL)
    fun sumarPrecioActivos(): Double{
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT TOTAL(${DatabaseHelper.COL_PRECIO}) FROM ${DatabaseHelper.TABLA_PRODUCTOS} WHERE ${DatabaseHelper.COL_ACTIVO}=1", null
        )
        var total = 0.0
        if (cursor.moveToFirst()){
            total = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return total
    }
}
