package com.example.appincidencias.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appincidencias.R
import com.google.firebase.firestore.FirebaseFirestore

class AvisarCAUActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var incidenciaId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avisar_cau)

        // Recuperamos el ID que nos pasan desde la pantalla anterior
        incidenciaId = intent.getStringExtra("id") ?: ""

        val inputPersonaAviso = findViewById<EditText>(R.id.inputPersonaAviso)
        val inputEmailCau = findViewById<EditText>(R.id.inputEmailCau)
        val btnAvisar = findViewById<Button>(R.id.btnAvisarCau)

        btnAvisar.setOnClickListener {
            val persona = inputPersonaAviso.text.toString().trim()
            val emailDestino = inputEmailCau.text.toString().trim()

            if (persona.isEmpty() || emailDestino.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Primero actualizamos el estado en Firebase para dejar constancia
            actualizarEstadoIncidencia(persona, emailDestino)
        }
    }

    private fun actualizarEstadoIncidencia(persona: String, emailDestino: String) {
        db.collection("incidencias").document(incidenciaId)
            .update(
                mapOf(
                    "estado" to "avisado_cau",
                    "personaAvisoCAU" to persona
                )
            )
            .addOnSuccessListener {
                // 2. Si se guardó bien en la base de datos, abrimos la app de correo
                abrirAppDeCorreo(persona, emailDestino)
                Toast.makeText(this, "Aviso registrado. Abriendo correo...", Toast.LENGTH_LONG).show()
                finish() // Cerramos esta pantalla
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar la incidencia", Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirAppDeCorreo(persona: String, emailDestino: String) {
        val asunto = "Aviso de Incidencia - ID: $incidenciaId"
        val cuerpoMensaje = """
            Hola,
            
            Se ha reportado una incidencia que requiere atención del CAU.
            
            ID Incidencia: $incidenciaId
            Avisado por: $persona
            
            Por favor, revisen el panel de administración para más detalles.
            
            Un saludo.
        """.trimIndent()

        // Intent explícito para enviar correos
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Solo apps de correo deben manejar esto
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailDestino))
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpoMensaje)
        }

        try {
            startActivity(Intent.createChooser(intent, "Elige tu app de correo"))
        } catch (e: Exception) {
            Toast.makeText(this, "No se encontró ninguna app de correo instalada", Toast.LENGTH_LONG).show()
        }
    }
}