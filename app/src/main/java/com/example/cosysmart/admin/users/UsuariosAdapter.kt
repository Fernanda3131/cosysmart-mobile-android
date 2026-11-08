package com.example.cosysmart.admin.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cosysmart.R
import com.example.cosysmart.network.UsuarioItem
import java.util.Locale

class UsuariosAdapter(private var listaUsuarios: List<UsuarioItem>) : RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder>() {

    private var listaCompleta: List<UsuarioItem> = ArrayList(listaUsuarios)

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuario)
        val tvCorreo: TextView = view.findViewById(R.id.tvCorreoUsuario)
        val tvRol: TextView = view.findViewById(R.id.tvRolUsuario)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_users, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = listaUsuarios[position]
        holder.tvNombre.text = usuario.nombre
        holder.tvCorreo.text = usuario.correo
        holder.tvRol.text = usuario.rol
        // Si el objeto UsuarioItem maneja estado, puedes asignarlo aquí:
        // holder.tvEstado.text = "• ${usuario.estado}"
    }

    override fun getItemCount(): Int = listaUsuarios.size

    fun actualizarLista(nuevaLista: List<UsuarioItem>) {
        listaCompleta = ArrayList(nuevaLista)
        listaUsuarios = nuevaLista
        notifyDataSetChanged()
    }

    fun filtrar(textoBusqueda: String, filtroRol: String) {
        val texto = textoBusqueda.lowercase(Locale.getDefault())

        listaUsuarios = listaCompleta.filter { usuario ->
            val coincideTexto = texto.isEmpty() ||
                    usuario.nombre.lowercase(Locale.getDefault()).contains(texto) ||
                    usuario.correo.lowercase(Locale.getDefault()).contains(texto)

            val coincideRol = when (filtroRol) {
                "TODOS" -> true
                else -> usuario.rol.uppercase(Locale.getDefault()) == filtroRol.uppercase(Locale.getDefault())
            }

            coincideTexto && coincideRol
        }
        notifyDataSetChanged()
    }
}