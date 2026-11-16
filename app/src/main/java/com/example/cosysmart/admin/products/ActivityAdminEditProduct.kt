package com.example.cosysmart.admin.products

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.cosysmart.R
import com.example.cosysmart.network.CategoriaResponse
import com.example.cosysmart.network.ProductoResponse
import com.example.cosysmart.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ActivityAdminEditProduct : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPrice: EditText
    private lateinit var etStock: EditText
    private lateinit var etDescription: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWidth: EditText
    private lateinit var etDepth: EditText
    private lateinit var etColor: EditText

    private lateinit var spCategory: Spinner
    private lateinit var spWeightUnit: Spinner

    private lateinit var btnSave: MaterialButton
    private lateinit var btnSelectImages: MaterialButton
    private lateinit var btnTakePhoto: MaterialButton

    private lateinit var tvPhotoCount: TextView
    private lateinit var layoutPreviews: LinearLayout
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView

    private val selectedImages = mutableListOf<Uri>()
    private val categories = mutableListOf<CategoriaResponse>()

    private val executor: ExecutorService =
        Executors.newFixedThreadPool(3)

    private var cameraImageUri: Uri? = null
    private var idProducto = -1
    private var guardando = false

    private val unidadesPeso = listOf(
        "Selecciona unidad",
        "g",
        "kg",
        "toneladas"
    )

    private val seleccionarImagenes =
        registerForActivityResult(
            ActivityResultContracts.GetMultipleContents()
        ) { uris ->

            if (uris.isEmpty()) return@registerForActivityResult

            val nuevas = uris
                .distinct()
                .filterNot { it in selectedImages }

            val disponibles = 5 - selectedImages.size

            selectedImages.addAll(nuevas.take(disponibles))

            mostrarPrevisualizaciones()

            if (nuevas.size > disponibles) {
                Toast.makeText(
                    this,
                    "Solo puedes tener un máximo de 5 fotografías",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val tomarFotografia =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { guardada ->

            val uri = cameraImageUri

            if (guardada && uri != null) {

                if (selectedImages.size < 5) {
                    selectedImages.add(uri)
                    mostrarPrevisualizaciones()
                } else {
                    contentResolver.delete(uri, null, null)

                    Toast.makeText(
                        this,
                        "Ya alcanzaste el máximo de 5 fotografías",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else if (uri != null) {
                contentResolver.delete(uri, null, null)
            }

            cameraImageUri = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_edit_product)

        RetrofitClient.init(applicationContext)

        idProducto = intent.getIntExtra("ID_PRODUCTO", -1)

        if (idProducto <= 0) {
            Toast.makeText(
                this,
                "No se recibió un producto válido",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        inicializarComponentes()
        configurarSpinners()
        configurarEventos()

        tvTitle.text = "Editar producto"
        tvSubtitle.text = "Actualiza la información del producto"
        btnSave.text = "Guardar cambios"

        cargarCategorias()
    }

    private fun inicializarComponentes() {

        etName = findViewById(R.id.etProductName)
        etPrice = findViewById(R.id.etProductPrice)
        etStock = findViewById(R.id.etProductStock)
        etDescription = findViewById(R.id.etProductDescription)

        etWeight = findViewById(R.id.etProductWeight)
        etHeight = findViewById(R.id.etProductHeight)
        etWidth = findViewById(R.id.etProductWidth)
        etDepth = findViewById(R.id.etProductDepth)
        etColor = findViewById(R.id.etProductColor)

        spCategory = findViewById(R.id.spProductCategory)
        spWeightUnit = findViewById(R.id.spWeightUnit)

        btnSave = findViewById(R.id.btnSaveProduct)
        btnSelectImages = findViewById(R.id.btnSelectProductImage)
        btnTakePhoto = findViewById(R.id.btnTakeProductPhoto)

        tvPhotoCount = findViewById(R.id.tvPhotoCount)
        layoutPreviews = findViewById(R.id.layoutProductPreviews)

        tvTitle = findViewById(R.id.tvCreateTitle)
        tvSubtitle = findViewById(R.id.tvCreateSubtitle)

        findViewById<ImageButton>(
            R.id.btnBackCreateProduct
        ).setOnClickListener {
            finish()
        }
    }

    private fun configurarSpinners() {

        val adapterUnidades = ArrayAdapter(
            this,
            R.layout.spinner_item,
            unidadesPeso
        )

        adapterUnidades.setDropDownViewResource(
            R.layout.spinner_dropdown_item
        )

        spWeightUnit.adapter = adapterUnidades
    }

    private fun configurarEventos() {

        btnSelectImages.setOnClickListener {
            seleccionarImagenes.launch("image/*")
        }

        btnTakePhoto.setOnClickListener {

            if (selectedImages.size >= 5) {
                Toast.makeText(
                    this,
                    "Ya alcanzaste el máximo de 5 fotografías",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                abrirCamaraProducto()
            }
        }

        btnSave.setOnClickListener {
            actualizarProducto()
        }
    }

    // CARGAR CATEGORÍAS

    private fun cargarCategorias() {

        RetrofitClient.instance.listarCategorias()
            .enqueue(object : Callback<List<CategoriaResponse>> {

                override fun onResponse(
                    call: Call<List<CategoriaResponse>>,
                    response: Response<List<CategoriaResponse>>
                ) {

                    if (!response.isSuccessful) {
                        mostrarMensaje(
                            "No se pudieron cargar las categorías"
                        )
                        return
                    }

                    val resultado = response.body()

                    if (resultado.isNullOrEmpty()) {
                        mostrarMensaje(
                            "No hay categorías disponibles"
                        )
                        return
                    }

                    categories.clear()
                    categories.addAll(resultado)

                    val nombres = mutableListOf(
                        "Selecciona una categoría"
                    )

                    nombres.addAll(categories.map { it.nombre })

                    val adapter = ArrayAdapter(
                        this@ActivityAdminEditProduct,
                        R.layout.spinner_item,
                        nombres
                    )

                    adapter.setDropDownViewResource(
                        R.layout.spinner_dropdown_item
                    )

                    spCategory.adapter = adapter

                    cargarProducto()
                }

                override fun onFailure(
                    call: Call<List<CategoriaResponse>>,
                    t: Throwable
                ) {
                    mostrarMensaje(
                        "Error al cargar categorías: ${t.message}"
                    )
                }
            })
    }

    // CONSULTAR PRODUCTOS DE ADMINISTRACIÓN

    private fun cargarProducto() {

        RetrofitClient.instance.listarProductos("administracion")
            .enqueue(object : Callback<List<ProductoResponse>> {

                override fun onResponse(
                    call: Call<List<ProductoResponse>>,
                    response: Response<List<ProductoResponse>>
                ) {

                    if (!response.isSuccessful) {
                        mostrarMensaje(
                            obtenerMensajeError(
                                response.errorBody()?.string()
                            )
                        )
                        return
                    }

                    val producto = response.body()
                        ?.firstOrNull {
                            it.idProducto == idProducto
                        }

                    if (producto == null) {
                        mostrarMensaje(
                            "No se encontró el producto"
                        )
                        finish()
                        return
                    }

                    llenarFormulario(producto)
                }

                override fun onFailure(
                    call: Call<List<ProductoResponse>>,
                    t: Throwable
                ) {
                    mostrarMensaje(
                        "Error al consultar el producto: ${t.message}"
                    )
                }
            })
    }

    // PRECARGAR INFORMACIÓN

    private fun llenarFormulario(
        producto: ProductoResponse
    ) {

        etName.setText(producto.nombre)
        etPrice.setText(producto.precio.toString())
        etStock.setText(producto.stock.toString())

        etDescription.setText(
            producto.descripcion.orEmpty()
        )

        etWeight.setText(
            producto.peso?.toString().orEmpty()
        )

        etHeight.setText(
            producto.alto?.toString().orEmpty()
        )

        etWidth.setText(
            producto.ancho?.toString().orEmpty()
        )

        etDepth.setText(
            producto.profundidad?.toString().orEmpty()
        )

        etColor.setText(
            producto.color.orEmpty()
        )

        val posicionCategoria = categories.indexOfFirst {
            it.idCategoria == producto.idCategoria
        }

        if (posicionCategoria >= 0) {
            spCategory.setSelection(posicionCategoria + 1)
        }

        val posicionUnidad = unidadesPeso.indexOf(
            producto.unidadPeso
        )

        if (posicionUnidad >= 0) {
            spWeightUnit.setSelection(posicionUnidad)
        }

        mostrarImagenesActuales(producto.imagenes)
    }

    // MOSTRAR FOTOGRAFÍAS ACTUALES

    private fun mostrarImagenesActuales(
        imagenes: List<String>
    ) {

        layoutPreviews.removeAllViews()

        tvPhotoCount.text =
            "Fotografías actuales: ${imagenes.size}/5. " +
                    "Si seleccionas nuevas, reemplazarán las anteriores."

        imagenes.forEach { ruta ->

            val imageView = ImageView(this)

            val params = LinearLayout.LayoutParams(
                dp(90),
                dp(90)
            )

            params.marginEnd = dp(10)

            imageView.layoutParams = params
            imageView.scaleType =
                ImageView.ScaleType.CENTER_CROP

            layoutPreviews.addView(imageView)

            executor.execute {

                var conexion: HttpURLConnection? = null

                try {

                    val url = URL(
                        RetrofitClient.construirUrl(ruta)
                    )

                    conexion = url.openConnection()
                            as HttpURLConnection

                    conexion.connectTimeout = 10000
                    conexion.readTimeout = 10000

                    val bitmap = conexion.inputStream.use {
                        android.graphics.BitmapFactory.decodeStream(it)
                    }

                    runOnUiThread {

                        if (!isFinishing && !isDestroyed && bitmap != null) {
                            imageView.setImageBitmap(bitmap)
                        }
                    }

                } catch (_: Exception) {

                    runOnUiThread {
                        imageView.setImageResource(
                            android.R.drawable.ic_menu_gallery
                        )
                    }

                } finally {
                    conexion?.disconnect()
                }
            }
        }
    }

    // CÁMARA

    private fun abrirCamaraProducto() {

        try {

            val directorio = File(cacheDir, "images")

            if (!directorio.exists() && !directorio.mkdirs()) {
                throw Exception(
                    "No fue posible crear el directorio de imágenes"
                )
            }

            val archivo = File.createTempFile(
                "producto_",
                ".jpg",
                directorio
            )

            val uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                archivo
            )

            cameraImageUri = uri
            tomarFotografia.launch(uri)

        } catch (e: Exception) {

            cameraImageUri = null

            mostrarMensaje(
                "No fue posible abrir la cámara: ${e.message}"
            )
        }
    }

    // PREVISUALIZAR FOTOGRAFÍAS NUEVAS

    private fun mostrarPrevisualizaciones() {

        layoutPreviews.removeAllViews()

        selectedImages.forEach { uri ->

            val imagen = ImageView(this)

            val parametros = LinearLayout.LayoutParams(
                dp(90),
                dp(90)
            )

            parametros.marginEnd = dp(10)

            imagen.layoutParams = parametros
            imagen.scaleType = ImageView.ScaleType.CENTER_CROP
            imagen.setImageURI(uri)

            layoutPreviews.addView(imagen)
        }

        tvPhotoCount.text =
            "Nuevas fotografías: ${selectedImages.size}/5. " +
                    "Reemplazarán las fotografías actuales."
    }

    // ACTUALIZAR PRODUCTO

    private fun actualizarProducto() {

        if (guardando) return

        val nombre = etName.text.toString().trim()
        val precioTexto = etPrice.text.toString().trim()
        val stockTexto = etStock.text.toString().trim()
        val descripcion = etDescription.text.toString().trim()

        val pesoTexto = etWeight.text.toString().trim()
        val altoTexto = etHeight.text.toString().trim()
        val anchoTexto = etWidth.text.toString().trim()
        val profundidadTexto = etDepth.text.toString().trim()
        val color = etColor.text.toString().trim()

        if (nombre.isEmpty() || nombre.length > 150) {
            etName.error = "Ingresa un nombre de máximo 150 caracteres"
            etName.requestFocus()
            return
        }

        if (spCategory.selectedItemPosition <= 0) {
            mostrarMensaje("Selecciona una categoría")
            return
        }

        val idCategoria =
            categories[spCategory.selectedItemPosition - 1].idCategoria

        val precio = convertirDecimal(precioTexto)

        if (
            precio == null ||
            precio <= BigDecimal.ZERO ||
            precio > BigDecimal("9999999999.99")
        ) {
            etPrice.error = "Ingresa un precio válido"
            etPrice.requestFocus()
            return
        }

        val stock = stockTexto.toIntOrNull()

        if (stock == null || stock < 0) {
            etStock.error = "Ingresa un stock válido"
            etStock.requestFocus()
            return
        }

        if (descripcion.length > 1000) {
            etDescription.error = "Máximo 1000 caracteres"
            etDescription.requestFocus()
            return
        }

        val peso = if (pesoTexto.isEmpty()) {
            null
        } else {
            convertirDecimal(pesoTexto)
        }

        if (
            pesoTexto.isNotEmpty() &&
            (
                    peso == null ||
                            peso <= BigDecimal.ZERO ||
                            peso > BigDecimal("9999999999.999")
                    )
        ) {
            etWeight.error = "Ingresa un peso válido"
            etWeight.requestFocus()
            return
        }

        val unidadPeso = if (peso != null) {

            if (spWeightUnit.selectedItemPosition <= 0) {
                mostrarMensaje("Selecciona la unidad del peso")
                return
            }

            unidadesPeso[spWeightUnit.selectedItemPosition]

        } else {
            null
        }

        val alto = validarDimension(altoTexto, etHeight)
        if (altoTexto.isNotEmpty() && alto == null) return

        val ancho = validarDimension(anchoTexto, etWidth)
        if (anchoTexto.isNotEmpty() && ancho == null) return

        val profundidad = validarDimension(
            profundidadTexto,
            etDepth
        )

        if (profundidadTexto.isNotEmpty() && profundidad == null) {
            return
        }

        if (color.length > 50) {
            etColor.error = "El color no puede superar 50 caracteres"
            etColor.requestFocus()
            return
        }

        val partesImagenes = try {

            if (selectedImages.isEmpty()) {
                null
            } else {
                convertirImagenesMultipart()
            }

        } catch (e: Exception) {

            mostrarMensaje(
                e.message ?: "Error al procesar fotografías"
            )

            return
        }

        val nombreParte = textoParte(nombre)
        val categoriaParte = textoParte(idCategoria.toString())
        val precioParte = textoParte(precio.toPlainString())
        val stockParte = textoParte(stock.toString())

        val descripcionParte = textoParte(descripcion)

        val pesoParte =
            peso?.let { textoParte(it.toPlainString()) }

        val unidadParte =
            unidadPeso?.let { textoParte(it) }

        val altoParte =
            alto?.let { textoParte(it.toPlainString()) }

        val anchoParte =
            ancho?.let { textoParte(it.toPlainString()) }

        val profundidadParte =
            profundidad?.let { textoParte(it.toPlainString()) }

        val colorParte = textoParte(color)

        guardando = true
        btnSave.isEnabled = false
        btnSave.text = "Guardando cambios..."

        RetrofitClient.instance.actualizarProducto(
            idProducto,
            nombreParte,
            categoriaParte,
            precioParte,
            descripcionParte,
            pesoParte,
            unidadParte,
            altoParte,
            anchoParte,
            profundidadParte,
            colorParte,
            stockParte,
            partesImagenes
        ).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                guardando = false
                btnSave.isEnabled = true
                btnSave.text = "Guardar cambios"

                if (response.isSuccessful) {

                    Toast.makeText(
                        this@ActivityAdminEditProduct,
                        "Producto actualizado correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()

                } else {

                    mostrarMensaje(
                        obtenerMensajeError(
                            response.errorBody()?.string()
                        )
                    )
                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {

                guardando = false
                btnSave.isEnabled = true
                btnSave.text = "Guardar cambios"

                mostrarMensaje(
                    "Error de conexión: ${t.message}"
                )
            }
        })
    }

    // VALIDAR DIMENSIONES

    private fun validarDimension(
        valor: String,
        campo: EditText
    ): BigDecimal? {

        if (valor.isEmpty()) return null

        val numero = convertirDecimal(valor)

        if (
            numero == null ||
            numero <= BigDecimal.ZERO ||
            numero > BigDecimal("99999999.99")
        ) {
            campo.error = "Ingresa un valor mayor que cero"
            campo.requestFocus()
            return null
        }

        return numero
    }

    private fun convertirDecimal(
        valor: String
    ): BigDecimal? {

        return try {
            valor.replace(",", ".").toBigDecimal()
        } catch (_: NumberFormatException) {
            null
        }
    }

    private fun textoParte(
        valor: String
    ): RequestBody {

        return valor.toRequestBody(
            "text/plain".toMediaType()
        )
    }

    private fun convertirImagenesMultipart():
            List<MultipartBody.Part> {

        val partes = mutableListOf<MultipartBody.Part>()

        selectedImages.forEachIndexed { index, uri ->

            val mimeType = contentResolver.getType(uri)
                ?: "image/jpeg"

            val extension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"

                else -> throw Exception(
                    "La fotografía ${index + 1} debe ser JPG, PNG o WEBP"
                )
            }

            val contenido = contentResolver
                .openInputStream(uri)
                ?.use { it.readBytes() }
                ?: throw Exception(
                    "No se pudo leer una fotografía"
                )

            if (contenido.isEmpty()) {
                throw Exception("La fotografía está vacía")
            }

            if (contenido.size > 5 * 1024 * 1024) {
                throw Exception(
                    "La fotografía ${index + 1} supera los 5 MB"
                )
            }

            val requestBody = contenido.toRequestBody(
                mimeType.toMediaType()
            )

            partes.add(
                MultipartBody.Part.createFormData(
                    "fotos",
                    "producto_${index + 1}.$extension",
                    requestBody
                )
            )
        }

        return partes
    }

    // ERRORES

    private fun obtenerMensajeError(
        error: String?
    ): String {

        if (error.isNullOrBlank()) {
            return "No fue posible completar la operación"
        }

        return try {

            JSONObject(error).optString(
                "detail",
                "Error al procesar la solicitud"
            )

        } catch (_: Exception) {
            error
        }
    }

    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun dp(valor: Int): Int {
        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}