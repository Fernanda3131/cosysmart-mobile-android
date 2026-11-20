
package com.example.cosysmart.buyer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.network.AgregarProductoCarritoRequest
import com.example.cosysmart.network.CarritoResponse
import com.example.cosysmart.network.ProductoResponse
import com.example.cosysmart.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Activity para ver el detalle de un producto
class ActivityProductDetail : AppCompatActivity() {

    private var quantity = 1
    private var stockDisponible = 0
    private var productoActual: ProductoResponse? = null

    private lateinit var tvQuantity: TextView
    private lateinit var btnMinus: TextView
    private lateinit var btnPlus: TextView
    private lateinit var btnAddToCart: MaterialButton

    private lateinit var imgMainProduct: ImageView
    private lateinit var layoutProductThumbnails: LinearLayout

    private lateinit var tvProductName: TextView
    private lateinit var tvProductCategory: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvProductStock: TextView
    private lateinit var tvSelectedColor: TextView
    private lateinit var tvProductDimensions: TextView
    private lateinit var tvProductWeight: TextView
    private lateinit var tvProductDescription: TextView

    private val imageExecutor: ExecutorService =
        Executors.newFixedThreadPool(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        RetrofitClient.init(applicationContext)

        inicializarVistas()
        configurarEventos()

        val idProducto = intent.getIntExtra("ID_PRODUCTO", -1)

        if (idProducto <= 0) {
            Toast.makeText(
                this,
                "No se recibió un producto válido",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        cargarProducto(idProducto)
    }

    private fun inicializarVistas() {
        tvQuantity = findViewById(R.id.tvQuantity)
        btnMinus = findViewById(R.id.btnMinus)
        btnPlus = findViewById(R.id.btnPlus)
        btnAddToCart = findViewById(R.id.btnAddToCart)

        imgMainProduct = findViewById(R.id.imgMainProduct)
        layoutProductThumbnails =
            findViewById(R.id.layoutProductThumbnails)

        tvProductName = findViewById(R.id.tvProductName)
        tvProductCategory = findViewById(R.id.tvProductCategory)
        tvProductPrice = findViewById(R.id.tvProductPrice)
        tvProductStock = findViewById(R.id.tvProductStock)
        tvSelectedColor = findViewById(R.id.tvSelectedColor)
        tvProductDimensions = findViewById(R.id.tvProductDimensions)
        tvProductWeight = findViewById(R.id.tvProductWeight)
        tvProductDescription = findViewById(R.id.tvProductDescription)
    }

    private fun configurarEventos() {

        val btnTopCart =
            findViewById<RelativeLayout>(R.id.btnTopCart)

        val btnFav =
            findViewById<ImageButton>(R.id.btnFav)

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
            }
        }

        btnPlus.setOnClickListener {
            if (quantity < stockDisponible) {
                quantity++
                tvQuantity.text = quantity.toString()
            } else {
                Toast.makeText(
                    this,
                    "No hay más unidades disponibles",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnTopCart.setOnClickListener {
            val intent = android.content.Intent(
                this,
                ActivityCart::class.java
            ).apply {
                flags =
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
        }

        btnAddToCart.setOnClickListener {
            val producto = productoActual
                ?: return@setOnClickListener

            if (stockDisponible <= 0) {
                Toast.makeText(
                    this,
                    "Este producto no tiene existencias",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            agregarProductoAlCarrito(producto)
        }

        btnFav.setOnClickListener {
            Toast.makeText(
                this,
                "La funcionalidad de favoritos está pendiente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun agregarProductoAlCarrito(producto: ProductoResponse) {

        if (quantity <= 0 || quantity > stockDisponible) {
            Toast.makeText(
                this,
                "La cantidad seleccionada no es válida",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val cantidadSeleccionada = quantity

        val request = AgregarProductoCarritoRequest(
            idProducto = producto.idProducto,
            cantidad = cantidadSeleccionada
        )

        val textoOriginal = btnAddToCart.text.toString()

        btnAddToCart.isEnabled = false
        btnAddToCart.text = "Agregando..."

        RetrofitClient.instance.agregarProductoCarrito(request)
            .enqueue(object : Callback<CarritoResponse> {

                override fun onResponse(
                    call: Call<CarritoResponse>,
                    response: Response<CarritoResponse>
                ) {
                    btnAddToCart.isEnabled = stockDisponible > 0
                    btnAddToCart.text = textoOriginal

                    if (response.isSuccessful) {

                        val carrito = response.body()

                        if (carrito == null) {
                            Toast.makeText(
                                this@ActivityProductDetail,
                                "El servidor no devolvió la información del carrito",
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }

                        Toast.makeText(
                            this@ActivityProductDetail,
                            "${producto.nombre}: $cantidadSeleccionada unidad(es) agregada(s) al carrito",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.d(
                            "ActivityProductDetail",
                            "Producto agregado. ID carrito: ${carrito.idCarrito}, Total: ${carrito.total}"
                        )

                    } else {

                        val detalleServidor = try {
                            JSONObject(
                                response.errorBody()?.string() ?: "{}"
                            ).optString("detail")
                        } catch (e: Exception) {
                            ""
                        }

                        val mensaje = when (response.code()) {
                            400 -> detalleServidor.takeIf {
                                it.isNotBlank()
                            } ?: "No se pudo agregar el producto"

                            401 -> "Tu sesión expiró. Inicia sesión nuevamente"

                            403 -> "No tienes permisos para utilizar el carrito"

                            404 -> "El producto ya no está disponible"

                            422 -> "Los datos enviados no son válidos"

                            else -> detalleServidor.takeIf {
                                it.isNotBlank()
                            } ?: "Error al agregar el producto. Código: ${response.code()}"
                        }

                        Log.e(
                            "ActivityProductDetail",
                            "Error agregando al carrito: ${response.code()} - $mensaje"
                        )

                        Toast.makeText(
                            this@ActivityProductDetail,
                            mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<CarritoResponse>,
                    t: Throwable
                ) {
                    btnAddToCart.isEnabled = stockDisponible > 0
                    btnAddToCart.text = textoOriginal

                    Log.e(
                        "ActivityProductDetail",
                        "Error de conexión al agregar al carrito",
                        t
                    )

                    Toast.makeText(
                        this@ActivityProductDetail,
                        "No fue posible conectar con el servidor",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun cargarProducto(idProducto: Int) {

        tvProductDescription.text = "Cargando información..."

        RetrofitClient.instance.obtenerProductoPorId(idProducto)
            .enqueue(object : Callback<ProductoResponse> {

                override fun onResponse(
                    call: Call<ProductoResponse>,
                    response: Response<ProductoResponse>
                ) {
                    if (!response.isSuccessful) {
                        val mensaje = when (response.code()) {
                            404 -> "El producto ya no está disponible"
                            else -> "No fue posible consultar el producto"
                        }

                        mostrarError(mensaje)
                        return
                    }

                    val producto = response.body()

                    if (producto == null) {
                        mostrarError(
                            "El servidor no devolvió información del producto"
                        )
                        return
                    }

                    productoActual = producto
                    mostrarDatosProducto(producto)
                }

                override fun onFailure(
                    call: Call<ProductoResponse>,
                    t: Throwable
                ) {
                    Log.e(
                        "ActivityProductDetail",
                        "Error consultando producto",
                        t
                    )

                    mostrarError(
                        "Error de conexión al cargar el producto"
                    )
                }
            })
    }

    private fun mostrarDatosProducto(producto: ProductoResponse) {

        tvProductName.text = producto.nombre

        tvProductCategory.text =
            producto.nombreCategoria?.uppercase(Locale.getDefault())
                ?: "SIN CATEGORÍA"

        tvProductPrice.text = formatearPrecio(producto.precio)

        stockDisponible = producto.stock.coerceAtLeast(0)

        tvProductStock.text = if (stockDisponible > 0) {
            "Disponible: $stockDisponible unidades"
        } else {
            "Producto agotado"
        }

        btnAddToCart.isEnabled = stockDisponible > 0
        btnAddToCart.alpha = if (stockDisponible > 0) 1f else 0.5f

        quantity = 1
        tvQuantity.text = quantity.toString()

        tvSelectedColor.text =
            producto.color?.takeIf { it.isNotBlank() }
                ?: "No especificado"

        val dimensiones = listOfNotNull(
            producto.alto?.let { "Alto: $it cm" },
            producto.ancho?.let { "Ancho: $it cm" },
            producto.profundidad?.let { "Profundidad: $it cm" }
        )

        tvProductDimensions.text =
            if (dimensiones.isNotEmpty()) {
                dimensiones.joinToString(" • ")
            } else {
                "Dimensiones: No especificadas"
            }

        tvProductWeight.text =
            if (producto.peso != null) {
                "Peso: ${producto.peso} ${producto.unidadPeso ?: ""}".trim()
            } else {
                "Peso: No especificado"
            }

        tvProductDescription.text =
            producto.descripcion?.takeIf { it.isNotBlank() }
                ?: "Este producto no tiene una descripción disponible."

        cargarImagenes(producto.imagenes)
    }

    private fun cargarImagenes(imagenes: List<String>) {

        layoutProductThumbnails.removeAllViews()

        if (imagenes.isEmpty()) {
            imgMainProduct.setImageResource(
                android.R.drawable.ic_menu_gallery
            )
            return
        }

        cargarImagen(imagenes[0]) { bitmap ->
            if (bitmap != null) {
                imgMainProduct.setImageBitmap(bitmap)
            }
        }

        imagenes.forEach { ruta ->

            val thumbnail = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dp(60),
                    dp(60)
                ).apply {
                    marginEnd = dp(8)
                }

                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(android.graphics.Color.LTGRAY)
                setImageResource(android.R.drawable.ic_menu_gallery)
            }

            layoutProductThumbnails.addView(thumbnail)

            cargarImagen(ruta) { bitmap ->
                if (bitmap != null) {
                    thumbnail.setImageBitmap(bitmap)
                }
            }

            thumbnail.setOnClickListener {
                cargarImagen(ruta) { bitmap ->
                    if (bitmap != null) {
                        imgMainProduct.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    private fun cargarImagen(
        ruta: String,
        callback: (Bitmap?) -> Unit
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
                Log.e(
                    "ActivityProductDetail",
                    "Error cargando imagen: $ruta",
                    e
                )
            } finally {
                connection?.disconnect()
            }

            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    callback(bitmap)
                }
            }
        }
    }

    private fun formatearPrecio(precio: Double): String {
        val formato = NumberFormat.getCurrencyInstance(
            Locale("es", "CO")
        )

        formato.maximumFractionDigits = 0
        formato.minimumFractionDigits = 0

        return formato.format(precio)
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()

        finish()
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        imageExecutor.shutdownNow()
        super.onDestroy()
    }
}
