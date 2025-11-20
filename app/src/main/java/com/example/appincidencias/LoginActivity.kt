package com.example.appincidencias

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // 1. Comprobación de sesión iniciada (Auto-Login de Firebase)
        // Si el usuario no cerró sesión explícitamente, Firebase lo recuerda.
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

        // 2. RECUPERAR DATOS GUARDADOS (SharedPreferences)
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

            // 3. Intentamos loguear con Authentication
            auth.signInWithEmailAndPassword(em, pw)
                .addOnSuccessListener { result ->

                    // 4. GESTIÓN DE "RECORDAR DATOS"
                    val editor = prefs.edit()
                    if (checkRecordar.isChecked) {
                        // Si está marcado, guardamos todo
                        editor.putString("email", em)
                        editor.putString("password", pw)
                        editor.putBoolean("remember", true)
                    } else {
                        // Si no está marcado, borramos todo (limpieza)
                        editor.clear()
                    }
                    editor.apply() // Guardar cambios

                    // Login correcto, verificamos BD
                    val uid = result.user?.uid ?: ""
                    verificarExistenciaYEntrar(uid)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error de acceso: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Función auxiliar para comprobar si el usuario tiene datos en Firestore
    private fun verificarExistenciaYEntrar(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // TODO CORRECTO: Existe login y existen datos
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // ERROR: Existe login PERO NO existen datos (lo borraste de la BD)
                    Toast.makeText(this, "Usuario no encontrado en la base de datos.", Toast.LENGTH_LONG).show()
                    auth.signOut() // Le cerramos la sesión forzosamente

                    // Si veníamos de la comprobación automática, hay que cargar la UI de login
                    // para que el usuario pueda intentar otra cuenta
                    if (intent.action == Intent.ACTION_MAIN) {
                        // Como ya estamos en LoginActivity, simplemente recargamos la vista
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