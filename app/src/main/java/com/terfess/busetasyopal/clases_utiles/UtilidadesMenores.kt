package com.terfess.busetasyopal.clases_utiles

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


interface AlertaCallback { //devolucion de llamada para el CrearAlerta
    fun onOpcionSeleccionada(opcion: Int, tipo_de_solicitud:String)
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
    ){
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
            toast = Toast.makeText(it, mensaje, Toast.LENGTH_LONG)
            toast?.show()
        }
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

}