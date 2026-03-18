package com.example.appincidencias.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appincidencias.R
import com.example.appincidencias.models.Incidencia
import com.google.android.material.card.MaterialCardView

class IncidenciasAdapter(
    private var lista: List<Incidencia>,
    private val onClick: (Incidencia) -> Unit
) : RecyclerView.Adapter<IncidenciasAdapter.IncidenciaViewHolder>() {

    class IncidenciaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAula: TextView = view.findViewById(R.id.tvAula)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvUrgencia: TextView = view.findViewById(R.id.tvUrgencia)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val cardEstado: MaterialCardView = view.findViewById(R.id.cardEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incidencia, parent, false)
        return IncidenciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncidenciaViewHolder, position: Int) {
        val incidencia = lista[position]

        holder.tvAula.text = incidencia.aula
        holder.tvDescripcion.text = incidencia.descripcion

        // Limpiamos un poco la fecha si es muy larga
        holder.tvFecha.text = incidencia.fecha.replace(" ", " • ")

        // Colores y Emojis dinámicos para URGENCIA
        when (incidencia.urgencia.lowercase()) {
            "alta" -> {
                holder.tvUrgencia.text = "🔴 Alta"
                holder.tvUrgencia.setTextColor(Color.parseColor("#EF4444"))
            }
            "media" -> {
                holder.tvUrgencia.text = "🟠 Media"
                holder.tvUrgencia.setTextColor(Color.parseColor("#F59E0B"))
            }
            else -> {
                holder.tvUrgencia.text = "🟢 Baja"
                holder.tvUrgencia.setTextColor(Color.parseColor("#10B981"))
            }
        }

        // Colores dinámicos para el ESTADO
        holder.tvEstado.text = incidencia.estado.uppercase()
        when (incidencia.estado.lowercase()) {
            "iniciada", "pendiente" -> {
                holder.cardEstado.setCardBackgroundColor(Color.parseColor("#FEF3C7")) // Fondo Amarillo
                holder.tvEstado.setTextColor(Color.parseColor("#D97706")) // Texto Naranja oscuro
            }
            "asignada", "en proceso" -> {
                holder.cardEstado.setCardBackgroundColor(Color.parseColor("#DBEAFE")) // Fondo Azul
                holder.tvEstado.setTextColor(Color.parseColor("#1D4ED8")) // Texto Azul oscuro
            }
            "reparado", "finalizada" -> {
                holder.cardEstado.setCardBackgroundColor(Color.parseColor("#D1FAE5")) // Fondo Verde
                holder.tvEstado.setTextColor(Color.parseColor("#059669")) // Texto Verde oscuro
            }
            "requiere_cau", "avisado_cau" -> {
                holder.cardEstado.setCardBackgroundColor(Color.parseColor("#FEE2E2")) // Fondo Rojo
                holder.tvEstado.setTextColor(Color.parseColor("#DC2626")) // Texto Rojo oscuro
            }
            else -> {
                holder.cardEstado.setCardBackgroundColor(Color.parseColor("#F3F4F6")) // Gris por defecto
                holder.tvEstado.setTextColor(Color.parseColor("#4B5563"))
            }
        }

        holder.itemView.setOnClickListener { onClick(incidencia) }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Incidencia>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}