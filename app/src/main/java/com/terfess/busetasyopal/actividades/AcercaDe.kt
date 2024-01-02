package com.terfess.busetasyopal.actividades

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.terfess.busetasyopal.R
import java.net.URLEncoder

class AcercaDe : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acerca_de)

        //actionbar transparente
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //---------------------------------------------------------------------------
        val activarHorarios = findViewById<Button>(R.id.verInfoHorarios)
        val infoHorarios = findViewById<LinearLayoutCompat>(R.id.horarios)
        activarHorarios.setOnClickListener {//mostrar/ocultar informacion de horarios
            if (infoHorarios.visibility == View.GONE){
                infoHorarios.visibility = View.VISIBLE
                activarHorarios.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_flecha_abajo,
                    0
                ) // cambiar el icono al final del boton
            }else{
                infoHorarios.visibility = View.GONE
                activarHorarios.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_flecha_aderecha,
                    0
                ) // cambiar el icono al final del boton
            }
        }

        val whatsappConeccion = findViewById<ImageButton>(R.id.contc_wts)
        val mailConection = findViewById<ImageButton>(R.id.contc_mail)

        whatsappConeccion.setOnClickListener{ //contectar a desarrollador por whatsapp
            enviarMensajeWhatsApp(
                "+573219822620",
                "Buenas, quiero hacer una felicitación/peticion/queja."
            )
        }
        mailConection.setOnClickListener{//contectar a desarrollador por mail
            abrirAppCorreo("terdevfess@gmail.com")
        }

        //_________LINK CLICKABLE____________________
        val textView: TextView = findViewById(R.id.link_alcaldia) // Asegúrate de cambiar a tu TextView

        val textoConEnlace = getString(R.string.enlace_alcaldia)

        // Configura el TextView para que sea clickable y muestre el enlace
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = textoConEnlace

        //cuando el usuario toca el textView se ejecuta un intent hacia la url de la alcaldia de Yopal
        textView.setOnClickListener {
            // Abre el enlace directamente al hacer clic en el TextView
            val uri = Uri.parse("https://www.yopal-casanare.gov.co/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    //enviar whatsapp
    private fun enviarMensajeWhatsApp(numTel: String, msj: String) {
        // Codificar el número de teléfono en formato E.164
        val numeroTelefonoCodificado = numTel.replace(" ", "")

        // Crear el URI para abrir WhatsApp con el número de teléfono y mensaje
        val uri = Uri.parse(
            "https://wa.me/$numeroTelefonoCodificado?text=${
                URLEncoder.encode(
                    msj,
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