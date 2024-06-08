package com.terfess.busetasyopal.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class NotificationChannelHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID_SIMPLE = "CHANNEL_SIMPLE"
        private const val CHANNEL_NAME_SIMPLE = "Notificaciones Simples"
        private const val CHANNEL_DESCRIPTION_SIMPLE = "Canal para noticias y novedades asi como respuestas a reportes"

        private const val CHANNEL_ID_LINK = "CHANNEL_UPDATE"
        private const val CHANNEL_NAME_LINK = "Notificaciones de Actualización"
        private const val CHANNEL_DESCRIPTION_LINK = "Canal para notificar sobre actualizaciones nuevas de la App"
    }

    fun crearCanalesNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_SIMPLE,
                    CHANNEL_NAME_SIMPLE,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = CHANNEL_DESCRIPTION_SIMPLE
                },
                NotificationChannel(
                    CHANNEL_ID_LINK,
                    CHANNEL_NAME_LINK,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION_LINK
                }
            )

            // Register the channels with the NotificationManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
