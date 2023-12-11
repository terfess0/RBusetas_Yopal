package com.terfess.busetasyopal.clases_utiles

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.android.gms.maps.model.LatLng

class DatosASqliteLocal(context: Context) : SQLiteOpenHelper(context, "Datos_App", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        // Crear las tablas
        val crearTablaVersion = "CREATE TABLE version" +
                "(numVersion INTEGER NOT NULL DEFAULT 0);"

        val crearTablaRuta = "CREATE TABLE ruta" +
                "(id_ruta INTEGER NOT NULL);"

        // Crear la tabla coordenadas con una clave foránea hacia la tabla ruta
        val crearTablaCoorPrimeraPart = "CREATE TABLE coordenadas1" +
                "(id_ruta INTEGER," +
                "latitud REAL NOT NULL," +
                "longitud REAL NOT NULL," +
                "FOREIGN KEY (id_ruta) REFERENCES ruta(id_ruta));"

        val crearTablaCoordSegundaPart = "CREATE TABLE coordenadas2" +
                "(id_ruta INTEGER," +
                "latitud REAL NOT NULL," +
                "longitud REAL NOT NULL," +
                "FOREIGN KEY (id_ruta) REFERENCES ruta(id_ruta));"

        // Ejecutar las sentencias SQL para crear las tablas
        db?.execSQL(crearTablaVersion)
        db?.execSQL(crearTablaRuta)
        db?.execSQL(crearTablaCoorPrimeraPart)
        db?.execSQL(crearTablaCoordSegundaPart)
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Eliminar las tablas existentes si es necesario
        val eliminarTablaVersion = "DROP TABLE IF EXISTS version;"
        val eliminarTablaRuta = "DROP TABLE IF EXISTS ruta;"
        val eliminarTablaCoordenadas1 = "DROP TABLE IF EXISTS coordenadas1;"
        val eliminarTablaCoordenadas2 = "DROP TABLE IF EXISTS coordenadas2;"

        db?.execSQL(eliminarTablaCoordenadas1)
        db?.execSQL(eliminarTablaCoordenadas2)
        db?.execSQL(eliminarTablaRuta)
        db?.execSQL(eliminarTablaVersion)

        // Volver a crear las tablas con la nueva estructura
        onCreate(db)
    }


    // Método para insertar una nueva ruta
    fun insertarRuta(idRuta: Int): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put("id_ruta", idRuta)
        return db.insert("ruta", null, values)
    }

    // Método para insertar coordenadas para una ruta específica
    fun insertarCoordSalida(idRuta:Int, listaCoordenadas1: List<LatLng>) {
        val db = writableDatabase

        for (coordenada in listaCoordenadas1) {
            val values = ContentValues()
            values.put("id_ruta", idRuta)
            values.put("latitud", coordenada.latitude)
            values.put("longitud", coordenada.longitude)

            db.insert("coordenadas1", null, values)
        }
    }

    fun insertarCoordLlegada(idRuta:Int, listaCoordenadas2: List<LatLng>) {
        val db = writableDatabase

        for (coordenada in listaCoordenadas2) {
            val values = ContentValues()
            values.put("id_ruta", idRuta)
            values.put("latitud", coordenada.latitude)
            values.put("longitud", coordenada.longitude)

            db.insert("coordenadas2", null, values)
        }
    }

    fun insertarVersionDatos(numVersion: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("numVersion", numVersion)

        // Actualizar la fila existente o insertar una nueva si no existe
        db.insertWithOnConflict("version", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerCoordenadas(idRuta: Int, tabla:String): List<LatLng> {
        val coordenadas = mutableListOf<LatLng>()
        val db = readableDatabase

        // Realizar la consulta SQL
        val query = "SELECT latitud, longitud FROM $tabla WHERE id_ruta=$idRuta"
        val cursor = db.rawQuery(query, null)

        // Procesar el resultado de la consulta
        // Mover el cursor al inicio
        cursor.moveToFirst()

        // Procesar el resultado de la consulta
        while (!cursor.isAfterLast) {
            val latitudIndex = cursor.getColumnIndex("latitud")
            val longitudIndex = cursor.getColumnIndex("longitud")

            if (latitudIndex >= 0 && longitudIndex >= 0) { //controlar -1 (columna no encontrada)
                val latitud = cursor.getDouble(latitudIndex)
                val longitud = cursor.getDouble(longitudIndex)
                val coordenada = LatLng(latitud, longitud)
                println("Esta es la coord: $coordenada")
                coordenadas.add(coordenada)
            } else {
                // Manejar el caso donde no se encuentran las columnas
                println("No se encontraron las columnas 'latitud' o 'longitud'")
            }

            // Mover al siguiente
            cursor.moveToNext()
        }


        // Cerrar el cursor y la base de datos
        cursor.close()
        db.close()

        return coordenadas
    }


}