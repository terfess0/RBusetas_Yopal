package com.terfess.busetasyopal.actividades

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.VideoView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.DatosASqliteLocal
import com.terfess.busetasyopal.clases_utiles.DatosDeFirebase
import com.terfess.busetasyopal.clases_utiles.RutaBasic
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.clases_utiles.allDatosRutas
import com.terfess.busetasyopal.modelos_dato.EstructuraDatosBaseDatos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pant_splash)


        val videoView: VideoView = findViewById(R.id.videoView)
        var idVideo = R.raw.ic_app_anim_light
        if (modoOscuroActivado(this)) {
            // Cargar video oscuro
            idVideo = R.raw.ic_app_anim_dark
        }
        val videoUri: Uri = Uri.parse("android.resource://$packageName/$idVideo")
        videoView.setVideoURI(videoUri)
        videoView.start()


        if (UtilidadesMenores().comprobarConexion(this)){
            //si hay conexion a internet entonces

            UtilidadesMenores().crearToast(this, "Conexión Reestablecida")
            //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>
            //DESCARGAR LOS DATOS DE COORDENADAS DE CADA RUTA DESDE FIREBASE Y GUARDARLOS EN SQLITE LOCAL
            val versionLocal: Int
            //buscar el numero de version actual (local)
            val dbHelper = DatosASqliteLocal(this)
            //obtener la version local
            val cursor = dbHelper.readableDatabase.rawQuery("SELECT * FROM version", null)
            versionLocal = if (cursor.moveToFirst()) {
                cursor.getInt(0) //indices de columnas inician en 0
            } else {
                0
            }
            cursor.close()

            //obtener la version externa y comparar
            CoroutineScope(Dispatchers.IO).launch {
                FirebaseDatabase.getInstance().getReference("/features/0/version")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val versionNube = snapshot.value.toString().toInt()
                            if (versionLocal != versionNube) {
                                dbHelper.insertarVersionDatos(versionNube)
                                if (!RutaBasic.CreatRuta.descargando) { // la variable descargando esta en el objeto de la clase RutaBasic.kt
                                    descargarDatos()
                                    RutaBasic.CreatRuta.descargando = true
                                }
                                Toast.makeText(
                                    this@Splash,
                                    "Descargando informacion",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }else{
                                startActivity(Intent(this@Splash, RutasSeccion::class.java))
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            UtilidadesMenores().crearToast(
                                this@Splash,
                                "La version no se pudo recibir desde internet"
                            )
                        }
                    })
            }
            //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        }else{
            //si no hay conexion
            UtilidadesMenores().crearToast(this, "Sin conexión a Internet")
            startActivity(Intent(this@Splash, RutasSeccion::class.java))
        }

    }

    private fun descargarDatos() {
        val dbHelper = DatosASqliteLocal(this)
        DatosDeFirebase().descargarInformacion(object : allDatosRutas {
            override fun todosDatosRecibidos(listaCompleta: MutableList<EstructuraDatosBaseDatos>) {
                dbHelper.eliminarTodasLasRutas() //evitar errores en trazos de rutas por puntos repetidos
                for (i in listaCompleta) {
                    dbHelper.insertarRuta(i.idRuta)
                    dbHelper.insertarCoordSalida(i.idRuta, i.listPrimeraParte)
                    dbHelper.insertarCoordLlegada(i.idRuta, i.listSegundaParte)
                }
                Toast.makeText(
                    this@Splash,
                    "Se descargo toda la información correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                RutaBasic.CreatRuta.descargando = false
                UtilidadesMenores().reiniciarApp(this@Splash, Splash::class.java)
            }
        })
    }

    fun modoOscuroActivado(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}