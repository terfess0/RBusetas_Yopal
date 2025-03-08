package com.terfess.busetasyopal.actividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.FirebaseDatabase
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.room.DatosDeFirebase
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.room.allDatosRutas
import com.terfess.busetasyopal.room.AppDatabase
import com.terfess.busetasyopal.room.model.Version
import com.terfess.busetasyopal.services.BillingService
import com.terfess.busetasyopal.services.NotificationChannelHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Splash : AppCompatActivity() {
    private var tiempo = Handler(Looper.getMainLooper()) //variable para temporizadores
    private var contador = 0
    private var firebaseInstance = FirebaseDatabase.getInstance()


    companion object {
        var downloading: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pant_splash)

        //recuperar el tema guardado en shared preferences
        UtilidadesMenores().applySavedNightMode(this)
        val roomDB by lazy { AppDatabase.getDatabase(this) }

        val intentToRutasSeccion = Intent(this, PantallaPrincipal::class.java)

        // Initialize ads sdk
        MobileAds.initialize(this) {}

        // Crear canales notificaciones
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

        //---------------DESCARGAR INFORMACION SI ES NECESARIO--------------------------

        val hayConexion = UtilidadesMenores().comprobarConexion(this)
        println("Hay conexion en splash = $hayConexion")
        if (hayConexion) {
            //obtener la version externa y comparar
            if (!downloading) {
                checkDataVersion(roomDB, intentToRutasSeccion)
                println("Verificando versiones")
            }

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

                        if (!downloading) {
                            checkDataVersion(roomDB, intentToRutasSeccion)
                            println("mandoa a descarga 2")
                        }

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

        firebaseInstance.getReference("/features/0/version")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.value != null) {
                    val versionNube = snapshot.value.toString().toInt()

                    // Trabajar con la base de datos en el hilo de fondo
                    CoroutineScope(Dispatchers.IO).launch {
                        val versionLocal = roomDB.versionDao().getVersion()
                        versionLocal ?: 0
                        if (versionLocal != versionNube) {
                            try {
                                roomDB.versionDao()
                                    .insertVersion(Version(num_version = versionNube))

                                roomDB.routeDao().deleteRoutesTable()

                                descargarDatos(intentToRutasSeccion = intent)
                            } catch (exp: Exception) {
                                println("error")
                            }
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
            }.addOnFailureListener {
                UtilidadesMenores().crearToast(
                    this@Splash,
                    "La versión no se pudo recibir desde internet"
                )
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

                            if (!downloading) {
                                checkDataVersion(roomDB, intentToRutasSeccion)
                                println("mandoa a descarga 1")
                            }
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

        println("Descargando informacion")
        DatosDeFirebase().descargarInformacion(contexto, firebaseInstance, object : allDatosRutas {
            override fun todosDatosRecibidos() {
                UtilidadesMenores().crearToast(this@Splash, "Informacion De Rutas Descargada")

                FirebaseDatabase.getInstance().goOffline()

                tiempo.removeCallbacksAndMessages(null)

                startActivity(intentToRutasSeccion)
                finish()
            }
        })
    }

    override fun onDestroy() {
        tiempo.removeCallbacksAndMessages(null)
        Log.i("Actividades", "Splash se destruyo")
        super.onDestroy()
    }
}