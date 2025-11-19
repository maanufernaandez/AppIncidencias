package com.example.appincidencias.models

data class User(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val rol: String = "" // admin / guardia / docente
)
