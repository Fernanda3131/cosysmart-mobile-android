
package com.example.cosysmart.admin.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cosysmart.R
import com.example.cosysmart.network.UsuarioAdminResponse
import com.google.android.material.button.MaterialButton
import java.util.Locale


// Adapter para listar usuarios del administrador
class UsuariosAdapter(
    private var listaUsuarios: List<UsuarioAdminResponse>,
    private val onConsultar: (UsuarioAdminResponse) -> Unit,
    private val onEditar: (UsuarioAdminResponse) -> Unit,
    private val onDesactivar: (UsuarioAdminResponse) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder>() {

    private var listaCompleta = listaUsuarios.toList()

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvNombre: TextView =
            view.findViewById(R.id.tvNombreUsuario)

        val tvCorreo: TextView =
            view.findViewById(R.id.tvCorreoUsuario)

        val tvRol: TextView =
            view.findViewById(R.id.tvRolUsuario)

        val tvEstado: TextView =
            view.findViewById(R.id.tvEstadoUsuario)

        val btnConsultar: MaterialButton =
            view.findViewById(R.id.btnConsultar)

        val btnEditar: MaterialButton =
            view.findViewById(R.id.btnEditar)

        val btnDesactivar: MaterialButton =
            view.findViewById(R.id.btnDesactivar)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsuarioViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_users, parent, false)

        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UsuarioViewHolder,
        position: Int
    ) {

        val usuario = listaUsuarios[position]

        holder.tvNombre.text =
            "${usuario.Nombres} ${usuario.Apellidos}"

        holder.tvCorreo.text = usuario.Correo

        holder.tvRol.text = usuario.Roles.joinToString(", ")
            .ifBlank { "Sin rol asignado" }

        val activo = usuario.Estado.equals(
            "ACTIVO",
            ignoreCase = true
        )
        holder.tvEstado.text =
            if (activo) "Activo" else "Inactivo"

        holder.tvEstado.setTextColor(
            android.graphics.Color.parseColor(
                if (activo) "#2E7D32" else "#C0392B"
            )
        )

        holder.btnDesactivar.text =
            if (activo) "Desactivar" else "Reactivar"

        holder.btnDesactivar.isEnabled = true

        holder.btnDesactivar.alpha = 1f

        holder.btnConsultar.setOnClickListener {
            onConsultar(usuario)
        }

        holder.btnEditar.setOnClickListener {
            onEditar(usuario)
        }

        holder.btnDesactivar.setOnClickListener {
            onDesactivar(usuario)
        }
    }

    override fun getItemCount(): Int =
        listaUsuarios.size

    fun actualizarLista(
        nuevaLista: List<UsuarioAdminResponse>
    ) {

        listaCompleta = nuevaLista.toList()
        listaUsuarios = nuevaLista.toList()

        notifyDataSetChanged()
    }

    fun filtrar(
        textoBusqueda: String,
        filtroRol: String
    ) {

        val texto = textoBusqueda.trim()
            .lowercase(Locale.getDefault())

        listaUsuarios = listaCompleta.filter { usuario ->

            val nombreCompleto =
                "${usuario.Nombres} ${usuario.Apellidos}"
                    .lowercase(Locale.getDefault())

            val coincideTexto =
                texto.isEmpty() ||
                        nombreCompleto.contains(texto) ||
                        usuario.Correo
                            .lowercase(Locale.getDefault())
                            .contains(texto)

            val coincideRol =
                filtroRol == "TODOS" ||
                        usuario.Roles.any {
                            it.equals(filtroRol, ignoreCase = true)
                        }

            coincideTexto && coincideRol
        }

        notifyDataSetChanged()
    }
}
