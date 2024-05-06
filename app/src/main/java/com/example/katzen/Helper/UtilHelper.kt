package com.example.katzen.Helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import com.example.katzen.Config.Config
import com.example.katzen.Model.VentaMesModel
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.Calendar

class UtilHelper {

    companion object{
        fun getDateYear() : String {
            val time = Calendar.getInstance().time
            val formatter = SimpleDateFormat("yyyy")

            return formatter.format(time).toString()
        }
        fun getDate() : String {
            val time = Calendar.getInstance().time
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

            return formatter.format(time).toString()
        }
        fun View.hideKeyboard() {
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(windowToken, 0)
        }
        fun getMonthYear(position: Int) : String {
            var mes = ""
            when(position){
                1 -> {
                    mes = "ENE"
                }
                2 -> {
                    mes = "FEB"
                }
                3 -> {
                    mes = "MAR"
                }
                4 -> {
                    mes = "ABR"
                }
                5 -> {
                    mes = "MAY"
                }
                6 -> {
                    mes = "JUN"
                }
                7 -> {
                    mes = "JUL"
                }
                8 -> {
                    mes = "AGO"
                }
                9 -> {
                    mes = "SEP"
                }
                10 -> {
                    mes = "OCT"
                }
                11 -> {
                    mes = "NOV"
                }
                12 -> {
                    mes = "DIC"
                }
            }
            return mes
        }
        fun getMontsThisYears() : ArrayList<String>{
            var listMonts = arrayListOf<String>()

            listMonts.add("01-${getDateYear()}")
            listMonts.add("02-${getDateYear()}")
            listMonts.add("03-${getDateYear()}")
            listMonts.add("04-${getDateYear()}")
            listMonts.add("05-${getDateYear()}")
            listMonts.add("06-${getDateYear()}")
            listMonts.add("07-${getDateYear()}")
            listMonts.add("08-${getDateYear()}")
            listMonts.add("09-${getDateYear()}")
            listMonts.add("10-${getDateYear()}")
            listMonts.add("11-${getDateYear()}")
            listMonts.add("12-${getDateYear()}")

            return listMonts
        }
        fun hideKeyBoardWorld(activity : Activity,view : View){
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view!!.getWindowToken(), 0)
        }
        fun expandirUrlGoogleMaps(activity: Activity,urlCorta: String) {
            Thread {
                try {
                    // Conectar a la URL corta
                    val connection = URL(urlCorta).openConnection() as HttpURLConnection
                    connection.instanceFollowRedirects = false
                    // Obtener la URL completa desde la cabecera de redirección
                    val expandedUrl = connection.getHeaderField("Location")
                    connection.disconnect()

                    // Verificar si la URL expandida no es nula ni vacía
                    if (!expandedUrl.isNullOrBlank()) {
                        // Llamar a la función para abrir Google Maps con la URL completa
                        abrirGoogleMaps(activity, expandedUrl)
                    } else {
                        // Mostrar un mensaje de error si la URL expandida es nula o vacía
                        activity.runOnUiThread {
                            DialogMaterialHelper.mostrarErrorDialog(activity, "No se pudo obtener la URL completa de Google Maps.")
                        }
                    }
                } catch (e: Exception) {
                    // Capturar y mostrar cualquier error que ocurra durante la expansión de la URL
                    activity.runOnUiThread {
                        DialogMaterialHelper.mostrarErrorDialog(activity, "Error al expandir la URL de Google Maps: ${e.message}")
                    }
                }
            }.start()
        }
        fun abrirGoogleMaps(activity: Activity, urlGoogleMaps: String) {
            if (urlGoogleMaps.isNotEmpty()) {
                try {
                    if (validarURLGoogleMaps(urlGoogleMaps)) {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(urlGoogleMaps)
                        )
                        if (intent.resolveActivity(activity.packageManager) != null) {
                            activity.startActivity(intent)
                        } else {
                            DialogMaterialHelper.mostrarErrorDialog(activity, "No se puede abrir Google Maps. No hay aplicaciones compatibles.")
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(activity, "La URL no es válida de Google Maps.")
                    }
                } catch (e: Exception) {
                    DialogMaterialHelper.mostrarErrorDialog(activity, "Error al abrir Google Maps: ${e.message}")
                }
            } else {
                DialogMaterialHelper.mostrarErrorDialog(activity, "No tiene una dirección relacionada.")
            }
        }
        fun enviarMensajeWhatsApp(activity: Activity, numeroTelefono: String) {
            try {
                // Validar que el número de teléfono no esté vacío
                if (numeroTelefono.isNotEmpty()) {
                    // Crear un intent para abrir WhatsApp con el número de teléfono
                    val uri = Uri.parse("https://wa.me/$numeroTelefono")
                    val intent = Intent(Intent.ACTION_VIEW, uri)

                    // Abrir WhatsApp
                    activity.startActivity(intent)
                } else {
                    // Mostrar un mensaje si el número de teléfono está vacío
                    DialogMaterialHelper.mostrarErrorDialog(activity, "El número de teléfono está vacío.")
                }
            } catch (e: Exception) {
                // Mostrar un mensaje si ocurre un error al intentar abrir WhatsApp
                DialogMaterialHelper.mostrarErrorDialog(activity, "Error al abrir WhatsApp: ${e.message}")
            }
        }
        fun llamarCliente(activity: Activity, phoneNumber: String) {
            if (phoneNumber.isNotEmpty()) {
                try {
                    // Validar el número de teléfono (opcional)
                    if (phoneNumber.matches(Regex("\\d+"))) { // Verifica si el número contiene solo dígitos
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phoneNumber")
                        }
                        activity.startActivity(intent)
                    } else {
                        // Mostrar un mensaje de error si el número de teléfono no es válido
                        DialogMaterialHelper.mostrarErrorDialog(activity, "El número de teléfono no es válido.")
                    }
                } catch (e: Exception) {
                    // Mostrar un mensaje de error si ocurre una excepción al intentar llamar
                    DialogMaterialHelper.mostrarErrorDialog(activity, "Error al llamar al cliente: ${e.message}")
                }
            } else {
                // Mostrar un mensaje si el número de teléfono está vacío
                DialogMaterialHelper.mostrarErrorDialog(activity, "El número de teléfono está vacío.")
            }
        }
        fun enviarCorreoElectronicoGmail(activity: Activity, correoDestinatario: String) {
            try {
                // Crear un intent para enviar correo electrónico con Gmail
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822" // Especificar el tipo de contenido como correo electrónico
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(correoDestinatario)) // Agregar el destinatario del correo
                    putExtra(Intent.EXTRA_SUBJECT, "") // Asunto vacío
                    putExtra(Intent.EXTRA_TEXT, "") // Cuerpo del correo vacío
                }

                // Verificar si hay alguna aplicación que pueda manejar el intent
                if (intent.resolveActivity(activity.packageManager) != null) {
                    // Abrir la aplicación de Gmail
                    activity.startActivity(intent)
                } else {
                    // Mostrar un mensaje si no se encuentra ninguna aplicación que pueda manejar el intent
                    DialogMaterialHelper.mostrarErrorDialog(activity, "No se pudo abrir la aplicación de Gmail.")
                }
            } catch (e: Exception) {
                // Mostrar un mensaje si ocurre un error al intentar abrir la aplicación de Gmail
                DialogMaterialHelper.mostrarErrorDialog(activity, "Error al abrir la aplicación de Gmail: ${e.message}")
            }
        }

        fun validarURLGoogleMaps(url: String): Boolean {
            // Patrón de la URL de Google Maps
            val patron = Regex("^https?://(?:www\\.)?google\\.(?:com?/maps)(?:\\?.*)?\$")
            // Comprobación de la coincidencia
            return patron.matches(url)
        }

    }
}