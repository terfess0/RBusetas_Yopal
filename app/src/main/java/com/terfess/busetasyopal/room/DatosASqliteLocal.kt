package com.terfess.busetasyopal.room

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.android.gms.maps.model.LatLng
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.modelos_dato.DatoCalcularRuta
import com.terfess.busetasyopal.modelos_dato.DatoFrecuencia
import com.terfess.busetasyopal.modelos_dato.DatoHorario

class DatosASqliteLocal(context: Context) : SQLiteOpenHelper(context, "Datos_App", null, 1) {
    private val contexto = context
    override fun onCreate(db: SQLiteDatabase?) {
        // Crear las tablas
        val crearTablaVersion = contexto.getString(R.string.crearTablaVersion)

        val crearTablaRuta = contexto.getString(R.string.crearTablaRuta)

        // Crear la tabla coordenadas con una clave foránea hacia la tabla ruta
        val crearTablaCoorPrimeraPart =
            contexto.getString(R.string.crearTablaCoordenadas1)

        val crearTablaCoordSegundaPart =
            contexto.getString(R.string.crearTablaCoordenadas2)

        val crearTablaHorario =
            contexto.getString(R.string.crearTablaHorario)

        val crearTablaFrecuencia =
            contexto.getString(R.string.crearTablaFrecuencia)

        // Ejecutar las sentencias SQL para crear las tablas
        db?.execSQL(crearTablaVersion)
        db?.execSQL(crearTablaRuta)
        db?.execSQL(crearTablaCoorPrimeraPart)
        db?.execSQL(crearTablaCoordSegundaPart)
        db?.execSQL(crearTablaHorario)
        db?.execSQL(crearTablaFrecuencia)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        if (oldVersion <= 56) {
            // Realiza acciones para actualizar desde la versión 1 a la versión 2
            db?.execSQL("CREATE TABLE IF NOT EXISTS horario(id_ruta INTEGER,horLunVie1 TEXT NOT NULL,horLunVie2 TEXT NOT NULL,horSab1 TEXT NOT NULL,horSab2 TEXT NOT NULL,horDomFest1 TEXT NOT NULL,horDomFest2 TEXT NOT NULL,FOREIGN KEY (id_ruta) REFERENCES ruta(id_ruta));")
            db?.execSQL("CREATE TABLE IF NOT EXISTS frecuencia(id_ruta INTEGER,frecLunVie TEXT NOT NULL,frecSab TEXT NOT NULL,frecDomFest TEXT NOT NULL,FOREIGN KEY (id_ruta) REFERENCES ruta(id_ruta));")

        }
        // Eliminar las tablas existentes si es necesario
        val eliminarTablaVersion = "DROP TABLE IF EXISTS version;"
        val eliminarTablaRuta = "DROP TABLE IF EXISTS ruta;"
        val eliminarTablaCoordenadas1 = "DROP TABLE IF EXISTS coordenadas1;"
        val eliminarTablaCoordenadas2 = "DROP TABLE IF EXISTS coordenadas2;"
        val eliminarTablaHorario = "DROP TABLE IF EXISTS horario;"
        val eliminarTablaFrecuencia = "DROP TABLE IF EXISTS frecuencia;"

        db?.execSQL(eliminarTablaRuta)
        db?.execSQL(eliminarTablaCoordenadas1)
        db?.execSQL(eliminarTablaCoordenadas2)
        db?.execSQL(eliminarTablaVersion)
        db?.execSQL(eliminarTablaHorario)
        db?.execSQL(eliminarTablaFrecuencia)

        // Volver a crear las tablas con la nueva estructura
        onCreate(db)
    }


    // Método para insertar una nueva ruta
    fun insertarRuta(idRuta: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("id_ruta", idRuta)
        db.insertWithOnConflict("ruta", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Método para insertar coordenadas para una ruta específica
    fun insertarCoordSalida(idRuta: Int, listaCoordenadas1: List<LatLng>) {
        val db = writableDatabase

        for (coordenada in listaCoordenadas1) {
            val values = ContentValues()
            values.put("id_ruta", idRuta)
            values.put("latitud", coordenada.latitude)
            values.put("longitud", coordenada.longitude)

            db.insertWithOnConflict("coordenadas1", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun insertarCoordLlegada(idRuta: Int, listaCoordenadas2: List<LatLng>) {
        val db = writableDatabase

        for (coordenada in listaCoordenadas2) {
            val values = ContentValues()
            values.put("id_ruta", idRuta)
            values.put("latitud", coordenada.latitude)
            values.put("longitud", coordenada.longitude)

            db.insertWithOnConflict("coordenadas2", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun insertarHorario(idRuta: Int, Horarios: DatoHorario) {
        writableDatabase.use { db ->
            val values = ContentValues()
            values.put("id_ruta", idRuta)
            values.put("horLunVie1", Horarios.horaInicioLunesViernes)
            values.put("horLunVie2", Horarios.horaFinalLunesViernes)
            values.put("horSab1", Horarios.horaInicioSab)
            values.put("horSab2", Horarios.horaFinalSab)
            values.put("horDomFest1", Horarios.horaInicioDom)
            values.put("horDomFest2", Horarios.horaFinalDom)

            db.insertWithOnConflict("horario", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun insertarFrecuencia(idRuta: Int, Frecuencia: DatoFrecuencia) {
        writableDatabase.use { db ->
            val values = ContentValues()
            values.put("id_ruta", idRuta)
            values.put("frecLunVie", Frecuencia.frecLunVie)
            values.put("frecSab", Frecuencia.frecSab)
            values.put("frecDomFest", Frecuencia.frecDomFest)

            db.insertWithOnConflict("frecuencia", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun insertarVersionDatos(numVersion: Int) {
        val db = writableDatabase
        db.delete("version", null, null)
        val values = ContentValues()
        values.put("numVersion", numVersion)

        // Actualizar la fila existente o insertar una nueva si no existe
        db.insertWithOnConflict("version", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerCoordenadas(idRuta: Int, tabla: String): List<LatLng> {
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

    fun obtenerCoordenadasCalcularRuta(idRuta: Int, tabla: String): List<DatoCalcularRuta> {
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
        val datoRuta = mutableListOf<DatoCalcularRuta>()
        datoRuta.add(DatoCalcularRuta(coordenadas.toList(), idRuta))
        return datoRuta
    }

    fun obtenerHorarioRuta(idRuta: Int): DatoHorario {
        val horario = DatoHorario(0, "", "", "", "", "", "")

        readableDatabase.use { db ->
            val obtenerHorario =
                "SELECT id_ruta, horLunVie1, horLunVie2, horSab1, horSab2, horDomFest1, horDomFest2 FROM horario WHERE id_ruta=$idRuta"

            val consultaHorario = db.rawQuery(obtenerHorario, null)
            consultaHorario.moveToFirst()

            if (consultaHorario.moveToFirst()) {
                horario.numRuta = consultaHorario.getInt(0)
                horario.horaInicioLunesViernes = consultaHorario.getString(1)
                horario.horaFinalLunesViernes = consultaHorario.getString(2)
                horario.horaInicioSab = consultaHorario.getString(3)
                horario.horaFinalSab = consultaHorario.getString(4)
                horario.horaInicioDom = consultaHorario.getString(5)
                horario.horaFinalDom = consultaHorario.getString(6)
            }

            consultaHorario.close()
        }

        return horario
    }


    fun obtenerFrecuenciaRuta(idRuta: Int): DatoFrecuencia {
        val db = readableDatabase

        val obtenerFrecuenia =
            "SELECT id_ruta, frecLunVie, frecSab, frecDomFest FROM frecuencia WHERE id_ruta=$idRuta"

        val consultaFrecuencia = db.rawQuery(obtenerFrecuenia, null)
        consultaFrecuencia.moveToFirst()
        val idruta = consultaFrecuencia.getInt(0)
        val frecLunVie = consultaFrecuencia.getString(1)
        val frecSab = consultaFrecuencia.getString(2)
        val frecDomFest = consultaFrecuencia.getString(3)

        consultaFrecuencia.close()
        db.close()

        return DatoFrecuencia(idruta, frecLunVie, frecSab, frecDomFest)
    }

}