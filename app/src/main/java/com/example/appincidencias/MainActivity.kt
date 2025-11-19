package com.example.appincidencias

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.appincidencias.ui.CrearIncidenciaActivity
import com.example.appincidencias.ui.ListaIncidenciasActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnCrearIncidencia).setOnClickListener {
            startActivity(Intent(this, CrearIncidenciaActivity::class.java))
        }

        findViewById<Button>(R.id.btnLista).setOnClickListener {
            startActivity(Intent(this, ListaIncidenciasActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
