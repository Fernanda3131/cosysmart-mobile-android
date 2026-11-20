
package com.example.cosysmart.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.network.RecuperacionResponse
import com.example.cosysmart.network.RestablecerContrasenaRequest
import com.example.cosysmart.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
// Activity para restablecer contraseña

class ActivityResetPassword : AppCompatActivity() {

    private lateinit var etNuevaContrasena: TextInputEditText
    private lateinit var etConfirmarContrasena: TextInputEditText
    private lateinit var btnRestablecer: MaterialButton
    private lateinit var btnCancelar: MaterialButton

    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etNuevaContrasena = findViewById(R.id.etNuevaContrasena)
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena)
        btnRestablecer = findViewById(R.id.btnRestablecerContrasena)
        btnCancelar = findViewById(R.id.btnCancelarRestablecimiento)

        token = intent.getStringExtra("token_recuperacion").orEmpty()

        if (token.isBlank()) {
            Toast.makeText(
                this,
                "El proceso de recuperación no es válido.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        btnRestablecer.setOnClickListener {
            restablecerContrasena()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun restablecerContrasena() {

        val nuevaContrasena =
            etNuevaContrasena.text.toString()

        val confirmarContrasena =
            etConfirmarContrasena.text.toString()

        if (nuevaContrasena.isBlank()) {
            etNuevaContrasena.error =
                "Ingresa tu nueva contraseña"
            return
        }

        if (nuevaContrasena.length < 8) {
            etNuevaContrasena.error =
                "Debe tener al menos 8 caracteres"
            return
        }

        if (confirmarContrasena.isBlank()) {
            etConfirmarContrasena.error =
                "Confirma tu contraseña"
            return
        }

        if (nuevaContrasena != confirmarContrasena) {
            etConfirmarContrasena.error =
                "Las contraseñas no coinciden"
            return
        }

        btnRestablecer.isEnabled = false
        btnRestablecer.text = "Actualizando..."

        val request = RestablecerContrasenaRequest(
            token = token,
            nueva_contrasena = nuevaContrasena
        )

        RetrofitClient.instance
            .restablecerContrasena(request)
            .enqueue(object : Callback<RecuperacionResponse> {

                override fun onResponse(
                    call: Call<RecuperacionResponse>,
                    response: Response<RecuperacionResponse>
                ) {

                    btnRestablecer.isEnabled = true
                    btnRestablecer.text = "Cambiar contraseña"

                    if (response.isSuccessful) {

                        Toast.makeText(
                            this@ActivityResetPassword,
                            response.body()?.mensaje
                                ?: "Contraseña actualizada correctamente.",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(
                            this@ActivityResetPassword,
                            LoginActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }

                        startActivity(intent)
                        finish()

                    } else {

                        Toast.makeText(
                            this@ActivityResetPassword,
                            "No fue posible cambiar la contraseña. El proceso puede haber expirado.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<RecuperacionResponse>,
                    t: Throwable
                ) {

                    btnRestablecer.isEnabled = true
                    btnRestablecer.text = "Cambiar contraseña"

                    Toast.makeText(
                        this@ActivityResetPassword,
                        "Error de conexión con el servidor.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
