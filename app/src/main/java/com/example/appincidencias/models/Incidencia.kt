package com.example.appincidencias.models

data class Incidencia(
    var id: String? = null,
    var aula: String = "",
    var descripcion: String = "",
    var urgencia: String = "",
    var fecha: String = "",
    var docenteId: String = "",
    var docenteNombre: String = "",
    var estado: String = "pendiente", // iniciada, asignada, en proceso, reparado, requiere_cau, avisado_cau, finalizada
    var asignadoA: String? = null,
    var comentarioGuardia: String = "", // NUEVO: Para que el guardia explique qué hizo
    var personaAvisoCAU: String = ""
)