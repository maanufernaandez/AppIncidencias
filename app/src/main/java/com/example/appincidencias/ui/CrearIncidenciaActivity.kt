package com.example.appincidencias.ui

import android.os.Bundle
import android.view.View
import android.widget.*
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
        supportActionBar?.hide()
        setContentView(R.layout.activity_crear_incidencia)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val inputAula = findViewById<EditText>(R.id.inputAula)
        val inputDescripcion = findViewById<EditText>(R.id.inputDescripcion)
        val spinnerUrgencia = findViewById<AutoCompleteTextView>(R.id.spinnerUrgencia)
        val btnCrear = findViewById<Button>(R.id.btnCrearIncidencia)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val niveles = arrayOf("Baja", "Media", "Alta")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, niveles)
        spinnerUrgencia.setAdapter(adapter)

        btnCrear.setOnClickListener {
            val aula = inputAula.text.toString().trim()
            val descripcion = inputDescripcion.text.toString().trim()
            val urgencia = spinnerUrgencia.text.toString()

            val usuario = auth.currentUser
            val docenteId = usuario?.uid ?: ""
            val docenteNombre = usuario?.displayName ?: usuario?.email ?: "Docente"

            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            if (aula.isEmpty() || descripcion.isEmpty() || urgencia.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnCrear.isEnabled = false

            val incidencia = Incidencia(
                aula = aula,
                descripcion = descripcion,
                urgencia = urgencia,
                fecha = fecha,
                docenteId = docenteId,
                docenteNombre = docenteNombre,
                estado = "iniciada",
                asignadoA = null,
                id = null
            )

            db.collection("incidencias")
                .add(incidencia)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Incidencia creada correctamente", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    btnCrear.isEnabled = true
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}