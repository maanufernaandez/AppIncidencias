package com.example.appincidencias.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.appincidencias.R
import com.google.firebase.firestore.FirebaseFirestore

class AsignarIncidenciaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var idIncidencia = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignar_incidencia)

        idIncidencia = intent.getStringExtra("id") ?: ""

        val spinner = findViewById<Spinner>(R.id.spinnerTecnicos)
        val btn = findViewById<Button>(R.id.btnGuardarAsignacion)

        // Cargamos usuarios que sean 'guardia' o 'tecnico'
        db.collection("usuarios")
            .whereEqualTo("rol", "guardia") // O 'tecnico' según como los guardes
            .get()
            .addOnSuccessListener { snap ->

                // Guardamos par (ID, Email) para usarlo luego
                val listaTecnicos = snap.documents.map {
                    Pair(it.id, it.getString("email") ?: "")
                }

                val nombres = snap.documents.map { it.getString("nombre") ?: it.getString("email") ?: "Sin nombre" }

                spinner.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    nombres
                )

                btn.setOnClickListener {
                    if (listaTecnicos.isEmpty()) return@setOnClickListener

                    val index = spinner.selectedItemPosition
                    val (uidTecnico, emailTecnico) = listaTecnicos[index]

                    // 1. Actualizamos Base de Datos (Estado pasa a 'asignada')
                    db.collection("incidencias").document(idIncidencia)
                        .update(mapOf(
                            "asignadoA" to uidTecnico,
                            "estado" to "asignada" // <--- Cambio importante según requisitos
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Asignado correctamente", Toast.LENGTH_SHORT).show()

                            // 2. REQUISITO CLAVE: Notificar por correo automáticamente
                            enviarCorreoNotificacion(emailTecnico)

                            finish()
                        }
                }
            }
    }

    private fun enviarCorreoNotificacion(emailDestino: String) {
        val asunto = "NUEVA ASIGNACIÓN - Incidencia $idIncidencia"
        val mensaje = "Hola,\n\nSe te ha asignado una nueva incidencia de urgencia.\nPor favor, entra en la app para ver los detalles y cambiar el estado a 'En Proceso'.\n\nGracias."

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailDestino))
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Si no tiene app de correo, no crasheamos, solo avisamos
            Toast.makeText(this, "No se pudo abrir app de correo", Toast.LENGTH_SHORT).show()
        }
    }
}