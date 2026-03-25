package com.example.appincidencias.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appincidencias.R
import com.example.appincidencias.models.User

class UsuariosAdapter(
    private var lista: List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuario)
        val tvEmail: TextView = view.findViewById(R.id.tvEmailUsuario)
        val tvRol: TextView = view.findViewById(R.id.tvRolUsuario)
        val cardRol: com.google.android.material.card.MaterialCardView = view.findViewById(R.id.cardRolUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val user = lista[position]

        holder.tvNombre.text = user.nombre
        holder.tvEmail.text = user.email

        holder.tvRol.text = user.rol.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }

        when (user.rol.lowercase()) {
            "administrador" -> {
                holder.cardRol.setCardBackgroundColor(android.graphics.Color.parseColor("#FEE2E2"))
                holder.tvRol.setTextColor(android.graphics.Color.parseColor("#DC2626"))
            }
            "guardia" -> {
                holder.cardRol.setCardBackgroundColor(android.graphics.Color.parseColor("#D1FAE5"))
                holder.tvRol.setTextColor(android.graphics.Color.parseColor("#059669"))
            }
            "docente" -> {
                holder.cardRol.setCardBackgroundColor(android.graphics.Color.parseColor("#E0F2FE"))
                holder.tvRol.setTextColor(android.graphics.Color.parseColor("#0284C7"))
            }
            else -> {
                holder.cardRol.setCardBackgroundColor(android.graphics.Color.parseColor("#F3F4F6"))
                holder.tvRol.setTextColor(android.graphics.Color.parseColor("#4B5563"))
            }
        }

        holder.itemView.setOnClickListener { onClick(user) }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<User>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}