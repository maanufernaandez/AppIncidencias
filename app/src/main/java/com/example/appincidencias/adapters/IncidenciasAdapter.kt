package com.example.appincidencias.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.appincidencias.R
import com.example.appincidencias.models.Incidencia

// Cambio clave: Heredamos de BaseAdapter en lugar de RecyclerView.Adapter
class IncidenciasAdapter(
    private val context: Context,
    private val lista: List<Incidencia>
) : BaseAdapter() {

    // Indica cuántos elementos tiene la lista
    override fun getCount(): Int {
        return lista.size
    }

    // Obtiene un objeto específico de la lista
    override fun getItem(position: Int): Any {
        return lista[position]
    }

    // Obtiene el ID numérico (usamos la posición)
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Este es el método más importante: Crea la vista de cada fila
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // 1. Si la vista no existe, la creamos (inflamos)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_incidencia, parent, false)

        // 2. Obtenemos los datos de la incidencia actual
        val incidencia = lista[position]

        // 3. Buscamos los elementos visuales (TextViews)
        // Nota: Usamos los IDs que pondremos en el XML a continuación
        val txtAula = view.findViewById<TextView>(R.id.txtAula)
        val txtDescripcion = view.findViewById<TextView>(R.id.txtDescripcion)
        val txtEstado = view.findViewById<TextView>(R.id.txtEstado)

        // 4. Rellenamos los datos
        txtAula.text = "Aula: ${incidencia.aula}"
        txtDescripcion.text = incidencia.descripcion
        txtEstado.text = incidencia.estado

        return view
    }
}