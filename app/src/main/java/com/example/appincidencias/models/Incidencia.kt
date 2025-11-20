package com.example.appincidencias.models

data class Incidencia(
    var id: String? = null,
    var aula: String = "",
    var descripcion: String = "",
    var urgencia: String = "",
    var fecha: String = "",
    var docenteId: String = "",
    var docenteNombre: String = "",
    var estado: String = "pendiente",
    var asignadoA: String? = null
)