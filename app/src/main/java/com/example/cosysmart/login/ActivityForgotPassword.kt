
package com.example.cosysmart.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.network.RecuperacionResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.SolicitudRecuperacionRequest
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityForgotPassword : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var btnEnviar: MaterialButton
    private lateinit var btnVolver: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etCorreo = findViewById(R.id.etCorreoRecuperacion)
        btnEnviar = findViewById(R.id.btnEnviarRecuperacion)
        btnVolver = findViewById(R.id.tvVolverLogin)

        btnEnviar.setOnClickListener {
            solicitarRecuperacion()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun solicitarRecuperacion() {

        val correo = etCorreo.text.toString().trim()

        if (correo.isEmpty()) {
            etCorreo.error = "Ingresa tu correo electrónico"
            etCorreo.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.error = "Ingresa un correo electrónico válido"
            etCorreo.requestFocus()
            return
        }

        btnEnviar.isEnabled = false
        btnEnviar.text = "Enviando..."

        val request = SolicitudRecuperacionRequest(
            correo = correo
        )

        RetrofitClient.instance
            .solicitarRecuperacion(request)
            .enqueue(object : Callback<RecuperacionResponse> {

                override fun onResponse(
                    call: Call<RecuperacionResponse>,
                    response: Response<RecuperacionResponse>
                ) {

                    btnEnviar.isEnabled = true
                    btnEnviar.text = "Enviar Código"

                    if (response.isSuccessful) {

                        val mensaje = response.body()?.mensaje
                            ?: "Si el correo está registrado, recibirás instrucciones."

                        Toast.makeText(
                            this@ActivityForgotPassword,
                            mensaje,
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(
                            this@ActivityForgotPassword,
                            ActivityVerifyCode::class.java
                        )

                        intent.putExtra("correo_recuperacion", correo)

                        startActivity(intent)

                    } else {

                        Toast.makeText(
                            this@ActivityForgotPassword,
                            "No fue posible procesar la solicitud. Intenta nuevamente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<RecuperacionResponse>,
                    t: Throwable
                ) {

                    btnEnviar.isEnabled = true
                    btnEnviar.text = "Enviar Código"

                    Toast.makeText(
                        this@ActivityForgotPassword,
                        "Error de conexión con el servidor: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
