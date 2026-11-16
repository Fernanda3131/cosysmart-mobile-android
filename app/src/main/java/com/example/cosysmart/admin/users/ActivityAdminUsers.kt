package com.example.cosysmart.admin.users

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cosysmart.R
import com.example.cosysmart.admin.ActivityHomeAdmin
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.UsuarioAdminMensajeResponse
import com.example.cosysmart.network.UsuarioAdminResponse
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Activity para listar usuarios del administrador
class ActivityAdminUsers : AppCompatActivity() {

    private lateinit var adapter: UsuariosAdapter
    private lateinit var recyclerUsuarios: RecyclerView
    private lateinit var tvEstadoListado: TextView

    private var rolActual = "TODOS"
    private var textoActual = ""

    private var listaUsuarios: List<UsuarioAdminResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_users)

        RetrofitClient.init(applicationContext)

        configurarNavegacion()
        configurarEstado()
        configurarRecycler()
        configurarBuscador()
        configurarFiltros()
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            cargarUsuarios()
        }
    }

    private fun configurarNavegacion() {
        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        BottomNavigationHelper.setupAdminNav(
            this,
            bottomNav,
            R.id.nav_usuarios
        )

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, ActivityHomeAdmin::class.java))
            finish()
        }
    }

    private fun configurarEstado() {
        tvEstadoListado = TextView(this).apply {
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#777777"))
            text = "Cargando usuarios..."
            setPadding(0, 12, 0, 8)
        }

        val recycler = findViewById<RecyclerView>(R.id.recyclerUsuarios)
        val contenedor = recycler.parent as? android.view.ViewGroup

        contenedor?.let {
            it.addView(
                tvEstadoListado,
                it.indexOfChild(recycler),
                android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun configurarRecycler() {
        recyclerUsuarios = findViewById(R.id.recyclerUsuarios)
        recyclerUsuarios.layoutManager = LinearLayoutManager(this)

        adapter = UsuariosAdapter(
            emptyList(),
            onConsultar = { usuario ->
                abrirDetalle(usuario)
            },
            onEditar = { usuario ->
                abrirEdicion(usuario)
            },
            onDesactivar = { usuario ->
                confirmarCambioEstado(usuario)
            }
        )

        recyclerUsuarios.adapter = adapter
    }

    private fun configurarBuscador() {
        val buscador = findViewById<EditText>(R.id.etBuscarUsuario)

        buscador.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                textoActual = s.toString()
                aplicarFiltros()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun configurarFiltros() {
        findViewById<MaterialButton>(R.id.btnFiltroTodos)
            .setOnClickListener {
                rolActual = "TODOS"
                aplicarFiltros()
            }

        findViewById<MaterialButton>(R.id.btnFiltroComprador)
            .setOnClickListener {
                rolActual = "COMPRADOR"
                aplicarFiltros()
            }

        findViewById<MaterialButton>(R.id.btnFiltroVendedor)
            .setOnClickListener {
                rolActual = "VENDEDOR"
                aplicarFiltros()
            }
    }

    private fun cargarUsuarios() {
        mostrarEstado("Cargando usuarios...")

        RetrofitClient.instance.listarUsuariosAdmin()
            .enqueue(object : Callback<List<UsuarioAdminResponse>> {

                override fun onResponse(
                    call: Call<List<UsuarioAdminResponse>>,
                    response: Response<List<UsuarioAdminResponse>>
                ) {
                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {
                        listaUsuarios = response.body() ?: emptyList()

                        adapter.actualizarLista(listaUsuarios)
                        aplicarFiltros()
                    } else {
                        mostrarEstado(
                            "Error al cargar usuarios. Código ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<UsuarioAdminResponse>>,
                    t: Throwable
                ) {
                    if (isFinishing || isDestroyed) return

                    mostrarEstado(
                        "Error de conexión: ${t.localizedMessage}"
                    )
                }
            })
    }

    private fun aplicarFiltros() {
        adapter.filtrar(textoActual, rolActual)

        if (adapter.itemCount == 0) {
            val mensaje = if (listaUsuarios.isEmpty()) {
                "No hay usuarios registrados."
            } else {
                "No se encontraron usuarios con esos filtros."
            }

            mostrarEstado(mensaje)
        } else {
            ocultarEstado()
        }
    }

    private fun abrirDetalle(usuario: UsuarioAdminResponse) {
        val intent = Intent(this, ActivityAdminUserDetail::class.java)
        intent.putExtra("ID_USUARIO", usuario.IdUsuario)
        startActivity(intent)
    }

    private fun abrirEdicion(usuario: UsuarioAdminResponse) {
        val intent = Intent(this, ActivityAdminEditUser::class.java)
        intent.putExtra("ID_USUARIO", usuario.IdUsuario)
        startActivity(intent)
    }

    // Determina si corresponde desactivar o reactivar.
    private fun confirmarCambioEstado(usuario: UsuarioAdminResponse) {
        val estaActivo = usuario.Estado.equals(
            "ACTIVO",
            ignoreCase = true
        )

        if (estaActivo) {
            confirmarDesactivacion(usuario)
        } else {
            confirmarReactivacion(usuario)
        }
    }

    private fun confirmarDesactivacion(usuario: UsuarioAdminResponse) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Desactivar usuario")
            .setMessage(
                "¿Seguro que deseas desactivar la cuenta de " +
                        "${usuario.Nombres} ${usuario.Apellidos}?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Desactivar") { _, _ ->
                desactivarUsuario(usuario.IdUsuario)
            }
            .show()
    }

    private fun confirmarReactivacion(usuario: UsuarioAdminResponse) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reactivar usuario")
            .setMessage(
                "¿Deseas reactivar la cuenta de " +
                        "${usuario.Nombres} ${usuario.Apellidos}?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Reactivar") { _, _ ->
                reactivarUsuario(usuario.IdUsuario)
            }
            .show()
    }

    private fun desactivarUsuario(idUsuario: Int) {
        mostrarEstado("Desactivando usuario...")

        RetrofitClient.instance.desactivarUsuarioAdmin(idUsuario)
            .enqueue(object : Callback<UsuarioAdminMensajeResponse> {

                override fun onResponse(
                    call: Call<UsuarioAdminMensajeResponse>,
                    response: Response<UsuarioAdminMensajeResponse>
                ) {
                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ActivityAdminUsers,
                            response.body()?.Mensaje
                                ?: "Usuario desactivado correctamente.",
                            Toast.LENGTH_LONG
                        ).show()

                        cargarUsuarios()
                    } else {
                        mostrarEstado(
                            "No se pudo desactivar. Código ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<UsuarioAdminMensajeResponse>,
                    t: Throwable
                ) {
                    if (isFinishing || isDestroyed) return

                    mostrarEstado(
                        "Error de conexión: ${t.localizedMessage}"
                    )
                }
            })
    }

    private fun reactivarUsuario(idUsuario: Int) {
        mostrarEstado("Reactivando usuario...")

        RetrofitClient.instance.reactivarUsuarioAdmin(idUsuario)
            .enqueue(object : Callback<UsuarioAdminMensajeResponse> {

                override fun onResponse(
                    call: Call<UsuarioAdminMensajeResponse>,
                    response: Response<UsuarioAdminMensajeResponse>
                ) {
                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ActivityAdminUsers,
                            response.body()?.Mensaje
                                ?: "Usuario reactivado correctamente.",
                            Toast.LENGTH_LONG
                        ).show()

                        cargarUsuarios()
                    } else {
                        mostrarEstado(
                            "No se pudo reactivar. Código ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<UsuarioAdminMensajeResponse>,
                    t: Throwable
                ) {
                    if (isFinishing || isDestroyed) return

                    mostrarEstado(
                        "Error de conexión: ${t.localizedMessage}"
                    )
                }
            })
    }

    private fun mostrarEstado(mensaje: String) {
        tvEstadoListado.text = mensaje
        tvEstadoListado.visibility = View.VISIBLE
        recyclerUsuarios.visibility = View.GONE
    }

    private fun ocultarEstado() {
        tvEstadoListado.visibility = View.GONE
        recyclerUsuarios.visibility = View.VISIBLE
    }
}
