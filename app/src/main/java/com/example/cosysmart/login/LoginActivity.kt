
package com.example.cosysmart.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.cosysmart.R
import com.example.cosysmart.admin.ActivityHomeAdmin
import com.example.cosysmart.home.ActivityHome
import com.example.cosysmart.network.BiometriaLoginResponse
import com.example.cosysmart.network.ChallengeRequest
import com.example.cosysmart.network.ChallengeResponse
import com.example.cosysmart.network.LoginRequest
import com.example.cosysmart.network.LoginResponse
import com.example.cosysmart.network.RegisterDeviceRequest
import com.example.cosysmart.network.RegisterDeviceResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.VerifyBiometricRequest
import com.example.cosysmart.register.RegisterActivity
import com.example.cosysmart.security.BiometricKeyManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.Signature
import java.util.Locale

// Activity para autenticación de usuario con biometrica y correo

class LoginActivity : AppCompatActivity() {

    private lateinit var etLoginCorreo: TextInputEditText
    private lateinit var etLoginContrasena: TextInputEditText
    private lateinit var btnLoginSubmit: MaterialButton

    private var autenticacionAutomaticaIniciada = false
    private var solicitudBiometricaEnCurso = false

    private val preferencias by lazy {
        getSharedPreferences("CosyPrefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.init(this)
        setContentView(R.layout.login_activity)

        etLoginCorreo = findViewById(R.id.etLoginCorreo)
        etLoginContrasena = findViewById(R.id.etLoginContrasena)
        btnLoginSubmit = findViewById(R.id.btnLoginSubmit)

        val tvGoToRegister =
            findViewById<TextView>(R.id.tvGoToRegister)

        val tvForgotPassword =
            findViewById<TextView>(R.id.tvForgotPassword)

        val tvOlvidasteContrasena =
            findViewById<TextView>(R.id.tvOlvidasteContrasena)

        btnLoginSubmit.setOnClickListener {
            iniciarSesionNormal()
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            abrirRecuperacionContrasena()
        }

        tvOlvidasteContrasena.setOnClickListener {
            abrirRecuperacionContrasena()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!autenticacionAutomaticaIniciada) {
            autenticacionAutomaticaIniciada = true

            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    verificarHardwareBiometrico()
                }
            }
        }
    }

    private fun abrirRecuperacionContrasena() {
        startActivity(
            Intent(this, ActivityForgotPassword::class.java)
        )
    }
    private fun iniciarSesionNormal() {

        val correo = etLoginCorreo.text.toString().trim()
        val contrasena = etLoginContrasena.text.toString()

        if (correo.isBlank() || contrasena.isBlank()) {
            mostrarMensaje("Completa el correo y la contraseña")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarMensaje("Ingresa un correo electrónico válido")
            return
        }

        btnLoginSubmit.isEnabled = false

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

                    btnLoginSubmit.isEnabled = true

                    if (!response.isSuccessful || response.body() == null) {
                        mostrarMensaje(
                            when (response.code()) {
                                400, 401 ->
                                    "Correo o contraseña incorrectos"

                                else ->
                                    "No fue posible iniciar sesión (${response.code()})"
                            }
                        )
                        return
                    }

                    val resultado = response.body()!!
                    val token = resultado.access_token

                    if (token.isNullOrBlank()) {
                        mostrarMensaje(
                            "El servidor no devolvió un token de acceso"
                        )
                        return
                    }

                    val guardado = preferencias.edit()
                        .putString("JWT_TOKEN", token)
                        .putString("LOGIN_EMAIL", correo)
                        .commit()

                    if (!guardado) {
                        mostrarMensaje("No fue posible guardar la sesión")
                        return
                    }

                    val rol = (
                            resultado.rol ?: resultado.usuario.Rol
                            ).trim().lowercase(Locale.ROOT)

                    comprobarYMostrarModalBiometria(
                        correo,
                        resultado.usuario.Nombres,
                        rol
                    )
                }

                override fun onFailure(
                    call: Call<LoginResponse>,
                    t: Throwable
                ) {
                    btnLoginSubmit.isEnabled = true
                    mostrarMensaje("Error de conexión: ${t.message}")
                }
            })
    }
    // COMPROBAR CONFIGURACIÓN BIOMÉTRICA

    private fun comprobarYMostrarModalBiometria(
        correo: String,
        nombreUsuario: String,
        rolUsuario: String
    ) {

        val correoBiometrico = preferencias.getString(
            "BIOMETRIC_EMAIL",
            null
        )

        val habilitada = preferencias.getBoolean(
            "BIOMETRIC_ENABLED",
            false
        )

        val idCredencial = preferencias.getString(
            "BIOMETRIC_CREDENTIAL_ID",
            null
        )

        if (
            habilitada &&
            !idCredencial.isNullOrBlank() &&
            correo.equals(correoBiometrico, ignoreCase = true)
        ) {
            redirigirSegunRol(rolUsuario, nombreUsuario)
            return
        }

        if (
            habilitada &&
            !correo.equals(correoBiometrico, ignoreCase = true)
        ) {
            AlertDialog.Builder(this)
                .setTitle("Otra cuenta tiene biometría registrada")
                .setMessage(
                    "Este dispositivo tiene una credencial biométrica vinculada a otra cuenta. Puedes continuar con tu contraseña."
                )
                .setPositiveButton("Continuar") { _, _ ->
                    redirigirSegunRol(rolUsuario, nombreUsuario)
                }
                .show()

            return
        }

        mostrarModalActivarBiometria(
            correo,
            nombreUsuario,
            rolUsuario
        )
    }

    private fun mostrarModalActivarBiometria(
        correo: String,
        nombreUsuario: String,
        rolUsuario: String
    ) {

        AlertDialog.Builder(this)
            .setTitle("Acceso biométrico")
            .setMessage(
                "Hola $nombreUsuario. ¿Deseas activar el inicio de sesión con huella para este dispositivo?"
            )
            .setCancelable(false)
            .setPositiveButton("Activar") { _, _ ->
                registrarDispositivoBiometrico(
                    correo,
                    nombreUsuario,
                    rolUsuario
                )
            }
            .setNegativeButton("Ahora no") { _, _ ->
                redirigirSegunRol(rolUsuario, nombreUsuario)
            }
            .show()
    }

    private fun registrarDispositivoBiometrico(
        correo: String,
        nombreUsuario: String,
        rolUsuario: String
    ) {

        try {

            if (!BiometricKeyManager.generarParDeLlaves()) {
                mostrarMensaje("No fue posible generar las claves biométricas")
                redirigirSegunRol(rolUsuario, nombreUsuario)
                return
            }

            val clavePublica =
                BiometricKeyManager.obtenerClavePublicaPem()

            if (clavePublica.isNullOrBlank()) {
                mostrarMensaje("No fue posible obtener la clave pública")
                redirigirSegunRol(rolUsuario, nombreUsuario)
                return
            }

            val request = RegisterDeviceRequest(
                NombreDispositivo = android.os.Build.MODEL ?: "Android",
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
                                .putBoolean("BIOMETRIC_ENABLED", true)
                                .putString("BIOMETRIC_EMAIL", correo)
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
                                mostrarMensaje(
                                    "Biometría configurada correctamente"
                                )
                            } else {
                                preferencias.edit()
                                    .putBoolean("BIOMETRIC_ENABLED", false)
                                    .apply()

                                mostrarMensaje(
                                    "No fue posible guardar la configuración biométrica"
                                )
                            }

                        } else {
                            mostrarMensaje(
                                "No se pudo registrar el dispositivo (${response.code()})"
                            )
                        }

                        redirigirSegunRol(rolUsuario, nombreUsuario)
                    }

                    override fun onFailure(
                        call: Call<RegisterDeviceResponse>,
                        t: Throwable
                    ) {
                        mostrarMensaje(
                            "Error de conexión al registrar la biometría"
                        )
                        redirigirSegunRol(rolUsuario, nombreUsuario)
                    }
                })

        } catch (e: Exception) {
            mostrarMensaje(
                "No fue posible configurar la biometría: ${e.message}"
            )
            redirigirSegunRol(rolUsuario, nombreUsuario)
        }
    }
    private fun verificarHardwareBiometrico() {

        if (solicitudBiometricaEnCurso) return

        val habilitada = preferencias.getBoolean(
            "BIOMETRIC_ENABLED",
            false
        )

        val correo = preferencias.getString(
            "BIOMETRIC_EMAIL",
            null
        )

        val idCredencial = preferencias.getString(
            "BIOMETRIC_CREDENTIAL_ID",
            null
        )

        if (
            !habilitada ||
            correo.isNullOrBlank() ||
            idCredencial.isNullOrBlank()
        ) {
            Log.d("BIOMETRIA", "No existe una credencial local válida")
            return
        }

        val biometricManager = BiometricManager.from(this)

        val resultado = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )

        if (
            resultado != BiometricManager.BIOMETRIC_SUCCESS
        ) {
            Log.d(
                "BIOMETRIA",
                "Biometría no disponible. Código: $resultado"
            )
            return
        }

        solicitudBiometricaEnCurso = true
        solicitarDesafioAlBackend()
    }
    // SOLICITAR DESAFÍO

    private fun solicitarDesafioAlBackend() {

        val correo = preferencias.getString(
            "BIOMETRIC_EMAIL",
            null
        )

        val idCredencial = preferencias.getString(
            "BIOMETRIC_CREDENTIAL_ID",
            null
        )

        if (correo.isNullOrBlank() || idCredencial.isNullOrBlank()) {
            solicitudBiometricaEnCurso = false
            return
        }

        val request = ChallengeRequest(
            Correo = correo,
            IdCredencial = idCredencial
        )

        RetrofitClient.instance.solicitarDesafio(request)
            .enqueue(object : Callback<ChallengeResponse> {

                override fun onResponse(
                    call: Call<ChallengeResponse>,
                    response: Response<ChallengeResponse>
                ) {

                    if (!response.isSuccessful) {

                        val error = response.errorBody()?.string()

                        Log.e(
                            "BIOMETRIA",
                            "Error ${response.code()}: $error"
                        )

                        solicitudBiometricaEnCurso = false
                        return
                    }

                    val desafio = response.body()

                    if (desafio == null) {
                        solicitudBiometricaEnCurso = false
                        return
                    }

                    val nonceBytes =
                        BiometricKeyManager.decodificarNonce(desafio.Nonce)

                    if (nonceBytes == null || nonceBytes.size != 32) {
                        solicitudBiometricaEnCurso = false
                        mostrarMensaje("El nonce recibido no es válido")
                        return
                    }

                    val mensaje =
                        BiometricKeyManager.construirMensajeFirma(
                            desafio.ChallengeId,
                            idCredencial,
                            nonceBytes
                        )

                    val firmaPreparada =
                        BiometricKeyManager.prepararFirma(mensaje)

                    if (firmaPreparada == null) {
                        solicitudBiometricaEnCurso = false
                        mostrarMensaje(
                            "No fue posible preparar la firma digital"
                        )
                        return
                    }

                    launchBiometricPrompt(
                        correo,
                        idCredencial,
                        desafio,
                        firmaPreparada
                    )
                }

                override fun onFailure(
                    call: Call<ChallengeResponse>,
                    t: Throwable
                ) {
                    solicitudBiometricaEnCurso = false

                    Log.e(
                        "BIOMETRIA",
                        "Error solicitando desafío",
                        t
                    )
                }
            })
    }

    // DIÁLOGO NATIVO DE ANDROID

    private fun launchBiometricPrompt(
        correo: String,
        idCredencial: String,
        desafio: ChallengeResponse,
        firmaPreparada: Signature
    ) {

        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)

                    solicitudBiometricaEnCurso = false

                    Log.d(
                        "BIOMETRIA",
                        "Autenticación cancelada: $errString"
                    )
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    try {

                        val firma = result.cryptoObject
                            ?.signature
                            ?.sign()

                        if (firma == null) {
                            solicitudBiometricaEnCurso = false
                            mostrarMensaje(
                                "No se pudo completar la firma digital"
                            )
                            return
                        }

                        val firmaBase64 =
                            BiometricKeyManager.codificarFirma(firma)

                        enviarFirmaAlBackend(
                            correo,
                            idCredencial,
                            desafio,
                            firmaBase64
                        )

                    } catch (e: Exception) {
                        solicitudBiometricaEnCurso = false

                        mostrarMensaje(
                            "Error al firmar el desafío: ${e.message}"
                        )
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    Toast.makeText(
                        this@LoginActivity,
                        "Huella no reconocida",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso biométrico CosySmart")
            .setSubtitle("Usa tu huella digital para continuar")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(
            promptInfo,
            BiometricPrompt.CryptoObject(firmaPreparada)
        )
    }
    private fun enviarFirmaAlBackend(
        correo: String,
        idCredencial: String,
        desafio: ChallengeResponse,
        firma: String
    ) {

        val request = VerifyBiometricRequest(
            Correo = correo,
            IdCredencial = idCredencial,
            ChallengeId = desafio.ChallengeId,
            Nonce = desafio.Nonce,
            Firma = firma
        )

        RetrofitClient.instance.verificarBiometria(request)
            .enqueue(object : Callback<BiometriaLoginResponse> {

                override fun onResponse(
                    call: Call<BiometriaLoginResponse>,
                    response: Response<BiometriaLoginResponse>
                ) {

                    if (
                        !response.isSuccessful ||
                        response.body() == null
                    ) {

                        solicitudBiometricaEnCurso = false

                        val error = response.errorBody()?.string()

                        Log.e(
                            "BIOMETRIA",
                            "Error verificando firma ${response.code()}: $error"
                        )

                        mostrarMensaje(
                            "La verificación biométrica falló (${response.code()})"
                        )
                        return
                    }

                    val resultado = response.body()!!
                    val token = resultado.access_token

                    if (token.isNullOrBlank()) {
                        solicitudBiometricaEnCurso = false
                        mostrarMensaje(
                            "El servidor no devolvió un token de acceso"
                        )
                        return
                    }

                    val usuario = resultado.usuario

                    val guardado = preferencias.edit()
                        .putString("JWT_TOKEN", token)
                        .putString("LOGIN_EMAIL", correo)
                        .commit()

                    if (!guardado) {
                        solicitudBiometricaEnCurso = false
                        mostrarMensaje(
                            "No fue posible guardar la sesión"
                        )
                        return
                    }

                    val rol = usuario.Rol.trim()
                        .lowercase(Locale.ROOT)

                    Toast.makeText(
                        this@LoginActivity,
                        "Bienvenido de nuevo, ${usuario.Nombres}",
                        Toast.LENGTH_SHORT
                    ).show()

                    redirigirSegunRol(rol, usuario.Nombres)
                }

                override fun onFailure(
                    call: Call<BiometriaLoginResponse>,
                    t: Throwable
                ) {
                    solicitudBiometricaEnCurso = false

                    mostrarMensaje(
                        "Error de conexión al verificar la biometría: ${t.message}"
                    )
                }
            })
    }
    private fun redirigirSegunRol(
        rolUsuario: String,
        nombreUsuario: String
    ) {

        val intent = when {

            rolUsuario.contains("admin") -> {
                Intent(this, ActivityHomeAdmin::class.java).apply {
                    putExtra("USER_NAME", nombreUsuario)
                }
            }

            rolUsuario.contains("seller") ||
                    rolUsuario.contains("vendedor") -> {
                Intent(this, ActivityHome::class.java).apply {
                    putExtra("USER_NAME", nombreUsuario)
                    putExtra("USER_ROLE", "seller")
                }
            }

            else -> {
                Intent(this, ActivityHome::class.java).apply {
                    putExtra("USER_NAME", nombreUsuario)
                    putExtra("USER_ROLE", "buyer")
                }
            }
        }

        startActivity(intent)
        finish()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
}
