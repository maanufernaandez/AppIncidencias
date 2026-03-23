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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ListaIncidenciasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: IncidenciasAdapter
    private var listaIncidencias = mutableListOf<Incidencia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_incidencias)

        val recycler = findViewById<RecyclerView>(R.id.recyclerIncidencias)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = IncidenciasAdapter(listaIncidencias) { incidencia ->
            val intent = Intent(this, DetalleIncidenciaActivity::class.java)
            intent.putExtra("id", incidencia.id)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        recycler.adapter = adapter

        cargarIncidencias(tvEmpty)
    }

    private fun cargarIncidencias(tvEmpty: TextView) {
        val userId = auth.currentUser?.uid ?: return

        val filtrarPropias = intent.getBooleanExtra("filtrar_propias", false)
        val filtroGuardia = intent.getStringExtra("filtro_guardia")

        db.collection("usuarios").document(userId).get().addOnSuccessListener { userDoc ->
            val rol = userDoc.getString("rol") ?: ""

            db.collection("incidencias")
                .addSnapshotListener { snap, error ->
                    if (error != null) {
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snap != null) {
                        val documentosFiltrados = snap.documents.filter { doc ->
                            val asignadoA = doc.getString("asignadoA")
                            val estado = doc.getString("estado")

                            if (filtrarPropias && rol == "docente") {
                                doc.getString("docenteId") == userId
                            } else if (rol == "guardia" && filtroGuardia == "proceso") {
                                asignadoA == userId && (estado == "asignada" || estado == "en proceso")
                            } else if (rol == "guardia" && filtroGuardia == "resto") {
                                !(asignadoA == userId && (estado == "asignada" || estado == "en proceso"))
                            } else {
                                true
                            }
                        }

                        val lista = documentosFiltrados.map { doc ->
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
                        }

                        // Función interna para dar peso a los estados y ordenarlos como pediste
                        fun obtenerPesoEstado(estado: String): Int {
                            return when (estado.lowercase()) {
                                "iniciada", "pendiente" -> 1
                                "asignada", "en proceso" -> 2
                                "reparado" -> 3
                                "requiere_cau", "avisado_cau" -> 4
                                "finalizada" -> 5
                                else -> 6
                            }
                        }

                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())

                        // Ordenamos primero por el peso del estado (1 arriba, 5 abajo) y luego por fecha más reciente
                        listaIncidencias = lista.sortedWith(
                            compareBy<Incidencia> { obtenerPesoEstado(it.estado) }
                                .thenByDescending {
                                    try {
                                        sdf.parse(it.fecha)?.time ?: 0L
                                    } catch (e: Exception) {
                                        0L
                                    }
                                }
                        ).toMutableList()

                        adapter.actualizarLista(listaIncidencias)

                        tvEmpty.visibility = if (listaIncidencias.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
        }
    }
}