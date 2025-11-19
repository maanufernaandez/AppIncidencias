package com.example.appincidencias.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appincidencias.R
import com.example.appincidencias.models.Incidencia
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrearIncidenciaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_incidencia)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val inputAula = findViewById<EditText>(R.id.inputAula)
        val inputDescripcion = findViewById<EditText>(R.id.inputDescripcion)
        val inputUrgencia = findViewById<EditText>(R.id.inputUrgencia)
        val btnCrear = findViewById<Button>(R.id.btnCrearIncidencia)

        btnCrear.setOnClickListener {

            val aula = inputAula.text.toString().trim()
            val descripcion = inputDescripcion.text.toString().trim()
            val urgencia = inputUrgencia.text.toString().trim()
            val docenteId = auth.currentUser?.uid ?: ""
            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            if (aula.isEmpty() || descripcion.isEmpty() || urgencia.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val incidencia = Incidencia(
                aula = aula,
                descripcion = descripcion,
                urgencia = urgencia,
                fecha = fecha,
                docenteId = docenteId,
                estado = "iniciada",
                asignadoA = null,
                id = null
            )

            db.collection("incidencias")
                .add(incidencia)
                .addOnSuccessListener {
                    Toast.makeText(this, "Incidencia creada correctamente", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
