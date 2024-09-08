package com.terfess.busetasyopal.actividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.room.DatosDeFirebase
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.enums.RoomPeriod
import com.terfess.busetasyopal.room.allDatosRutas
import com.terfess.busetasyopal.room.AppDatabase
import com.terfess.busetasyopal.room.model.Version
import com.terfess.busetasyopal.services.NotificationChannelHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Splash : AppCompatActivity() {
    private var tiempo = Handler(Looper.getMainLooper()) //variable para temporizadores
    var contador = 0
    var rootView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        //recuperar el tema guardado en shared preferences
        UtilidadesMenores().applySavedNightMode(this)

        val roomDB = AppDatabase.getDatabase(this)

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pant_splash)
        rootView = findViewById<View>(android.R.id.content)

        //splash
        splashScreen.setKeepOnScreenCondition { true }

        //intent general paso a la siguiente actividad
        val intentToRutasSeccion = Intent(this, RutasSeccion::class.java)

        //crear canales notificaciones
        NotificationChannelHelper(this).crearCanalesNotificaciones()

        //recibir datos de notis en segundo plano firebase messaging
        // Pasar los extras del intent recibido a la actividad principal
        if (intent.hasExtra("link")) {
            intentToRutasSeccion.putExtra("link", intent.getStringExtra("link"))
            // Limpiar el intent
            intent.removeExtra("link")
            intent.removeExtra("respuesta")
        }
        if (intent.hasExtra("respuesta")) {
            intentToRutasSeccion.putExtra("respuesta", intent.getStringExtra("respuesta"))
            // Limpiar el intent
            intent.removeExtra("link")
            intent.removeExtra("respuesta")
        }

        //----------------------------TIEMPO AGOTADO---------------------------------

        val tiempoAgotado = Runnable {
            // Si no pudo conectarse correctamente tras 15 segundos (mala conexion)
            if (contador != 1) {
                CoroutineScope(Dispatchers.Default).launch {
                    val dato = roomDB.scheduleDao()
                        .getSchedule(2, RoomPeriod.LUN_VIE.toString()).sche_start

                    runOnUiThread {
                        if (dato.isBlank()) {
                            Toast.makeText(
                                this@Splash,
                                "Se necesita conexión a Internet",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            finish()
//                    findViewById<TextView>(R.id.tiempoAgotado).visibility = View.VISIBLE
                        } else {
                            UtilidadesMenores().crearToast(this@Splash, "Tiempo Agotado")
                            startActivity(intentToRutasSeccion)
                            finish()
                        }
                    }
                }


            }
        }
        tiempo.postDelayed(tiempoAgotado, 15000)

        //---------------DESCARGAR INFORMACION SI ES NECESARIO--------------------------

        if (UtilidadesMenores().comprobarConexion(this)) {
            //si hay conexion a internet entonces

            //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>
            //DESCARGAR LOS DATOS DE COORDENADAS DE CADA RUTA DESDE FIREBASE Y GUARDARLOS EN SQLITE LOCAL

            //obtener la version externa y comparar
            CoroutineScope(Dispatchers.IO).launch {

                // Buscar la versión local en el hilo de fondo
                var versionLocal = roomDB.versionDao().getVersion()

                FirebaseDatabase.getInstance().getReference("/features/0/versionPruebas")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val versionNube = snapshot.value.toString().toInt()

                            // Trabajar con la base de datos en el hilo de fondo
                            CoroutineScope(Dispatchers.IO).launch {
                                if (versionLocal == null) versionLocal = 0
                                if (versionLocal != versionNube) {
                                    println("Version nube es diferente a local")
                                    roomDB.routeDao().deleteRoutesTable()

                                    descargarDatos()

                                    roomDB.versionDao()
                                        .insertVersion(Version(num_version = versionNube))

                                    // Cambiar al hilo principal para actualizar la UI
                                    withContext(Dispatchers.Main) {
                                        startActivity(intentToRutasSeccion)
                                        tiempo.removeCallbacksAndMessages(null)
                                        finish()
                                    }
                                } else {
                                    // Cambiar al hilo principal para iniciar la actividad si no hay descarga
                                    withContext(Dispatchers.Main) {
                                        startActivity(intentToRutasSeccion)
                                        tiempo.removeCallbacksAndMessages(null)
                                        finish()
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            UtilidadesMenores().crearToast(
                                this@Splash,
                                "La versión no se pudo recibir desde internet"
                            )
                        }
                    })
            }

            //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        } else {
            //si no hay conexion
            tiempo.removeCallbacksAndMessages(null)
            CoroutineScope(Dispatchers.Default).launch {
                val dato =
                    roomDB.scheduleDao().getSchedule(2, RoomPeriod.LUN_VIE.toString()).sche_start

                runOnUiThread {
                    if (dato.isBlank()) {
                        Toast.makeText(
                            this@Splash,
                            "Se necesita conexión a Internet",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        finish()
                        findViewById<TextView>(R.id.tiempoAgotado).visibility = View.VISIBLE
                        findViewById<TextView>(R.id.tiempoAgotado).text = "Sin Conexión"
                    } else {
                        UtilidadesMenores().crearToast(this@Splash, "Sin conexión a Internet")
                        startActivity(intentToRutasSeccion)
                        finish()
                    }
                }
            }
        }
    }


    private fun descargarDatos() {
        val contexto = this


        DatosDeFirebase().descargarInformacion(contexto, object : allDatosRutas {
            override fun todosDatosRecibidos() {
                UtilidadesMenores().crearSnackbar("Información De Rutas Descargada", rootView!!)

                UtilidadesMenores().reiniciarApp(this@Splash, Splash::class.java)
                tiempo.removeCallbacksAndMessages(null)
            }
        })
    }
}