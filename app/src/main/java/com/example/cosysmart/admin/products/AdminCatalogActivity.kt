package com.example.cosysmart.admin.products

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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.cosysmart.R
import com.example.cosysmart.admin.ActivityHomeAdmin
import com.example.cosysmart.admin.products.ActivityAdminEditProduct
import com.example.cosysmart.network.EstadoProductoRequest
import com.example.cosysmart.network.ProductoResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
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


// Activity para el catálogo de productos del administrador
class AdminCatalogActivity : AppCompatActivity() {

    private lateinit var etSearchAdminProducts: EditText
    private lateinit var tvAdminProductCount: TextView
    private lateinit var tvAdminProductsStatus: TextView
    private lateinit var layoutAdminProducts: LinearLayout

    private lateinit var imageExecutor: ExecutorService

    private var listaProductos = listOf<ProductoResponse>()
    private var textoBusqueda = ""

    private val colorPrincipal = Color.rgb(200, 109, 81)
    private val colorTexto = Color.rgb(61, 48, 40)
    private val colorSecundario = Color.rgb(122, 109, 99)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_catalog)

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
        etSearchAdminProducts =
            findViewById(R.id.etSearchAdminProducts)

        tvAdminProductCount =
            findViewById(R.id.tvAdminProductCount)

        tvAdminProductsStatus =
            findViewById(R.id.tvAdminProductsStatus)

        layoutAdminProducts =
            findViewById(R.id.layoutAdminProducts)
    }

    private fun configurarNavegacion() {
        val bottomNav = findViewById<BottomNavigationView>(
            R.id.bottomNavigationView
        )

        BottomNavigationHelper.setupAdminNav(
            this,
            bottomNav,
            R.id.nav_catalogo
        )
    }

    private fun configurarAcciones() {

        val btnBack = findViewById<ImageButton>(
            R.id.btnBackCatalog
        )

        btnBack.setOnClickListener {
            val intent = Intent(
                this,
                ActivityHomeAdmin::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
            finish()
        }

        val btnTabProducts = findViewById<Button>(
            R.id.btnTabAdminProducts
        )

        btnTabProducts.setOnClickListener {
            cargarProductos()
        }

        val btnTabCategories = findViewById<Button>(
            R.id.btnTabAdminCategories
        )

        btnTabCategories.setOnClickListener {
            android.widget.Toast.makeText(
                this,
                "La gestión de categorías se implementará por separado.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun configurarBuscador() {
        etSearchAdminProducts.addTextChangedListener(
            object : TextWatcher {

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
            }
        )
    }

    private fun cargarProductos() {
        mostrarEstado("Cargando catálogo global...")

        RetrofitClient.instance.listarProductos("administracion")
            .enqueue(object : Callback<List<ProductoResponse>> {

                override fun onResponse(
                    call: Call<List<ProductoResponse>>,
                    response: Response<List<ProductoResponse>>
                ) {
                    if (!response.isSuccessful) {
                        Log.e(
                            "AdminCatalogActivity",
                            "Error HTTP ${response.code()}: ${response.errorBody()?.string()}"
                        )

                        mostrarEstado(
                            "No fue posible cargar el catálogo."
                        )
                        return
                    }

                    listaProductos = response.body() ?: emptyList()

                    mostrarProductosFiltrados()
                }

                override fun onFailure(
                    call: Call<List<ProductoResponse>>,
                    t: Throwable
                ) {
                    Log.e(
                        "AdminCatalogActivity",
                        "Error consultando catálogo global",
                        t
                    )

                    mostrarEstado(
                        "Error de conexión. Comprueba tu conexión con el servidor."
                    )
                }
            })
    }

    private fun mostrarProductosFiltrados() {

        val productosFiltrados = listaProductos.filter { producto ->

            val coincideNombre = producto.nombre.contains(
                textoBusqueda,
                ignoreCase = true
            )

            val coincideId = producto.idProducto
                .toString()
                .contains(textoBusqueda, ignoreCase = true)

            coincideNombre || coincideId
        }

        tvAdminProductCount.text =
            if (productosFiltrados.size == 1) {
                "1 producto"
            } else {
                "${productosFiltrados.size} productos"
            }

        if (productosFiltrados.isEmpty()) {
            layoutAdminProducts.removeAllViews()

            mostrarEstado(
                if (listaProductos.isEmpty()) {
                    "No hay productos registrados en el sistema."
                } else {
                    "No encontramos productos con esa búsqueda."
                }
            )
            return
        }

        tvAdminProductsStatus.visibility = View.GONE

        dibujarProductos(productosFiltrados)
    }

    private fun dibujarProductos(
        productos: List<ProductoResponse>
    ) {
        layoutAdminProducts.removeAllViews()

        productos.forEach { producto ->
            layoutAdminProducts.addView(
                crearTarjetaProducto(producto)
            )
        }
    }

    private fun crearTarjetaProducto(
        producto: ProductoResponse
    ): CardView {

        val tarjeta = CardView(this).apply {
            radius = dp(12).toFloat()
            cardElevation = dp(2).toFloat()
            setCardBackgroundColor(Color.WHITE)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        }

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
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

        val filaNombreEstado = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val nombre = TextView(this).apply {
            text = producto.nombre
            textSize = 14f
            setTextColor(colorTexto)
            setTypeface(null, android.graphics.Typeface.BOLD)
            maxLines = 2

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val estado = TextView(this).apply {
            text = if (producto.estado == 1) {
                "Publicado"
            } else {
                "Inactivo"
            }

            textSize = 10f
            setTypeface(null, android.graphics.Typeface.BOLD)

            setTextColor(
                if (producto.estado == 1) {
                    Color.rgb(90, 107, 92)
                } else {
                    colorPrincipal
                }
            )

            setPadding(
                dp(8),
                dp(3),
                dp(8),
                dp(3)
            )

            background = crearFondo(
                if (producto.estado == 1) {
                    Color.rgb(234, 242, 235)
                } else {
                    Color.rgb(253, 243, 240)
                },
                6
            )
        }

        filaNombreEstado.addView(nombre)
        filaNombreEstado.addView(estado)

        informacion.addView(filaNombreEstado)

        val vendedor = TextView(this).apply {
            text = "ID vendedor: ${producto.idVendedor}"
            textSize = 11f
            setTextColor(colorSecundario)
            setPadding(0, dp(4), 0, 0)
        }

        informacion.addView(vendedor)

        val categoria = TextView(this).apply {
            text = producto.nombreCategoria ?: "Sin categoría"
            textSize = 11f
            setTextColor(colorSecundario)
            setPadding(0, dp(2), 0, 0)
        }

        informacion.addView(categoria)

        val precio = TextView(this).apply {
            text = formatearPrecio(producto.precio)
            textSize = 13f
            setTextColor(colorPrincipal)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(3), 0, 0)
        }

        informacion.addView(precio)

        filaSuperior.addView(informacion)
        contenido.addView(filaSuperior)

        val separador = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(10)
            }

            setBackgroundColor(Color.rgb(213, 203, 193))
        }

        contenido.addView(separador)

        val detalles = TextView(this).apply {
            text = "ID producto: ${producto.idProducto}    |    Stock: ${producto.stock}"
            textSize = 11f
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

        // BOTÓN EDITAR

        val btnEditar = MaterialButton(
            this,
            null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            text = "Editar"
            textSize = 12f
            isAllCaps = false

            setTextColor(colorTexto)

            strokeColor = android.content.res.ColorStateList.valueOf(
                Color.rgb(166, 138, 120)
            )

            cornerRadius = dp(8)

            layoutParams = LinearLayout.LayoutParams(
                0,
                dp(38),
                1f
            ).apply {
                marginEnd = dp(6)
            }
        }

        // BOTÓN MODERACIÓN

        val btnModeracion = MaterialButton(
            this,
            null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            text = if (producto.estado == 1) {
                "Desactivar"
            } else {
                "Reactivar"
            }

            textSize = 12f
            isAllCaps = false

            setTextColor(colorPrincipal)

            strokeColor = android.content.res.ColorStateList.valueOf(
                colorPrincipal
            )

            cornerRadius = dp(8)

            layoutParams = LinearLayout.LayoutParams(
                0,
                dp(38),
                1f
            ).apply {
                marginStart = dp(6)
            }
        }

        // ABRIR EDICIÓN DE PRODUCTO

        btnEditar.setOnClickListener {

            val intent = Intent(
                this@AdminCatalogActivity,
                ActivityAdminEditProduct::class.java
            )

            intent.putExtra(
                "ID_PRODUCTO",
                producto.idProducto
            )

            startActivity(intent)
        }

        // ACCIÓN DE MODERACIÓN (CAMBIAR ESTADO)

        btnModeracion.setOnClickListener {

            val nuevoEstado = if (producto.estado == 1) 0 else 1

            val accion = if (nuevoEstado == 0) {
                "inactivar"
            } else {
                "reactivar"
            }

            androidx.appcompat.app.AlertDialog.Builder(this)
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

        filaBotones.addView(btnEditar)
        filaBotones.addView(btnModeracion)

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

                    Toast.makeText(
                        this@AdminCatalogActivity,
                        if (nuevoEstado == 0) {
                            "Producto inactivado correctamente"
                        } else {
                            "Producto reactivado correctamente"
                        },
                        Toast.LENGTH_SHORT
                    ).show()

                    cargarProductos()

                } else {

                    val error = response.errorBody()?.string()

                    Toast.makeText(
                        this@AdminCatalogActivity,
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
                    this@AdminCatalogActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
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
                    "AdminCatalogActivity",
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
        tvAdminProductsStatus.text = mensaje
        tvAdminProductsStatus.visibility = View.VISIBLE
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