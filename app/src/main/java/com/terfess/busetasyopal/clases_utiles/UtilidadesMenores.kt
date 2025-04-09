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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.maps.GoogleMap
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
import com.terfess.busetasyopal.actividades.mapa.view.Mapa
import com.terfess.busetasyopal.enums.FirebaseEnums
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.room.model.Coordinate
import com.terfess.busetasyopal.services.workers.OnGetCallback
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
    fun onOpcionSeleccionada(opcion: Int, tipoDeSolicitud: String)
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
        tipoSolicitud: String,
        mensaje: String,
        op1: String,
        op2: String,
        callback: AlertaCallback
    ) {
        val builder = AlertDialog.Builder(contexto, R.style.AlertDialogTheme)
        builder.setTitle("Alerta!")
        builder.setMessage(mensaje)
            .setPositiveButton(op1) { _, _ ->
                if (tipoSolicitud == "ubicacion") {
                    callback.onOpcionSeleccionada(2, "permiso_ubicacion") //opcion lado derecho
                }
                if (tipoSolicitud == "notificacion") {
                    callback.onOpcionSeleccionada(1, "permiso_notificacion") //opcion lado derecho
                }
            }
        builder.setNegativeButton(op2) { _, _ ->
            if (tipoSolicitud == "ubicacion") {
                callback.onOpcionSeleccionada(1, "permiso_ubicacion") //opcion lado izquierdo
            }
            if (tipoSolicitud == "notificacion") {
                callback.onOpcionSeleccionada(2, "permiso_notificacion") //opcion lado derecho
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


    private var toast: Toast? = null // variable toast "global" intencion no acumulacion de toast

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
        val typedValue = TypedValue()

        // Resolvemos el atributo colorPrimaryDark del tema
        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark, typedValue, true)

        // Devolvemos el color en formato hexadecimal
        return String.format("#%06X", typedValue.data and 0xFFFFFF)
    }


    fun isNightMode(): Boolean {
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        var nightModeState = false
        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            nightModeState = true
        }
        return nightModeState
    }

    // SHARED PREFERENCES
    fun saveToSharedPreferences(context: Context, key: String, value: Any) {
        val strShareds = context.getString(R.string.nombre_shared_preferences)
        val sharedPref = context.getSharedPreferences(strShareds, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            else -> throw IllegalArgumentException("Tipo no soportado para SharedPreferences")
        }

        editor.apply()
    }

    fun readSharedIntPref(context: Context, key: String): Int {
        val sharedPreferences =
            context.getSharedPreferences("PreferenciasGuardadas", Context.MODE_PRIVATE)
        return sharedPreferences.getInt(key, 0)
    }

    fun readSharedBooleanPref(context: Context, key: String): Boolean {
        val sharedPreferences =
            context.getSharedPreferences("PreferenciasGuardadas", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, false)
    }

    fun readSharedBooleanShowStatesPrincPref(context: Context, key: String): Boolean {
        val sharedPreferences =
            context.getSharedPreferences("PreferenciasGuardadas", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, true)
    }

    //..

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

    fun applySavedNightMode(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("PreferenciasGuardadas", Context.MODE_PRIVATE)
        val savedNightMode = sharedPreferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(savedNightMode)
    }

    fun extractCoordToLatLng(
        data: List<Coordinate>,
        path: String,
        idRuta: Int
    ): List<LatLng> {
        val listPoint = mutableListOf<LatLng>()

        for (i in data.indices) {
            if (
                data[i].type_path == path
                &&
                data[i].id_route == idRuta
            ) {
                val lng = data[i].longitude
                val lat = data[i].latitude

                val coord = LatLng(lat, lng)
                listPoint.add(coord)
            }
        }
        return listPoint
    }

    fun getColorHambugerIcon(): Int {
        val d = UtilidadesMenores().isNightMode()
        val themeColor = if (d) {
            R.color.white
        } else {
            R.color.black
        }
        return themeColor
    }

    fun buildDialogConfirmAction(
        context: Context,
        message: String,
        onConfirm: (Boolean) -> Unit
    ) {
        //create dialog
        val dialog = AlertDialog.Builder(context, R.style.AlertDialogTheme)
            .setTitle(context.getString(R.string.alert_text_literal))
            .setMessage(message)
            .setPositiveButton(context.getString(R.string.accept)) { _, _ ->
                onConfirm(true)  // Callback if confirm
            }
            .setNegativeButton(context.getString(R.string.cancel)) { _, _ ->
                onConfirm(false)  // Callback if cancel
            }
            .create()

        dialog.show()
    }

    fun toggleNightMode(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences(
                context.getString(R.string.nombre_shared_preferences),
                Context.MODE_PRIVATE
            )

        val nightMode = AppCompatDelegate.getDefaultNightMode()

        val editor = sharedPreferences.edit()

        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            val newNightMode = AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(newNightMode)
            editor.putInt("night_mode", newNightMode)
        } else {
            val newNightMode = AppCompatDelegate.MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(newNightMode)
            editor.putInt("night_mode", newNightMode)
        }

        editor.apply()
    }

    fun colorRandom(): Int {
        val numero = (0..3).random()
        var color = 0
        when (numero) {
            0 -> color = R.color.rojo
            1 -> color = R.color.verde
            2 -> color = R.color.azul
            3 -> color = R.color.amarillo
        }
        return color
    }

    fun getScreenPercentDp(
        context: Context,
        percent: Double
    ): Int {
        // Get screen height
        val displayMetrics = context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        // Calculate percent height
        return (screenHeight * percent).toInt()
    }

    fun getIndexTypeMap(context: Context): Int {
        val nameShared = getString(context, R.string.nombre_shared_preferences)
        val sharedPreferences = context.getSharedPreferences(nameShared, Context.MODE_PRIVATE)

        val savedTypeCode = sharedPreferences.getInt(
            "type_map_user",
            GoogleMap.MAP_TYPE_NORMAL // Valor por defecto
        )

        val refs = mapOf(
            "Mapa Normal" to GoogleMap.MAP_TYPE_NORMAL,
            "Mapa Hybrido" to GoogleMap.MAP_TYPE_HYBRID,
            "Mapa Satelital" to GoogleMap.MAP_TYPE_SATELLITE,
            "Mapa Relieve" to GoogleMap.MAP_TYPE_TERRAIN
        )

        val mapTypeList = refs.keys.toList()

        val currentSelection = refs.entries.find { it.value == savedTypeCode }?.key
        return mapTypeList.indexOf(currentSelection)
    }

    fun getSavedTypeMap(context: Context): Int {
        val nameShared = getString(context, R.string.nombre_shared_preferences)
        val sharedPreferences = context.getSharedPreferences(nameShared, Context.MODE_PRIVATE)

        val savedTypeCode = sharedPreferences.getInt(
            "type_map_user",
            GoogleMap.MAP_TYPE_NORMAL // Valor por defecto
        )

        return savedTypeCode
    }

    fun saveAdsShowState(context: Context, value: Boolean) {
        val nameShared = context.getString(R.string.nombre_shared_preferences)
        val nameSharedValue = context.getString(R.string.name_shared_ads_restriction)
        val sharedPreferences = context.getSharedPreferences(nameShared, Context.MODE_PRIVATE)

        // Save value
        sharedPreferences.edit()
            .putBoolean(nameSharedValue, value)
            .apply()
    }

    fun loadAds(contexto: Context, adViewElement: AdView, interestView: ImageView?) {
        val str = contexto.getString(R.string.name_shared_ads_restriction)
        val state = UtilidadesMenores().readSharedBooleanPref(contexto, str)

        if (state) {
            adViewElement.visibility = View.GONE
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                // Ad request
                val adRequest = AdRequest.Builder().build()

                withContext(Dispatchers.Main) {
                    adViewElement.loadAd(adRequest)
                }
            }
        }

        //Interest element show if not have ads restriction and vice
        if (interestView != null) {
            if (state) {
                interestView.visibility = View.GONE
            } else {
                interestView.visibility = View.VISIBLE
            }
        }
    }

    fun convertTimeNumToDate(timeNum: Long): String {
        val date = Date(timeNum)

        // Define format date: "d 'de' MMMM 'de' yyyy"
        val dateFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))

        return dateFormat.format(date)
    }


    fun handlePurchaseState(purchaseState: Int): String {
        return when (purchaseState) {
            0 -> "Estado no especificado"
            1 -> "Comprado"
            2 -> "Pendiente"
            else -> "Estado desconocido"
        }
    }

    fun reportar(context: Context, instanciaMapa: Mapa? = null, opcionActual: String) {
        val ubiUser = Mapa.ubiUser //ubicacion del usuario
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

        // Agregar un CheckBox para permitir al usuario elegir si desea enviar la ubicación
        val checkBox = CheckBox(context)
        checkBox.setTextColor(Color.GRAY)
        checkBox.text = context.getString(R.string.option_add_ubi_to_report)

        if (isNightMode()) {
            input.setTextColor(Color.WHITE)
            checkBox.setTextColor(Color.WHITE)
        }

        layout.addView(input)

        if (instanciaMapa != null) { // Verificar si se proporcionó una instancia del mapa
            layout.addView(checkBox)
        }

        // Establecer el LinearLayout como la vista del cuadro de diálogo
        builder.setView(layout)
        builder.setPositiveButton("Enviar") { _, _ ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result
                    if (comprobarConexion(context)) {

                        // Obtener el texto ingresado por el usuario
                        val texto = input.text.toString()

                        if (texto.isEmpty() || texto.isBlank()) {
                            crearAlertaSencilla(
                                context,
                                context.getString(R.string.empty_eport_error)
                            )

                        } else {
                            CoroutineScope(Dispatchers.IO).launch {

                                val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()

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
                                        instanciaMapa?.requestLocationPermission()
                                    }

                                    var contador = 0
                                    val tiempoMaximo = 10 // segundos
                                    val intervaloEspera = 1000L // 1 segundo

                                    while (contador < tiempoMaximo && (ubiUser.latitude == 0.0 && ubiUser.longitude == 0.0)) {
                                        delay(intervaloEspera)
                                        contador++
                                    }

                                    ubicacion =
                                        if (ubiUser.latitude == 0.0 && ubiUser.longitude == 0.0) {
                                            "No proporcionada"
                                        } else {
                                            ubiUser.toString()
                                        }
                                }
                                //si no selecciona enviar ubicacion
                                if (!checkBox.isChecked) {
                                    ubicacion = "No proporcionada"
                                }

                                // Obtener el token de registro
                                var tokenIdApp: String
                                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        tokenIdApp = task.result


                                        val ref: DatabaseReference =
                                            firebase.getReference(
                                                context.getString(
                                                    R.string.ruta_reportes_db_nube
                                                )
                                            )
                                        val e = ref.child("reportsUser").push()

                                        // Crear un nuevo nodo con el ID único, fecha, texto y ubicación del reporte
                                        val nuevoReporte = mapOf(
                                            "id" to e.key,
                                            "dateReport" to fechaFormateada,
                                            "timeReport" to horaFormateada,
                                            "situationReport" to texto,
                                            "location" to ubicacion,
                                            "currentTask" to opcionActual,
                                            "idUser" to userId,
                                            "statusCheckedView" to false, // Indicate if the user had view the noti on ReportsUser screen
                                        )

                                        // Subir el nuevo reporte a la base de datos de Firebase
                                        e.setValue(nuevoReporte).addOnCompleteListener { subida ->
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
                                                println("Error al enviar el reporte: ${subida.exception}")
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
                        crearAlertaSencilla(
                            context,
                            "No hay conexión, inténtalo de nuevo más tarde"
                        )
                    }
                }
            }
        }
        builder.show()
    }

    fun registLastConectionUser() {
        var tokenIdApp: String
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                tokenIdApp = task.result

                val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()

                val ref: DatabaseReference =
                    firebase.getReference(
                        "features/0/users/$tokenIdApp/lastConnection"
                    )

                val str = obtenerUltimaConexion()
                ref.setValue(str)
            }
        }
    }

    private fun obtenerUltimaConexion(): String {
        val calendario = Calendar.getInstance()
        val formatoHora = SimpleDateFormat("h:mm a", Locale("es", "ES"))
        val formatoFecha = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES"))

        val hora = formatoHora.format(calendario.time).lowercase()
        val fecha = formatoFecha.format(calendario.time)

        return "Último inicio de conexión a las $hora del $fecha"
    }
}
