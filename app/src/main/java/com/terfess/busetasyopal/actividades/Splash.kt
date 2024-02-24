package com.terfess.busetasyopal.actividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.DatosASqliteLocal
import com.terfess.busetasyopal.clases_utiles.DatosDeFirebase
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.clases_utiles.allDatosRutas
import com.terfess.busetasyopal.modelos_dato.EstructuraDatosBaseDatos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {
    private var tiempo = Handler(Looper.getMainLooper()) //variable para temporizadores
    var contador = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pant_splash)

        //splash
        splashScreen.setKeepOnScreenCondition { true }

        //ads
        MobileAds.initialize(this) {}//inicializar sdk de anuncios google

        //----------------------------TIEMPO AGOTADO---------------------------------

        val tiempoAgotado = Runnable {
            // Si no pudo conectarse correctamente tras 15 segundos (mala conexion)
            if (contador != 1) {
                val sqlDB = DatosASqliteLocal(this)
                val dato = sqlDB.obtenerHorarioRuta(2).horaFinalDom
                if (dato.isBlank()) {
                    Toast.makeText(this, "Se necesita conexión a Internet", Toast.LENGTH_LONG)
                        .show()
                    finish()
//                    findViewById<TextView>(R.id.tiempoAgotado).visibility = View.VISIBLE
                } else {
                    UtilidadesMenores().crearToast(this, "Tiempo Agotado")
                    startActivity(Intent(this@Splash, RutasSeccion::class.java))
                    finish()
                }
            }
        }
        tiempo.postDelayed(tiempoAgotado, 15000)

        //---------------DESCARGAR INFORMACION SI ES NECESARIO--------------------------

        if (UtilidadesMenores().comprobarConexion(this)) {
            //si hay conexion a internet entonces

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
                                dbHelper.onUpgrade(
                                    dbHelper.readableDatabase,
                                    versionLocal,
                                    versionNube
                                )
                                descargarDatos()
                                println(
                                    "Descargando informacion"
                                )
                                dbHelper.insertarVersionDatos(versionNube)
                            } else {
                                //si la informacion descargable ya esta guardada entonces iniciar
                                startActivity(Intent(this@Splash, RutasSeccion::class.java))
                                tiempo.removeCallbacksAndMessages(null)
                                finish()
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
        } else {
            //si no hay conexion
            tiempo.removeCallbacksAndMessages(null)

            val sqlDB = DatosASqliteLocal(this)
            val dato = sqlDB.obtenerHorarioRuta(2).horaFinalDom
            if (dato.isBlank()) {
                Toast.makeText(this, "Se necesita conexión a Internet", Toast.LENGTH_LONG).show()
                finish()
//                findViewById<TextView>(R.id.tiempoAgotado).visibility = View.VISIBLE
//                findViewById<TextView>(R.id.tiempoAgotado).text = "Sin Conexión"
            } else {
                UtilidadesMenores().crearToast(this, "Sin conexión a Internet")
                startActivity(Intent(this@Splash, RutasSeccion::class.java))
                finish()
            }
        }
    }


    private fun descargarDatos() {
        val contexto = this
        CoroutineScope(Dispatchers.Default).launch {
            val dbHelper = DatosASqliteLocal(contexto)
            DatosDeFirebase().descargarInformacion(contexto, object : allDatosRutas {
                override fun todosDatosRecibidos(listaCompleta: MutableList<EstructuraDatosBaseDatos>) {
                    println("Tamaño ------------- ${listaCompleta.size}")
                    for (f in 0..listaCompleta.size - 1) {
                        val i = listaCompleta[f]
                        dbHelper.insertarRuta(i.idRuta)
                        dbHelper.insertarCoordSalida(i.idRuta, i.listPrimeraParte)
                        dbHelper.insertarCoordLlegada(i.idRuta, i.listSegundaParte)
                    }
                    println(
                        "Se descargo toda la información correctamente"
                    )
                    contador = 1
                    UtilidadesMenores().reiniciarApp(this@Splash, Splash::class.java)
                    tiempo.removeCallbacksAndMessages(null)
                }
            })
        }
    }

}