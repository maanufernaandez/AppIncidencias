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
import com.example.appincidencias.ui.ListaUsuariosActivity
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

        val btnCrear = findViewById<Button>(R.id.btnCrearIncidencia)
        val btnLista = findViewById<Button>(R.id.btnLista)
        val btnTodas = findViewById<Button>(R.id.btnListaTodas)
        val btnGuardiaProceso = findViewById<Button>(R.id.btnGuardiaProceso)
        val btnGuardiaFinalizadas = findViewById<Button>(R.id.btnGuardiaFinalizadas)
        val btnUsuarios = findViewById<Button>(R.id.btnVerUsuarios)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnCrear.visibility = View.GONE
        btnLista.visibility = View.GONE
        btnTodas.visibility = View.GONE
        btnGuardiaProceso.visibility = View.GONE
        btnGuardiaFinalizadas.visibility = View.GONE
        btnUsuarios.visibility = View.GONE

        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId).get().addOnSuccessListener { doc ->
            val rol = doc.getString("rol") ?: ""

            when (rol) {
                "docente" -> {
                    btnCrear.visibility = View.VISIBLE
                    btnLista.visibility = View.VISIBLE
                    btnLista.text = "Ver Incidencias Propias"
                    btnTodas.visibility = View.VISIBLE
                    btnTodas.text = "Ver Todas las Incidencias"
                }
                "guardia" -> {
                    btnGuardiaProceso.visibility = View.VISIBLE
                    btnGuardiaFinalizadas.visibility = View.VISIBLE
                }
                "administrador" -> {
                    btnUsuarios.visibility = View.VISIBLE
                    btnLista.visibility = View.VISIBLE
                    btnLista.text = "Ver Incidencias"
                }
            }
        }

        btnCrear.setOnClickListener {
            startActivity(Intent(this, CrearIncidenciaActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnLista.setOnClickListener {
            val intent = Intent(this, ListaIncidenciasActivity::class.java)
            intent.putExtra("filtrar_propias", true)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnTodas.setOnClickListener {
            val intent = Intent(this, ListaIncidenciasActivity::class.java)
            intent.putExtra("filtrar_propias", false)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnGuardiaProceso.setOnClickListener {
            val intent = Intent(this, ListaIncidenciasActivity::class.java)
            intent.putExtra("filtro_guardia", "proceso")
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnGuardiaFinalizadas.setOnClickListener {
            val intent = Intent(this, ListaIncidenciasActivity::class.java)
            intent.putExtra("filtro_guardia", "finalizadas")
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnUsuarios.setOnClickListener {
            startActivity(Intent(this, ListaUsuariosActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    // --- Lógica de Notificaciones ---

    private fun verificarNuevasAsignaciones() {
        val user = auth.currentUser ?: return

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