package com.example.appincidencias

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.appincidencias.ui.CrearIncidenciaActivity
import com.example.appincidencias.ui.DetalleIncidenciaActivity
import com.example.appincidencias.ui.ListaIncidenciasActivity
import com.example.appincidencias.ui.ListaUsuariosActivity // Asegúrate de que este archivo existe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        crearCanalNotificacion()
        verificarNuevasAsignaciones()

        // Referencias a los botones del XML
        val btnCrear = findViewById<Button>(R.id.btnCrearIncidencia)
        val btnLista = findViewById<Button>(R.id.btnLista)
        val btnUsuarios = findViewById<Button>(R.id.btnVerUsuarios)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // 1. Por defecto ocultamos todo hasta saber el rol
        btnCrear.visibility = View.GONE
        btnLista.visibility = View.GONE
        btnUsuarios.visibility = View.GONE

        val userId = auth.currentUser?.uid ?: return

        // 2. Comprobamos el rol en Firebase
        db.collection("usuarios").document(userId).get().addOnSuccessListener { doc ->
            val rol = doc.getString("rol") ?: ""

            when (rol) {
                "docente" -> {
                    btnCrear.visibility = View.VISIBLE
                    btnLista.visibility = View.VISIBLE
                    btnLista.text = "Ver Mis Incidencias"
                }
                "guardia" -> {
                    btnLista.visibility = View.VISIBLE
                    btnLista.text = "Ver Incidencias"
                }
                "administrador" -> {
                    btnUsuarios.visibility = View.VISIBLE
                    btnLista.visibility = View.VISIBLE
                    btnLista.text = "Ver Incidencias"
                }
            }
        }

        // 3. Configuración de Listeners (Clicks)
        btnCrear.setOnClickListener {
            startActivity(Intent(this, CrearIncidenciaActivity::class.java))
        }

        btnLista.setOnClickListener {
            val intent = Intent(this, ListaIncidenciasActivity::class.java)
            startActivity(intent)
        }

        btnUsuarios.setOnClickListener {
            // Si te da error aquí es porque no has creado el archivo ListaUsuariosActivity.kt
            startActivity(Intent(this, ListaUsuariosActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // --- Lógica de Notificaciones ---

    private fun verificarNuevasAsignaciones() {
        val user = auth.currentUser ?: return

        // Escuchar si hay incidencias asignadas a mí (Guardia)
        db.collection("incidencias")
            .whereEqualTo("asignadoA", user.uid)
            .whereEqualTo("estado", "asignada")
            .addSnapshotListener { value, _ ->
                if (value != null && !value.isEmpty) {
                    val documento = value.documents[0]
                    val idIncidencia = documento.id
                    lanzarNotificacion("Nueva Incidencia", "Tienes tareas pendientes", idIncidencia)
                }
            }
    }

    private fun lanzarNotificacion(titulo: String, mensaje: String, idIncidencia: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, DetalleIncidenciaActivity::class.java).apply {
            putExtra("id", idIncidencia)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

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
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        manager.notify(1, builder.build())
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "canal_incidencias",
                "Avisos de Incidencias",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}