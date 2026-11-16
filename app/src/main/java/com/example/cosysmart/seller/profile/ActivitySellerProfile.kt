
package com.example.cosysmart.seller.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cosysmart.R
import com.example.cosysmart.network.FotoPerfilResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.UsuarioActualResponse
import com.example.cosysmart.security.SesionManager
import com.example.cosysmart.seller.management.ActivitySellerSales
import com.example.cosysmart.seller.management.ActivitySellerOrders
import com.example.cosysmart.seller.management.ActivitySellerProducts
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

// Activity para ver el perfil de un vendedor
class ActivitySellerProfile : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var imgAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_profile)
        tvNombre = findViewById(R.id.tvNombreSeller)
        tvCorreo = findViewById(R.id.tvCorreoSeller)
        imgAvatar = findViewById(R.id.imgAvatarSeller)

        val selectorImagen = SelectorImagenPerfil(
            activity = this,
            imageView = imgAvatar
        ) { uri ->
            subirFotoPerfil(uri)
        }

        imgAvatar.setOnClickListener {
            selectorImagen.mostrarOpciones()
        }

        cargarDatosVendedor()
        val tvVerReporte = findViewById<TextView>(R.id.tvVerReporte)

        tvVerReporte.setOnClickListener {
            startActivity(
                Intent(this, ActivitySellerSales::class.java)
            )
        }

        val tvVerTodoPedidos =
            findViewById<TextView>(R.id.tvVerTodoPedidos)

        tvVerTodoPedidos.setOnClickListener {
            startActivity(
                Intent(this, ActivitySellerOrders::class.java)
            )
        }

        val btnGestionProductos =
            findViewById<LinearLayout>(R.id.btnGestionProductos)

        btnGestionProductos.setOnClickListener {
            startActivity(
                Intent(this, ActivitySellerProducts::class.java)
            )
        }

        val btnGestionPedidos =
            findViewById<LinearLayout>(R.id.btnGestionPedidos)

        btnGestionPedidos.setOnClickListener {
            startActivity(
                Intent(this, ActivitySellerOrders::class.java)
            )
        }

        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            Toast.makeText(
                this,
                "Cerrando sesión...",
                Toast.LENGTH_SHORT
            ).show()

            SesionManager.cerrarSesion(this)
        }

        val bottomNav = findViewById<BottomNavigationView>(
            R.id.bottomNavigationViewSeller
        )

        BottomNavigationHelper.setupSellerNav(
            this,
            bottomNav,
            R.id.nav_seller_panel
        )
    }

    private fun cargarDatosVendedor() {

        tvNombre.text = "Cargando nombre..."
        tvCorreo.text = "Cargando correo..."

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

                            if (!usuario.Rol.equals(
                                    "Vendedor",
                                    ignoreCase = true
                                )
                            ) {
                                mostrarMensaje(
                                    "La cuenta no corresponde a un vendedor."
                                )

                                SesionManager.cerrarSesion(
                                    this@ActivitySellerProfile
                                )
                                return
                            }

                            tvNombre.text =
                                "${usuario.Nombres} ${usuario.Apellidos}"

                            tvCorreo.text = usuario.Correo

                            val ruta = usuario.FotoPerfil

                            if (!ruta.isNullOrBlank()) {
                                cargarFotoDesdeServidor(ruta)
                            } else {
                                Glide.with(this@ActivitySellerProfile)
                                    .clear(imgAvatar)

                                imgAvatar.setImageResource(
                                    R.drawable.ic_person
                                )
                            }

                        } else {
                            mostrarMensaje(
                                "No se recibieron datos del perfil."
                            )
                        }

                    } else if (response.code() == 401) {

                        mostrarMensaje(
                            "Tu sesión ha expirado. Inicia sesión nuevamente."
                        )

                        SesionManager.cerrarSesion(
                            this@ActivitySellerProfile
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
                                this@ActivitySellerProfile
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
