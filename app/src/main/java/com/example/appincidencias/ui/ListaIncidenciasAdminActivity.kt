package com.example.appincidencias.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appincidencias.R
import com.example.appincidencias.adapters.IncidenciasAdapter
import com.example.appincidencias.models.Incidencia
import com.google.firebase.firestore.FirebaseFirestore

class ListaIncidenciasAdminActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: IncidenciasAdapter
    private val db = FirebaseFirestore.getInstance()
    // Usamos ArrayList para que sea compatible con BaseAdapter si necesitamos modificarla
    private val listaIncidencias = ArrayList<Incidencia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_incidencias_admin)

        // 1. Cambiamos RecyclerView por ListView
        listView = findViewById(R.id.listIncidenciasAdmin)

        // 2. Inicializamos el adaptador pasando el Contexto (this) y la lista
        adapter = IncidenciasAdapter(this, listaIncidencias)
        listView.adapter = adapter

        // 3. Evento Click (Opcional: para ir al detalle)
        listView.setOnItemClickListener { _, _, position, _ ->
            val incidencia = listaIncidencias[position]
            val intent = Intent(this, DetalleIncidenciaActivity::class.java)
            intent.putExtra("id", incidencia.id)
            startActivity(intent)
        }

        cargarIncidencias()
    }

    private fun cargarIncidencias() {
        db.collection("incidencias")
            .get()
            .addOnSuccessListener { result ->
                listaIncidencias.clear()

                // 4. Mapeo manual (Más seguro para evitar errores de tipos de datos)
                for (doc in result) {
                    val incidencia = Incidencia(
                        id = doc.id,
                        aula = doc.getString("aula") ?: "",
                        titulo = doc.getString("aula") ?: "Sin Aula", // Compatibilidad
                        descripcion = doc.getString("descripcion") ?: "",
                        urgencia = doc.getString("urgencia") ?: "",
                        fecha = doc.getString("fecha") ?: "",         // Leemos como String
                        docenteId = doc.getString("docenteId") ?: "",
                        creador = doc.getString("docenteId") ?: "",   // Compatibilidad
                        estado = doc.getString("estado") ?: "iniciada",
                        asignadoA = doc.getString("asignadoA")
                    )
                    listaIncidencias.add(incidencia)
                }

                // 5. Notificamos al adaptador que los datos cambiaron
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando incidencias", Toast.LENGTH_SHORT).show()
            }
    }
}