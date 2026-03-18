package com.example.appincidencias.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
        val mainCard: MaterialCardView = view.findViewById(R.id.mainCard)
        val tvAula: TextView = view.findViewById(R.id.tvAula)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvUrgencia: TextView = view.findViewById(R.id.tvUrgencia)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val cardEstado: MaterialCardView = view.findViewById(R.id.cardEstado)
        val llDiagonalGroup: LinearLayout = view.findViewById(R.id.llBannerDiagonalGroup)
        val tvDiagonalText: TextView = view.findViewById(R.id.tvBannerOverlayText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incidencia, parent, false)
        return IncidenciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncidenciaViewHolder, position: Int) {
        val incidencia = lista[position]

        // --- RESETEAR VALORES POR DEFECTO (Necesario al reciclar vistas) ---
        holder.mainCard.setCardBackgroundColor(Color.WHITE)
        holder.llDiagonalGroup.visibility = View.GONE
        holder.tvUrgencia.visibility = View.VISIBLE
        holder.cardEstado.visibility = View.VISIBLE

        holder.tvAula.text = incidencia.aula
        holder.tvDescripcion.text = incidencia.descripcion
        holder.tvFecha.text = incidencia.fecha.replace(" ", " • ")

        val estadoLower = incidencia.estado.lowercase()

        // --- LÓGICA DE ESTADOS ESPECIALES ---
        if (estadoLower == "reparado") {
            // Fondo normal, cambiamos la urgencia por el aviso de comprobar CC
            holder.tvUrgencia.text = "Necesario Comprobar"
            holder.tvUrgencia.setTextColor(Color.parseColor("#D97706")) // Naranja oscuro

            holder.cardEstado.setCardBackgroundColor(Color.parseColor("#D1FAE5"))
            holder.tvEstado.setTextColor(Color.parseColor("#059669"))
            holder.tvEstado.text = "REPARADO"

        } else if (estadoLower == "finalizada") {
            // Tarjeta ensombrecida, ocultamos la etiqueta y la urgencia, mostramos franja verde
            holder.mainCard.setCardBackgroundColor(Color.parseColor("#F3F4F6")) // Gris claro
            holder.tvUrgencia.visibility = View.INVISIBLE
            holder.cardEstado.visibility = View.INVISIBLE

            // Capa diagonal visual CC
            holder.llDiagonalGroup.visibility = View.VISIBLE
            holder.llDiagonalGroup.setBackgroundColor(Color.parseColor("#CC10B981")) // Verde al 80% opacidad

            // Texto diagonal CC
            holder.tvDiagonalText.text = "FINALIZADA"

        } else if (estadoLower == "requiere_cau" || estadoLower == "avisado_cau") {
            // Tarjeta enrojecida, ocultamos etiqueta y urgencia, mostramos franja roja
            holder.mainCard.setCardBackgroundColor(Color.parseColor("#FEF2F2")) // Rojo muy claro
            holder.tvUrgencia.visibility = View.INVISIBLE
            holder.cardEstado.visibility = View.INVISIBLE

            // Capa diagonal visual CC
            holder.llDiagonalGroup.visibility = View.VISIBLE
            holder.llDiagonalGroup.setBackgroundColor(Color.parseColor("#CCEF4444")) // Rojo al 80% opacidad

            // Texto diagonal CC
            holder.tvDiagonalText.text = if(estadoLower == "requiere_cau") "REQUIERE CAU" else "AVISADO CAU"

        } else {
            // --- LÓGICA DE ESTADOS Y URGENCIAS NORMALES ---
            when (incidencia.urgencia.lowercase()) {
                "alta" -> {
                    holder.tvUrgencia.text = "Alta"
                    holder.tvUrgencia.setTextColor(Color.parseColor("#EF4444"))
                }
                "media" -> {
                    holder.tvUrgencia.text = "Media"
                    holder.tvUrgencia.setTextColor(Color.parseColor("#F59E0B"))
                }
                else -> {
                    holder.tvUrgencia.text = "Baja"
                    holder.tvUrgencia.setTextColor(Color.parseColor("#10B981"))
                }
            }

            holder.tvEstado.text = incidencia.estado.uppercase()
            when (estadoLower) {
                "iniciada", "pendiente" -> {
                    holder.cardEstado.setCardBackgroundColor(Color.parseColor("#FEF3C7"))
                    holder.tvEstado.setTextColor(Color.parseColor("#D97706"))
                }
                "asignada", "en proceso" -> {
                    holder.cardEstado.setCardBackgroundColor(Color.parseColor("#DBEAFE"))
                    holder.tvEstado.setTextColor(Color.parseColor("#1D4ED8"))
                }
                else -> {
                    holder.cardEstado.setCardBackgroundColor(Color.parseColor("#F3F4F6"))
                    holder.tvEstado.setTextColor(Color.parseColor("#4B5563"))
                }
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