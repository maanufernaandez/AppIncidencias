package com.example.appincidencias.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.appincidencias.R
import com.google.firebase.firestore.FirebaseFirestore

class DetalleIncidenciaActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    var docId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_incidencia)

        docId = intent.getStringExtra("id")!!

        val t = findViewById<TextView>(R.id.txtDTitulo)
        val d = findViewById<TextView>(R.id.txtDDesc)
        val e = findViewById<TextView>(R.id.txtDEstado)
        val btnAsig = findViewById<Button>(R.id.btnAsignar)
        val btnEstado = findViewById<Button>(R.id.btnCambiarEstado)

        db.collection("incidencias").document(docId)
            .addSnapshotListener { snap, _ ->

                t.text = snap?.getString("titulo")
                d.text = snap?.getString("descripcion")
                e.text = snap?.getString("estado")
            }

        btnAsig.setOnClickListener {
            val intent = Intent(this, AsignarIncidenciaActivity::class.java)
            intent.putExtra("id", docId)
            startActivity(intent)
        }

        btnEstado.setOnClickListener {
            val estados = listOf("pendiente", "en proceso", "resuelta")

            val cur = e.text.toString()
            val next = estados[(estados.indexOf(cur) + 1) % estados.size]

            db.collection("incidencias").document(docId)
                .update("estado", next)
        }
    }
}
