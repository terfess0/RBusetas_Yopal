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
    private var firebaseInstance = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pant_splash)

        //recuperar el tema guardado en shared preferences
        UtilidadesMenores().applySavedNightMode(this)
        val roomDB by lazy { AppDatabase.getDatabase(this) }

        //splash
        splashScreen.setKeepOnScreenCondition{ false }

        val intentToRutasSeccion = Intent(this, RutasSeccion::class.java)

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
            checkDataVersion(roomDB, intentToRutasSeccion)

            //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        } else {
            //si no hay conexion
            tiempo.removeCallbacksAndMessages(null)
            CoroutineScope(Dispatchers.Default).launch {
                val dato =
                    roomDB.versionDao().getVersion()

                runOnUiThread {
                    if (dato == null) {

                        val txtConection = findViewById<TextView>(R.id.tiempoAgotado)
                        txtConection.visibility = View.VISIBLE
                        txtConection.text =
                            getString(R.string.es_necesario_tener_acceso_a_internet_revisa_tu_conexi_n)

                        checkDataVersion(roomDB, intentToRutasSeccion)

                    } else {
                        UtilidadesMenores().crearToast(this@Splash, "Sin conexión a Internet")
                        startActivity(intentToRutasSeccion)
                        finish()
                    }
                }
            }
        }
    }

    private fun checkDataVersion(roomDB: AppDatabase, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {

            val versionLocal = roomDB.versionDao().getVersion()

            firebaseInstance.getReference("/features/0/versionPruebas")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val versionNube = snapshot.value.toString().toInt()

                        // Trabajar con la base de datos en el hilo de fondo
                        CoroutineScope(Dispatchers.IO).launch {
                            versionLocal ?: 0
                            if (versionLocal != versionNube) {

                                roomDB.routeDao().deleteRoutesTable()

                                descargarDatos(intentToRutasSeccion = intent)

                                roomDB.versionDao()
                                    .insertVersion(Version(num_version = versionNube))

                            } else {
                                // Cambiar al hilo principal para iniciar la actividad si no hay descarga
                                withContext(Dispatchers.Main) {
                                    tiempo.removeCallbacksAndMessages(null)
                                    startActivity(intent)
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
    }

    private fun timeVerifyConextion(roomDB: AppDatabase, intentToRutasSeccion: Intent) {
        val tiempoAgotado = Runnable {
            // Si no pudo conectarse correctamente tras 15 segundos (mala conexion)
            if (contador != 1) {
                CoroutineScope(Dispatchers.IO).launch {
                    val dato =
                        roomDB.versionDao().getVersion()

                    runOnUiThread {
                        if (dato == null) {
                            findViewById<TextView>(R.id.tiempoAgotado).visibility = View.VISIBLE
                            checkDataVersion(roomDB, intentToRutasSeccion)

                        } else {
                            UtilidadesMenores().crearToast(
                                this@Splash,
                                getString(R.string.tiempo_agotado)
                            )
                            startActivity(intentToRutasSeccion)
                            finish()
                        }
                    }
                }
            }
        }
        tiempo.postDelayed(tiempoAgotado, 15000)
    }


    private fun descargarDatos(intentToRutasSeccion: Intent) {
        val contexto = this


        DatosDeFirebase().descargarInformacion(contexto, object : allDatosRutas {
            override fun todosDatosRecibidos() {
                UtilidadesMenores().crearToast(this@Splash, "Información De Rutas Descargada")
                tiempo.removeCallbacksAndMessages(null)

                FirebaseDatabase.getInstance().goOffline()

                startActivity(intentToRutasSeccion)
                finish()
            }
        })
    }

    override fun onDestroy() {
        Log.i("Actividades", "Splash se destruyó")
        super.onDestroy()
    }
}