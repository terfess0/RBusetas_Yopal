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


}