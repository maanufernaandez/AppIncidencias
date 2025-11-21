package com.example.appincidencias

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMsgService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token generado: $token")
        // Aquí es donde se guardaría el token en la base de datos si
        // quisieras enviar notificaciones Push desde un servidor en el futuro.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Este método se activaría si recibes una notificación real de Firebase Cloud Messaging.
        // De momento lo dejamos vacío porque ya tienes un sistema de alertas local en MainActivity.
    }
}