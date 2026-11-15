
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.admin.ActivityHomeAdmin
import com.example.cosysmart.home.ActivityHome
import com.example.cosysmart.login.LoginActivity
import com.example.cosysmart.network.LoginRequest
import com.example.cosysmart.network.LoginResponse
import com.example.cosysmart.network.RegisterDeviceRequest
import com.example.cosysmart.network.RegisterDeviceResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.UsuarioRequest
import com.example.cosysmart.network.UsuarioResponse
import com.example.cosysmart.security.BiometricKeyManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private val preferencias by lazy {
        getSharedPreferences("CosyPrefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.init(this)

        setContentView(R.layout.register_activity)

        val etNombres =
            findViewById<TextInputEditText>(R.id.etNombres)

        val etApellidos =
            findViewById<TextInputEditText>(R.id.etApellidos)

        val etCorreo =
            findViewById<TextInputEditText>(R.id.etCorreo)

        val etContrasena =
            findViewById<TextInputEditText>(R.id.etContrasena)

        val etConfirmarContrasena =
            findViewById<TextInputEditText>(R.id.etConfirmarContrasena)

        val cbTerminos =
            findViewById<CheckBox>(R.id.cbTerminos)

        val btnRegister =
            findViewById<MaterialButton>(R.id.btnRegisterSubmit)

        val tvGoToLogin =
            findViewById<TextView>(R.id.tvGoToLogin)

        configurarEnlaceTerminos(cbTerminos)

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnRegister.setOnClickListener {

            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString()
            val confirmarContrasena =
                etConfirmarContrasena.text.toString()

            if (
                nombres.isEmpty() ||
                apellidos.isEmpty() ||
                correo.isEmpty() ||
                contrasena.isEmpty() ||
                confirmarContrasena.isEmpty()
            ) {
                mostrarMensaje("Por favor completa todos los campos")
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                mostrarMensaje("Ingresa un correo electrónico válido")
                return@setOnClickListener
            }

            if (contrasena.length < 8) {
                mostrarMensaje(
                    "La contraseña debe tener al menos 8 caracteres"
                )
                return@setOnClickListener
            }

            if (contrasena != confirmarContrasena) {
                mostrarMensaje("Las contraseñas no coinciden")
                return@setOnClickListener
            }

            if (!cbTerminos.isChecked) {
                mostrarMensaje(
                    "Debes aceptar los términos y condiciones"
                )
                return@setOnClickListener
            }

            val nuevoUsuario = UsuarioRequest(
                Nombres = nombres,
                Apellidos = apellidos,
                Correo = correo,
                Telefono = null,
                Contrasena = contrasena,
                FotoPerfil = null
            )

            btnRegister.isEnabled = false

            RetrofitClient.instance.registrarUsuario(nuevoUsuario)
                .enqueue(object : Callback<UsuarioResponse> {

                    override fun onResponse(
                        call: Call<UsuarioResponse>,
                        response: Response<UsuarioResponse>
                    ) {
                        if (response.isSuccessful) {

                            /*
                             * La nueva cuenta no debe reutilizar
                             * la credencial biométrica de una cuenta anterior.
                             */
                            BiometricKeyManager.eliminarLlaves()

                            preferencias.edit()
                                .remove("JWT_TOKEN")
                                .remove("BIOMETRIC_ENABLED")
                                .remove("BIOMETRIC_EMAIL")
                                .remove("BIOMETRIC_CREDENTIAL_ID")
                                .remove("BIOMETRIC_DEVICE_ID")
                                .putString("LOGIN_EMAIL", correo)
                                .apply()

                            Toast.makeText(
                                this@RegisterActivity,
                                "Cuenta creada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            iniciarSesionAutomatica(
                                correo,
                                contrasena,
                                btnRegister
                            )

                        } else {

                            btnRegister.isEnabled = true

                            val mensaje = when (response.code()) {
                                400, 409 ->
                                    "El correo electrónico ya se encuentra registrado"

                                422 ->
                                    "Verifica los datos ingresados"

                                500 ->
                                    "Error en el servidor. Intenta más tarde"

                                else ->
                                    "Error en el registro (${response.code()})"
                            }

                            mostrarMensaje(mensaje)
                        }
                    }

                    override fun onFailure(
                        call: Call<UsuarioResponse>,
                        t: Throwable
                    ) {
                        btnRegister.isEnabled = true

                        mostrarMensaje(
                            "No fue posible conectar con el servidor: ${t.message}"
                        )
                    }
                })
        }
    }

    private fun iniciarSesionAutomatica(
        correo: String,
        contrasena: String,
        btnRegister: MaterialButton
    ) {

        val request = LoginRequest(
            Correo = correo,
            Contrasena = contrasena
        )

        RetrofitClient.instance.loginUsuario(request)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {

                    if (!response.isSuccessful || response.body() == null) {

                        btnRegister.isEnabled = true

                        mostrarMensaje(
                            "La cuenta se creó, pero no fue posible iniciar sesión automáticamente. Inicia sesión manualmente."
                        )

                        irAlLogin()
                        return
                    }

                    val loginResponse = response.body()!!
                    val token = loginResponse.access_token

                    if (token.isNullOrBlank()) {

                        btnRegister.isEnabled = true

                        mostrarMensaje(
                            "El servidor no devolvió un token de acceso"
                        )

                        irAlLogin()
                        return
                    }

                    val guardado = preferencias.edit()
                        .putString("JWT_TOKEN", token)
                        .putString("LOGIN_EMAIL", correo)
                        .commit()

                    if (!guardado) {

                        btnRegister.isEnabled = true

                        mostrarMensaje(
                            "No fue posible guardar la sesión"
                        )

                        irAlLogin()
                        return
                    }

                    val rolUsuario = (
                            loginResponse.rol
                                ?: loginResponse.usuario.Rol
                            ).trim().lowercase()

                    val nombreUsuario =
                        loginResponse.usuario.Nombres

                    btnRegister.isEnabled = true

                    mostrarModalActivarBiometria(
                        nombreUsuario,
                        rolUsuario
                    )
                }

                override fun onFailure(
                    call: Call<LoginResponse>,
                    t: Throwable
                ) {
                    btnRegister.isEnabled = true

                    mostrarMensaje(
                        "La cuenta se creó, pero no fue posible conectar para iniciar sesión."
                    )

                    irAlLogin()
                }
            })
    }

    private fun mostrarModalActivarBiometria(
        nombreUsuario: String,
        rolUsuario: String
    ) {

        AlertDialog.Builder(this)
            .setTitle("Acceso más rápido y seguro")
            .setMessage(
                "Hola $nombreUsuario, ¿deseas activar el inicio de sesión biométrico para futuras ocasiones?"
            )
            .setCancelable(false)
            .setPositiveButton("Sí, activar") { _, _ ->

                registrarDispositivoBiometrico(
                    nombreUsuario,
                    rolUsuario
                )
            }
            .setNegativeButton("Ahora no") { _, _ ->

                redirigirSegunRol(
                    rolUsuario,
                    nombreUsuario
                )
            }
            .show()
    }

    private fun registrarDispositivoBiometrico(
        nombreUsuario: String,
        rolUsuario: String
    ) {

        try {

            val generado =
                BiometricKeyManager.generarParDeLlaves()

            if (!generado) {

                mostrarMensaje(
                    "No fue posible generar las claves biométricas"
                )

                redirigirSegunRol(rolUsuario, nombreUsuario)
                return
            }

            val clavePublica =
                BiometricKeyManager.obtenerClavePublicaPem()

            if (clavePublica.isNullOrBlank()) {

                mostrarMensaje(
                    "No fue posible obtener la clave pública"
                )

                redirigirSegunRol(rolUsuario, nombreUsuario)
                return
            }

            val request = RegisterDeviceRequest(
                NombreDispositivo =
                    android.os.Build.MODEL ?: "Android",
                ClavePublica = clavePublica
            )

            RetrofitClient.instance.registrarDispositivo(request)
                .enqueue(object : Callback<RegisterDeviceResponse> {

                    override fun onResponse(
                        call: Call<RegisterDeviceResponse>,
                        response: Response<RegisterDeviceResponse>
                    ) {

                        if (
                            response.isSuccessful &&
                            response.body() != null
                        ) {

                            val dispositivo = response.body()!!

                            val guardado = preferencias.edit()
                                .putBoolean(
                                    "BIOMETRIC_ENABLED",
                                    true
                                )
                                .putString(
                                    "BIOMETRIC_EMAIL",
                                    preferencias.getString(
                                        "LOGIN_EMAIL",
                                        null
                                    )
                                )
                                .putString(
                                    "BIOMETRIC_CREDENTIAL_ID",
                                    dispositivo.IdCredencial
                                )
                                .putInt(
                                    "BIOMETRIC_DEVICE_ID",
                                    dispositivo.IdDispositivo
                                )
                                .commit()

                            if (guardado) {

                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Dispositivo biométrico registrado",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {

                                preferencias.edit()
                                    .putBoolean(
                                        "BIOMETRIC_ENABLED",
                                        false
                                    )
                                    .apply()

                                mostrarMensaje(
                                    "No fue posible guardar la configuración biométrica"
                                )
                            }

                        } else {

                            preferencias.edit()
                                .putBoolean(
                                    "BIOMETRIC_ENABLED",
                                    false
                                )
                                .apply()

                            mostrarMensaje(
                                "No se pudo registrar la biometría (${response.code()})"
                            )
                        }

                        redirigirSegunRol(
                            rolUsuario,
                            nombreUsuario
                        )
                    }

                    override fun onFailure(
                        call: Call<RegisterDeviceResponse>,
                        t: Throwable
                    ) {

                        preferencias.edit()
                            .putBoolean(
                                "BIOMETRIC_ENABLED",
                                false
                            )
                            .apply()

                        mostrarMensaje(
                            "Error de conexión al registrar la biometría"
                        )

                        redirigirSegunRol(
                            rolUsuario,
                            nombreUsuario
                        )
                    }
                })

        } catch (e: Exception) {

            preferencias.edit()
                .putBoolean("BIOMETRIC_ENABLED", false)
                .apply()

            mostrarMensaje(
                "No fue posible configurar la biometría: ${e.message}"
            )

            redirigirSegunRol(
                rolUsuario,
                nombreUsuario
            )
        }
    }

    private fun redirigirSegunRol(
        rolUsuario: String,
        nombreUsuario: String
    ) {

        val intent = when {

            rolUsuario.contains("admin") -> {

                Intent(
                    this,
                    ActivityHomeAdmin::class.java
                ).apply {
                    putExtra("USER_NAME", nombreUsuario)
                }
            }

            rolUsuario.contains("seller") ||
                    rolUsuario.contains("vendedor") -> {

                Intent(
                    this,
                    ActivityHome::class.java
                ).apply {
                    putExtra("USER_NAME", nombreUsuario)
                    putExtra("USER_ROLE", "seller")
                }
            }

            else -> {

                Intent(
                    this,
                    ActivityHome::class.java
                ).apply {
                    putExtra("USER_NAME", nombreUsuario)
                    putExtra("USER_ROLE", "buyer")
                }
            }
        }

        startActivity(intent)
        finish()
    }

    private fun irAlLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
        )
        finish()
    }

    private fun configurarEnlaceTerminos(checkBox: CheckBox) {

        val textoCompleto =
            "Acepto los Términos y condiciones y la Política de privacidad."

        val palabraClave =
            "Términos y condiciones y la Política de privacidad"

        val spannableString = SpannableString(textoCompleto)

        val startIndex = textoCompleto.indexOf(palabraClave)
        val endIndex = startIndex + palabraClave.length

        if (startIndex >= 0) {

            spannableString.setSpan(
                object : ClickableSpan() {

                    override fun onClick(widget: View) {

                        startActivity(
                            Intent(
                                this@RegisterActivity,
                                SecurityPoliciesActivity::class.java
                            )
                        )
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = true
                        ds.color = 0xFFC86D51.toInt()
                    }
                },
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            checkBox.text = spannableString
            checkBox.movementMethod =
                LinkMovementMethod.getInstance()
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
}
