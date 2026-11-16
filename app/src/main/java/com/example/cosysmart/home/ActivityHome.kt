
package com.example.cosysmart.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.network.ProductoResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Activity principal de la aplicación para compradores y vendedores
class ActivityHome : AppCompatActivity() {

    private lateinit var layoutHomeProducts: LinearLayout
    private lateinit var txtHomeProductsStatus: TextView

    private val mainHandler = Handler(Looper.getMainLooper())

    private val imageExecutor: ExecutorService =
        Executors.newFixedThreadPool(4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        RetrofitClient.init(applicationContext)

        val userRole = intent.getStringExtra("USER_ROLE")
            ?.trim()
            ?.lowercase()
            ?: ""

        val bottomNav = findViewById<BottomNavigationView>(
            R.id.bottomNavigationViewHome
        )

        val txtTitle = findViewById<TextView>(
            R.id.txtHeaderTitle
        )

        val txtSubtitle = findViewById<TextView>(
            R.id.txtHeaderSubtitle
        )

        val layoutSearch = findViewById<View>(
            R.id.layoutSearchHome
        )

        val txtSectionTitle = findViewById<View>(
            R.id.txtSection1Title
        )

        val txtSectionAction = findViewById<View>(
            R.id.txtSection1Action
        )

        val layoutDynamicCards = findViewById<View>(
            R.id.layoutDynamicCards
        )

        val layoutCategoryHeader = findViewById<View>(
            R.id.layoutCategoryHeader
        )

        val layoutCategoryCards = findViewById<View>(
            R.id.layoutCategoryCards
        )

        val txtCatalogAction = findViewById<View>(
            R.id.txtHomeCatalogAction
        )

        layoutHomeProducts = findViewById(
            R.id.layoutHomeProducts
        )

        txtHomeProductsStatus = findViewById(
            R.id.txtHomeProductsStatus
        )

        bottomNav.menu.clear()

        when (userRole) {

            "vendor", "seller", "vendedor" -> {

                txtSubtitle.text = "PANEL DE SOCIO VENDEDOR"
                txtTitle.text = "Gestiona tu negocio y catálogo."

                layoutSearch.visibility = View.GONE
                txtSectionTitle.visibility = View.GONE
                txtSectionAction.visibility = View.GONE
                layoutDynamicCards.visibility = View.GONE
                layoutCategoryHeader.visibility = View.GONE
                layoutCategoryCards.visibility = View.GONE
                txtCatalogAction.visibility = View.GONE

                BottomNavigationHelper.setupSellerNav(
                    this,
                    bottomNav,
                    R.id.nav_seller_panel
                )

                Toast.makeText(
                    this,
                    "Modo Vendedor Activo",
                    Toast.LENGTH_SHORT
                ).show()
            }

            "buyer", "comprador" -> {

                txtSubtitle.text = "TU HOGAR, A TU MANERA"
                txtTitle.text = "Encuentra tu lugar de bienestar."

                layoutSearch.visibility = View.VISIBLE
                txtSectionTitle.visibility = View.VISIBLE
                txtSectionAction.visibility = View.VISIBLE
                layoutDynamicCards.visibility = View.VISIBLE
                layoutCategoryHeader.visibility = View.VISIBLE
                layoutCategoryCards.visibility = View.VISIBLE
                txtCatalogAction.visibility = View.VISIBLE

                BottomNavigationHelper.setupBuyerNav(
                    this,
                    bottomNav,
                    R.id.nav_buyer_inicio
                )

                Toast.makeText(
                    this,
                    "Modo Comprador Activo",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {

                Toast.makeText(
                    this,
                    "Rol de usuario no reconocido",
                    Toast.LENGTH_LONG
                ).show()

                finish()
                return
            }
        }

        cargarProductosHome()
    }

    private fun cargarProductosHome() {

        txtHomeProductsStatus.visibility = View.VISIBLE
        txtHomeProductsStatus.text = "Cargando productos..."

        layoutHomeProducts.removeAllViews()

        RetrofitClient.instance.listarProductos("home")
            .enqueue(object : Callback<List<ProductoResponse>> {

                override fun onResponse(
                    call: Call<List<ProductoResponse>>,
                    response: Response<List<ProductoResponse>>
                ) {

                    if (!response.isSuccessful) {

                        mostrarEstado(
                            "No fue posible cargar los productos."
                        )

                        return
                    }

                    val productos = response.body().orEmpty()

                    if (productos.isEmpty()) {

                        mostrarEstado(
                            "Todavía no hay productos disponibles."
                        )

                        return
                    }

                    mostrarProductos(productos)
                }

                override fun onFailure(
                    call: Call<List<ProductoResponse>>,
                    t: Throwable
                ) {

                    mostrarEstado(
                        "Error de conexión. Intenta nuevamente."
                    )
                }
            })
    }

    private fun mostrarProductos(
        productos: List<ProductoResponse>
    ) {

        layoutHomeProducts.removeAllViews()

        val inflater = LayoutInflater.from(this)

        val productosLimitados = productos.take(4)

        for (indice in productosLimitados.indices step 2) {

            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
            }

            val productoIzquierdo = productosLimitados[indice]

            val tarjetaIzquierda = inflater.inflate(
                R.layout.item_producto_home,
                fila,
                false
            )

            configurarTarjeta(
                tarjetaIzquierda,
                productoIzquierdo
            )

            val paramsIzquierda = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            paramsIzquierda.marginEnd = dp(6)

            fila.addView(
                tarjetaIzquierda,
                paramsIzquierda
            )

            if (indice + 1 < productosLimitados.size) {

                val productoDerecho =
                    productosLimitados[indice + 1]

                val tarjetaDerecha = inflater.inflate(
                    R.layout.item_producto_home,
                    fila,
                    false
                )

                configurarTarjeta(
                    tarjetaDerecha,
                    productoDerecho
                )

                val paramsDerecha = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

                paramsDerecha.marginStart = dp(6)

                fila.addView(
                    tarjetaDerecha,
                    paramsDerecha
                )

            } else {

                val espacio = View(this)

                fila.addView(
                    espacio,
                    LinearLayout.LayoutParams(
                        0,
                        1,
                        1f
                    )
                )
            }

            val paramsFila = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            paramsFila.bottomMargin = dp(12)

            layoutHomeProducts.addView(
                fila,
                paramsFila
            )
        }

        txtHomeProductsStatus.visibility = View.GONE
    }

    private fun configurarTarjeta(
        tarjeta: View,
        producto: ProductoResponse
    ) {

        val imgProducto = tarjeta.findViewById<ImageView>(
            R.id.imgHomeProducto
        )

        val txtNombre = tarjeta.findViewById<TextView>(
            R.id.txtHomeProductoNombre
        )

        val txtPrecio = tarjeta.findViewById<TextView>(
            R.id.txtHomeProductoPrecio
        )

        txtNombre.text = producto.nombre
        txtPrecio.text = formatearPrecio(producto.precio)

        imgProducto.setImageResource(
            android.R.drawable.ic_menu_gallery
        )

        val primeraImagen = producto.imagenes.firstOrNull()

        if (!primeraImagen.isNullOrBlank()) {

            val urlImagen = RetrofitClient.construirUrl(
                primeraImagen
            )

            cargarImagen(
                urlImagen,
                imgProducto
            )
        }
    }

    private fun formatearPrecio(precio: Double): String {

        val formato = NumberFormat.getNumberInstance(
            Locale("es", "CO")
        )

        formato.maximumFractionDigits = 0
        formato.minimumFractionDigits = 0

        return "$${formato.format(precio)} COP"
    }

    private fun cargarImagen(
        urlImagen: String,
        imageView: ImageView
    ) {

        imageView.tag = urlImagen

        imageExecutor.execute {

            var conexion: HttpURLConnection? = null

            try {

                conexion = URL(urlImagen)
                    .openConnection() as HttpURLConnection

                conexion.connectTimeout = 10000
                conexion.readTimeout = 15000
                conexion.requestMethod = "GET"

                conexion.connect()

                if (conexion.responseCode == HttpURLConnection.HTTP_OK) {

                    val bitmap = conexion.inputStream.use {
                        BitmapFactory.decodeStream(it)
                    }

                    if (bitmap != null) {

                        mainHandler.post {

                            if (imageView.tag == urlImagen) {
                                imageView.setImageBitmap(bitmap)
                            }
                        }
                    }
                }

            } catch (e: Exception) {

                android.util.Log.e(
                    "CosySmart",
                    "Error cargando imagen: $urlImagen",
                    e
                )

            } finally {

                conexion?.disconnect()
            }
        }
    }

    private fun mostrarEstado(mensaje: String) {

        layoutHomeProducts.removeAllViews()

        txtHomeProductsStatus.text = mensaje
        txtHomeProductsStatus.visibility = View.VISIBLE
    }

    private fun dp(valor: Int): Int {

        return (valor * resources.displayMetrics.density)
            .toInt()
    }

    override fun onDestroy() {
        super.onDestroy()

        imageExecutor.shutdownNow()
        mainHandler.removeCallbacksAndMessages(null)
    }
}
