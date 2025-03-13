package pointererp.com.mensajeyape

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                NotificationReaderScreen()


                Button(
                    onClick = {
                        sendDataToGoogleSheets()
                    },
                    modifier = Modifier.background(color = Color.Yellow)
                ) {
                    Text("Enviar")
                }
            }
        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task.result)
            }
        }

    private fun handleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) {
            println("Autenticación exitosa: ${account.email}")
            //  sendDataToGoogleSheets()
        } else {
            println("Error: No se pudo autenticar")
        }
    }

    private fun sendDataToGoogleSheets() {
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
                    val range = "Hoja 1!A$rowIndex:B$rowIndex"
                    val result = sheetsService.spreadsheets().values()
                        .get(spreadsheetId, range)
                        .execute()
                    val existingValues = result.getValues()
                    if (existingValues.isNullOrEmpty() || existingValues[0].isEmpty() || existingValues[0][0].toString()
                            .isEmpty()
                    ) {
                        val values = listOf(
                            listOf("Mensaje", "Este es un mensaje de prueba")
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

}