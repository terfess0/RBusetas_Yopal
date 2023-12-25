package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import javax.security.auth.callback.Callback

interface AlertaCallback {
    fun onOpcionSeleccionada(opcion: Int, es_ajuste_permiso: Boolean)
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
        builder.setMessage(mensaje)
            .setPositiveButton(op1) { _, _ ->
                if (tipo_solicitud == "ubicacion") {
                    Callback.onOpcionSeleccionada(2, true) //opcion lado derecho
                }
            }
        builder.setNegativeButton(op2) { _, _ ->
            if (tipo_solicitud == "ubicacion") {
                Callback.onOpcionSeleccionada(1, true) //opcion lado izquierdo
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun crearToast(contexto: Context, mensaje: String) { //crear mensaje toast
        Toast.makeText(
            contexto,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
}