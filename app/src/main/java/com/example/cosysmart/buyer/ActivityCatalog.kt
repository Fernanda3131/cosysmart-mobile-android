
package com.example.cosysmart.buyer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.cosysmart.R
import com.example.cosysmart.network.ProductoResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.utils.BottomNavigationHelper
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


// Activity para ver el catálogo de productos
class ActivityCatalog : AppCompatActivity() {

    private lateinit var etSearchCatalog: EditText
    private lateinit var layoutCatalogCategories: LinearLayout
    private lateinit var layoutCatalogProducts: LinearLayout
    private lateinit var tvCatalogProductCount: TextView
    private lateinit var tvCatalogProductsStatus: TextView

    private lateinit var imageExecutor: ExecutorService

    private var listaProductos = listOf<ProductoResponse>()
    private var categoriaSeleccionada: Int? = null
    private var textoBusqueda = ""

    private val colorPrincipal = Color.rgb(192, 92, 56)
    private val colorTexto = Color.rgb(44, 34, 30)
    private val colorSecundario = Color.rgb(122, 111, 104)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        RetrofitClient.init(applicationContext)

        imageExecutor = Executors.newFixedThreadPool(4)

        inicializarVistas()
        configurarNavegacion()
        configurarBuscador()
        configurarFiltros()

        cargarProductos()
    }

    private fun inicializarVistas() {
        etSearchCatalog = findViewById(R.id.etSearchCatalog)
        layoutCatalogCategories = findViewById(R.id.layoutCatalogCategories)
        layoutCatalogProducts = findViewById(R.id.layoutCatalogProducts)
        tvCatalogProductCount = findViewById(R.id.tvCatalogProductCount)
        tvCatalogProductsStatus = findViewById(R.id.tvCatalogProductsStatus)
    }

    private fun configurarNavegacion() {

        val btnTopCart = findViewById<RelativeLayout>(R.id.btnTopCart)

        btnTopCart.setOnClickListener {
            val intent = Intent(this, ActivityCart::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
        }

        val bottomNav = findViewById<BottomNavigationView>(
            R.id.bottomNavigationViewBuyer
        )

        BottomNavigationHelper.setupBuyerNav(
            this,
            bottomNav,
            R.id.nav_buyer_catalogo
        )
    }

    private fun configurarBuscador() {

        etSearchCatalog.addTextChangedListener(object : TextWatcher {

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

    private fun configurarFiltros() {

        val tvClearFilters = findViewById<TextView>(R.id.tvClearFilters)

        tvClearFilters.setOnClickListener {
            categoriaSeleccionada = null
            etSearchCatalog.setText("")
            textoBusqueda = ""

            mostrarCategorias()
            mostrarProductosFiltrados()
        }
    }

    private fun cargarProductos() {

        mostrarEstado("Cargando productos...")

        RetrofitClient.instance.listarProductos("catalogo")
            .enqueue(object : Callback<List<ProductoResponse>> {

                override fun onResponse(
                    call: Call<List<ProductoResponse>>,
                    response: Response<List<ProductoResponse>>
                ) {

                    if (!response.isSuccessful) {
                        mostrarEstado(
                            "No fue posible cargar el catálogo."
                        )
                        return
                    }

                    listaProductos = response.body() ?: emptyList()

                    if (listaProductos.isEmpty()) {
                        layoutCatalogCategories.removeAllViews()
                        layoutCatalogProducts.removeAllViews()

                        tvCatalogProductCount.text = "0 productos"

                        mostrarEstado(
                            "Todavía no hay productos disponibles."
                        )
                        return
                    }

                    mostrarCategorias()
                    mostrarProductosFiltrados()
                }

                override fun onFailure(
                    call: Call<List<ProductoResponse>>,
                    t: Throwable
                ) {
                    mostrarEstado(
                        "Error de conexión. Comprueba tu conexión con el servidor."
                    )

                    android.util.Log.e(
                        "ActivityCatalog",
                        "Error consultando catálogo",
                        t
                    )
                }
            })
    }

    private fun mostrarCategorias() {

        layoutCatalogCategories.removeAllViews()

        agregarFiltroCategoria(
            idCategoria = null,
            nombre = "Todas"
        )

        val categorias = listaProductos
            .filter {
                it.nombreCategoria?.isNotBlank() == true
            }
            .distinctBy {
                it.idCategoria
            }
            .sortedBy {
                it.nombreCategoria?.lowercase(Locale.getDefault())
            }

        categorias.forEach { producto ->
            agregarFiltroCategoria(
                idCategoria = producto.idCategoria,
                nombre = producto.nombreCategoria ?: "Categoría"
            )
        }
    }

    private fun agregarFiltroCategoria(
        idCategoria: Int?,
        nombre: String
    ) {

        val seleccionado = categoriaSeleccionada == idCategoria

        val filtro = TextView(this).apply {

            text = nombre
            textSize = 12f
            setTextColor(
                if (seleccionado) Color.WHITE else Color.rgb(74, 63, 56)
            )

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(8))

            background = crearFondo(
                color = if (seleccionado) {
                    colorPrincipal
                } else {
                    Color.WHITE
                },
                radio = 10f
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(34)
            ).apply {
                marginEnd = dp(6)
            }

            isClickable = true
            isFocusable = true

            contentDescription = "Filtrar por $nombre"
        }

        filtro.setOnClickListener {

            categoriaSeleccionada = idCategoria

            mostrarCategorias()
            mostrarProductosFiltrados()
        }

        layoutCatalogCategories.addView(filtro)
    }

    private fun mostrarProductosFiltrados() {

        val productosFiltrados = listaProductos.filter { producto ->

            val coincideBusqueda =
                producto.nombre.contains(
                    textoBusqueda,
                    ignoreCase = true
                ) ||
                        (producto.nombreCategoria ?: "").contains(
                            textoBusqueda,
                            ignoreCase = true
                        )

            val coincideCategoria =
                categoriaSeleccionada == null ||
                        producto.idCategoria == categoriaSeleccionada

            coincideBusqueda && coincideCategoria
        }

        tvCatalogProductCount.text =
            if (productosFiltrados.size == 1) {
                "1 producto"
            } else {
                "${productosFiltrados.size} productos"
            }

        if (productosFiltrados.isEmpty()) {
            layoutCatalogProducts.removeAllViews()

            mostrarEstado(
                "No encontramos productos con esos criterios."
            )
            return
        }

        tvCatalogProductsStatus.visibility = View.GONE

        dibujarProductos(productosFiltrados)
    }

    private fun dibujarProductos(
        productos: List<ProductoResponse>
    ) {

        layoutCatalogProducts.removeAllViews()

        var fila: LinearLayout? = null

        productos.forEachIndexed { index, producto ->

            if (index % 2 == 0) {

                fila = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                layoutCatalogProducts.addView(fila)
            }

            val tarjeta = crearTarjetaProducto(producto)

            val parametros = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {

                if (index % 2 == 0) {
                    marginEnd = dp(6)
                } else {
                    marginStart = dp(6)
                }

                bottomMargin = dp(12)
            }

            fila?.addView(tarjeta, parametros)
        }
    }

    private fun crearTarjetaProducto(
        producto: ProductoResponse
    ): CardView {

        val tarjeta = CardView(this).apply {

            radius = dp(14).toFloat()
            cardElevation = dp(2).toFloat()
            setCardBackgroundColor(Color.WHITE)

            isClickable = true
            isFocusable = true
        }

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }

        val imagen = ImageView(this).apply {

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(120)
            )

            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.rgb(239, 236, 230))
            setImageResource(android.R.drawable.ic_menu_gallery)
        }

        contenido.addView(imagen)

        val categoria = TextView(this).apply {

            text = producto.nombreCategoria ?: "Sin categoría"
            textSize = 9f
            setTextColor(Color.rgb(140, 125, 117))

            setPadding(0, dp(8), 0, dp(2))
        }

        contenido.addView(categoria)

        val nombre = TextView(this).apply {

            text = producto.nombre
            textSize = 13f
            setTextColor(colorTexto)

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            maxLines = 2
        }

        contenido.addView(nombre)

        val precio = TextView(this).apply {

            text = formatearPrecio(producto.precio)
            textSize = 13f
            setTextColor(colorTexto)

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            setPadding(0, dp(8), 0, dp(2))
        }

        contenido.addView(precio)

        val disponibilidad = TextView(this).apply {

            text = if (producto.stock > 0) {
                "Disponible"
            } else {
                "Agotado"
            }

            textSize = 10f

            setTextColor(
                if (producto.stock > 0) {
                    Color.rgb(46, 125, 50)
                } else {
                    Color.rgb(192, 92, 56)
                }
            )
        }

        contenido.addView(disponibilidad)

        tarjeta.addView(contenido)

        if (producto.imagenes.isNotEmpty()) {
            cargarImagen(
                producto.imagenes.first(),
                imagen
            )
        }

        tarjeta.setOnClickListener {

            val intent = Intent(
                this,
                ActivityProductDetail::class.java
            ).apply {
                putExtra("ID_PRODUCTO", producto.idProducto)
            }

            startActivity(intent)
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

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {

                    bitmap = connection.inputStream.use {
                        BitmapFactory.decodeStream(it)
                    }
                }

            } catch (e: Exception) {

                android.util.Log.e(
                    "ActivityCatalog",
                    "Error cargando imagen: $ruta",
                    e
                )

            } finally {
                connection?.disconnect()
            }

            runOnUiThread {

                if (!isFinishing && !isDestroyed && bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun mostrarEstado(mensaje: String) {

        tvCatalogProductsStatus.text = mensaje
        tvCatalogProductsStatus.visibility = View.VISIBLE
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
        radio: Float
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radio.toInt()).toFloat()
        }
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        imageExecutor.shutdownNow()
        super.onDestroy()
    }
}
