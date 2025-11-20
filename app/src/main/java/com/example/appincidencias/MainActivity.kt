package com.example.appincidencias

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.appincidencias.ui.CrearIncidenciaActivity
import com.example.appincidencias.ui.DetalleIncidenciaActivity
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
                    // CAMBIO 1: Obtenemos el ID de la primera incidencia que encontremos
                    val documento = value.documents[0]
                    val idIncidencia = documento.id

                    lanzarNotificacion("Nueva Incidencia Asignada", "Tienes incidencias pendientes de revisar.", idIncidencia)
                }
            }
    }

    private fun lanzarNotificacion(titulo: String, mensaje: String, idIncidencia: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // CAMBIO 2: Creamos el Intent para ir al Detalle
        val intent = Intent(this, DetalleIncidenciaActivity::class.java).apply {
            putExtra("id", idIncidencia)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Creamos el PendingIntent que la notificación ejecutará al pulsarse
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "canal_incidencias")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // <--- AQUÍ VINCULAMOS EL CLICK

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