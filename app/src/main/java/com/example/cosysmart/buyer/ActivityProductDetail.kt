package com.example.cosysmart.buyer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.network.ProductoResponse
import com.example.cosysmart.network.RetrofitClient
import com.google.android.material.button.MaterialButton
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

    private val imageExecutor: ExecutorService = Executors.newFixedThreadPool(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        RetrofitClient.init(applicationContext)

        inicializarVistas()
        configurarEventos()

        val idProducto = intent.getIntExtra("ID_PRODUCTO", -1)

        if (idProducto == -1) {
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
        layoutProductThumbnails = findViewById(R.id.layoutProductThumbnails)

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

        val btnTopCart = findViewById<RelativeLayout>(R.id.btnTopCart)
        val btnFav = findViewById<ImageButton>(R.id.btnFav)

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
                flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
        }

        btnAddToCart.setOnClickListener {
            val producto = productoActual ?: return@setOnClickListener

            if (stockDisponible <= 0) {
                Toast.makeText(
                    this,
                    "Este producto no tiene existencias",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "${producto.nombre}: $quantity unidad(es)",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnFav.setOnClickListener {
            Toast.makeText(
                this,
                "La funcionalidad de favoritos está pendiente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun cargarProducto(idProducto: Int) {

        tvProductDescription.text = "Cargando información..."

        RetrofitClient.instance.listarProductos("catalogo")
            .enqueue(object : Callback<List<ProductoResponse>> {

                override fun onResponse(
                    call: Call<List<ProductoResponse>>,
                    response: Response<List<ProductoResponse>>
                ) {
                    if (!response.isSuccessful) {
                        mostrarError("No fue posible consultar el producto")
                        return
                    }

                    val producto = response.body()?.find {
                        it.idProducto == idProducto
                    }

                    if (producto == null) {
                        mostrarError("El producto ya no está disponible")
                        return
                    }

                    productoActual = producto
                    mostrarDatosProducto(producto)
                }

                override fun onFailure(
                    call: Call<List<ProductoResponse>>,
                    t: Throwable
                ) {
                    mostrarError("Error de conexión al cargar el producto")
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
            producto.color?.takeIf { it.isNotBlank() } ?: "No especificado"

        val dimensiones = listOfNotNull(
            producto.alto?.let { "Alto: ${it} cm" },
            producto.ancho?.let { "Ancho: ${it} cm" },
            producto.profundidad?.let { "Profundidad: ${it} cm" }
        )

        tvProductDimensions.text = if (dimensiones.isNotEmpty()) {
            dimensiones.joinToString(" • ")
        } else {
            "Dimensiones: No especificadas"
        }

        tvProductWeight.text = if (producto.peso != null) {
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

        imagenes.forEachIndexed { index, ruta ->

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
                android.util.Log.e(
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
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
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
