package com.example.cosysmart.admin.users

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.UsuarioAdminResponse
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Activity para ver detalles de un usuario del administrador
class ActivityAdminUserDetail : AppCompatActivity() {

    private var idUsuario: Int = -1

    private lateinit var tvStatus: TextView
    private lateinit var layoutData: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_detail)

        RetrofitClient.init(applicationContext)

        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        if (idUsuario <= 0) {
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvStatus = findViewById(R.id.tvDetailStatus)
        layoutData = findViewById(R.id.layoutDetailData)

        findViewById<ImageButton>(R.id.btnBackDetail).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnEditFromDetail)
            .setOnClickListener {
                abrirEdicion()
            }

        cargarUsuario()
    }

    private fun cargarUsuario() {
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Cargando información..."
        layoutData.visibility = View.GONE

        RetrofitClient.instance.obtenerUsuarioAdmin(idUsuario)
            .enqueue(object : Callback<UsuarioAdminResponse> {

                override fun onResponse(
                    call: Call<UsuarioAdminResponse>,
                    response: Response<UsuarioAdminResponse>
                ) {
                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {
                        response.body()?.let {
                            mostrarUsuario(it)
                        } ?: run {
                            tvStatus.text = "El servidor no devolvió información."
                        }
                    } else {
                        tvStatus.text =
                            "No se pudo consultar el usuario. Código ${response.code()}"
                    }
                }

                override fun onFailure(
                    call: Call<UsuarioAdminResponse>,
                    t: Throwable
                ) {
                    if (isFinishing || isDestroyed) return
                    tvStatus.text =
                        "Error de conexión: ${t.localizedMessage}"
                }
            })
    }

    private fun mostrarUsuario(usuario: UsuarioAdminResponse) {
        tvStatus.visibility = View.GONE
        layoutData.visibility = View.VISIBLE

        findViewById<TextView>(R.id.tvDetailName).text =
            "${usuario.Nombres} ${usuario.Apellidos}"

        findViewById<TextView>(R.id.tvDetailEmail).text =
            "Correo: ${usuario.Correo}"

        findViewById<TextView>(R.id.tvDetailPhone).text =
            "Teléfono: ${usuario.Telefono ?: "No registrado"}"

        findViewById<TextView>(R.id.tvDetailRoles).text =
            "Roles: ${usuario.Roles.joinToString(", ").ifBlank { "Sin roles" }}"

        findViewById<TextView>(R.id.tvDetailState).text =
            "Estado: ${usuario.Estado}"

        findViewById<TextView>(R.id.tvDetailRegistration).text =
            "Fecha de registro: ${usuario.FechaRegistro}"

        findViewById<TextView>(R.id.tvDetailLastAccess).text =
            "Último acceso: ${usuario.UltimoAcceso ?: "Sin registros"}"
    }

    private fun abrirEdicion() {
        val intent = Intent(this, ActivityAdminEditUser::class.java)
        intent.putExtra("ID_USUARIO", idUsuario)
        startActivity(intent)
    }
}