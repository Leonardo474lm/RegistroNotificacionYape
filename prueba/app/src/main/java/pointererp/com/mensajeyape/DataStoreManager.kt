import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
object NotificationState {
    var notificationText by mutableStateOf("")
}
class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            val notificationText = event.text.joinToString(", ")
            NotificationState.notificationText = notificationText
        }
    }

    override fun onInterrupt() {
    }
}

