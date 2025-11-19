package com.example.appincidencias.models

data class Incidencia(
    var id: String? = null,
    var aula: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var urgencia: String = "",
    var fecha: String = "",
    var docenteId: String = "",
    var creador: String = "",
    var estado: String = "",
    var asignadoA: String? = null
)