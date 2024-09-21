package com.terfess.busetasyopal.services

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.Splash
import java.util.UUID

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val link = remoteMessage.data["link"]
        val respuesta = remoteMessage.data["respuesta"]

        when {
            !link.isNullOrEmpty() -> {
                mostrarNotificacion(remoteMessage.notification?.title, remoteMessage.notification?.body, link, "enlace")
            }
            !respuesta.isNullOrEmpty() -> {
                mostrarNotificacion(remoteMessage.notification?.title, remoteMessage.notification?.body, respuesta, "respuesta")
            }
            else -> {
                mostrarNotificacion(remoteMessage.notification?.title, remoteMessage.notification?.body, null, "ninguno")
            }
        }
    }

    private fun mostrarNotificacion(title: String?, body: String?, valor: String?, tipo: String) {
        val notificationId = UUID.randomUUID().hashCode()

        val openLinkIntent = Intent(this, Splash::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            when (tipo) {
                "enlace" -> putExtra("link", valor)
                "respuesta" -> putExtra("respuesta", valor)
            }
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, openLinkIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_UPDATE")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_noti)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Request the missing permissions if needed
                return
            }

            notify(notificationId, notificationBuilder.build())
        }
    }
    
}
