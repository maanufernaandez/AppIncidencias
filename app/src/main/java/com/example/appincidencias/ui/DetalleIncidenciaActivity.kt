package com.example.appincidencias.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appincidencias.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetalleIncidenciaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var docId = ""
    private var estadoActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_detalle_incidencia)

        docId = intent.getStringExtra("id") ?: return

        val txtTitulo = findViewById<TextView>(R.id.txtDTitulo)
        val txtDesc = findViewById<TextView>(R.id.txtDDesc)
        val txtEstado = findViewById<TextView>(R.id.txtDEstado)

        val btnAsignar = findViewById<Button>(R.id.btnAsignar)
        val btnEstado = findViewById<Button>(R.id.btnCambiarEstado)
        val btnCau = findViewById<Button>(R.id.btnAvisarCau)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar) // Nueva referencia

        btnAsignar.visibility = View.GONE
        btnEstado.visibility = View.GONE
        btnCau.visibility = View.GONE
        btnEliminar.visibility = View.GONE // Oculto por defecto

        db.collection("incidencias").document(docId)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    txtTitulo.text = "Aula " + (snap.getString("aula") ?: "?")
                    val descripcion = snap.getString("descripcion")
                    val comentarioGuardia = snap.getString("comentarioGuardia") ?: ""

                    if (comentarioGuardia.isNotEmpty()) {
                        txtDesc.text = "$descripcion\n\n[GUARDIA]: $comentarioGuardia"
                    } else {
                        txtDesc.text = descripcion
                    }

                    estadoActual = snap.getString("estado") ?: "iniciada"
                    txtEstado.text = estadoActual.uppercase().replace("_", " ")

                    configurarBotones(btnAsignar, btnEstado, btnCau)

                    // Lógica para mostrar el botón Eliminar SOLO si está finalizada
                    if (estadoActual.lowercase() == "finalizada") {
                        btnEliminar.visibility = View.VISIBLE
                    } else {
                        btnEliminar.visibility = View.GONE
                    }
                }
            }

        btnAsignar.setOnClickListener {
            val intent = Intent(this, AsignarIncidenciaActivity::class.java)
            intent.putExtra("id", docId)
            startActivity(intent)
        }

        btnCau.setOnClickListener {
            val intent = Intent(this, AvisarCAUActivity::class.java)
            intent.putExtra("id", docId)
            startActivity(intent)
        }

        btnEstado.setOnClickListener {
            ejecutarAccionEstado()
        }

        // --- NUEVA LÓGICA DE ELIMINACIÓN (Modified to use custom view CC) ---
        btnEliminar.setOnClickListener {
            // Inflamos el diseño XML moderno personalizado
            val view = layoutInflater.inflate(R.layout.dialog_confirmar_eliminacion, null)

            // Usamos MaterialAlertDialogBuilder para un diseño moderno con bordes redondeados
            val builder = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            builder.setView(view) // <-- Usar el diseño personalizado CC

            builder.setPositiveButton("ELIMINAR") { _, _ ->
                db.collection("incidencias").document(docId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Incidencia eliminada", Toast.LENGTH_SHORT).show()
                        finish() // Cierra la pantalla y vuelve a la lista
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
            builder.setNegativeButton("CANCELAR", null) // El bot&oacute;n negativo por defecto es moderno en el builder

            builder.show()
        }
    }

    private fun configurarBotones(btnAsignar: Button, btnEstado: Button, btnCau: Button) {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(myUid).get().addOnSuccessListener { userDoc ->
            val rol = userDoc.getString("rol") ?: ""

            // Reiniciar visibilidad
            btnAsignar.visibility = View.GONE
            btnEstado.visibility = View.GONE
            btnCau.visibility = View.GONE

            when (rol) {
                "administrador" -> {
                    // Admin puede asignar si está iniciada
                    if (estadoActual == "iniciada") {
                        btnAsignar.visibility = View.VISIBLE
                    }
                    // Admin finaliza si está reparada
                    if (estadoActual == "reparado") {
                        btnEstado.visibility = View.VISIBLE
                        btnEstado.text = "FINALIZAR INCIDENCIA"
                    }
                    // Admin gestiona CAU si el guardia dice que hace falta
                    if (estadoActual == "requiere_cau") {
                        btnCau.visibility = View.VISIBLE
                        btnCau.text = "GESTIONAR AVISO AL CAU"
                    }
                    // Admin finaliza si ya se avisó al CAU
                    if (estadoActual == "avisado_cau") {
                        btnEstado.visibility = View.VISIBLE
                        btnEstado.text = "CERRAR (YA AVISADO)"
                    }
                }
                "guardia" -> {
                    // Guardia recibe "asignada" -> Pasa a "en proceso"
                    if (estadoActual == "asignada") {
                        btnEstado.visibility = View.VISIBLE
                        btnEstado.text = "COMENZAR (EN PROCESO)"
                    }
                    // Guardia está trabajando -> Puede Reparar o Pedir CAU
                    if (estadoActual == "en proceso") {
                        btnEstado.visibility = View.VISIBLE
                        btnEstado.text = "MARCAR COMO REPARADO"

                        // Usamos el botón secundario para la opción del CAU
                        btnCau.visibility = View.VISIBLE
                        btnCau.text = "NECESARIO AVISAR CAU"
                        btnCau.setOnClickListener { mostrarDialogoGuardia("requiere_cau") }
                    }
                }
                // Docente: solo mira, no botones
            }
        }
    }

    private fun ejecutarAccionEstado() {
        when (estadoActual) {
            "asignada" -> actualizarEstado("en proceso", "")
            "en proceso" -> mostrarDialogoGuardia("reparado")
            "reparado", "avisado_cau" -> actualizarEstado("finalizada", "")
        }
    }

    // Diálogo para que el guardia explique qué hizo o por qué requiere CAU
    private fun mostrarDialogoGuardia(nuevoEstado: String) {
        // Inflamos el diseño XML moderno personalizado
        val view = layoutInflater.inflate(R.layout.dialog_motivo_guardia, null)

        val tvTitulo = view.findViewById<TextView>(R.id.tvDDialogoTitulo)
        val etMotivo = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDDialogoMotivo)

        // Cambiamos el título dinámicamente según la acción
        if (nuevoEstado == "reparado") {
            tvTitulo.text = "Detalle de Reparación"
        } else {
            tvTitulo.text = "Motivo de Aviso CAU"
        }

        // Usamos MaterialAlertDialogBuilder para un diseño moderno con bordes redondeados
        val builder = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
        builder.setView(view)

        builder.setPositiveButton("GUARDAR") { _, _ ->
            val comentario = etMotivo.text.toString().trim()
            if (comentario.isNotEmpty()) {
                actualizarEstado(nuevoEstado, comentario)
            } else {
                Toast.makeText(this, "El comentario es obligatorio", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("CANCELAR") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun actualizarEstado(estado: String, comentario: String) {
        val datos = mutableMapOf<String, Any>("estado" to estado)
        if (comentario.isNotEmpty()) {
            datos["comentarioGuardia"] = comentario
        }

        db.collection("incidencias").document(docId).update(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Estado actualizado a $estado", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}