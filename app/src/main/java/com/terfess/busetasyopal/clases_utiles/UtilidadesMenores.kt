package com.terfess.busetasyopal.clases_utiles

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import androidx.core.view.setPadding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.app
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.Mapa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.coroutineContext


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
        val builder = AlertDialog.Builder(contexto)
        builder.setTitle("Alerta!")
        builder.setMessage(mensaje)
            .setPositiveButton(op1) { _, _ ->
                if (tipo_solicitud == "ubicacion") {
                    Callback.onOpcionSeleccionada(2, "permiso_ubicacion") //opcion lado derecho
                }
            }
        builder.setNegativeButton(op2) { _, _ ->
            if (tipo_solicitud == "ubicacion") {
                Callback.onOpcionSeleccionada(1, "permiso_ubicacion") //opcion lado izquierdo
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

    var toast: Toast? = null // variable toast "global" intencion no acumulacion de toast

    fun crearToast(contexto: Context?, mensaje: String) {
        contexto?.applicationContext?.let {//si contexto y contexo aplicacion no son nulos entonces let ejecutara el bloque de codigo en {}
            toast?.cancel() // cancelar el toast anterior si existe
            toast = Toast.makeText(it, mensaje, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }

    fun crearSnackbar(mensaje: String, rootView: View) {
        Snackbar.make(rootView, mensaje, Snackbar.LENGTH_SHORT).show()
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
        val subTitulo = theme.resolveAttribute(androidx.appcompat.R.attr.subtitleTextColor, typedValue2, true)

        return String.format("#%06X", typedValue.data and 0xFFFFFF)
    }

    fun colorSubTituloTema(contexto: Context): String {

        val theme = contexto.theme
        val typedValue2 = TypedValue()

        val subTitulo = theme.resolveAttribute(androidx.appcompat.R.attr.subtitleTextColor, typedValue2, true)

        return String.format("#%06X", typedValue2.data and 0xFFFFFF)
    }


    fun reportar(context: Context, instanciaMapa: Mapa? = null, opcion_actual:String) {
        var ubiUser = Mapa.ubiUser ?: LatLng(0.0, 0.0)
        val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        builder.setTitle("Reportar novedad")


        // Crear un LinearLayout vertical para contener el EditText y el CheckBox
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        // Agregar un EditText para que el usuario ingrese texto
        val input = EditText(context)
        input.hint = "Escribe tu reporte"
        input.setTextColor(Color.WHITE)
        input.setHintTextColor(Color.WHITE)
        input.maxLines = 5
        input.maxEms = 5
        layout.addView(input)

        // Agregar un CheckBox para permitir al usuario elegir si desea enviar la ubicación
        val checkBox = CheckBox(context)
        checkBox.text = "Enviar también ubicación actual"
        checkBox.setTextColor(Color.WHITE)
        if (instanciaMapa != null) { // Verificar si se proporcionó una instancia del mapa
            layout.addView(checkBox)
        }

        // Establecer el LinearLayout como la vista del cuadro de diálogo
        builder.setView(layout)
        builder.setPositiveButton("Enviar") { dialog, which ->
            if (comprobarConexion(context)) {
                // Obtener el texto ingresado por el usuario
                val texto = input.text.toString()
                if (texto.isEmpty()) {
                    crearToast(context, "Reporte vacío, no se envió")
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
                        val ref: DatabaseReference = firebase.getReference(context.getString(R.string.ruta_reportes_db_nube))
                        val nuevoReporteKey = ref.push().key ?: return@launch

                        val currentDate: Date = Calendar.getInstance().time
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val fechaFormateada: String = dateFormat.format(currentDate)
                        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val horaFormateada: String = timeFormat.format(currentDate)

                        var ubicacion = "Ninguna"
                        if (checkBox.isChecked && ubiUser.latitude != 0.0 && ubiUser.longitude != 0.0) {
                            ubicacion = ubiUser.toString()
                        }
                        if (checkBox.isChecked && (ubiUser.latitude == 0.0 && ubiUser.longitude == 0.0)) {
                            instanciaMapa?.activarLocalizacion()
                            instanciaMapa?.irPosGps()
                            while (Mapa.ubiUser.latitude == 0.0 && Mapa.ubiUser.longitude == 0.0) {
                                delay(1000) // Esperar 1 segundo antes de verificar la ubicación nuevamente
                            }
                            ubiUser = Mapa.ubiUser
                            ubicacion = ubiUser.toString()
                        }
                        if (!checkBox.isChecked) {
                            ubicacion = "No proporcionada"
                        }

                        // Crear un nuevo nodo con el ID único, fecha, texto y ubicación del reporte
                        val nuevoReporte = mapOf<String, Any>(
                            "fecha" to fechaFormateada,
                            "hora" to horaFormateada,
                            "situacion" to texto,
                            "ubicacion" to ubicacion,
                            "tareaActual" to opcion_actual
                        )

                        // Subir el nuevo reporte a la base de datos de Firebase
                        ref.child(nuevoReporteKey).setValue(nuevoReporte)
                            .addOnCompleteListener { subida ->
                                if (subida.isSuccessful) {
                                    // La subida fue exitosa
                                    Toast.makeText(
                                        context,
                                        "Reporte enviado exitosamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // La subida falló
                                    Toast.makeText(
                                        context,
                                        "Error al enviar el reporte. Inténtalo de nuevo más tarde",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
            } else {
                crearToast(context, "No hay conexión, inténtalo de nuevo más tarde")
            }
        }

        builder.show()
    }

}
