package com.example.appincidencias

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.appincidencias.ui.CrearIncidenciaActivity
import com.example.appincidencias.ui.ListaIncidenciasActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        crearCanalNotificacion()
        verificarNuevasAsignaciones()

        findViewById<Button>(R.id.btnCrearIncidencia).setOnClickListener {
            startActivity(Intent(this, CrearIncidenciaActivity::class.java))
        }

        findViewById<Button>(R.id.btnLista).setOnClickListener {
            startActivity(Intent(this, ListaIncidenciasActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Lógica de alarma/notificación para el Guardia
    private fun verificarNuevasAsignaciones() {
        val user = auth.currentUser ?: return

        // Escuchar en tiempo real incidencias asignadas a mi (Guardia)
        db.collection("incidencias")
            .whereEqualTo("asignadoA", user.uid)
            .whereEqualTo("estado", "asignada")
            .addSnapshotListener { value, _ ->
                if (value != null && !value.isEmpty) {
                    // Hay incidencias nuevas asignadas, lanzar alarma
                    lanzarNotificacion("Nueva Incidencia Asignada", "Tienes incidencias pendientes de revisar.")
                }
            }
    }

    private fun lanzarNotificacion(titulo: String, mensaje: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, "canal_incidencias")
            .setSmallIcon(R.mipmap.ic_launcher_round) // Asegúrate de tener icono
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Esto activa sonido y vibración (Alarma)
            .setAutoCancel(true)

        manager.notify(1, builder.build())
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "canal_incidencias",
                "Avisos de Incidencias",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Canal para alarmas de incidencias urgentes"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}