package com.terfess.busetasyopal.services.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.Splash
import com.terfess.busetasyopal.actividades.reports.view.ReportsUser
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface OnGetCallback {
    fun onCallback(notis: List<Triple<String, String, String>>)
}

class ListenerNotifications(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                UserResponsesNotificationsFun().checkNotificationsByFirebaseTokenUser(
                    object : OnGetCallback {
                        override fun onCallback(notis: List<Triple<String, String, String>>) {
                            notis.forEach {
                                enviarNotificacion(it.second, it.third)
                            }
                        }

                    }
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry() // O Result.failure() si no quieres reintentar
        }
    }

    private fun enviarNotificacion(titulo: String, mensaje: String) {
        val channelId = "firebase_channel"
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones Respuestas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre respuestas a sus reportes."
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, ReportsUser::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_noti)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(1, notification)
        Log.d("WorkManager", "✅ Notificación enviada: $titulo - $mensaje")
    }
}
