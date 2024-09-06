package com.terfess.busetasyopal.clases_utiles

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.TypedValue
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.Mapa
import com.terfess.busetasyopal.enums.FirebaseEnums
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


interface AlertaCallback { //devolucion de llamada para el CrearAlerta
    fun onOpcionSeleccionada(opcion: Int, tipo_de_solicitud: String)
}

class UtilidadesMenores {
    //esta funcion comprobarConexion tiene su contraparte en la clase Mapa.kt y que detecta cuando regresa/se establece conexion
    fun comprobarConexion(context: Context): Boolean {
        val returne: Boolean
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager //transformar el valor resultante en un connectibitymanager mediante as
        val networkCapabilities =
            connectivityManager.activeNetwork ?: return false //devuelve identificador de red
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities)
                ?: return false //obtiene propiedades de la red apartir del identificardor de networkcapabilities
        returne = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return returne
    }

    fun crearAlerta(
        contexto: Context,
        tipo_solicitud: String,
        mensaje: String,
        op1: String,
        op2: String,
        Callback: AlertaCallback
    ) {
        val builder = AlertDialog.Builder(contexto, R.style.AlertDialogTheme)
        builder.setTitle("Alerta!")
        builder.setMessage(mensaje)
            .setPositiveButton(op1) { _, _ ->
                if (tipo_solicitud == "ubicacion") {
                    Callback.onOpcionSeleccionada(2, "permiso_ubicacion") //opcion lado derecho
                }
                if (tipo_solicitud == "notificacion") {
                    Callback.onOpcionSeleccionada(1, "permiso_notificacion") //opcion lado derecho
                }
            }
        builder.setNegativeButton(op2) { _, _ ->
            if (tipo_solicitud == "ubicacion") {
                Callback.onOpcionSeleccionada(1, "permiso_ubicacion") //opcion lado izquierdo
            }
            if (tipo_solicitud == "notificacion") {
                Callback.onOpcionSeleccionada(2, "permiso_notificacion") //opcion lado derecho
            }
        }
        val dialog = builder.create()
        //personalizar alerta
        // dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Fondo transparente
        dialog.setOnShowListener { //aqui se puede personalizar mas
            // cambiar el color de los botones
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
        }
        dialog.show()
    }

    fun crearAlertaSencilla(
        contexto: Context,
        mensaje: String
    ) {

        //esta alerta sera para anuncios y por ello solo tendra la op de aceptar

        val builder = AlertDialog.Builder(contexto, R.style.AlertDialogTheme)
        builder.setTitle(contexto.getString(R.string.alert_text_literal))
        builder.setIcon(R.drawable.ic_app)
        builder.setMessage(mensaje)
            .setPositiveButton(contexto.getString(R.string.accept)) { _, _ ->

            }

        val dialog = builder.create()
        //personalizar alerta
        // dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Fondo transparente
        dialog.setOnShowListener { //aqui se puede personalizar mas
            // cambiar el color de los botones
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        }
        dialog.show()
    }


    var toast: Toast? = null // variable toast "global" intencion no acumulacion de toast

    fun crearToast(contexto: Context?, mensaje: String) {
        contexto?.applicationContext?.let {//si contexto y contexo aplicacion no son nulos entonces let ejecutara el bloque de codigo en {}
            toast?.cancel() // cancelar el toast anterior si existe
            toast = Toast.makeText(it, mensaje, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }

    fun crearSnackbar(mensaje: String, rootView: View) {
        val snk = Snackbar.make(
            rootView,
            mensaje,
            Snackbar.LENGTH_SHORT
        )
        snk.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snk.setBackgroundTint(
            ContextCompat.getColor(
                rootView.context,
                R.color.enfasis_azul
            )
        )
        snk.show()
    }

    fun reiniciarApp(context: Context, claseObjetivo: Class<*>) {
        val intent = Intent(context, claseObjetivo)

        if (context.javaClass == claseObjetivo) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        } else {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }


        // Finalizar la actividad actual si es necesario
        if (context is Activity) {
            context.finish()
        }
    }

    fun colorTituloTema(contexto: Context): String {

        val typedValue = TypedValue()

        val theme = contexto.theme
        theme.resolveAttribute(androidx.appcompat.R.attr.titleTextColor, typedValue, true)

        val typedValue2 = TypedValue()

        theme.resolveAttribute(androidx.appcompat.R.attr.subtitleTextColor, typedValue2, true)

        return String.format("#%06X", typedValue.data and 0xFFFFFF)
    }

    fun colorSubTituloTema(contexto: Context): String {

        val theme = contexto.theme
        val typedValue2 = TypedValue()

        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark, typedValue2, true)

        return String.format("#%06X", typedValue2.data and 0xFFFFFF)
    }


    fun isNightMode(): Boolean {
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        var nightModeState = false
        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            nightModeState = true
        }
        return nightModeState
    }

    fun reportar(context: Context, instanciaMapa: Mapa? = null, opcion_actual: String) {
        var ubiUser = Mapa.ubiUser ?: LatLng(0.0, 0.0)
        val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)

        //set titulo, descripcion y icono
        builder.setTitle("Reportar novedad")
        builder.setMessage(
            context.getString(R.string.description_alert_report)
        )
        builder.setIcon(R.drawable.ic_app)

        // Crear un LinearLayout vertical para contener el EditText y el CheckBox
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        // Agregar un EditText para que el usuario ingrese texto
        val input = EditText(context)
        input.hint = "Escribe aquí"
        input.maxLines = 5
        input.maxEms = 5
        input.setHintTextColor(Color.GRAY)

        if (isNightMode()) {
            input.setTextColor(Color.WHITE)
        }

        layout.addView(input)

        // Agregar un CheckBox para permitir al usuario elegir si desea enviar la ubicación
        val checkBox = CheckBox(context)
        checkBox.text = context.getString(R.string.option_add_ubi_to_report)

        if (instanciaMapa != null) { // Verificar si se proporcionó una instancia del mapa
            layout.addView(checkBox)
        }

        // Establecer el LinearLayout como la vista del cuadro de diálogo
        builder.setView(layout)
        builder.setPositiveButton("Enviar") { dialog, which ->

            if (comprobarConexion(context)) {

                // Obtener el texto ingresado por el usuario
                val texto = input.text.toString()
                if (texto.isEmpty() || texto.isBlank()) {
                    crearAlertaSencilla(context, context.getString(R.string.empty_eport_error))
                } else {
                    CoroutineScope(Dispatchers.IO).launch {

                        val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
                        val ref: DatabaseReference =
                            firebase.getReference(context.getString(R.string.ruta_reportes_db_nube))
                        val nuevoReporteKey = ref.push().key ?: return@launch

                        val currentDate: Date = Calendar.getInstance().time
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val fechaFormateada: String = dateFormat.format(currentDate)

                        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val horaFormateada: String = timeFormat.format(currentDate)

                        var ubicacion = "Ninguna"

                        //if selecciona enviar ubicacion y la ubicacion ya fue obtenida
                        if (checkBox.isChecked && ubiUser.latitude != 0.0 && ubiUser.longitude != 0.0) {
                            ubicacion =
                                ubiUser.latitude.toString() + ", " + ubiUser.longitude.toString()
                        }
                        //si selecciona enviar ubicacion pero falta obtener ubicacion
                        if (checkBox.isChecked && (ubiUser.latitude == 0.0 && ubiUser.longitude == 0.0)) {

                            withContext(Dispatchers.Main) {
                                instanciaMapa?.activarLocalizacion()
                            }

                            var contador = 0
                            val tiempoMaximo = 10 // segundos
                            val intervaloEspera = 1000L // 1 segundo

                            while (contador < tiempoMaximo && (ubiUser.latitude == 0.0 && ubiUser.longitude == 0.0)) {
                                delay(intervaloEspera)
                                contador++
                            }

                            if (ubiUser.latitude == 0.0 && ubiUser.longitude == 0.0) {
                                ubicacion = "No proporcionada"
                            } else {
                                ubicacion = ubiUser.toString()
                            }
                        }
                        //si no selecciona enviar ubicacion
                        if (!checkBox.isChecked) {
                            ubicacion = "No proporcionada"
                        }

                        // Obtener el token de registro
                        var tokenIdApp = ""
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                tokenIdApp = task.result

                                // Crear un nuevo nodo con el ID único, fecha, texto y ubicación del reporte
                                val nuevoReporte = mapOf<String, Any>(
                                    "dateReport" to fechaFormateada,
                                    "timeReport" to horaFormateada,
                                    "situationReport" to texto,
                                    "location" to ubicacion,
                                    "currentTask" to opcion_actual,
                                    "origin" to tokenIdApp
                                )

                                // Subir el nuevo reporte a la base de datos de Firebase
                                ref.child(nuevoReporteKey).setValue(nuevoReporte)
                                    .addOnCompleteListener { subida ->
                                        if (subida.isSuccessful) {
                                            // La subida fue exitosa
                                            crearAlertaSencilla(
                                                context,
                                                "Reporte enviado exitosamente"
                                            )
                                        } else {
                                            // La subida falló
                                            crearAlertaSencilla(
                                                context,
                                                "Error al enviar el reporte. Inténtalo de nuevo más tarde"
                                            )
                                        }
                                    }
                            } else {
                                // La subida falló
                                crearAlertaSencilla(
                                    context,
                                    "Error al enviar el reporte. Inténtalo de nuevo más tarde"
                                )
                            }
                        }


                    }
                }
            } else {
                crearAlertaSencilla(context, "No hay conexión, inténtalo de nuevo más tarde")
            }
        }

        builder.show()
    }

    fun readSharedPref(context: Context, key: String): Int {
        val sharedPreferences =
            context.getSharedPreferences("PreferenciasGuardadas", Context.MODE_PRIVATE)
        return sharedPreferences.getInt(key, 0)
    }

    fun handleFirebaseError(exception: Exception): FirebaseEnums {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> FirebaseEnums.ERROR_CREDENTIAL
            is FirebaseAuthInvalidUserException -> FirebaseEnums.ERROR_INVALID_USER
            is FirebaseAuthUserCollisionException -> FirebaseEnums.ERROR_USER_COLLISION
            is FirebaseNetworkException -> FirebaseEnums.ERROR_CONNECTION
            is DatabaseException -> FirebaseEnums.ERROR_DATABASE
            is FirebaseAuthException -> FirebaseEnums.ERROR_AUTH
            else -> FirebaseEnums.ERROR_UNKNOWN
        }
    }
}
