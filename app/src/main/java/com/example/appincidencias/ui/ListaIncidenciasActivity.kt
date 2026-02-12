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

        // Configurar RecyclerView
        recycler.layoutManager = LinearLayoutManager(this)

        // Inicializamos adaptador con lista vacía y la acción al hacer click
        adapter = IncidenciasAdapter(listaIncidencias) { incidencia ->
            // Al hacer click en la tarjeta vamos al detalle
            val intent = Intent(this, DetalleIncidenciaActivity::class.java)
            intent.putExtra("id", incidencia.id)
            startActivity(intent)
        }
        recycler.adapter = adapter

        // Llamamos a la función que gestiona la carga según el rol
        cargarIncidencias(tvEmpty)
    }

    private fun cargarIncidencias(tvEmpty: TextView) {
        val userId = auth.currentUser?.uid ?: return

        // 1. Obtenemos el rol del usuario actual
        db.collection("usuarios").document(userId).get().addOnSuccessListener { userDoc ->
            val rol = userDoc.getString("rol") ?: ""

            // 2. Escuchamos cambios en tiempo real en la colección de incidencias
            db.collection("incidencias")
                .addSnapshotListener { snap, error ->
                    if (error != null) {
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snap != null) {
                        // Mapeamos los documentos a objetos Incidencia
                        var lista = snap.documents.map { doc ->
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

                        // 3. FILTRO: Si es docente, solo mostramos las que él creó
                        if (rol == "docente") {
                            lista = lista.filter { it.docenteId == userId }
                        }

                        // 4. ORDENACIÓN:
                        // - Primero agrupa por Estado (alfabéticamente)
                        // - Luego ordena por Fecha descendente (la más nueva arriba dentro de cada estado)
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                        listaIncidencias = lista.sortedWith(
                            compareBy<Incidencia> { it.estado }
                                .thenByDescending {
                                    try {
                                        sdf.parse(it.fecha)?.time ?: 0L
                                    } catch (e: Exception) {
                                        0L
                                    }
                                }
                        ).toMutableList()

                        // Actualizamos la interfaz
                        adapter.actualizarLista(listaIncidencias)

                        // Mostramos u ocultamos el mensaje de "No hay incidencias"
                        tvEmpty.visibility = if (listaIncidencias.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
        }
    }
}