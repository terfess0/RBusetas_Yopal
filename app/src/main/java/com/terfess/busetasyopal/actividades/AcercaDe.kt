package com.terfess.busetasyopal.actividades

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.view.AdminPanel
import com.terfess.busetasyopal.admin.view.LoginAdmin
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.services.AuthFirebase
import java.net.URLEncoder

class AcercaDe : AppCompatActivity() {
    private lateinit var adView: AdView
    private val instUtilidades = UtilidadesMenores()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acerca_de)

        adView = findViewById(R.id.adViewAcerca)
        instUtilidades.loadAds(this, adView, null)

        //save mode night/light
        instUtilidades.applySavedNightMode(this)

        //actionbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarAcercade)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Sobre la App"
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = instUtilidades.getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))
        //..

        //---------------------------------------------------------------------------
        val activarHorarios = findViewById<Button>(R.id.verInfoHorarios)
        val infoHorarios = findViewById<LinearLayoutCompat>(R.id.horarios)
        activarHorarios.setOnClickListener {//mostrar/ocultar informacion de horarios
            if (infoHorarios.visibility == View.GONE) {
                infoHorarios.visibility = View.VISIBLE
                activarHorarios.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_flecha_abajo,
                    0
                ) // cambiar el icono al final del boton
            } else {
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

        whatsappConeccion.setOnClickListener { //contectar a desarrollador por whatsapp
            enviarMensajeWhatsApp(
                "+573225857486",
                "Buenas, quiero hacer una felicitación/peticion/queja o informar sobre información que deben actualizar."
            )
        }
        mailConection.setOnClickListener {//contctar a desarrollador por mail
            abrirAppCorreo("terdevfess@gmail.com")
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.panelAdmin -> {

                val authInstance = AuthFirebase()
                val isLogged = authInstance.isUserLoggedIn()
                val isAnonymous = authInstance.sessionIsAnonymously()

                //check if user admin is logged not anonymous
                if (isLogged && !isAnonymous) {
                    val intent = Intent(this, AdminPanel::class.java)
                    startActivity(intent)
                } else {
                    //close session and go to login
                    authInstance.logOutSessionUser()
                    val intent = Intent(this, LoginAdmin::class.java)
                    startActivity(intent)
                }
            }

            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
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

    override fun onResume() {
        super.onResume()
        instUtilidades.loadAds(this, adView, null)
    }
}