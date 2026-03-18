package com.example.appincidencias

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val inputEmail = findViewById<EditText>(R.id.etEmail)
        val inputPassword = findViewById<EditText>(R.id.etPassword)
        val inputConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val inputNombre = findViewById<EditText>(R.id.etNombre)

        // Enlazamos el nuevo menú desplegable
        val autoCompleteRol = findViewById<AutoCompleteTextView>(R.id.autoCompleteRol)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Nombres más visuales (primera en mayúscula)
        val roles = listOf("Docente", "Guardia", "Administrador")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        autoCompleteRol.setAdapter(adapter)

        btnRegister.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val confirmPassword = inputConfirmPassword.text.toString().trim()
            val nombre = inputNombre.text.toString().trim()

            // Capturamos el texto y lo convertimos a minúscula para guardarlo en Firebase
            val rolTexto = autoCompleteRol.text.toString()
            val rol = rolTexto.lowercase()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || nombre.isEmpty() || rolTexto.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos y selecciona un rol", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tieneMayuscula = password.any { it.isUpperCase() }
            val tieneMinuscula = password.any { it.isLowerCase() }
            val tieneNumero = password.any { it.isDigit() }

            if (password.length < 6 || !tieneMayuscula || !tieneMinuscula || !tieneNumero) {
                Toast.makeText(this, "La contraseña debe tener 6 caracteres, mayúscula, minúscula y número", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: return@addOnSuccessListener

                    val userMap = hashMapOf(
                        "uid" to userId,
                        "nombre" to nombre,
                        "email" to email,
                        "rol" to rol
                    )

                    db.collection("usuarios")
                        .document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar datos: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error en registro: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}