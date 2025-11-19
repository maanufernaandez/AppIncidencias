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

class ListaIncidenciasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_incidencias)

        val listView = findViewById<ListView>(R.id.listIncidencias)

        // Escuchamos la colección "incidencias" en tiempo real
        db.collection("incidencias")
            .addSnapshotListener { snap, error ->

                // 1. Gestión de errores de conexión
                if (error != null) {
                    Toast.makeText(this, "Error al cargar: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // 2. Procesar los datos si existen
                if (snap != null) {
                    val lista = snap.documents.map { doc ->
                        Incidencia(
                            id = doc.id,

                            // TRUCO: Como no guardamos 'titulo', usamos el 'aula' como título
                            titulo = doc.getString("aula") ?: "Incidencia General",

                            // Datos reales guardados en CrearIncidencia
                            aula = doc.getString("aula") ?: "",
                            descripcion = doc.getString("descripcion") ?: "Sin descripción",
                            urgencia = doc.getString("urgencia") ?: "Media",
                            estado = doc.getString("estado") ?: "iniciada",

                            // TRUCO: Mapeamos docenteId a creador para compatibilidad
                            creador = doc.getString("docenteId") ?: "Desconocido",
                            docenteId = doc.getString("docenteId") ?: "",

                            asignadoA = doc.getString("asignadoA"), // Puede ser null

                            // IMPORTANTE: Leer fecha como String (Texto), no como Long (Número)
                            fecha = doc.getString("fecha") ?: ""
                        )
                    }

                    // 3. Asignar la lista al adaptador
                    // Asegúrate de que tu IncidenciasAdapter acepte esta lista
                    listView.adapter = IncidenciasAdapter(this, lista)

                    // 4. Clic para ver detalles
                    listView.setOnItemClickListener { _, _, pos, _ ->
                        val intent = Intent(this, DetalleIncidenciaActivity::class.java)
                        intent.putExtra("id", lista[pos].id)
                        startActivity(intent)
                    }
                }
            }
    }
}