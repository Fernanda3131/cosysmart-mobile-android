package com.example.cosysmart.register

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.UsuarioRequest
import com.example.cosysmart.network.UsuarioResponse
import com.example.cosysmart.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        val etNombres = findViewById<TextInputEditText>(R.id.etNombres)
        val etApellidos = findViewById<TextInputEditText>(R.id.etApellidos)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)
        val etConfirmarContrasena = findViewById<TextInputEditText>(R.id.etConfirmarContrasena)
        val rgTipoUsuario = findViewById<RadioGroup>(R.id.rgTipoUsuario)
        val rbVendedor = findViewById<RadioButton>(R.id.rbVendedor)
        val cbTerminos = findViewById<CheckBox>(R.id.cbTerminos)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegisterSubmit)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        configurarEnlaceTerminos(cbTerminos)

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnRegister.setOnClickListener {
            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()
            val confirmarContrasena = etConfirmarContrasena.text.toString().trim()

            if (nombres.isEmpty() || apellidos.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena.length < 8) {
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena != confirmarContrasena) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbTerminos.isChecked) {
                Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val esVendedor = rbVendedor.isChecked
            val rolSolicitado = if (esVendedor) "Vendedor" else "Comprador"

            val nuevoUsuario = UsuarioRequest(
                Nombres = nombres,
                Apellidos = apellidos,
                Correo = correo,
                Telefono = null,
                Contrasena = contrasena,
                FotoPerfil = null
            )

            RetrofitClient.instance.registrarUsuario(nuevoUsuario).enqueue(object : Callback<UsuarioResponse> {
                override fun onResponse(call: Call<UsuarioResponse>, response: Response<UsuarioResponse>) {
                    if (response.isSuccessful) {
                        val mensajeExito = if (esVendedor) {
                            "¡Registro enviado! Tu solicitud para ser vendedor está en revisión. 🛋️"
                        } else {
                            "¡Cuenta creada con éxito! Bienvenido a CosySmart. 🎉"
                        }
                        Toast.makeText(this@RegisterActivity, mensajeExito, Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        when (response.code()) {
                            400, 409 -> Toast.makeText(this@RegisterActivity, "El correo electrónico ya se encuentra registrado", Toast.LENGTH_LONG).show()
                            500 -> Toast.makeText(this@RegisterActivity, "Error en el servidor. Intenta más tarde", Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(this@RegisterActivity, "Error en el registro (${response.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<UsuarioResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Sin conexión a la API: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun configurarEnlaceTerminos(checkBox: CheckBox) {
        val textoCompleto = "Acepto los Términos y condiciones y la Política de privacidad."
        val palabraClave = "Términos y condiciones y la Política de privacidad"

        val spannableString = SpannableString(textoCompleto)
        val startIndex = textoCompleto.indexOf(palabraClave)
        val endIndex = startIndex + palabraClave.length

        if (startIndex >= 0) {
            spannableString.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(this@RegisterActivity, SecurityPoliciesActivity::class.java)
                    startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = 0xFFC86D51.toInt()
                }
            }, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            checkBox.text = spannableString
            checkBox.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}