package com.terfess.busetasyopal.actividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
    private var contador = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        //recuperar el tema guardado en shared preferences
        UtilidadesMenores().applySavedNightMode(this)
        val roomDB by lazy { AppDatabase.getDatabase(this) }

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pant_splash)

        val intentToRutasSeccion by lazy { Intent(this, RutasSeccion::class.java) }
        //splash
        splashScreen.setKeepOnScreenCondition { true }

        //crear canales notificaciones
        NotificationChannelHelper(this).crearCanalesNotificaciones()

        // Procesar intent recibido
        intent?.let {
            it.getStringExtra("link")?.let { link ->
                intentToRutasSeccion.putExtra("link", link)
                it.removeExtra("link")
            }
            it.getStringExtra("respuesta")?.let { respuesta ->
                intentToRutasSeccion.putExtra("respuesta", respuesta)
                it.removeExtra("respuesta")
            }
        }

        //----------------------------TIEMPO AGOTADO---------------------------------

        timeVerifyConextion(roomDB, intentToRutasSeccion)

        //---------------DESCARGAR INFORMACION SI ES NECESARIO--------------------------

        if (UtilidadesMenores().comprobarConexion(this)) {
            //obtener la version externa y comparar
            CoroutineScope(Dispatchers.IO).launch {

                val versionLocal = roomDB.versionDao().getVersion()

                FirebaseDatabase.getInstance().getReference("/features/0/versionPruebas")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val versionNube = snapshot.value.toString().toInt()

                            // Trabajar con la base de datos en el hilo de fondo
                            CoroutineScope(Dispatchers.IO).launch {
                                versionLocal ?: 0
                                if (versionLocal != versionNube) {
                                    println("Version nube es diferente a local")
                                    roomDB.routeDao().deleteRoutesTable()

                                    descargarDatos()

                                    roomDB.versionDao()
                                        .insertVersion(Version(num_version = versionNube))

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

    private fun timeVerifyConextion(roomDB: AppDatabase, intentToRutasSeccion: Intent) {
        val tiempoAgotado = Runnable {
            // Si no pudo conectarse correctamente tras 15 segundos (mala conexion)
            if (contador != 1) {
                CoroutineScope(Dispatchers.IO).launch {
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
    }


    private fun descargarDatos() {
        val contexto = this


        DatosDeFirebase().descargarInformacion(contexto, object : allDatosRutas {
            override fun todosDatosRecibidos() {
                UtilidadesMenores().crearToast(this@Splash, "Información De Rutas Descargada")

                FirebaseDatabase.getInstance().goOffline()
                FirebaseDatabase.getInstance().goOnline()

                tiempo.removeCallbacksAndMessages(null)
                UtilidadesMenores().reiniciarApp(this@Splash, Splash::class.java)
            }
        })
    }

    override fun onDestroy() {
        Log.i("Actividades", "Splash se destruyó")
        super.onDestroy()
    }
}