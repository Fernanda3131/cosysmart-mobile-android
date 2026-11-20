
package com.example.cosysmart.buyer

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.cosysmart.R
import com.example.cosysmart.home.ActivityHome
import com.example.cosysmart.network.ActualizarCantidadCarritoRequest
import com.example.cosysmart.network.CarritoResponse
import com.example.cosysmart.network.DetalleCarritoResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.payments.ActivityCheckout
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Activity implementada para el carrito de compras

class ActivityCart : AppCompatActivity() {

    private lateinit var layoutCartItems: LinearLayout
    private lateinit var tvCartEmpty: TextView
    private lateinit var tvCartSubtitle: TextView
    private lateinit var tvCartSubtotal: TextView
    private lateinit var tvCartShipping: TextView
    private lateinit var tvCartTotal: TextView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var btnVaciarCarrito: MaterialButton

    private var carritoActual: CarritoResponse? = null
    private var operacionEnCurso = false

    private val imageExecutor: ExecutorService =
        Executors.newFixedThreadPool(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        RetrofitClient.init(applicationContext)

        inicializarVistas()
        configurarEventos()
        cargarCarrito()
    }

    private fun inicializarVistas() {
        layoutCartItems = findViewById(R.id.layoutCartItems)
        tvCartEmpty = findViewById(R.id.tvCartEmpty)
        tvCartSubtitle = findViewById(R.id.tvCartSubtitle)
        tvCartSubtotal = findViewById(R.id.tvCartSubtotal)
        tvCartShipping = findViewById(R.id.tvCartShipping)
        tvCartTotal = findViewById(R.id.tvCartTotal)
        btnCheckout = findViewById(R.id.btnCheckout)
        btnVaciarCarrito = findViewById(R.id.btnVaciarCarrito)
    }

    private fun configurarEventos() {

        val btnCloseCart = findViewById<ImageButton>(R.id.btnCloseCart)

        btnCloseCart.setOnClickListener {
            val intent = Intent(this, ActivityHome::class.java).apply {
                putExtra("USER_ROLE", "buyer")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
            finish()
        }

        btnCheckout.setOnClickListener {

            val carrito = carritoActual

            if (carrito == null || carrito.detalles.isEmpty()) {
                mostrarMensaje("Tu carrito está vacío")
                return@setOnClickListener
            }

            val intent = Intent(this, ActivityCheckout::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
        }

        // EVENTO PARA VACIAR TODO EL CARRITO

        btnVaciarCarrito.setOnClickListener {
            confirmarVaciarCarrito()
        }

        val bottomNav = findViewById<BottomNavigationView>(
            R.id.bottomNavigationViewBuyer
        )

        BottomNavigationHelper.setupBuyerNav(
            this,
            bottomNav,
            R.id.nav_buyer_carrito
        )
    }

    private fun cargarCarrito() {

        tvCartSubtitle.text = "Cargando carrito..."

        RetrofitClient.instance.obtenerCarrito()
            .enqueue(object : Callback<CarritoResponse> {

                override fun onResponse(
                    call: Call<CarritoResponse>,
                    response: Response<CarritoResponse>
                ) {
                    if (response.isSuccessful) {

                        val carrito = response.body()

                        if (carrito != null) {
                            carritoActual = carrito
                            mostrarCarrito(carrito)
                        } else {
                            mostrarError(
                                "El servidor no devolvió información del carrito"
                            )
                        }

                    } else {
                        mostrarErrorServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(
                    call: Call<CarritoResponse>,
                    t: Throwable
                ) {
                    mostrarError(
                        "No se pudo conectar con el servidor: ${t.message}"
                    )
                }
            })
    }

    private fun mostrarCarrito(carrito: CarritoResponse) {

        layoutCartItems.removeAllViews()

        val detalles = carrito.detalles

        val cantidadArticulos = detalles.sumOf {
            it.cantidad
        }

        tvCartSubtitle.text = if (cantidadArticulos == 1) {
            "1 artículo seleccionado"
        } else {
            "$cantidadArticulos artículos seleccionados"
        }

        if (detalles.isEmpty()) {

            layoutCartItems.visibility = View.GONE
            tvCartEmpty.visibility = View.VISIBLE
            btnVaciarCarrito.visibility = View.GONE

            tvCartSubtotal.text = formatoMoneda(0.0)
            tvCartShipping.text = "Se calcula en el pago"
            tvCartTotal.text = formatoMoneda(0.0)

            btnCheckout.isEnabled = false

        } else {

            layoutCartItems.visibility = View.VISIBLE
            tvCartEmpty.visibility = View.GONE
            btnVaciarCarrito.visibility = View.VISIBLE

            detalles.forEach { detalle ->
                agregarTarjetaProducto(detalle)
            }

            tvCartSubtotal.text = formatoMoneda(carrito.total)
            tvCartShipping.text = "Se calcula en el pago"
            tvCartTotal.text = formatoMoneda(carrito.total)

            btnCheckout.isEnabled = true
        }

        btnVaciarCarrito.isEnabled = !operacionEnCurso
    }

    private fun confirmarVaciarCarrito() {

        if (operacionEnCurso) return

        val carrito = carritoActual

        if (carrito == null || carrito.detalles.isEmpty()) {
            mostrarMensaje("El carrito ya está vacío")
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Vaciar carrito")
            .setMessage(
                "¿Estás segura de que deseas eliminar todos los productos del carrito?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Vaciar") { _, _ ->
                vaciarCarrito()
            }
            .show()
    }

    private fun vaciarCarrito() {

        if (operacionEnCurso) return

        operacionEnCurso = true
        btnVaciarCarrito.isEnabled = false
        btnCheckout.isEnabled = false

        RetrofitClient.instance.vaciarCarrito()
            .enqueue(object : Callback<CarritoResponse> {

                override fun onResponse(
                    call: Call<CarritoResponse>,
                    response: Response<CarritoResponse>
                ) {

                    operacionEnCurso = false

                    if (response.isSuccessful) {

                        val carrito = response.body()

                        if (carrito != null) {

                            carritoActual = carrito
                            mostrarCarrito(carrito)

                            mostrarMensaje(
                                "Carrito vaciado correctamente"
                            )

                        } else {
                            btnVaciarCarrito.isEnabled = true
                            mostrarMensaje(
                                "No se recibió la respuesta del servidor"
                            )
                        }

                    } else {

                        btnVaciarCarrito.isEnabled = true
                        mostrarErrorServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )

                        btnCheckout.isEnabled =
                            carritoActual?.detalles?.isNotEmpty() == true
                    }
                }

                override fun onFailure(
                    call: Call<CarritoResponse>,
                    t: Throwable
                ) {

                    operacionEnCurso = false

                    btnVaciarCarrito.isEnabled = true
                    btnCheckout.isEnabled =
                        carritoActual?.detalles?.isNotEmpty() == true

                    mostrarMensaje(
                        "No se pudo vaciar el carrito. Verifica tu conexión."
                    )
                }
            })
    }

    private fun agregarTarjetaProducto(
        detalle: DetalleCarritoResponse
    ) {

        val producto = detalle.producto

        val card = CardView(this).apply {
            radius = dp(16).toFloat()
            cardElevation = dp(2).toFloat()
            setCardBackgroundColor(Color.WHITE)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.bottomMargin = dp(12)
            layoutParams = params
        }

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }


        val imagen = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dp(75),
                dp(75)
            )

            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.rgb(239, 236, 230))
            setImageResource(android.R.drawable.ic_menu_gallery)
        }

        contenido.addView(imagen)

        val informacion = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = dp(12)
            }
        }

        val tvNombre = TextView(this).apply {
            text = producto.nombre
            textSize = 14f
            setTextColor(Color.rgb(44, 34, 30))
            setTypeface(null, Typeface.BOLD)
        }

        val tvPrecio = TextView(this).apply {
            text = formatoMoneda(producto.precio)
            textSize = 13f
            setTextColor(Color.rgb(44, 34, 30))
            setTypeface(null, Typeface.BOLD)
        }

        val tvStock = TextView(this).apply {
            text = "Disponibles: ${producto.stock}"
            textSize = 11f
            setTextColor(Color.rgb(140, 125, 117))
        }

        val tvSubtotal = TextView(this).apply {
            text = "Subtotal: ${formatoMoneda(detalle.subtotal)}"
            textSize = 12f
            setTextColor(Color.rgb(192, 92, 56))
            setTypeface(null, Typeface.BOLD)
        }

        informacion.addView(tvNombre)
        informacion.addView(tvPrecio)
        informacion.addView(tvStock)
        informacion.addView(tvSubtotal)

        val controles = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, 0)
        }

        val btnRestar = crearBotonCantidad("-")

        val tvCantidad = TextView(this).apply {
            text = detalle.cantidad.toString()
            textSize = 14f
            setTextColor(Color.rgb(44, 34, 30))
            gravity = Gravity.CENTER
            setPadding(dp(12), 0, dp(12), 0)
        }

        val btnSumar = crearBotonCantidad("+")

        btnRestar.setOnClickListener {

            if (detalle.cantidad > 1) {
                actualizarCantidad(
                    detalle.idDetalleCarrito,
                    detalle.cantidad - 1
                )
            }
        }

        btnSumar.setOnClickListener {

            if (detalle.cantidad < producto.stock) {
                actualizarCantidad(
                    detalle.idDetalleCarrito,
                    detalle.cantidad + 1
                )
            } else {
                mostrarMensaje("No hay más unidades disponibles")
            }
        }

        controles.addView(btnRestar)
        controles.addView(tvCantidad)
        controles.addView(btnSumar)

        informacion.addView(controles)
        val btnEliminar = TextView(this).apply {
            text = "Eliminar"
            textSize = 11f
            setTextColor(Color.rgb(192, 92, 56))
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(8), dp(4), dp(8))
        }

        btnEliminar.setOnClickListener {
            eliminarProducto(detalle.idDetalleCarrito)
        }

        informacion.addView(btnEliminar)

        contenido.addView(informacion)
        card.addView(contenido)

        layoutCartItems.addView(card)

        val rutaImagen = producto.imagenes.firstOrNull()

        if (!rutaImagen.isNullOrBlank()) {
            cargarImagen(rutaImagen, imagen)
        }
    }

    private fun crearBotonCantidad(texto: String): TextView {

        return TextView(this).apply {
            this.text = texto
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(74, 63, 56))
            gravity = Gravity.CENTER

            layoutParams = LinearLayout.LayoutParams(
                dp(32),
                dp(32)
            )

            background = GradientDrawable().apply {
                setColor(Color.rgb(242, 236, 228))
                cornerRadius = dp(8).toFloat()
            }
        }
    }
    private fun actualizarCantidad(
        idDetalle: Int,
        nuevaCantidad: Int
    ) {

        if (operacionEnCurso) return

        operacionEnCurso = true

        val request = ActualizarCantidadCarritoRequest(
            cantidad = nuevaCantidad
        )

        RetrofitClient.instance.actualizarCantidadCarrito(
            idDetalle,
            request
        ).enqueue(object : Callback<CarritoResponse> {

            override fun onResponse(
                call: Call<CarritoResponse>,
                response: Response<CarritoResponse>
            ) {

                operacionEnCurso = false

                if (response.isSuccessful) {

                    val carrito = response.body()

                    if (carrito != null) {
                        carritoActual = carrito
                        mostrarCarrito(carrito)
                    } else {
                        mostrarMensaje(
                            "No se recibió el carrito actualizado"
                        )
                    }

                } else {
                    mostrarErrorServidor(
                        response.code(),
                        response.errorBody()?.string()
                    )
                }
            }

            override fun onFailure(
                call: Call<CarritoResponse>,
                t: Throwable
            ) {
                operacionEnCurso = false
                btnVaciarCarrito.isEnabled = true
                mostrarMensaje("Error al actualizar la cantidad")
            }
        })
    }

    private fun eliminarProducto(idDetalle: Int) {

        if (operacionEnCurso) return

        operacionEnCurso = true

        RetrofitClient.instance.eliminarProductoCarrito(
            idDetalle
        ).enqueue(object : Callback<CarritoResponse> {

            override fun onResponse(
                call: Call<CarritoResponse>,
                response: Response<CarritoResponse>
            ) {

                operacionEnCurso = false

                if (response.isSuccessful) {

                    val carrito = response.body()

                    if (carrito != null) {
                        carritoActual = carrito
                        mostrarCarrito(carrito)
                        mostrarMensaje("Producto eliminado")
                    } else {
                        mostrarMensaje(
                            "No se recibió el carrito actualizado"
                        )
                    }

                } else {
                    mostrarErrorServidor(
                        response.code(),
                        response.errorBody()?.string()
                    )
                }
            }

            override fun onFailure(
                call: Call<CarritoResponse>,
                t: Throwable
            ) {
                operacionEnCurso = false
                btnVaciarCarrito.isEnabled = true
                mostrarMensaje("Error al eliminar el producto")
            }
        })
    }

    private fun cargarImagen(
        ruta: String,
        imageView: ImageView
    ) {

        imageExecutor.execute {

            var connection: HttpURLConnection? = null

            try {
                val url = URL(RetrofitClient.construirUrl(ruta))

                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doInput = true
                connection.connect()

                val bitmap = connection.inputStream.use {
                    BitmapFactory.decodeStream(it)
                }

                if (bitmap != null && !isFinishing && !isDestroyed) {
                    runOnUiThread {
                        imageView.setImageBitmap(bitmap)
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e(
                    "ActivityCart",
                    "Error cargando imagen: ${e.message}"
                )
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun formatoMoneda(valor: Double): String {

        val formato = NumberFormat.getCurrencyInstance(
            Locale("es", "CO")
        )

        formato.maximumFractionDigits = 0
        formato.minimumFractionDigits = 0

        return formato.format(valor)
    }

    private fun mostrarErrorServidor(
        codigo: Int,
        detalle: String?
    ) {

        android.util.Log.e(
            "ActivityCart",
            "Error HTTP $codigo: $detalle"
        )

        when (codigo) {
            401 -> mostrarError(
                "Tu sesión ha expirado. Inicia sesión nuevamente."
            )

            403 -> mostrarError(
                "No tienes permiso para acceder al carrito."
            )

            404 -> mostrarError(
                "No se encontró el recurso solicitado."
            )

            422 -> mostrarError(
                "Los datos enviados no son válidos."
            )

            else -> mostrarError(
                "Error del servidor. Código: $codigo"
            )
        }
    }

    private fun mostrarError(mensaje: String) {
        tvCartSubtitle.text = "No se pudo cargar el carrito"
        mostrarMensaje(mensaje)
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }

    // CONVERTIR DP

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        imageExecutor.shutdownNow()
    }
}
