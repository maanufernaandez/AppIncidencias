package com.example.appincidencias.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appincidencias.R
import com.example.appincidencias.adapters.UsuariosAdapter
import com.example.appincidencias.models.User
import com.google.firebase.firestore.FirebaseFirestore

class ListaUsuariosActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: UsuariosAdapter
    private var listaUsuarios = listOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_usuarios)

        val recycler = findViewById<RecyclerView>(R.id.recyclerUsuarios)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = UsuariosAdapter(listaUsuarios) { user ->
            mostrarDialogoEditar(user)
        }
        recycler.adapter = adapter

        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        db.collection("usuarios").addSnapshotListener { snap, _ ->
            if (snap != null) {
                val usuarios = snap.documents.map { doc ->
                    User(
                        uid = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        email = doc.getString("email") ?: "",
                        rol = doc.getString("rol") ?: "docente"
                    )
                }

                listaUsuarios = usuarios.sortedBy { it.rol }
                adapter.actualizarLista(listaUsuarios)
            }
        }
    }

    private fun mostrarDialogoEditar(user: User) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Usuario")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre"
        inputNombre.setText(user.nombre)
        layout.addView(inputNombre)

        val inputEmail = EditText(this)
        inputEmail.hint = "Email"
        inputEmail.setText(user.email)
        layout.addView(inputEmail)

        val spinnerRol = Spinner(this)
        val roles = listOf("docente", "guardia", "administrador")
        val adapterRol = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRol.adapter = adapterRol
        val rolIndex = roles.indexOf(user.rol)
        if (rolIndex >= 0) spinnerRol.setSelection(rolIndex)
        layout.addView(spinnerRol)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nuevoNombre = inputNombre.text.toString()
            val nuevoEmail = inputEmail.text.toString()
            val nuevoRol = spinnerRol.selectedItem.toString()

            val datos = mapOf(
                "nombre" to nuevoNombre,
                "email" to nuevoEmail,
                "rol" to nuevoRol
            )

            db.collection("usuarios").document(user.uid).update(datos)
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}