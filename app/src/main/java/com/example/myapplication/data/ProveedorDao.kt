package com.example.myapplication.data

import android.content.ContentValues
import android.content.Context
import com.example.myapplication.screens.Proveedor

class ProveedorDao (context: Context) {
    private val dbHelper= DatabaseHelper (context)

    // 1-Inserción
    fun insertar(proveedor: Proveedor): Long {
        val db=dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_PROV_RAZON_SOCIAL,proveedor.razonSocial)
            put(DatabaseHelper.COL_PROV_RUC,proveedor.ruc)
            put(DatabaseHelper.COL_PROV_DIRECCION,proveedor.direccion)
            put(DatabaseHelper.COL_PROV_TELEFONO,proveedor.telefono)
            put(DatabaseHelper.COL_PROV_CORREO,proveedor.correo)
            put(DatabaseHelper.COL_PROV_CONTACTO,proveedor.contacto)
            put(DatabaseHelper.COL_PROV_ACTIVO,if (proveedor.activo) 1 else 0)
        }
        //devolver el id generado por autoincrement o -1 si falla
        val idGenerado = db.insert(DatabaseHelper.TABLA_PROVEEDORES, null, valores)
        db.close()
        return idGenerado
    }

    //2- Consulta y recuperacion de registros
    fun listarTodos():List<Proveedor>{
        val lista =mutableListOf<Proveedor>()
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLA_PROVEEDORES} ORDER BY ${DatabaseHelper.COL_PROV_ID} DESC", null
        )
        if (cursor.moveToFirst()){
            do{
                val id= cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROV_ID))
                val razonSocial = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROV_RAZON_SOCIAL))
                val ruc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROV_RUC))
                val direccion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROV_DIRECCION))
                val telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROV_TELEFONO))
                val correo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROV_CORREO))
                val contacto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROV_CONTACTO))
                val activo= cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROV_ACTIVO)) == 1
                lista.add(Proveedor(id, razonSocial, ruc, direccion, telefono, correo, contacto, activo))

            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }


    //3- Actualización de registros
    fun actualizar(proveedor: Proveedor):Int{
        val db =dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_PROV_RAZON_SOCIAL,proveedor.razonSocial)
            put(DatabaseHelper.COL_PROV_RUC,proveedor.ruc)
            put(DatabaseHelper.COL_PROV_DIRECCION,proveedor.direccion)
            put(DatabaseHelper.COL_PROV_TELEFONO,proveedor.telefono)
            put(DatabaseHelper.COL_PROV_CORREO,proveedor.correo)
            put(DatabaseHelper.COL_PROV_CONTACTO,proveedor.contacto)
            put(DatabaseHelper.COL_PROV_ACTIVO,if (proveedor.activo) 1 else 0)
        }
        val filasAfectadas = db.update(
            DatabaseHelper.TABLA_PROVEEDORES,
            valores,
            "${DatabaseHelper.COL_PROV_ID}=?",
            arrayOf(proveedor.id.toString())
        )
        db.close()
        return filasAfectadas
    }

    //4- Eliminación de registros
    fun eliminar(id:Int): Int{
        val db= dbHelper.writableDatabase
        val filasEliminadas = db.delete(
            DatabaseHelper.TABLA_PROVEEDORES,
            "${DatabaseHelper.COL_PROV_ID}=?",
            arrayOf(id.toString())
        )
        db.close()
        return filasEliminadas
    }

    //5- Cantidad de productos asociados a un proveedor (para no eliminarlo si tiene productos)
    fun contarProductos(idProveedor: Int): Int{
        val db =dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLA_PRODUCTOS} WHERE ${DatabaseHelper.COL_PROVEEDOR_ID}=?",
            arrayOf(idProveedor.toString())
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
    fun restaurar(proveedor: Proveedor): Long {
        val db=dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put(DatabaseHelper.COL_PROV_ID,proveedor.id)
            put(DatabaseHelper.COL_PROV_RAZON_SOCIAL,proveedor.razonSocial)
            put(DatabaseHelper.COL_PROV_RUC,proveedor.ruc)
            put(DatabaseHelper.COL_PROV_DIRECCION,proveedor.direccion)
            put(DatabaseHelper.COL_PROV_TELEFONO,proveedor.telefono)
            put(DatabaseHelper.COL_PROV_CORREO,proveedor.correo)
            put(DatabaseHelper.COL_PROV_CONTACTO,proveedor.contacto)
            put(DatabaseHelper.COL_PROV_ACTIVO,if (proveedor.activo) 1 else 0)
        }
        val idRestaurado = db.insert(DatabaseHelper.TABLA_PROVEEDORES, null, valores)
        db.close()
        return idRestaurado
    }

}
