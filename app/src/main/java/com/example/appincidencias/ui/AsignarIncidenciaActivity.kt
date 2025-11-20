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

    // Variables para guardar temporalmente los detalles de la incidencia
    private var aulaIncidencia = ""
    private var descIncidencia = ""
    private var urgenciaIncidencia = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignar_incidencia)

        idIncidencia = intent.getStringExtra("id") ?: ""

        val spinner = findViewById<Spinner>(R.id.spinnerTecnicos)
        val btn = findViewById<Button>(R.id.btnGuardarAsignacion)

        // 1. PRIMERO: Recuperamos los datos de la incidencia para poder enviarlos por correo luego
        db.collection("incidencias").document(idIncidencia).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    aulaIncidencia = doc.getString("aula") ?: "Desconocida"
                    descIncidencia = doc.getString("descripcion") ?: "Sin descripción"
                    urgenciaIncidencia = doc.getString("urgencia") ?: "Normal"
                }
            }

        // 2. CARGAMOS LA LISTA DE TÉCNICOS (Rol: "guardia")
        db.collection("usuarios")
            .whereEqualTo("rol", "guardia")
            .get()
            .addOnSuccessListener { snap ->

                // Guardamos una lista de pares (ID, Email)
                val listaTecnicos = snap.documents.map {
                    Pair(it.id, it.getString("email") ?: "")
                }

                // Creamos la lista de nombres para el Spinner
                val nombres = snap.documents.map {
                    it.getString("nombre") ?: it.getString("email") ?: "Sin nombre"
                }

                spinner.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    nombres
                )

                btn.setOnClickListener {
                    if (listaTecnicos.isEmpty()) {
                        Toast.makeText(this, "No hay técnicos disponibles", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val index = spinner.selectedItemPosition
                    val (uidTecnico, emailTecnico) = listaTecnicos[index]

                    // 3. ACTUALIZAMOS LA INCIDENCIA EN FIREBASE
                    db.collection("incidencias").document(idIncidencia)
                        .update(mapOf(
                            "asignadoA" to uidTecnico,
                            "estado" to "asignada" // Cambia el estado a asignada
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Asignado correctamente", Toast.LENGTH_SHORT).show()

                            // 4. ENVIAMOS EL CORREO CON LOS DATOS RECUPERADOS
                            enviarCorreoNotificacion(emailTecnico, aulaIncidencia, descIncidencia, urgenciaIncidencia)

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
            data = Uri.parse("mailto:") // Asegura que solo se abran apps de correo
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