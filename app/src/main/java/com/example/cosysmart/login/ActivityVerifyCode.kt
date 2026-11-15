
package com.example.cosysmart.login

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.VerificarCodigoRequest
import com.example.cosysmart.network.VerificarCodigoResponse
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityVerifyCode : AppCompatActivity() {

    private lateinit var etCodigo: EditText
    private lateinit var btnVerificar: MaterialButton
    private lateinit var btnVolver: MaterialButton

    private var correo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        etCodigo = findViewById(R.id.etCodigo)
        btnVerificar = findViewById(R.id.btnVerificarCodigo)
        btnVolver = findViewById(R.id.btnVolverRecuperacion)

        correo = intent.getStringExtra("correo_recuperacion").orEmpty()

        if (correo.isBlank()) {
            Toast.makeText(
                this,
                "No se recibió el correo de recuperación.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        btnVerificar.setOnClickListener {
            verificarCodigo()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun verificarCodigo() {

        val codigo = etCodigo.text.toString().trim()

        if (!codigo.matches(Regex("\\d{6}"))) {
            etCodigo.error = "Ingresa el código de seis dígitos"
            etCodigo.requestFocus()
            return
        }

        btnVerificar.isEnabled = false
        btnVerificar.text = "Verificando..."

        val request = VerificarCodigoRequest(
            correo = correo,
            codigo = codigo
        )

        RetrofitClient.instance
            .verificarCodigoRecuperacion(request)
            .enqueue(object : Callback<VerificarCodigoResponse> {

                override fun onResponse(
                    call: Call<VerificarCodigoResponse>,
                    response: Response<VerificarCodigoResponse>
                ) {
                    btnVerificar.isEnabled = true
                    btnVerificar.text = "Verificar código"

                    if (response.isSuccessful) {

                        val token = response.body()?.token

                        if (token.isNullOrBlank()) {
                            Toast.makeText(
                                this@ActivityVerifyCode,
                                "El servidor no devolvió un token válido.",
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }

                        val intent = Intent(
                            this@ActivityVerifyCode,
                            ActivityResetPassword::class.java
                        ).apply {
                            putExtra("token_recuperacion", token)
                        }

                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(
                            this@ActivityVerifyCode,
                            "Código incorrecto o expirado. Verifica e intenta nuevamente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<VerificarCodigoResponse>,
                    t: Throwable
                ) {
                    btnVerificar.isEnabled = true
                    btnVerificar.text = "Verificar código"

                    Toast.makeText(
                        this@ActivityVerifyCode,
                        "No se pudo conectar con el servidor. Intenta nuevamente.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
