package com.terfess.busetasyopal.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class NotificationChannelHelper(val context: Context) {

    companion object {
        private const val CHANNEL_ID_SIMPLE = "CHANNEL_SIMPLE"
        private const val CHANNEL_NAME_SIMPLE = "Notificaciones Simples"

        private const val CHANNEL_ID_LINK = "CHANNEL_UPDATE"
        private const val CHANNEL_NAME_LINK = "Notificationes de Actualización"
    }

    fun crearCanalesNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // crear canal para notificaciones simples
            val channelSimple = NotificationChannel(
                CHANNEL_ID_SIMPLE,
                CHANNEL_NAME_SIMPLE,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para noticias y novedades"
            }

            // crear canal para notificar actualizaciones (con link)
            val channelLink = NotificationChannel(
                CHANNEL_ID_LINK,
                CHANNEL_NAME_LINK,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificar sobre actualizaciones nuevas de la App"
            }

            // Register the channels with the NotificationManager
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelSimple)
            notificationManager.createNotificationChannel(channelLink)
        }
    }
}
