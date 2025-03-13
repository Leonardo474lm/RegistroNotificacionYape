package pointererp.com.mensajeyape

import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyNotificationListenerService : NotificationListenerService() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn != null) {
            val packageName = sbn.packageName
            val notificationTitle = sbn.notification.extras.getString("android.title")
            val notificationText = sbn.notification.extras.getString("android.text")
            val time = sbn.postTime
            val localDateTime = Instant.ofEpochMilli(time)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val fecha = localDateTime.format(dateFormatter)
            val hora = localDateTime.format(timeFormatter)
            Log.d("GoogleSheets", "$fecha    :-:   $hora")
            if (packageName == "com.bcp.innovacxion.yapeapp") {
                if(notificationTitle=="Confirmación de Pago"){
                    sendDataToGoogleSheets(notificationTitle, notificationText, fecha, hora);
                }

                Log.d(
                    "NotificationListener",
                    "Notificación de YAPE: $notificationTitle - $notificationText"
                )

            }
        }


    }

    private fun sendDataToGoogleSheets(title: String?, text: String?, fecha: String, hora: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = resources.openRawResource(R.raw.credentials)
                val credentials = GoogleCredential.fromStream(inputStream)
                    .createScoped(listOf(SheetsScopes.SPREADSHEETS))

                val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                val sheetsService =
                    Sheets.Builder(httpTransport, GsonFactory.getDefaultInstance(), credentials)
                        .setApplicationName("Mi Aplicación")
                        .build()
                val spreadsheetId = "1v5J7-zgZWQiqKIf35YHxtCSYABQmAm5UH1Jv_cZ46uQ"
                var rowIndex = 1
                while (true) {
                    val range = "Hoja 1!A$rowIndex:D$rowIndex"
                    val result = sheetsService.spreadsheets().values()
                        .get(spreadsheetId, range)
                        .execute()
                    val existingValues = result.getValues()
                    if (existingValues.isNullOrEmpty() || existingValues[0].isEmpty() || existingValues[0][0].toString()
                            .isEmpty()
                    ) {
                        val values = listOf(
                            listOf(title, text, fecha, hora)
                        )
                        val body = ValueRange().setValues(values)
                        try {
                            val updateResult = sheetsService.spreadsheets().values()
                                .update(spreadsheetId, range, body)
                                .setValueInputOption("RAW")
                                .execute()

                            Log.d("GoogleSheets", "Fila actualizada: ${updateResult.updatedCells}")
                            break  // Salir del ciclo después de actualizar la fila
                        } catch (e: Exception) {
                            Log.e("GoogleSheets", "Error al enviar datos", e)
                        }
                    } else {
                        rowIndex++
                    }
                }


            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("GoogleSheets", "Error al enviar datos", e)
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {

    }
}

