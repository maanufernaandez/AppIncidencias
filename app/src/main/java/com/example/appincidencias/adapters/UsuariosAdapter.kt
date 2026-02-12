package com.example.appincidencias.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appincidencias.models.User

class UsuariosAdapter(
    private var lista: List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Usamos un layout nativo de Android (simple_list_item_2) que tiene dos TextViews: text1 y text2
        val tvNombre: TextView = view.findViewById(android.R.id.text1)
        val tvDetalles: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        // Inflamos el layout predefinido de Android para ahorrarte crear un XML nuevo
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val user = lista[position]

        // Texto principal: Nombre y Rol en mayúsculas
        holder.tvNombre.text = "${user.nombre} (${user.rol.uppercase()})"

        // Texto secundario: Email y contraseña oculta
        holder.tvDetalles.text = "Email: ${user.email}\nPass: •••••••• (Encriptado)"

        // Detectar click para abrir el editor
        holder.itemView.setOnClickListener { onClick(user) }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<User>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}