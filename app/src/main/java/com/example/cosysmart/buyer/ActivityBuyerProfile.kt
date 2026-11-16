
package com.example.cosysmart.buyer

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cosysmart.R
import com.example.cosysmart.network.FotoPerfilResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.UsuarioActualResponse
import com.example.cosysmart.security.SesionManager
import com.example.cosysmart.utils.BottomNavigationHelper
import com.example.cosysmart.utils.SelectorImagenPerfil
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityBuyerProfile : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var imgAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_profile)

        tvNombre = findViewById(R.id.tvNombreBuyer)
        tvCorreo = findViewById(R.id.tvCorreoBuyer)
        imgAvatar = findViewById(R.id.imgAvatarBuyer)

        cargarDatosComprador()

        val selectorImagen = SelectorImagenPerfil(
            activity = this,
            imageView = imgAvatar
        ) { uri ->
            subirFotoPerfil(uri)
        }

        imgAvatar.setOnClickListener {
            selectorImagen.mostrarOpciones()
        }

        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            SesionManager.cerrarSesion(this)
        }

        val bottomNav = findViewById<BottomNavigationView>(
            R.id.bottomNavigationViewBuyer
        )

        BottomNavigationHelper.setupBuyerNav(
            this,
            bottomNav,
            R.id.nav_buyer_perfil
        )
    }

    private fun cargarDatosComprador() {

        tvNombre.text = "Cargando..."
        tvCorreo.text = "Cargando..."

        RetrofitClient.instance
            .obtenerUsuarioActual()
            .enqueue(object : Callback<UsuarioActualResponse> {

                override fun onResponse(
                    call: Call<UsuarioActualResponse>,
                    response: Response<UsuarioActualResponse>
                ) {

                    if (response.isSuccessful) {

                        val usuario = response.body()

                        if (usuario != null) {

                            tvNombre.text =
                                "${usuario.Nombres} ${usuario.Apellidos}"

                            tvCorreo.text = usuario.Correo

                            val ruta = usuario.FotoPerfil

                            if (!ruta.isNullOrBlank()) {
                                cargarFotoDesdeServidor(ruta)
                            } else {
                                Glide.with(this@ActivityBuyerProfile)
                                    .clear(imgAvatar)

                                imgAvatar.setImageResource(
                                    R.drawable.ic_person
                                )
                            }

                        } else {
                            mostrarMensaje(
                                "No se recibieron los datos del usuario."
                            )
                        }

                    } else if (response.code() == 401) {

                        mostrarMensaje(
                            "Tu sesión ha expirado. Inicia sesión nuevamente."
                        )

                        SesionManager.cerrarSesion(
                            this@ActivityBuyerProfile
                        )

                    } else {

                        mostrarMensaje(
                            "No fue posible cargar el perfil. Código: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<UsuarioActualResponse>,
                    t: Throwable
                ) {
                    mostrarMensaje(
                        "Error de conexión con el servidor."
                    )
                }
            })
    }

    private fun cargarFotoDesdeServidor(ruta: String) {

        // Construye la URL sin acceder directamente a BASE_URL.
        val urlFoto = RetrofitClient.construirUrl(ruta)

        Glide.with(this)
            .load(urlFoto)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .centerCrop()
            .into(imgAvatar)
    }

    private fun subirFotoPerfil(uri: Uri) {

        try {

            val bytes = contentResolver
                .openInputStream(uri)
                ?.use { it.readBytes() }

            if (bytes == null || bytes.isEmpty()) {
                mostrarMensaje(
                    "No fue posible leer la fotografía."
                )
                return
            }

            val mimeType = contentResolver.getType(uri)
                ?: "image/jpeg"

            if (mimeType !in listOf(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
                )
            ) {
                mostrarMensaje(
                    "Selecciona una imagen JPG, PNG o WebP."
                )
                return
            }

            if (bytes.size > 5 * 1024 * 1024) {
                mostrarMensaje(
                    "La fotografía no puede superar los 5 MB."
                )
                return
            }

            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val requestBody = bytes.toRequestBody(
                mimeType.toMediaTypeOrNull()
            )

            val archivo = MultipartBody.Part.createFormData(
                "foto",
                "perfil.$extension",
                requestBody
            )

            RetrofitClient.instance
                .actualizarFotoPerfil(archivo)
                .enqueue(object : Callback<FotoPerfilResponse> {

                    override fun onResponse(
                        call: Call<FotoPerfilResponse>,
                        response: Response<FotoPerfilResponse>
                    ) {

                        if (response.isSuccessful) {

                            val resultado = response.body()

                            if (resultado != null &&
                                resultado.FotoPerfil.isNotBlank()
                            ) {

                                cargarFotoDesdeServidor(
                                    resultado.FotoPerfil
                                )

                                mostrarMensaje(
                                    "Fotografía actualizada correctamente."
                                )

                            } else {
                                mostrarMensaje(
                                    "El servidor no devolvió la ruta de la fotografía."
                                )
                            }

                        } else if (response.code() == 401) {

                            mostrarMensaje(
                                "Tu sesión ha expirado."
                            )

                            SesionManager.cerrarSesion(
                                this@ActivityBuyerProfile
                            )

                        } else {

                            mostrarMensaje(
                                "No se pudo actualizar la fotografía. Código: ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<FotoPerfilResponse>,
                        t: Throwable
                    ) {
                        mostrarMensaje(
                            "Error al conectar con el servidor."
                        )
                    }
                })

        } catch (e: Exception) {

            e.printStackTrace()

            mostrarMensaje(
                "Ocurrió un error al procesar la fotografía."
            )
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
