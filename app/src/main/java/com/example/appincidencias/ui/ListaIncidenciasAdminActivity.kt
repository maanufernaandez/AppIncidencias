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

class ListaIncidenciasAdminActivity : AppCompatActivity() {

    private lateinit var adapter: IncidenciasAdapter
    private val db = FirebaseFirestore.getInstance()
    private var listaIncidencias = mutableListOf<Incidencia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_incidencias_admin)

        val recycler = findViewById<RecyclerView>(R.id.recyclerIncidenciasAdmin)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyAdmin)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = IncidenciasAdapter(listaIncidencias) { incidencia ->
            val intent = Intent(this, DetalleIncidenciaActivity::class.java)
            intent.putExtra("id", incidencia.id)
            startActivity(intent)
        }
        recycler.adapter = adapter

        cargarIncidencias(tvEmpty)
    }

    private fun cargarIncidencias(tvEmpty: TextView) {
        db.collection("incidencias")
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (result != null) {
                    val lista = result.documents.map { doc ->
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

                    // Función interna para dar peso a los estados y ordenarlos
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

                    // Aplicamos el orden: Primero estado (arriba iniciadas, abajo finalizadas), luego más recientes
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

                    // Actualizamos el adaptador con los datos nuevos
                    adapter.actualizarLista(listaIncidencias)

                    // Mostrar/Ocultar mensaje de "No hay incidencias"
                    tvEmpty.visibility = if (listaIncidencias.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }
}