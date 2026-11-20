
package com.example.cosysmart.payments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.cosysmart.R
import com.example.cosysmart.buyer.ActivityCart
import com.example.cosysmart.network.CarritoResponse
import com.example.cosysmart.network.DetalleCarritoResponse
import com.example.cosysmart.network.RetrofitClient
import com.example.cosysmart.network.CrearOrdenRequest
import com.example.cosysmart.network.OrdenResponse
import com.example.cosysmart.network.IniciarPagoRequest
import com.example.cosysmart.network.PagoWompiResponse
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.gms.tasks.CancellationTokenSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
// ACTIVITY DE PAGO
class ActivityCheckout : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var layoutCheckoutItems: LinearLayout
    private lateinit var tvCheckoutEmpty: TextView
    private lateinit var tvCheckoutItemsTitle: TextView
    private lateinit var tvCheckoutSubtotal: TextView
    private lateinit var tvCheckoutShipping: TextView
    private lateinit var tvCheckoutTotal: TextView
    private lateinit var etCheckoutName: EditText
    private lateinit var etCheckoutPhone: EditText
    private lateinit var etCheckoutCity: EditText
    private lateinit var etCheckoutAddress: EditText
    private lateinit var etCheckoutNotes: EditText

    // MÉTODOS DE PAGO
    private lateinit var rgPaymentMethods: RadioGroup
    private lateinit var btnConfirmPayment: MaterialButton

    // MAPA
    private lateinit var mapCheckout: MapView
    private lateinit var btnUseCurrentLocation: MaterialButton
    private lateinit var tvSelectedLocation: TextView

    private var googleMap: GoogleMap? = null
    private var marcadorSeleccionado: Marker? = null
    private var ubicacionSeleccionada: LatLng? = null

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var cancellationTokenSource: CancellationTokenSource? = null

    // CARRITO Y ESTADO DE LA ORDEN
    private var carritoActual: CarritoResponse? = null
    private var cargandoCarrito = false
    private var creandoOrden = false

    // ORDEN CREADA QUE NECESITA INICIAR O RECUPERAR SU PAGO
    private var ordenPendientePago: Int? = null

    // EJECUTORES
    private val imageExecutor: ExecutorService =
        Executors.newFixedThreadPool(2)

    private val geocoderExecutor: ExecutorService =
        Executors.newSingleThreadExecutor()

    // FORMATO DE MONEDA
    private val formatoMoneda: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }

    // SOLICITUD DE PERMISOS DE UBICACIÓN
    private val permisoUbicacionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permisos ->

            val permisoConcedido =
                permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (permisoConcedido) {
                activarMiUbicacion()
                obtenerUbicacionActual()
            } else {
                mostrarMensaje(
                    "No concediste el permiso de ubicación. Puedes seleccionar el punto manualmente en el mapa."
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        RetrofitClient.init(applicationContext)

        inicializarVistas(savedInstanceState)
        configurarEventos()
        configurarMapa()
        cargarCarrito()
    }

    private fun inicializarVistas(savedInstanceState: Bundle?) {

        layoutCheckoutItems = findViewById(R.id.layoutCheckoutItems)
        tvCheckoutEmpty = findViewById(R.id.tvCheckoutEmpty)
        tvCheckoutItemsTitle = findViewById(R.id.tvCheckoutItemsTitle)

        tvCheckoutSubtotal = findViewById(R.id.tvCheckoutSubtotal)
        tvCheckoutShipping = findViewById(R.id.tvCheckoutShipping)
        tvCheckoutTotal = findViewById(R.id.tvCheckoutTotal)

        etCheckoutName = findViewById(R.id.etCheckoutName)
        etCheckoutPhone = findViewById(R.id.etCheckoutPhone)
        etCheckoutCity = findViewById(R.id.etCheckoutCity)
        etCheckoutAddress = findViewById(R.id.etCheckoutAddress)
        etCheckoutNotes = findViewById(R.id.etCheckoutNotes)

        rgPaymentMethods = findViewById(R.id.rgPaymentMethods)
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment)

        btnUseCurrentLocation =
            findViewById(R.id.btnUseCurrentLocation)

        tvSelectedLocation =
            findViewById(R.id.tvSelectedLocation)

        mapCheckout = findViewById(R.id.mapCheckout)

        // INICIALIZAR CICLO DE VIDA DEL MAPA
        mapCheckout.onCreate(savedInstanceState)

        rgPaymentMethods.check(R.id.rbCard)
    }
    private fun configurarEventos() {

        findViewById<ImageButton>(R.id.btnBackCheckout)
            .setOnClickListener {

                val intent = Intent(
                    this,
                    ActivityCart::class.java
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

                startActivity(intent)
                finish()
            }

        btnConfirmPayment.setOnClickListener {
            confirmarDatosCheckout()
        }

        btnUseCurrentLocation.setOnClickListener {
            solicitarPermisoUbicacion()
        }
    }

    private fun configurarMapa() {
        mapCheckout.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {

        googleMap = map

        googleMap?.apply {

            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = false

            setOnMapClickListener { latLng ->
                seleccionarUbicacion(latLng)
            }

            setOnMarkerDragListener(
                object : GoogleMap.OnMarkerDragListener {

                    override fun onMarkerDragStart(marker: Marker) {
                        // El usuario comenzó a mover el marcador.
                    }

                    override fun onMarkerDrag(marker: Marker) {
                        // El marcador se está moviendo.
                    }

                    override fun onMarkerDragEnd(marker: Marker) {
                        seleccionarUbicacion(marker.position)
                    }
                }
            )
        }

        if (tienePermisoUbicacion()) {
            activarMiUbicacion()
        }
    }

    private fun solicitarPermisoUbicacion() {

        if (tienePermisoUbicacion()) {
            activarMiUbicacion()
            obtenerUbicacionActual()
        } else {
            permisoUbicacionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun tienePermisoUbicacion(): Boolean {

        val permisoPreciso =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return permisoPreciso || permisoAproximado
    }

    private fun activarMiUbicacion() {

        if (!tienePermisoUbicacion()) return

        try {
            googleMap?.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            mostrarMensaje("No se pudo acceder a la ubicación.")
        }
    }

    private fun obtenerUbicacionActual() {

        if (!tienePermisoUbicacion()) {
            mostrarMensaje("Debes conceder el permiso de ubicación.")
            return
        }

        val mapa = googleMap

        if (mapa == null) {
            mostrarMensaje("El mapa todavía se está cargando.")
            return
        }

        btnUseCurrentLocation.isEnabled = false

        cancellationTokenSource?.cancel()
        cancellationTokenSource = CancellationTokenSource()

        try {

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource!!.token
            ).addOnSuccessListener { location ->

                btnUseCurrentLocation.isEnabled = true

                if (isFinishing || isDestroyed) return@addOnSuccessListener

                if (location != null) {

                    val latLng = LatLng(
                        location.latitude,
                        location.longitude
                    )

                    seleccionarUbicacion(latLng)

                } else {

                    mostrarMensaje(
                        "No fue posible obtener tu ubicación. Comprueba que la ubicación del dispositivo esté activada."
                    )
                }

            }.addOnFailureListener { error ->

                btnUseCurrentLocation.isEnabled = true

                if (!isFinishing && !isDestroyed) {
                    mostrarMensaje(
                        "Error al obtener la ubicación: ${error.message}"
                    )
                }
            }

        } catch (e: SecurityException) {

            btnUseCurrentLocation.isEnabled = true
            mostrarMensaje("No tienes permiso para acceder a la ubicación.")
        }
    }
    private fun seleccionarUbicacion(latLng: LatLng) {

        val mapa = googleMap ?: return

        ubicacionSeleccionada = latLng

        marcadorSeleccionado?.remove()

        marcadorSeleccionado = mapa.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Dirección de entrega")
                .draggable(true)
        )

        mapa.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 17f)
        )

        tvSelectedLocation.text =
            "Ubicación seleccionada: %.6f, %.6f".format(
                Locale.US,
                latLng.latitude,
                latLng.longitude
            )

        obtenerDireccionDesdeCoordenadas(latLng)
    }

    private fun obtenerDireccionDesdeCoordenadas(latLng: LatLng) {

        geocoderExecutor.execute {

            try {

                val geocoder = Geocoder(
                    this,
                    Locale.getDefault()
                )

                @Suppress("DEPRECATION")
                val direcciones = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
                )

                val direccionEncontrada = direcciones?.firstOrNull()

                val direccionCompleta =
                    direccionEncontrada?.getAddressLine(0)

                val ciudad =
                    direccionEncontrada?.locality
                        ?: direccionEncontrada?.subAdminArea
                        ?: direccionEncontrada?.adminArea

                runOnUiThread {

                    if (isFinishing || isDestroyed) return@runOnUiThread

                    // Evitar aplicar el resultado de una ubicación anterior.
                    if (ubicacionSeleccionada != latLng) return@runOnUiThread

                    if (!direccionCompleta.isNullOrBlank()) {

                        etCheckoutAddress.setText(direccionCompleta)

                        tvSelectedLocation.text =
                            "Ubicación seleccionada: $direccionCompleta"
                    }

                    if (!ciudad.isNullOrBlank()) {
                        etCheckoutCity.setText(ciudad)
                    }

                    if (direccionCompleta.isNullOrBlank()) {

                        tvSelectedLocation.text =
                            "Punto seleccionado. Completa manualmente la dirección de entrega."
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {

                    if (!isFinishing && !isDestroyed &&
                        ubicacionSeleccionada == latLng
                    ) {
                        tvSelectedLocation.text =
                            "Punto seleccionado. No se pudo obtener la dirección automáticamente; puedes escribirla manualmente."
                    }
                }
            }
        }
    }

    private fun cargarCarrito() {

        if (cargandoCarrito) return

        cargandoCarrito = true
        btnConfirmPayment.isEnabled = false

        RetrofitClient.instance.obtenerCarrito()
            .enqueue(object : Callback<CarritoResponse> {

                override fun onResponse(
                    call: Call<CarritoResponse>,
                    response: Response<CarritoResponse>
                ) {

                    cargandoCarrito = false

                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful && response.body() != null) {

                        carritoActual = response.body()
                        mostrarCarrito(carritoActual!!)

                    } else {

                        btnConfirmPayment.isEnabled = false

                        Toast.makeText(
                            this@ActivityCheckout,
                            "No fue posible cargar el carrito.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<CarritoResponse>,
                    t: Throwable
                ) {

                    cargandoCarrito = false

                    if (isFinishing || isDestroyed) return

                    btnConfirmPayment.isEnabled = false

                    Toast.makeText(
                        this@ActivityCheckout,
                        "Error de conexión: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun mostrarCarrito(carrito: CarritoResponse) {

        layoutCheckoutItems.removeAllViews()

        if (carrito.detalles.isEmpty()) {

            tvCheckoutEmpty.visibility = View.VISIBLE
            tvCheckoutItemsTitle.text = "Tu pedido"

            tvCheckoutSubtotal.text = formatoMoneda.format(0)
            tvCheckoutShipping.text = "Por calcular"
            tvCheckoutTotal.text = formatoMoneda.format(0)

            btnConfirmPayment.isEnabled = false

            return
        }

        tvCheckoutEmpty.visibility = View.GONE

        val cantidadProductos = carrito.detalles.sumOf {
            it.cantidad
        }

        tvCheckoutItemsTitle.text =
            "Tu pedido ($cantidadProductos unidades)"

        carrito.detalles.forEachIndexed { index, detalle ->

            agregarProducto(detalle)

            if (index < carrito.detalles.lastIndex) {

                val divider = View(this).apply {

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                    ).apply {
                        topMargin = dp(10)
                        bottomMargin = dp(10)
                    }

                    setBackgroundColor(
                        android.graphics.Color.parseColor("#F0EAE1")
                    )
                }

                layoutCheckoutItems.addView(divider)
            }
        }

        val subtotal = carrito.total

        tvCheckoutSubtotal.text = formatoMoneda.format(subtotal)

        tvCheckoutShipping.text = "Por calcular"

        tvCheckoutTotal.text = formatoMoneda.format(subtotal)

        btnConfirmPayment.isEnabled = true
    }

    private fun agregarProducto(detalle: DetalleCarritoResponse) {

        val producto = detalle.producto

        val contenedor = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val imagen = ImageView(this).apply {

            layoutParams = LinearLayout.LayoutParams(
                dp(76),
                dp(76)
            )

            scaleType = ImageView.ScaleType.CENTER_CROP

            setBackgroundColor(
                android.graphics.Color.parseColor("#F4EFE6")
            )
        }

        contenedor.addView(imagen)

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
            textSize = 13f

            setTextColor(
                android.graphics.Color.parseColor("#2C221E")
            )

            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val cantidad = TextView(this).apply {

            text = "Cantidad: ${detalle.cantidad}"
            textSize = 12f

            setTextColor(
                android.graphics.Color.parseColor("#8C7D75")
            )

            setPadding(0, dp(4), 0, 0)
        }

        val precioUnitario = TextView(this).apply {

            text = "Precio unitario: ${formatoMoneda.format(producto.precio)}"
            textSize = 11f

            setTextColor(
                android.graphics.Color.parseColor("#7A6F68")
            )

            setPadding(0, dp(3), 0, 0)
        }

        informacion.addView(nombre)
        informacion.addView(cantidad)
        informacion.addView(precioUnitario)

        contenedor.addView(informacion)

        val subtotal = TextView(this).apply {

            text = formatoMoneda.format(detalle.subtotal)
            textSize = 12f

            setTextColor(
                android.graphics.Color.parseColor("#2C221E")
            )

            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        contenedor.addView(subtotal)

        layoutCheckoutItems.addView(contenedor)

        if (producto.imagenes.isNotEmpty()) {
            cargarImagen(producto.imagenes.first(), imagen)
        }
    }

    private fun cargarImagen(ruta: String, imageView: ImageView) {

        imageExecutor.execute {

            try {

                val url = URL(RetrofitClient.construirUrl(ruta))

                val bitmap = url.openStream().use { stream ->
                    BitmapFactory.decodeStream(stream)
                }

                if (bitmap != null) {

                    runOnUiThread {

                        if (!isFinishing && !isDestroyed) {
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                }

            } catch (e: Exception) {

                android.util.Log.e(
                    "Checkout",
                    "Error cargando imagen: ${e.message}"
                )
            }
        }
    }

    private fun confirmarDatosCheckout() {

        if (creandoOrden) return
        val idOrdenPendiente = ordenPendientePago

        if (idOrdenPendiente != null) {
            recuperarPagoWompi(idOrdenPendiente)
            return
        }

        val carrito = carritoActual

        if (carrito == null || carrito.detalles.isEmpty()) {
            mostrarMensaje("No tienes productos para comprar.")
            return
        }

        val nombre = etCheckoutName.text.toString().trim()
        val telefono = etCheckoutPhone.text.toString().trim()
        val ciudad = etCheckoutCity.text.toString().trim()
        val direccion = etCheckoutAddress.text.toString().trim()
        val notas = etCheckoutNotes.text.toString().trim()

        if (nombre.isEmpty()) {
            etCheckoutName.error = "Ingresa el nombre del destinatario"
            etCheckoutName.requestFocus()
            return
        }

        if (telefono.length < 7) {
            etCheckoutPhone.error = "Ingresa un teléfono válido"
            etCheckoutPhone.requestFocus()
            return
        }

        if (ciudad.isEmpty()) {
            etCheckoutCity.error = "Ingresa la ciudad"
            etCheckoutCity.requestFocus()
            return
        }

        if (direccion.length < 5) {
            etCheckoutAddress.error = "Ingresa una dirección válida"
            etCheckoutAddress.requestFocus()
            return
        }

        val metodoPago = when (rgPaymentMethods.checkedRadioButtonId) {
            R.id.rbCard -> "TARJETA"
            R.id.rbPse -> "PSE"
            R.id.rbCash -> "CONTRA_ENTREGA"

            else -> {
                mostrarMensaje("Selecciona un método de pago.")
                return
            }
        }

        val solicitud = CrearOrdenRequest(
            nombreDestinatario = nombre,
            telefonoDestinatario = telefono,
            ciudadEntrega = ciudad,
            direccionEntrega = direccion,
            notasEntrega = notas.ifBlank { null },
            metodoPago = metodoPago,
            latitud = ubicacionSeleccionada?.latitude,
            longitud = ubicacionSeleccionada?.longitude
        )

        crearOrdenEnBackend(
            solicitud = solicitud,
            carrito = carrito,
            nombre = nombre,
            telefono = telefono,
            ciudad = ciudad,
            direccion = direccion,
            metodoPago = metodoPago
        )
    }

    private fun crearOrdenEnBackend(
        solicitud: CrearOrdenRequest,
        carrito: CarritoResponse,
        nombre: String,
        telefono: String,
        ciudad: String,
        direccion: String,
        metodoPago: String
    ) {

        if (creandoOrden) return

        creandoOrden = true
        btnConfirmPayment.isEnabled = false
        btnConfirmPayment.text = "Creando orden..."

        RetrofitClient.instance.crearOrden(solicitud)
            .enqueue(object : Callback<OrdenResponse> {

                override fun onResponse(
                    call: Call<OrdenResponse>,
                    response: Response<OrdenResponse>
                ) {

                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {

                        val orden = response.body()

                        if (orden == null || orden.idOrden <= 0) {

                            mostrarMensaje(
                                "No se recibió una confirmación válida. Verifica tu historial de órdenes antes de volver a intentar."
                            )

                            btnConfirmPayment.text = "Verificar orden"
                            return
                        }

                        if (metodoPago == "CONTRA_ENTREGA") {

                            creandoOrden = false

                            abrirPantallaConfirmacion(
                                orden = orden,
                                carrito = carrito,
                                nombre = nombre,
                                telefono = telefono,
                                ciudad = ciudad,
                                direccion = direccion,
                                metodoPago = metodoPago
                            )

                        } else {
                            ordenPendientePago = orden.idOrden

                            recuperarPagoWompi(
                                idOrden = orden.idOrden
                            )
                        }

                    } else {

                        val codigo = response.code()

                        if (codigo in 400..499) {

                            creandoOrden = false
                            btnConfirmPayment.isEnabled = true
                            btnConfirmPayment.text = "Confirmar compra"

                            mostrarMensaje(
                                "No se pudo crear la orden. Verifica los datos y el estado de tu carrito. Código: $codigo"
                            )

                        } else {

                            mostrarMensaje(
                                "El servidor presentó un error. No vuelvas a enviar la compra hasta verificar si la orden fue creada."
                            )

                            btnConfirmPayment.text = "Verificar orden"
                        }
                    }
                }

                override fun onFailure(
                    call: Call<OrdenResponse>,
                    t: Throwable
                ) {

                    if (isFinishing || isDestroyed) return
                    mostrarMensaje(
                        "No se pudo confirmar la respuesta del servidor. Verifica tu historial de órdenes antes de intentar nuevamente."
                    )

                    btnConfirmPayment.text = "Verificar orden"
                }
            })
    }

    private fun recuperarPagoWompi(idOrden: Int) {
        btnConfirmPayment.isEnabled = false
        btnConfirmPayment.text = "Consultando pago..."

        RetrofitClient.instance.obtenerPagoWompiPendiente(idOrden)
            .enqueue(object : Callback<PagoWompiResponse> {

                override fun onResponse(
                    call: Call<PagoWompiResponse>,
                    response: Response<PagoWompiResponse>
                ) {

                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {

                        val pago = response.body()

                        if (pago == null || pago.idOrden != idOrden) {

                            finalizarErrorPago(
                                "El backend no devolvió datos válidos del pago."
                            )
                            return
                        }

                        abrirCheckoutWompi(pago)

                    } else if (
                        response.code() == 404 &&
                        esIntentoPagoInexistente(
                            response.errorBody()?.string()
                        )
                    ) {
                        iniciarPagoWompi(idOrden)

                    } else {

                        finalizarErrorPago(
                            "No se pudo recuperar el intento de pago. Código: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<PagoWompiResponse>,
                    t: Throwable
                ) {

                    if (isFinishing || isDestroyed) return

                    finalizarErrorPago(
                        "Error consultando el pago: ${t.message}"
                    )
                }
            })
    }
    private fun esIntentoPagoInexistente(error: String?): Boolean {

        if (error.isNullOrBlank()) return false

        return try {

            val detalle = org.json.JSONObject(error)
                .optString("detail")

            detalle == "No existe un intento de pago pendiente"

        } catch (e: Exception) {
            false
        }
    }
    private fun iniciarPagoWompi(idOrden: Int) {

        btnConfirmPayment.isEnabled = false
        btnConfirmPayment.text = "Iniciando pago..."

        RetrofitClient.instance.iniciarPagoWompi(
            IniciarPagoRequest(idOrden = idOrden)
        ).enqueue(object : Callback<PagoWompiResponse> {

            override fun onResponse(
                call: Call<PagoWompiResponse>,
                response: Response<PagoWompiResponse>
            ) {

                if (isFinishing || isDestroyed) return

                if (response.isSuccessful) {

                    val pago = response.body()

                    if (pago == null || pago.idOrden != idOrden) {

                        finalizarErrorPago(
                            "No se recibieron datos válidos del intento de pago."
                        )
                        return
                    }

                    abrirCheckoutWompi(pago)

                } else {

                    finalizarErrorPago(
                        "No fue posible iniciar el pago. Código: ${response.code()}"
                    )
                }
            }

            override fun onFailure(
                call: Call<PagoWompiResponse>,
                t: Throwable
            ) {

                if (isFinishing || isDestroyed) return

                finalizarErrorPago(
                    "Error iniciando el pago: ${t.message}"
                )
            }
        })
    }

    private fun abrirCheckoutWompi(pago: PagoWompiResponse) {

        if (pago.publicKey.isBlank() ||
            pago.reference.isBlank() ||
            pago.signature.integrity.isBlank() ||
            pago.amountInCents <= 0 ||
            pago.currency != "COP"
        ) {

            finalizarErrorPago(
                "Los datos recibidos para el pago están incompletos o no son válidos."
            )
            return
        }

        try {

            val checkoutUri = android.net.Uri
                .parse("https://checkout.wompi.co/p/")
                .buildUpon()
                .appendQueryParameter("public-key", pago.publicKey)
                .appendQueryParameter("currency", pago.currency)
                .appendQueryParameter(
                    "amount-in-cents",
                    pago.amountInCents.toString()
                )
                .appendQueryParameter("reference", pago.reference)
                .appendQueryParameter(
                    "signature:integrity",
                    pago.signature.integrity
                )
                .build()

            val intent = Intent(
                Intent.ACTION_VIEW,
                checkoutUri
            )

            if (intent.resolveActivity(packageManager) != null) {

                creandoOrden = false
                btnConfirmPayment.isEnabled = true
                btnConfirmPayment.text = "Volver a abrir pago"

                startActivity(intent)

            } else {

                finalizarErrorPago(
                    "No hay un navegador disponible para abrir Wompi."
                )
            }

        } catch (e: Exception) {

            finalizarErrorPago(
                "No se pudo abrir el checkout de Wompi: ${e.message}"
            )
        }
    }
    private fun finalizarErrorPago(mensaje: String) {

        creandoOrden = false

        btnConfirmPayment.isEnabled =
            ordenPendientePago != null

        btnConfirmPayment.text =
            if (ordenPendientePago != null) {
                "Reintentar pago"
            } else {
                "Confirmar compra"
            }

        mostrarMensaje(mensaje)
    }

    private fun abrirPantallaConfirmacion(
        orden: OrdenResponse,
        carrito: CarritoResponse,
        nombre: String,
        telefono: String,
        ciudad: String,
        direccion: String,
        metodoPago: String
    ) {

        val nombres = ArrayList<String>()
        val cantidades = ArrayList<String>()
        val subtotales = ArrayList<String>()

        // Usamos la copia del carrito porque el backend actualmente
        // puede devolver Detalles vacío en la respuesta de creación.
        carrito.detalles.forEach { detalle ->

            nombres.add(detalle.producto.nombre)
            cantidades.add(detalle.cantidad.toString())
            subtotales.add(detalle.subtotal.toString())
        }

        val intent = Intent(
            this,
            ActivityPaymentConfirmation::class.java
        ).apply {

            putExtra("ID_ORDEN", orden.idOrden)
            putExtra("METODO_PAGO", orden.metodoPago)
            putExtra("ESTADO_PAGO", orden.estadoPago)

            putExtra("NOMBRE_DESTINATARIO", nombre)
            putExtra("TELEFONO_DESTINATARIO", telefono)
            putExtra("CIUDAD_ENTREGA", ciudad)
            putExtra("DIRECCION_ENTREGA", direccion)

            // Se utiliza el total confirmado por FastAPI.
            putExtra("TOTAL_COMPRA", orden.total.toDouble())

            putStringArrayListExtra("PRODUCT_NAMES", nombres)
            putStringArrayListExtra("PRODUCT_QUANTITIES", cantidades)
            putStringArrayListExtra("PRODUCT_SUBTOTALS", subtotales)

            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        startActivity(intent)
        finish()
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

    // CICLO DE VIDA DE GOOGLE MAPS
    override fun onStart() {
        super.onStart()
        if (::mapCheckout.isInitialized) {
            mapCheckout.onStart()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mapCheckout.isInitialized) {
            mapCheckout.onResume()
        }
    }

    override fun onPause() {
        if (::mapCheckout.isInitialized) {
            mapCheckout.onPause()
        }
        super.onPause()
    }

    override fun onStop() {
        if (::mapCheckout.isInitialized) {
            mapCheckout.onStop()
        }
        super.onStop()
    }
}
