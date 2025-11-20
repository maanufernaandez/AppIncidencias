package com.example.appincidencias.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appincidencias.R
import com.example.appincidencias.adapters.IncidenciasAdapter
import com.example.appincidencias.models.Incidencia
import com.google.firebase.firestore.FirebaseFirestore

class ListaIncidenciasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: IncidenciasAdapter
    private var listaIncidencias = mutableListOf<Incidencia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_incidencias)

        val recycler = findViewById<RecyclerView>(R.id.recyclerIncidencias)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        // Configurar RecyclerView
        recycler.layoutManager = LinearLayoutManager(this)

        // Inicializamos adaptador con lista vacía y la acción al hacer click
        adapter = IncidenciasAdapter(listaIncidencias) { incidencia ->
            // Al hacer click en la tarjeta
            val intent = Intent(this, DetalleIncidenciaActivity::class.java)
            intent.putExtra("id", incidencia.id)
            startActivity(intent)
        }
        recycler.adapter = adapter

        // Cargar datos
        db.collection("incidencias")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snap != null) {
                    listaIncidencias = snap.documents.map { doc ->
                        Incidencia(
                            id = doc.id,
                            aula = doc.getString("aula") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            urgencia = doc.getString("urgencia") ?: "Media",
                            fecha = doc.getString("fecha") ?: "",
                            estado = doc.getString("estado") ?: "pendiente",
                            docenteId = doc.getString("docenteId") ?: "",
                            docenteNombre = doc.getString("docenteNombre") ?: ""
                        )
                    }.toMutableList()

                    adapter.actualizarLista(listaIncidencias)

                    // Mostrar mensaje si está vacío
                    tvEmpty.visibility = if (listaIncidencias.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }
}