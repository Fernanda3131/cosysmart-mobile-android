package com.example.cosysmart.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.network.LoginRequest
import com.example.cosysmart.network.LoginResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.register.RegisterActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val etCorreo = findViewById<TextInputEditText>(R.id.etLoginCorreo)
        val etContrasena = findViewById<TextInputEditText>(R.id.etLoginContrasena)
        val btnLoginSubmit = findViewById<MaterialButton>(R.id.btnLoginSubmit)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        tvGoToRegister?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvForgotPassword?.setOnClickListener {
            Toast.makeText(this, "Función de recuperación de contraseña en proceso", Toast.LENGTH_SHORT).show()
        }

        btnLoginSubmit.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(
                Correo = correo,
                Contrasena = contrasena
            )

            RetrofitClient.instance.loginUsuario(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginData = response.body()!!
                        Toast.makeText(this@LoginActivity, "¡Bienvenido a CosySmart!", Toast.LENGTH_LONG).show()

                        finish()
                    } else {
                        when (response.code()) {
                            401 -> Toast.makeText(this@LoginActivity, "Credenciales incorrectas", Toast.LENGTH_LONG).show()
                            404 -> Toast.makeText(this@LoginActivity, "El usuario no existe", Toast.LENGTH_LONG).show()
                            else -> Toast.makeText(this@LoginActivity, "Error al iniciar sesión (${response.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Sin conexión al servidor: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}