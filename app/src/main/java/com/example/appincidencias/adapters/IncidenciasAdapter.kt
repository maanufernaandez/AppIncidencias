package com.example.appincidencias.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appincidencias.R
import com.example.appincidencias.models.Incidencia

class IncidenciasAdapter(
    private var lista: List<Incidencia>,
    private val onItemClick: (Incidencia) -> Unit // Función lambda para el click
) : RecyclerView.Adapter<IncidenciasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtAula: TextView = view.findViewById(R.id.txtAula)
        val txtDescripcion: TextView = view.findViewById(R.id.txtDescripcion)
        val txtEstado: TextView = view.findViewById(R.id.txtEstado)
        val txtFecha: TextView = view.findViewById(R.id.txtFecha)
        val viewStatus: View = view.findViewById(R.id.viewStatusColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incidencia, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.txtAula.text = "Aula ${item.aula}"
        holder.txtDescripcion.text = item.descripcion
        holder.txtFecha.text = item.fecha
        holder.txtEstado.text = item.estado.uppercase()

        // Lógica visual profesional: Color según estado
        when (item.estado.lowercase()) {
            "pendiente" -> {
                holder.viewStatus.setBackgroundColor(Color.parseColor("#D32F2F")) // Rojo
                holder.txtEstado.setTextColor(Color.parseColor("#D32F2F"))
            }
            "en proceso" -> {
                holder.viewStatus.setBackgroundColor(Color.parseColor("#FBC02D")) // Amarillo
                holder.txtEstado.setTextColor(Color.parseColor("#F9A825"))
            }
            "resuelta" -> {
                holder.viewStatus.setBackgroundColor(Color.parseColor("#388E3C")) // Verde
                holder.txtEstado.setTextColor(Color.parseColor("#388E3C"))
            }
            else -> {
                holder.viewStatus.setBackgroundColor(Color.GRAY)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Incidencia>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}