package com.example.appincidencias.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.appincidencias.R
import com.google.firebase.firestore.FirebaseFirestore

class AsignarIncidenciaActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignar_incidencia)

        id = intent.getStringExtra("id")!!

        val spinner = findViewById<Spinner>(R.id.spinnerTecnicos)
        val btn = findViewById<Button>(R.id.btnGuardarAsignacion)

        db.collection("usuarios")
            .whereEqualTo("rol", "tecnico")
            .get()
            .addOnSuccessListener { snap ->

                val tecnicos = snap.documents.map {
                    it.id to it.getString("email").toString()
                }

                spinner.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    tecnicos.map { it.second }
                )

                btn.setOnClickListener {
                    val index = spinner.selectedItemPosition
                    val uid = tecnicos[index].first

                    db.collection("incidencias").document(id)
                        .update("asignadoA", uid)

                    Toast.makeText(this, "Asignado!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
    }
}
