package com.example.appincidencias

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- NUEVO: Ocultar la barra superior por defecto ---
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            verificarExistenciaYEntrar(auth.currentUser!!.uid)
            return
        }

        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.inputEmail)
        val passInput = findViewById<EditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGotoRegister = findViewById<Button>(R.id.btnGotoRegister)
        val checkRecordar = findViewById<CheckBox>(R.id.checkRecordar)

        val prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        val savedEmail = prefs.getString("email", "")
        val savedPass = prefs.getString("password", "")
        val isRemembered = prefs.getBoolean("remember", false)

        if (isRemembered) {
            emailInput.setText(savedEmail)
            passInput.setText(savedPass)
            checkRecordar.isChecked = true
        }

        btnGotoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val em = emailInput.text.toString().trim()
            val pw = passInput.text.toString().trim()

            if (em.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(em, pw)
                .addOnSuccessListener { result ->
                    val editor = prefs.edit()
                    if (checkRecordar.isChecked) {
                        editor.putString("email", em)
                        editor.putString("password", pw)
                        editor.putBoolean("remember", true)
                    } else {
                        editor.clear()
                    }
                    editor.apply()

                    val uid = result.user?.uid ?: ""
                    verificarExistenciaYEntrar(uid)
                }
                .addOnFailureListener { exception ->
                    val mensaje = when (exception) {
                        is FirebaseAuthInvalidUserException -> "No existe un usuario con ese email"
                        is FirebaseAuthInvalidCredentialsException -> "La contraseña o el email están incorrectos"
                        else -> "Error de acceso: ${exception.message}"
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun verificarExistenciaYEntrar(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Usuario no encontrado en la base de datos.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                    if (intent.action == Intent.ACTION_MAIN) {
                        recreate()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
                auth.signOut()
            }
    }
}