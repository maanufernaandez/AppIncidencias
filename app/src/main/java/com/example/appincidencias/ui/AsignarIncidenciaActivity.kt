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

    private var aulaIncidencia = ""
    private var descIncidencia = ""
    private var urgenciaIncidencia = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_asignar_incidencia)

        idIncidencia = intent.getStringExtra("id") ?: ""

        val autoCompleteTecnicos = findViewById<AutoCompleteTextView>(R.id.autoCompleteTecnicos)
        val btn = findViewById<Button>(R.id.btnGuardarAsignacion)

        db.collection("incidencias").document(idIncidencia).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    aulaIncidencia = doc.getString("aula") ?: "Desconocida"
                    descIncidencia = doc.getString("descripcion") ?: "Sin descripción"
                    urgenciaIncidencia = doc.getString("urgencia") ?: "Normal"
                }
            }

        db.collection("usuarios")
            .whereEqualTo("rol", "guardia")
            .get()
            .addOnSuccessListener { snap ->

                val listaTecnicos = snap.documents.map {
                    Pair(it.id, it.getString("email") ?: "")
                }

                val nombres = snap.documents.map {
                    it.getString("nombre") ?: it.getString("email") ?: "Sin nombre"
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    nombres
                )
                autoCompleteTecnicos.setAdapter(adapter)

                btn.setOnClickListener {
                    val tecnicoSeleccionado = autoCompleteTecnicos.text.toString()

                    if (listaTecnicos.isEmpty()) {
                        Toast.makeText(this, "No hay técnicos disponibles", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (tecnicoSeleccionado.isEmpty()) {
                        Toast.makeText(this, "Por favor, selecciona un técnico", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val index = nombres.indexOf(tecnicoSeleccionado)
                    if (index == -1) return@setOnClickListener

                    val (uidTecnico, _) = listaTecnicos[index]

                    db.collection("incidencias").document(idIncidencia)
                        .update(mapOf(
                            "asignadoA" to uidTecnico,
                            "estado" to "asignada"
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Asignado correctamente", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al asignar: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun enviarCorreoNotificacion(emailDestino: String, aula: String, desc: String, urgencia: String) {
        val asunto = "NUEVA INCIDENCIA - URGENCIA: $urgencia"

        val mensaje = """
            Hola,
            
            Se te ha asignado una nueva incidencia.
            
            Aula: $aula
            Urgencia: $urgencia
            Descripción: $desc
            
            Por favor, entra en la App Incidencias para gestionarla y cambiar su estado.
            
            Gracias.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailDestino))
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }

        try {
            startActivity(Intent.createChooser(intent, "Enviar aviso al técnico"))
        } catch (e: Exception) {
            Toast.makeText(this, "No se encontró ninguna app de correo instalada", Toast.LENGTH_LONG).show()
        }
    }
}