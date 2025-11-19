package com.example.appincidencias.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appincidencias.utils.EmailSender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appincidencias.R

class AvisarCAUActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var incidenciaId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avisar_cau)

        incidenciaId = intent.getStringExtra("id") ?: ""

        val inputPersonaAviso = findViewById<EditText>(R.id.inputPersonaAviso)
        val inputEmailCau = findViewById<EditText>(R.id.inputEmailCau)
        val btnAvisar = findViewById<Button>(R.id.btnAvisarCau)

        btnAvisar.setOnClickListener {
            val persona = inputPersonaAviso.text.toString()
            val email = inputEmailCau.text.toString()

            if (persona.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            enviarEmailACAU(email)

            db.collection("incidencias").document(incidenciaId)
                .update(
                    mapOf(
                        "estado" to "avisado_cau",
                        "personaAvisoCAU" to persona
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Aviso al CAU registrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    private fun enviarEmailACAU(destinatario: String) {
        val asunto = "Aviso de incidencia al CAU"
        val mensaje = "Se ha avisado al CAU para la incidencia con ID: $incidenciaId"

        EmailSender.sendEmail(destinatario, asunto, mensaje)
    }
}
