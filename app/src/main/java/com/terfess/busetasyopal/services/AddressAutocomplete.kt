package com.terfess.busetasyopal.services

import android.content.Context
import android.os.AsyncTask
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

//FUERA DE USO, API DE OPENSTREETMAP AUTOCOMPLETE
class AddressAutocomplete(
    private val autoCompleteTextView: AutoCompleteTextView,
    private val context: Context
) {

    fun start() {
        autoCompleteTextView.setAdapter(null)
        autoCompleteTextView.threshold = 1
        val text = autoCompleteTextView.text.toString()
        if (text.isNotBlank()) {
            val url = "https://nominatim.openstreetmap.org/search?q=$text&countrycodes=CO&format=json&limit=5"
            AddressAutocompleteTask().execute(url)
        }
    }

    private inner class AddressAutocompleteTask : AsyncTask<String, Void, List<AddressResult>>() {

        override fun doInBackground(vararg params: String?): List<AddressResult> {
            val urlStr = params[0]
            val addressResults = mutableListOf<AddressResult>()
            try {
                val url = URL(urlStr)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String? = bufferedReader.readLine()
                    while (line != null) {
                        response.append(line)
                        line = bufferedReader.readLine()
                    }
                    bufferedReader.close()

                    val jsonArray = JSONArray(response.toString())
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val displayName = jsonObject.optString("display_name")
                        val lat = jsonObject.getString("lat").toDouble()
                        val lon = jsonObject.getString("lon").toDouble()
                        addressResults.add(AddressResult(displayName, LatLng(lat, lon)))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return addressResults
        }

        override fun onPostExecute(result: List<AddressResult>?) {
            super.onPostExecute(result)
            if (result != null) {
                val suggestions = result.map { it.displayName }
                val adapter =
                    ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, suggestions)
                autoCompleteTextView.setAdapter(adapter)
                autoCompleteTextView.showDropDown()
                autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                    val selectedAddress = result[position]
                    // Aquí puedes manejar las coordenadas resultantes, por ejemplo, puedes llamar a un método de tu actividad
                    // para agregar un marcador en el mapa con estas coordenadas
                    val latLng = selectedAddress.latLng
                    Toast.makeText(context, "Coordenadas: $latLng", Toast.LENGTH_SHORT).show()

                    // Quitar el foco y limpiar el texto del AutoCompleteTextView
                    autoCompleteTextView.clearFocus()
                    autoCompleteTextView.setText("")

                    // Ocultar la lista de sugerencias
                    autoCompleteTextView.dismissDropDown()
                }
            }
        }

    }

    data class AddressResult(val displayName: String, val latLng: LatLng)
}
