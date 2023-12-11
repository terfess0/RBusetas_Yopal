package com.terfess.busetasyopal.actividades

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import com.terfess.busetasyopal.clases_utiles.DatosDeFirebase
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.DatosASqliteLocal
import com.terfess.busetasyopal.clases_utiles.allDatosRutas
import com.terfess.busetasyopal.modelos_dato.EstructuraDatosBaseDatos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder

class AcercaDe : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acerca_de)

        val whatsappConeccion = findViewById<ImageButton>(R.id.contc_wts)
        val mailConection = findViewById<ImageButton>(R.id.contc_mail)

        whatsappConeccion.setOnClickListener{
            enviarMensajeWhatsApp(
                "+573225857486",
                "Buenos dias, quiero hacer una felicitación/peticion/queja."
            )
        }
        mailConection.setOnClickListener{
            abrirAppCorreo("terdevfess@gmail.com")
        }
    }

    //enviar whatsapp
    private fun enviarMensajeWhatsApp(numeroTelefono: String, mensaje: String) {
        // Codificar el número de teléfono en formato E.164
        val numeroTelefonoCodificado = numeroTelefono.replace(" ", "")

        // Crear el URI para abrir WhatsApp con el número de teléfono y mensaje
        val uri = Uri.parse(
            "https://wa.me/$numeroTelefonoCodificado?text=${
                URLEncoder.encode(
                    mensaje,
                    "UTF-8"
                )
            }"
        )

        // Crear el Intent
        val intent = Intent(Intent.ACTION_VIEW, uri)

        // Especificar que el Intent debe ser manejado por WhatsApp
        intent.setPackage("com.whatsapp")

        // Iniciar la actividad
        startActivity(intent)
    }


    //enviar correo
    private fun abrirAppCorreo(destinatario: String) {
        // Crear un Intent con la acción ACTION_SENDTO
        val intent = Intent(Intent.ACTION_SENDTO)

        // Especificar el correo electrónico destinatario en la URI
        intent.data = Uri.parse("mailto:$destinatario")

        // Agregar un campo asunto al mensaje de correo electrónico
        intent.putExtra(Intent.EXTRA_SUBJECT, "Contacto desarrollador")

        // Iniciar la actividad para abrir la aplicación de correo electrónico externa
        startActivity(intent)
    }
}