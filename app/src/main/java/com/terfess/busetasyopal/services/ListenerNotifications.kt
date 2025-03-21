package com.terfess.busetasyopal.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.firebase.database.*
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.PantallaPrincipal
import com.terfess.busetasyopal.actividades.Splash

class ListenerNotifications(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("features/0/noti")

    override fun doWork(): Result {
        Log.d("WorkManager", "⏳ Iniciando escucha de Firebase en segundo plano")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nuevoValor = snapshot.value.toString()
                    Log.d("WorkManager", "🔥 Cambio detectado en Firebase: $nuevoValor")
                    enviarNotificacion("Datos actualizados", "Nuevo valor: $nuevoValor")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WorkManager", "❌ Error en Firebase: ${error.message}", error.toException())
            }
        })

        return Result.success()
    }

    private fun enviarNotificacion(titulo: String, mensaje: String) {
        val channelId = "firebase_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones Firebase",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de cambios en Firebase"
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, Splash::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
