package pointererp.com.mensajeyape

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

@Composable
fun NotificationReaderScreen() {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Habilita el servicio de escucha de notificaciones")
        Button(onClick = {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            context.startActivity(intent)
        }) {
            Text("Habilitar servicio de notificaciones")
        }
     }
}

@Composable
fun MessageScreen() {

}