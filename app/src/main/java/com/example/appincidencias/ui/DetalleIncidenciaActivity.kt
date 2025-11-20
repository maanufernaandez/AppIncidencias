package com.example.appincidencias.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
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

        val btnAsignar = findViewById<Button>(R.id.btnAsignar)
        val btnEstado = findViewById<Button>(R.id.btnCambiarEstado)
        val btnCau = findViewById<Button>(R.id.btnAvisarCau)

        // 1. Ocultar todo por defecto (Seguridad UI)
        btnAsignar.visibility = View.GONE
        btnEstado.visibility = View.GONE
        btnCau.visibility = View.GONE

        // 2. Escuchar cambios en la incidencia
        db.collection("incidencias").document(docId)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    txtTitulo.text = "Aula " + (snap.getString("aula") ?: "?")
                    txtDesc.text = snap.getString("descripcion")
                    estadoActual = snap.getString("estado") ?: "iniciada"
                    txtEstado.text = estadoActual.uppercase()

                    // Actualizar textos de botones según estado
                    actualizarUIsegunEstado(btnEstado, estadoActual)
                }
            }

        // 3. Determinar ROL del usuario y mostrar botones
        val myUid = auth.currentUser?.uid
        if (myUid != null) {
            db.collection("usuarios").document(myUid).get()
                .addOnSuccessListener { userDoc ->
                    val rol = userDoc.getString("rol") ?: ""

                    if (rol == "administrador") {
                        btnAsignar.visibility = View.VISIBLE

                        // CORRECCIÓN: El Admin también puede avisar al CAU según requisitos
                        btnCau.visibility = View.VISIBLE

                        // El admin puede finalizar si ya está reparada o avisada
                        if (estadoActual == "reparado" || estadoActual == "avisado_cau") {
                            btnEstado.visibility = View.VISIBLE
                            btnEstado.text = "FINALIZAR INCIDENCIA"
                        }
                    }
                    else if (rol == "guardia") {
                        // El guardia trabaja la incidencia
                        btnEstado.visibility = View.VISIBLE
                        btnCau.visibility = View.VISIBLE
                    }
                    // El docente no ve botones de acción, solo consulta
                }
        }

        // 4. Lógica de botones
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
            siguienteEstado()
        }
    }

    private fun actualizarUIsegunEstado(btn: Button, estado: String) {
        when (estado) {
            "asignada" -> btn.text = "PONER EN PROCESO"
            "en proceso" -> btn.text = "MARCAR COMO REPARADO"
            "reparado" -> btn.text = "FINALIZAR (Solo Admin)"
            "finalizada" -> btn.visibility = View.GONE
        }
    }

    private fun siguienteEstado() {
        var nuevoEstado = ""

        // Lógica de transición estricta del documento
        when (estadoActual) {
            "asignada" -> nuevoEstado = "en proceso"
            "en proceso" -> nuevoEstado = "reparado"
            "reparado" -> nuevoEstado = "finalizada" // Solo admin (controlado por visibilidad)
            "avisado_cau" -> nuevoEstado = "finalizada" // Solo admin
            else -> {
                Toast.makeText(this, "Estado actual: $estadoActual. Espera a que se asigne.", Toast.LENGTH_LONG).show()
                return
            }
        }

        if (nuevoEstado.isNotEmpty()) {
            db.collection("incidencias").document(docId).update("estado", nuevoEstado)
        }
    }
}