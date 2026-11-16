
package com.example.cosysmart.seller.management

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
import com.example.cosysmart.network.CrearProductoResponse
import com.example.cosysmart.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.math.BigDecimal

// Activity para crear productos
class ActivitySellerCreateProduct : AppCompatActivity() {

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

    private val selectedImages = mutableListOf<Uri>()
    private val categories = mutableListOf<CategoriaResponse>()

    private var cameraImageUri: Uri? = null

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

            if (uris.isEmpty()) {
                return@registerForActivityResult
            }

            val nuevasImagenes = uris
                .distinct()
                .filterNot { it in selectedImages }

            val disponibles = 5 - selectedImages.size

            selectedImages.addAll(
                nuevasImagenes.take(disponibles)
            )

            mostrarPrevisualizaciones()

            if (nuevasImagenes.size > disponibles) {
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

                    Toast.makeText(
                        this,
                        "Fotografía agregada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

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

        setContentView(R.layout.activity_seller_create_product)

        inicializarComponentes()
        configurarSpinners()
        configurarEventos()

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

        val btnBack = findViewById<ImageButton>(
            R.id.btnBackCreateProduct
        )

        btnBack.setOnClickListener {
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
        spWeightUnit.setSelection(0, false)
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
            registrarProducto()
        }
    }


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

            Toast.makeText(
                this,
                "No fue posible abrir la cámara: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cargarCategorias() {

        RetrofitClient.instance.listarCategorias()
            .enqueue(object : Callback<List<CategoriaResponse>> {

                override fun onResponse(
                    call: Call<List<CategoriaResponse>>,
                    response: Response<List<CategoriaResponse>>
                ) {

                    if (response.isSuccessful) {

                        val resultado = response.body()

                        if (resultado.isNullOrEmpty()) {

                            Toast.makeText(
                                this@ActivitySellerCreateProduct,
                                "No hay categorías disponibles",
                                Toast.LENGTH_LONG
                            ).show()

                            return
                        }

                        categories.clear()
                        categories.addAll(resultado)

                        val nombres = mutableListOf(
                            "Selecciona una categoría"
                        )

                        nombres.addAll(
                            categories.map { it.nombre }
                        )

                        val adapter = ArrayAdapter(
                            this@ActivitySellerCreateProduct,
                            R.layout.spinner_item,
                            nombres
                        )

                        adapter.setDropDownViewResource(
                            R.layout.spinner_dropdown_item
                        )

                        spCategory.adapter = adapter
                        spCategory.setSelection(0, false)

                    } else {

                        Toast.makeText(
                            this@ActivitySellerCreateProduct,
                            obtenerMensajeError(
                                response.errorBody()?.string()
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<CategoriaResponse>>,
                    t: Throwable
                ) {

                    Toast.makeText(
                        this@ActivitySellerCreateProduct,
                        "Error al cargar categorías: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

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
            imagen.contentDescription = "Fotografía del producto"

            layoutPreviews.addView(imagen)
        }

        tvPhotoCount.text =
            "Fotografías seleccionadas: ${selectedImages.size}/5"
    }


    private fun registrarProducto() {

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

            Toast.makeText(
                this,
                "Selecciona una categoría",
                Toast.LENGTH_SHORT
            ).show()

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


        val stock = if (stockTexto.isEmpty()) {
            0
        } else {
            stockTexto.toIntOrNull()
        }

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
                            peso > BigDecimal("999999999.999")
                    )
        ) {

            etWeight.error = "Ingresa un peso válido"
            etWeight.requestFocus()

            return
        }

        val unidadPeso = if (peso != null) {

            if (spWeightUnit.selectedItemPosition <= 0) {

                Toast.makeText(
                    this,
                    "Selecciona la unidad del peso",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            unidadesPeso[spWeightUnit.selectedItemPosition]

        } else {
            null
        }

        val alto = validarDimension(altoTexto, etHeight)

        if (altoTexto.isNotEmpty() && alto == null) {
            return
        }

        val ancho = validarDimension(anchoTexto, etWidth)

        if (anchoTexto.isNotEmpty() && ancho == null) {
            return
        }

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


        if (selectedImages.isEmpty() || selectedImages.size > 5) {

            Toast.makeText(
                this,
                "Selecciona entre 1 y 5 fotografías",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val partesImagenes = try {

            convertirImagenesMultipart()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                e.message ?: "No se pudieron procesar las fotografías",
                Toast.LENGTH_LONG
            ).show()

            return
        }


        val nombreParte = textoParte(nombre)
        val categoriaParte = textoParte(idCategoria.toString())
        val precioParte = textoParte(precio.toPlainString())
        val stockParte = textoParte(stock.toString())

        val descripcionParte =
            descripcion.takeIf { it.isNotEmpty() }
                ?.let { textoParte(it) }

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

        val colorParte =
            color.takeIf { it.isNotEmpty() }
                ?.let { textoParte(it) }


        btnSave.isEnabled = false
        btnSave.text = "Registrando producto..."

        RetrofitClient.instance.crearProducto(
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
        ).enqueue(object : Callback<CrearProductoResponse> {

            override fun onResponse(
                call: Call<CrearProductoResponse>,
                response: Response<CrearProductoResponse>
            ) {

                btnSave.isEnabled = true
                btnSave.text = "Guardar y publicar producto"

                if (response.code() == 201) {

                    val resultado = response.body()

                    if (resultado != null) {

                        Toast.makeText(
                            this@ActivitySellerCreateProduct,
                            "Producto registrado correctamente",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()

                    } else {

                        Toast.makeText(
                            this@ActivitySellerCreateProduct,
                            "El servidor no devolvió los datos del producto",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {

                    val mensaje = obtenerMensajeError(
                        response.errorBody()?.string()
                    )

                    Toast.makeText(
                        this@ActivitySellerCreateProduct,
                        mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<CrearProductoResponse>,
                t: Throwable
            ) {

                btnSave.isEnabled = true
                btnSave.text = "Guardar y publicar producto"

                Toast.makeText(
                    this@ActivitySellerCreateProduct,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

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

    private fun convertirDecimal(valor: String): BigDecimal? {

        return try {
            valor.replace(",", ".").toBigDecimal()
        } catch (e: NumberFormatException) {
            null
        }
    }


    private fun textoParte(valor: String): RequestBody {

        return valor.toRequestBody(
            "text/plain".toMediaType()
        )
    }


    private fun convertirImagenesMultipart(): List<MultipartBody.Part> {

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

                throw Exception(
                    "Una de las fotografías está vacía"
                )
            }

            if (contenido.size > 5 * 1024 * 1024) {

                throw Exception(
                    "La fotografía ${index + 1} supera los 5 MB"
                )
            }

            val requestBody = contenido.toRequestBody(
                mimeType.toMediaType()
            )

            val nombreArchivo = "producto_${index + 1}.$extension"

            val parte = MultipartBody.Part.createFormData(
                "fotos",
                nombreArchivo,
                requestBody
            )

            partes.add(parte)
        }

        return partes
    }

    private fun obtenerMensajeError(error: String?): String {

        if (error.isNullOrBlank()) {
            return "No fue posible completar la operación"
        }

        return try {

            val json = JSONObject(error)

            json.optString(
                "detail",
                "Error al procesar la solicitud"
            )

        } catch (e: Exception) {

            "Error al procesar la solicitud"
        }
    }


    private fun dp(valor: Int): Int {

        return (valor * resources.displayMetrics.density).toInt()
    }
}