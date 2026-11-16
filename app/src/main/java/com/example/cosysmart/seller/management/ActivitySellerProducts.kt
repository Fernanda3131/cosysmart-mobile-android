package com.example.cosysmart.seller.management

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.cosysmart.R
import com.example.cosysmart.home.ActivityHome
import com.example.cosysmart.network.EstadoProductoRequest
import com.example.cosysmart.network.ProductoResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Activity para ver productos de un vendedor
class ActivitySellerProducts : AppCompatActivity() {

    private lateinit var etSearchProduct: EditText
    private lateinit var tvSellerProductCount: TextView
    private lateinit var tvSellerProductsStatus: TextView
    private lateinit var layoutSellerProducts: LinearLayout

    private lateinit var imageExecutor: ExecutorService

    private var listaProductos = listOf<ProductoResponse>()
    private var textoBusqueda = ""

    private val colorPrincipal = Color.rgb(192, 92, 56)
    private val colorTexto = Color.rgb(44, 34, 30)
    private val colorSecundario = Color.rgb(122, 111, 104)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_products)

        RetrofitClient.init(applicationContext)

        imageExecutor = Executors.newFixedThreadPool(4)

        inicializarVistas()
        configurarNavegacion()
        configurarAcciones()
        configurarBuscador()
    }

    override fun onResume() {
        super.onResume()
        cargarProductos()
    }

    private fun inicializarVistas() {
        etSearchProduct = findViewById(R.id.etSearchProduct)
        tvSellerProductCount = findViewById(R.id.tvSellerProductCount)
        tvSellerProductsStatus = findViewById(R.id.tvSellerProductsStatus)
        layoutSellerProducts = findViewById(R.id.layoutSellerProducts)
    }

    private fun configurarNavegacion() {
        val bottomNav = findViewById<BottomNavigationView>(
            R.id.bottomNavigationViewSeller
        )

        BottomNavigationHelper.setupSellerNav(
            this,
            bottomNav,
            R.id.nav_seller_productos
        )
    }

    private fun configurarAcciones() {

        val btnBackToHome = findViewById<ImageButton>(
            R.id.btnBackToHome
        )

        btnBackToHome.setOnClickListener {
            val intent = Intent(this, ActivityHome::class.java).apply {
                putExtra("USER_ROLE", "seller")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
            finish()
        }

        val btnSalesStats = findViewById<ImageButton>(
            R.id.btnSalesStats
        )

        btnSalesStats.setOnClickListener {
            val intent = Intent(
                this,
                ActivitySellerSales::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
        }

        val fabAddProduct = findViewById<FloatingActionButton>(
            R.id.fabAddProduct
        )

        fabAddProduct.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ActivitySellerCreateProduct::class.java
                )
            )
        }
    }

    private fun configurarBuscador() {
        etSearchProduct.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                textoBusqueda = s?.toString()?.trim() ?: ""
                mostrarProductosFiltrados()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun cargarProductos() {
        mostrarEstado("Cargando tus productos...")

        RetrofitClient.instance.listarProductos("mis-productos")
            .enqueue(object : Callback<List<ProductoResponse>> {

                override fun onResponse(
                    call: Call<List<ProductoResponse>>,
                    response: Response<List<ProductoResponse>>
                ) {
                    if (!response.isSuccessful) {
                        Log.e(
                            "ActivitySellerProducts",
                            "Error HTTP: ${response.code()}\n${response.errorBody()?.string()}"
                        )

                        mostrarEstado(
                            "No fue posible cargar tus productos."
                        )
                        return
                    }

                    listaProductos = response.body() ?: emptyList()

                    if (listaProductos.isEmpty()) {
                        layoutSellerProducts.removeAllViews()
                        tvSellerProductCount.text = "0 productos"

                        mostrarEstado(
                            "Todavía no has registrado productos."
                        )
                        return
                    }

                    mostrarProductosFiltrados()
                }

                override fun onFailure(
                    call: Call<List<ProductoResponse>>,
                    t: Throwable
                ) {
                    Log.e(
                        "ActivitySellerProducts",
                        "Error consultando mis productos",
                        t
                    )

                    mostrarEstado(
                        "Error de conexión. Comprueba tu conexión con el servidor."
                    )
                }
            })
    }

    private fun cambiarEstadoProducto(
        idProducto: Int,
        nuevoEstado: Int
    ) {
        val request = EstadoProductoRequest(nuevoEstado)

        RetrofitClient.instance.cambiarEstadoProducto(
            idProducto,
            request
        ).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {

                    val mensaje = if (nuevoEstado == 0) {
                        "Producto inactivado correctamente"
                    } else {
                        "Producto reactivado correctamente"
                    }

                    Toast.makeText(
                        this@ActivitySellerProducts,
                        mensaje,
                        Toast.LENGTH_SHORT
                    ).show()

                    cargarProductos()

                } else {

                    val error = response.errorBody()?.string()

                    Toast.makeText(
                        this@ActivitySellerProducts,
                        "Error ${response.code()}: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {
                Toast.makeText(
                    this@ActivitySellerProducts,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun mostrarProductosFiltrados() {

        val productosFiltrados = listaProductos.filter { producto ->

            val nombreCoincide = producto.nombre.contains(
                textoBusqueda,
                ignoreCase = true
            )

            val idCoincide = producto.idProducto
                .toString()
                .contains(textoBusqueda, ignoreCase = true)

            nombreCoincide || idCoincide
        }

        tvSellerProductCount.text =
            if (productosFiltrados.size == 1) {
                "1 producto"
            } else {
                "${productosFiltrados.size} productos"
            }

        if (productosFiltrados.isEmpty()) {
            layoutSellerProducts.removeAllViews()

            mostrarEstado(
                if (listaProductos.isEmpty()) {
                    "Todavía no has registrado productos."
                } else {
                    "No encontramos productos con esa búsqueda."
                }
            )
            return
        }

        tvSellerProductsStatus.visibility = View.GONE
        dibujarProductos(productosFiltrados)
    }

    private fun dibujarProductos(
        productos: List<ProductoResponse>
    ) {
        layoutSellerProducts.removeAllViews()

        productos.forEach { producto ->
            layoutSellerProducts.addView(
                crearTarjetaProducto(producto)
            )
        }
    }

    private fun crearTarjetaProducto(
        producto: ProductoResponse
    ): CardView {

        val tarjeta = CardView(this).apply {
            radius = dp(12).toFloat()
            cardElevation = dp(3).toFloat()
            setCardBackgroundColor(Color.WHITE)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        }

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
            )
        }

        val filaSuperior = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val imagen = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dp(76),
                dp(76)
            )

            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.rgb(239, 236, 230))
            setImageResource(android.R.drawable.ic_menu_gallery)
        }

        filaSuperior.addView(imagen)

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

        val nombre = TextView(this).apply {
            text = producto.nombre
            textSize = 15f
            setTextColor(colorTexto)
            setTypeface(null, android.graphics.Typeface.BOLD)
            maxLines = 2
        }

        val precio = TextView(this).apply {
            text = formatearPrecio(producto.precio)
            textSize = 14f
            setTextColor(colorPrincipal)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(4), 0, 0)
        }

        val categoria = TextView(this).apply {
            text = producto.nombreCategoria ?: "Sin categoría"
            textSize = 11f
            setTextColor(colorSecundario)
            setPadding(0, dp(3), 0, 0)
        }

        informacion.addView(nombre)
        informacion.addView(precio)
        informacion.addView(categoria)

        filaSuperior.addView(informacion)

        val estado = TextView(this).apply {
            text = if (producto.estado == 1) {
                "Publicado"
            } else {
                "Inactivo"
            }

            textSize = 11f
            setTypeface(null, android.graphics.Typeface.BOLD)

            setTextColor(
                if (producto.estado == 1) {
                    Color.rgb(46, 125, 50)
                } else {
                    colorPrincipal
                }
            )

            setPadding(
                dp(8),
                dp(4),
                dp(8),
                dp(4)
            )

            background = crearFondo(
                if (producto.estado == 1) {
                    Color.rgb(232, 245, 233)
                } else {
                    Color.rgb(252, 235, 229)
                },
                8
            )
        }

        filaSuperior.addView(estado)
        contenido.addView(filaSuperior)

        val separador = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(10)
            }

            setBackgroundColor(Color.rgb(240, 234, 225))
        }

        contenido.addView(separador)

        val detalles = TextView(this).apply {
            text = "ID: ${producto.idProducto}   |   Stock: ${producto.stock}"
            textSize = 12f
            setTextColor(colorSecundario)
        }

        contenido.addView(detalles)

        val filaBotones = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        }

        val btnEditar = MaterialButton(
            this,
            null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {

            text = "Editar ficha"
            textSize = 12f
            isAllCaps = false
            isEnabled = true

            setTextColor(Color.rgb(74, 63, 56))

            strokeColor = android.content.res.ColorStateList.valueOf(
                Color.rgb(211, 203, 194)
            )

            cornerRadius = dp(8)

            layoutParams = LinearLayout.LayoutParams(
                0,
                dp(40),
                1f
            ).apply {
                marginEnd = dp(4)
            }

            setOnClickListener {

                val intent = Intent(
                    this@ActivitySellerProducts,
                    ActivitySellerEditProduct::class.java
                ).apply {
                    putExtra(
                        "ID_PRODUCTO",
                        producto.idProducto
                    )
                }

                startActivity(intent)
            }
        }

        val btnEstado = MaterialButton(
            this,
            null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {

            text = if (producto.estado == 1) {
                "Desactivar"
            } else {
                "Activar"
            }

            textSize = 12f
            isAllCaps = false
            isEnabled = true

            setTextColor(colorPrincipal)

            strokeColor = android.content.res.ColorStateList.valueOf(
                Color.rgb(232, 197, 188)
            )

            cornerRadius = dp(8)

            layoutParams = LinearLayout.LayoutParams(
                0,
                dp(40),
                1f
            ).apply {
                marginStart = dp(4)
            }

            setOnClickListener {
                val nuevoEstado = if (producto.estado == 1) 0 else 1

                val accion = if (nuevoEstado == 0) {
                    "inactivar"
                } else {
                    "reactivar"
                }

                AlertDialog.Builder(this@ActivitySellerProducts)
                    .setTitle("Confirmar acción")
                    .setMessage("¿Deseas $accion este producto?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Confirmar") { _, _ ->
                        cambiarEstadoProducto(
                            producto.idProducto,
                            nuevoEstado
                        )
                    }
                    .show()
            }
        }

        filaBotones.addView(btnEditar)
        filaBotones.addView(btnEstado)

        contenido.addView(filaBotones)

        tarjeta.addView(contenido)

        if (producto.imagenes.isNotEmpty()) {
            cargarImagen(
                producto.imagenes.first(),
                imagen
            )
        }

        return tarjeta
    }

    private fun cargarImagen(
        ruta: String,
        imageView: ImageView
    ) {
        imageExecutor.execute {

            var bitmap: Bitmap? = null
            var connection: HttpURLConnection? = null

            try {
                val urlImagen = RetrofitClient.construirUrl(ruta)

                connection = URL(urlImagen)
                    .openConnection() as HttpURLConnection

                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode ==
                    HttpURLConnection.HTTP_OK
                ) {
                    bitmap = connection.inputStream.use {
                        BitmapFactory.decodeStream(it)
                    }
                }

            } catch (e: Exception) {
                Log.e(
                    "ActivitySellerProducts",
                    "Error cargando imagen: $ruta",
                    e
                )
            } finally {
                connection?.disconnect()
            }

            runOnUiThread {
                if (!isFinishing &&
                    !isDestroyed &&
                    bitmap != null
                ) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun mostrarEstado(mensaje: String) {
        tvSellerProductsStatus.text = mensaje
        tvSellerProductsStatus.visibility = View.VISIBLE
    }

    private fun formatearPrecio(precio: Double): String {
        val formato = NumberFormat.getCurrencyInstance(
            Locale("es", "CO")
        )

        formato.maximumFractionDigits = 0
        formato.minimumFractionDigits = 0

        return formato.format(precio)
    }

    private fun crearFondo(
        color: Int,
        radio: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radio).toFloat()
        }
    }

    private fun dp(valor: Int): Int {
        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }

    override fun onDestroy() {
        imageExecutor.shutdownNow()
        super.onDestroy()
    }
}