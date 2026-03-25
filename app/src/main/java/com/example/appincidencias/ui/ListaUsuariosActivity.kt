package com.example.appincidencias.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
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
        val view = layoutInflater.inflate(R.layout.dialog_editar_usuario, null)

        val etNombre = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditNombre)
        val etEmail = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditEmail)
        val autoCompleteRol = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteEditRol)

        etNombre.setText(user.nombre)
        etEmail.setText(user.email)

        val rolesMenu = listOf("Docente", "Guardia", "Administrador")
        val adapterRol = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, rolesMenu)
        autoCompleteRol.setAdapter(adapterRol)

        val rolActualCapitalizado = user.rol.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
        autoCompleteRol.setText(rolActualCapitalizado, false)

        val builder = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
        builder.setView(view)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevoEmail = etEmail.text.toString().trim()
            val nuevoRol = autoCompleteRol.text.toString().lowercase()

            if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty() || nuevoRol.isEmpty()) {
                Toast.makeText(this, "No se pueden dejar campos vacíos", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

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