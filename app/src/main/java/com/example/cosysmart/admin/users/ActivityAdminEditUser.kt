
package com.example.cosysmart.admin.users

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.RolAdminResponse
import com.example.cosysmart.network.UsuarioAdminActualizarRequest
import com.example.cosysmart.network.UsuarioAdminResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Activity para editar usuarios del administrador
class ActivityAdminEditUser : AppCompatActivity() {

    private var idUsuario = -1

    private lateinit var etNombres: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etContrasena: EditText
    private lateinit var tvStatus: TextView
    private lateinit var tvRoles: TextView
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnSeleccionarRoles: MaterialButton

    private var rolesDisponibles: List<RolAdminResponse> = emptyList()
    private val rolesSeleccionados = mutableSetOf<Int>()

    private var usuarioCargado = false
    private var rolesCargados = false
    private var guardando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit_user)

        RetrofitClient.init(applicationContext)

        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        if (idUsuario <= 0) {
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarVistas()

        findViewById<ImageButton>(R.id.btnBackEditUser)
            .setOnClickListener {
                finish()
            }

        btnGuardar.setOnClickListener {
            guardarCambios()
        }

        btnSeleccionarRoles.setOnClickListener {
            mostrarSelectorRoles()
        }

        cargarUsuario()
        cargarRoles()
    }

    private fun inicializarVistas() {
        etNombres = findViewById(R.id.etEditNombres)
        etApellidos = findViewById(R.id.etEditApellidos)
        etCorreo = findViewById(R.id.etEditCorreo)
        etTelefono = findViewById(R.id.etEditTelefono)
        etContrasena = findViewById(R.id.etEditContrasena)

        tvStatus = findViewById(R.id.tvEditUserStatus)
        tvRoles = findViewById(R.id.tvSelectedUserRoles)

        btnGuardar = findViewById(R.id.btnSaveUserChanges)
        btnSeleccionarRoles = findViewById(R.id.btnSelectUserRoles)

        btnGuardar.isEnabled = false
        btnSeleccionarRoles.isEnabled = false
    }

    private fun cargarUsuario() {
        mostrarEstado("Cargando usuario...")

        RetrofitClient.instance.obtenerUsuarioAdmin(idUsuario)
            .enqueue(object : Callback<UsuarioAdminResponse> {

                override fun onResponse(
                    call: Call<UsuarioAdminResponse>,
                    response: Response<UsuarioAdminResponse>
                ) {
                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {
                        val usuario = response.body()

                        if (usuario == null) {
                            mostrarEstado("No se recibieron datos del usuario.")
                            return
                        }

                        etNombres.setText(usuario.Nombres)
                        etApellidos.setText(usuario.Apellidos)
                        etCorreo.setText(usuario.Correo)
                        etTelefono.setText(usuario.Telefono.orEmpty())

                        usuarioCargado = true

                        if (rolesCargados) {
                            aplicarRolesDelUsuario(usuario.Roles)
                        }

                        comprobarFormularioListo()

                    } else {
                        mostrarEstado(
                            "Error al consultar usuario: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<UsuarioAdminResponse>,
                    t: Throwable
                ) {
                    if (isFinishing || isDestroyed) return
                    mostrarEstado("Error de conexión: ${t.localizedMessage}")
                }
            })
    }

    private fun cargarRoles() {
        RetrofitClient.instance.listarRolesAdmin()
            .enqueue(object : Callback<List<RolAdminResponse>> {

                override fun onResponse(
                    call: Call<List<RolAdminResponse>>,
                    response: Response<List<RolAdminResponse>>
                ) {
                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {
                        rolesDisponibles = response.body() ?: emptyList()
                        rolesCargados = true

                        if (rolesDisponibles.isEmpty()) {
                            mostrarEstado("No hay roles activos disponibles.")
                            return
                        }

                        // Si el usuario ya se cargó, se conservan sus roles.
                        comprobarFormularioListo()

                    } else {
                        mostrarEstado(
                            "No se pudieron cargar los roles: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<RolAdminResponse>>,
                    t: Throwable
                ) {
                    if (isFinishing || isDestroyed) return
                    mostrarEstado("Error al cargar roles: ${t.localizedMessage}")
                }
            })
    }

    private fun aplicarRolesDelUsuario(nombresRoles: List<String>) {
        rolesSeleccionados.clear()

        rolesDisponibles.forEach { rol ->
            if (nombresRoles.any {
                    it.equals(rol.Nombre, ignoreCase = true)
                }
            ) {
                rolesSeleccionados.add(rol.IdRol)
            }
        }

        actualizarTextoRoles()
    }

    private fun comprobarFormularioListo() {
        if (usuarioCargado && rolesCargados) {
            tvStatus.visibility = View.GONE
            btnGuardar.isEnabled = true
            btnSeleccionarRoles.isEnabled = true
        }
    }

    private fun mostrarSelectorRoles() {
        if (rolesDisponibles.isEmpty()) return

        val nombres = rolesDisponibles.map { it.Nombre }.toTypedArray()

        val seleccionados = BooleanArray(rolesDisponibles.size) { index ->
            rolesSeleccionados.contains(rolesDisponibles[index].IdRol)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Asignar roles")
            .setMultiChoiceItems(nombres, seleccionados) { _, which, checked ->
                val idRol = rolesDisponibles[which].IdRol

                if (checked) {
                    rolesSeleccionados.add(idRol)
                } else {
                    rolesSeleccionados.remove(idRol)
                }
            }
            .setPositiveButton("Aceptar") { _, _ ->
                actualizarTextoRoles()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarTextoRoles() {
        val nombres = rolesDisponibles
            .filter { rolesSeleccionados.contains(it.IdRol) }
            .map { it.Nombre }

        tvRoles.text = if (nombres.isEmpty()) {
            "Roles seleccionados: ninguno"
        } else {
            "Roles seleccionados: ${nombres.joinToString(", ")}"
        }
    }

    private fun guardarCambios() {
        if (guardando) return

        val nombres = etNombres.text.toString().trim()
        val apellidos = etApellidos.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val contrasena = etContrasena.text.toString()

        if (nombres.isEmpty() || apellidos.isEmpty() || correo.isEmpty()) {
            Toast.makeText(
                this,
                "Completa nombres, apellidos y correo.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.error = "Correo electrónico no válido"
            return
        }

        if (contrasena.isNotEmpty() && contrasena.length < 8) {
            etContrasena.error = "La contraseña debe tener al menos 8 caracteres"
            return
        }

        if (rolesSeleccionados.isEmpty()) {
            Toast.makeText(
                this,
                "Debes asignar al menos un rol.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val request = UsuarioAdminActualizarRequest(
            Nombres = nombres,
            Apellidos = apellidos,
            Correo = correo,
            Telefono = telefono.ifBlank { null },
            Contrasena = contrasena.ifBlank { null },
            Roles = rolesSeleccionados.toList()
        )

        guardando = true
        btnGuardar.isEnabled = false
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Guardando cambios..."

        RetrofitClient.instance.actualizarUsuarioAdmin(idUsuario, request)
            .enqueue(object : Callback<UsuarioAdminResponse> {

                override fun onResponse(
                    call: Call<UsuarioAdminResponse>,
                    response: Response<UsuarioAdminResponse>
                ) {
                    if (isFinishing || isDestroyed) return

                    guardando = false
                    btnGuardar.isEnabled = true

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ActivityAdminEditUser,
                            "Usuario actualizado correctamente.",
                            Toast.LENGTH_LONG
                        ).show()

                        setResult(RESULT_OK)
                        finish()

                    } else {
                        tvStatus.text =
                            "No se pudieron guardar los cambios. Código: ${response.code()}"
                    }
                }

                override fun onFailure(
                    call: Call<UsuarioAdminResponse>,
                    t: Throwable
                ) {
                    if (isFinishing || isDestroyed) return

                    guardando = false
                    btnGuardar.isEnabled = true

                    tvStatus.text =
                        "Error de conexión: ${t.localizedMessage}"
                }
            })
    }

    private fun mostrarEstado(mensaje: String) {
        tvStatus.text = mensaje
        tvStatus.visibility = View.VISIBLE
    }
}