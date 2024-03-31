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
import java.util.UUID

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // TODO(developer): Handle FCM messages here.
// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        println("From: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            println("Message Notification Body: ${it.body}")
        }

        println("recibida noti ")
        val link = remoteMessage.data["link"]
        if (link != null) {
            println("1")
            // Mostrar la notificación al usuario
            mostrarNotificacion(
                remoteMessage.notification?.title,
                remoteMessage.notification?.body,
                link
            )
        } else {
            println("2")
            // Si no hay un enlace presente en los datos adicionales, puedes mostrar la notificación sin el enlace o tomar alguna otra acción según tus necesidades.
            // Mostrar la notificación al usuario sin el enlace
            mostrarNotificacion(
                remoteMessage.notification?.title,
                remoteMessage.notification?.body,
                null // Pasar null como enlace
            )
        }

    }

    private fun mostrarNotificacion(title: String?, body: String?, link: String?) {
        println("noti = $link")
        val notificationId = UUID.randomUUID().hashCode()

        val openLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        openLinkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Asegúrate de que el intent se abra en una nueva tarea

        // Crear un PendingIntent para abrir el enlace cuando se hace clic en la notificación
        val pendingIntent = PendingIntent.getActivity(this, 0, openLinkIntent, PendingIntent.FLAG_IMMUTABLE)

        // Construir la notificación
        val notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_UPDATE")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_noti)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Para cerrar la notificación cuando se hace clic en ella

        // Mostrar la notificación
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }

            notify(notificationId, notificationBuilder.build())
        }
    }
}