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
        setContentView(R.layout.activity_detalle_incidencia)

        docId = intent.getStringExtra("id") ?: return

        val txtTitulo = findViewById<TextView>(R.id.txtDTitulo)
        val txtDesc = findViewById<TextView>(R.id.txtDDesc)
        val txtEstado = findViewById<TextView>(R.id.txtDEstado)

        // Referencia al nuevo campo de comentario del guardia (asegúrate de añadir un TextView en el XML si quieres verlo, o úsalo en un Toast)
        // Por simplicidad, aquí mostramos la descripción original.

        val btnAsignar = findViewById<Button>(R.id.btnAsignar)
        val btnEstado = findViewById<Button>(R.id.btnCambiarEstado) // Botón principal de acción
        val btnCau = findViewById<Button>(R.id.btnAvisarCau) // Botón secundario

        // Ocultar por defecto
        btnAsignar.visibility = View.GONE
        btnEstado.visibility = View.GONE
        btnCau.visibility = View.GONE

        db.collection("incidencias").document(docId)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    txtTitulo.text = "Aula " + (snap.getString("aula") ?: "?")
                    val descripcion = snap.getString("descripcion")
                    val comentarioGuardia = snap.getString("comentarioGuardia") ?: ""

                    // Si el guardia ya comentó, lo mostramos junto a la descripción
                    if (comentarioGuardia.isNotEmpty()) {
                        txtDesc.text = "$descripcion\n\n[GUARDIA]: $comentarioGuardia"
                    } else {
                        txtDesc.text = descripcion
                    }

                    estadoActual = snap.getString("estado") ?: "iniciada"
                    txtEstado.text = estadoActual.uppercase().replace("_", " ")

                    configurarBotones(btnAsignar, btnEstado, btnCau)
                }
            }

        // Listeners de botones
        btnAsignar.setOnClickListener {
            val intent = Intent(this, AsignarIncidenciaActivity::class.java)
            intent.putExtra("id", docId)
            startActivity(intent)
        }

        // Botón para gestionar el aviso al CAU (Solo Admin cuando el guardia lo solicita)
        btnCau.setOnClickListener {
            val intent = Intent(this, AvisarCAUActivity::class.java)
            intent.putExtra("id", docId)
            startActivity(intent)
        }

        // Botón principal de flujo de estados
        btnEstado.setOnClickListener {
            ejecutarAccionEstado()
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
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (nuevoEstado == "reparado") "Detalle de Reparación" else "Motivo aviso CAU")

        val input = EditText(this)
        input.hint = "Escribe aquí una descripción..."
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        builder.setView(input)

        builder.setPositiveButton("Guardar") { _, _ ->
            val comentario = input.text.toString().trim()
            if (comentario.isNotEmpty()) {
                actualizarEstado(nuevoEstado, comentario)
            } else {
                Toast.makeText(this, "La descripción es obligatoria", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

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