
package com.example.cosysmart.buyer

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cosysmart.R
import com.example.cosysmart.network.CancelarOrdenResponse
import com.example.cosysmart.network.OrdenResponse
import com.example.cosysmart.network.PagoWompiResponse
import com.example.cosysmart.network.RetrofitClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Activity para ver el detalle de un pedido
class ActivityOrderDetail : AppCompatActivity() {

    private lateinit var tvOrderNumber: TextView
    private lateinit var tvOrderStatus: TextView
    private lateinit var tvPaymentStatus: TextView
    private lateinit var tvOrderDate: TextView

    private lateinit var tvRecipient: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvNotes: TextView

    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvPaymentMethod: TextView

    private lateinit var rvOrderDetails: RecyclerView
    private lateinit var btnCancelOrder: Button
    private lateinit var btnContinuePayment: Button

    private lateinit var adapter: OrderDetailAdapter

    private var idOrden: Int = -1

    private var detalleCall: Call<OrdenResponse>? = null
    private var cancelarCall: Call<CancelarOrdenResponse>? = null
    private var pagoCall: Call<PagoWompiResponse>? = null

    private var checkoutAbierto = false
    private var consultandoPago = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_order_detail)

        RetrofitClient.init(applicationContext)

        idOrden = intent.getIntExtra("idOrden", -1)

        if (idOrden <= 0) {
            Toast.makeText(
                this,
                "No se recibió un pedido válido.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        inicializarVistas()
        configurarLista()
        configurarEventos()
        cargarDetalle()
    }

    private fun inicializarVistas() {

        tvOrderNumber = findViewById(R.id.tvDetailOrderNumber)
        tvOrderStatus = findViewById(R.id.tvDetailOrderStatus)
        tvPaymentStatus = findViewById(R.id.tvDetailPaymentStatus)
        tvOrderDate = findViewById(R.id.tvDetailOrderDate)

        tvRecipient = findViewById(R.id.tvDetailRecipient)
        tvPhone = findViewById(R.id.tvDetailPhone)
        tvCity = findViewById(R.id.tvDetailCity)
        tvAddress = findViewById(R.id.tvDetailAddress)
        tvNotes = findViewById(R.id.tvDetailNotes)

        tvSubtotal = findViewById(R.id.tvDetailSubtotal)
        tvTotal = findViewById(R.id.tvDetailTotal)
        tvPaymentMethod = findViewById(R.id.tvDetailPaymentMethod)

        rvOrderDetails = findViewById(R.id.rvOrderDetails)
        btnCancelOrder = findViewById(R.id.btnCancelOrder)
        btnContinuePayment = findViewById(R.id.btnContinuePayment)
    }

    private fun configurarLista() {

        adapter = OrderDetailAdapter()

        rvOrderDetails.layoutManager = LinearLayoutManager(this)
        rvOrderDetails.adapter = adapter
        rvOrderDetails.setHasFixedSize(false)
    }

    private fun configurarEventos() {

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnCancelOrder.setOnClickListener {
            confirmarCancelacion()
        }

        btnContinuePayment.setOnClickListener {
            recuperarPagoPendiente()
        }
    }

    private fun cargarDetalle() {

        btnCancelOrder.visibility = View.GONE
        btnContinuePayment.visibility = View.GONE

        detalleCall?.cancel()

        val llamada = RetrofitClient.instance.obtenerOrdenPorId(idOrden)
        detalleCall = llamada

        llamada.enqueue(object : Callback<OrdenResponse> {

            override fun onResponse(
                call: Call<OrdenResponse>,
                response: Response<OrdenResponse>
            ) {

                if (isFinishing || isDestroyed) return

                if (response.isSuccessful) {

                    val pedido = response.body()

                    if (pedido == null) {
                        Toast.makeText(
                            this@ActivityOrderDetail,
                            "El servidor no devolvió información del pedido.",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    mostrarInformacion(pedido)
                    consultarIntentoPendiente(pedido)

                } else {

                    Toast.makeText(
                        this@ActivityOrderDetail,
                        obtenerMensajeError(response),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<OrdenResponse>,
                t: Throwable
            ) {

                if (call.isCanceled || isFinishing || isDestroyed) return

                Toast.makeText(
                    this@ActivityOrderDetail,
                    "No se pudo conectar con el servidor.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun mostrarInformacion(pedido: OrdenResponse) {

        tvOrderNumber.text = "Pedido #${pedido.idOrden}"

        tvOrderStatus.text =
            "Estado: ${obtenerEstadoLegible(pedido.estadoOrden)}"

        tvPaymentStatus.text =
            "Estado del pago: ${obtenerEstadoLegible(pedido.estadoPago)}"

        tvOrderDate.text =
            "Fecha: ${formatearFecha(pedido.fechaOrden)}"

        tvRecipient.text =
            "Destinatario: ${pedido.nombreDestinatario}"

        tvPhone.text =
            "Teléfono: ${pedido.telefonoDestinatario}"

        tvCity.text =
            "Ciudad: ${pedido.ciudadEntrega}"

        tvAddress.text =
            "Dirección: ${pedido.direccionEntrega}"

        tvNotes.text =
            "Notas de entrega: ${pedido.notasEntrega ?: "Sin notas"}"

        tvSubtotal.text =
            formatearMoneda(pedido.subtotal)

        tvTotal.text =
            formatearMoneda(pedido.total)

        tvPaymentMethod.text =
            "Método de pago: ${pedido.metodoPago}"

        adapter.actualizarDetalles(pedido.detalles)

        configurarBotonCancelacion(
            pedido.estadoOrden,
            pedido.estadoPago
        )
    }
    private fun consultarIntentoPendiente(pedido: OrdenResponse) {

        val metodo = pedido.metodoPago.uppercase(Locale.ROOT)

        val esWompi =
            metodo == "TARJETA" || metodo == "PSE"

        val ordenPendiente =
            pedido.estadoOrden.equals("PENDIENTE", ignoreCase = true)

        val pagoPendiente =
            pedido.estadoPago.equals("PENDIENTE", ignoreCase = true)

        if (!esWompi || !ordenPendiente || !pagoPendiente) {
            btnContinuePayment.visibility = View.GONE
            return
        }

        if (consultandoPago) return

        consultandoPago = true
        btnContinuePayment.isEnabled = false
        btnContinuePayment.visibility = View.GONE

        pagoCall?.cancel()

        val llamada =
            RetrofitClient.instance.obtenerPagoWompiPendiente(idOrden)

        pagoCall = llamada

        llamada.enqueue(object : Callback<PagoWompiResponse> {

            override fun onResponse(
                call: Call<PagoWompiResponse>,
                response: Response<PagoWompiResponse>
            ) {

                consultandoPago = false

                if (isFinishing || isDestroyed) return

                if (response.isSuccessful) {

                    val pago = response.body()

                    val pagoValido =
                        pago != null &&
                                pago.idOrden == idOrden &&
                                pago.publicKey.isNotBlank() &&
                                pago.reference.isNotBlank() &&
                                pago.signature.integrity.isNotBlank() &&
                                pago.amountInCents > 0 &&
                                pago.currency == "COP"

                    if (pagoValido) {

                        btnContinuePayment.visibility = View.VISIBLE
                        btnContinuePayment.isEnabled = true

                    } else {

                        btnContinuePayment.visibility = View.GONE

                        Toast.makeText(
                            this@ActivityOrderDetail,
                            "Los datos del intento de pago no son válidos.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {

                    btnContinuePayment.visibility = View.GONE

                    if (response.code() != 404) {
                        Toast.makeText(
                            this@ActivityOrderDetail,
                            "No se pudo consultar el intento de pago.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(
                call: Call<PagoWompiResponse>,
                t: Throwable
            ) {

                consultandoPago = false

                if (call.isCanceled || isFinishing || isDestroyed) return

                btnContinuePayment.visibility = View.GONE

                Toast.makeText(
                    this@ActivityOrderDetail,
                    "No se pudo verificar el pago pendiente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun recuperarPagoPendiente() {

        if (consultandoPago) return

        consultandoPago = true
        btnContinuePayment.isEnabled = false
        btnContinuePayment.text = "Consultando pago..."

        pagoCall?.cancel()

        val llamada =
            RetrofitClient.instance.obtenerPagoWompiPendiente(idOrden)

        pagoCall = llamada

        llamada.enqueue(object : Callback<PagoWompiResponse> {

            override fun onResponse(
                call: Call<PagoWompiResponse>,
                response: Response<PagoWompiResponse>
            ) {

                consultandoPago = false

                if (isFinishing || isDestroyed) return

                btnContinuePayment.text = "Continuar pago"
                btnContinuePayment.isEnabled = true

                if (response.isSuccessful) {

                    val pago = response.body()

                    if (pago == null ||
                        pago.idOrden != idOrden ||
                        pago.publicKey.isBlank() ||
                        pago.reference.isBlank() ||
                        pago.signature.integrity.isBlank() ||
                        pago.amountInCents <= 0 ||
                        pago.currency != "COP"
                    ) {

                        Toast.makeText(
                            this@ActivityOrderDetail,
                            "Los datos del pago no son válidos.",
                            Toast.LENGTH_LONG
                        ).show()

                        return
                    }

                    abrirCheckoutWompi(pago)

                } else {

                    btnContinuePayment.visibility = View.GONE

                    Toast.makeText(
                        this@ActivityOrderDetail,
                        if (response.code() == 404) {
                            "Ya no existe un intento de pago pendiente para este pedido."
                        } else {
                            "No se pudo recuperar el pago. Código: ${response.code()}"
                        },
                        Toast.LENGTH_LONG
                    ).show()

                    cargarDetalle()
                }
            }

            override fun onFailure(
                call: Call<PagoWompiResponse>,
                t: Throwable
            ) {

                consultandoPago = false

                if (call.isCanceled || isFinishing || isDestroyed) return

                btnContinuePayment.text = "Continuar pago"
                btnContinuePayment.isEnabled = true

                Toast.makeText(
                    this@ActivityOrderDetail,
                    "Error consultando el pago: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
    private fun abrirCheckoutWompi(pago: PagoWompiResponse) {

        try {

            val checkoutUri = Uri
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

            val intent = Intent(Intent.ACTION_VIEW, checkoutUri)

            checkoutAbierto = true

            startActivity(intent)

        } catch (e: ActivityNotFoundException) {

            checkoutAbierto = false

            Toast.makeText(
                this,
                "No hay un navegador disponible para abrir Wompi.",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {

            checkoutAbierto = false

            Toast.makeText(
                this,
                "No se pudo abrir Wompi: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun configurarBotonCancelacion(
        estadoOrden: String,
        estadoPago: String
    ) {

        val ordenPendiente =
            estadoOrden.equals("PENDIENTE", ignoreCase = true)
        val pagoRechazado =
            estadoPago.equals("RECHAZADA", ignoreCase = true)

        btnCancelOrder.visibility =
            if (ordenPendiente && pagoRechazado) {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnCancelOrder.isEnabled = true
    }

    private fun confirmarCancelacion() {

        AlertDialog.Builder(this)
            .setTitle("Cancelar pedido")
            .setMessage(
                "¿Deseas cancelar este pedido? " +
                        "Si el servidor acepta la operación, " +
                        "los productos serán devueltos al inventario."
            )
            .setNegativeButton("Volver", null)
            .setPositiveButton(
                "Cancelar pedido",
                DialogInterface.OnClickListener { _, _ ->
                    cancelarPedido()
                }
            )
            .show()
    }

    private fun cancelarPedido() {

        btnCancelOrder.isEnabled = false

        cancelarCall?.cancel()

        val llamada =
            RetrofitClient.instance.cancelarOrden(idOrden)

        cancelarCall = llamada

        llamada.enqueue(object : Callback<CancelarOrdenResponse> {

            override fun onResponse(
                call: Call<CancelarOrdenResponse>,
                response: Response<CancelarOrdenResponse>
            ) {

                if (isFinishing || isDestroyed) return

                btnCancelOrder.isEnabled = true

                if (response.isSuccessful) {

                    val resultado = response.body()

                    Toast.makeText(
                        this@ActivityOrderDetail,
                        resultado?.mensaje
                            ?: "No se recibió confirmación de la cancelación.",
                        Toast.LENGTH_LONG
                    ).show()

                    cargarDetalle()

                } else {

                    Toast.makeText(
                        this@ActivityOrderDetail,
                        obtenerMensajeError(response),
                        Toast.LENGTH_LONG
                    ).show()

                    cargarDetalle()
                }
            }

            override fun onFailure(
                call: Call<CancelarOrdenResponse>,
                t: Throwable
            ) {

                if (call.isCanceled || isFinishing || isDestroyed) return

                btnCancelOrder.isEnabled = true

                Toast.makeText(
                    this@ActivityOrderDetail,
                    "No se pudo confirmar el resultado. Se consultará nuevamente el pedido.",
                    Toast.LENGTH_LONG
                ).show()

                cargarDetalle()
            }
        })
    }

    private fun obtenerMensajeError(response: Response<*>): String {

        return try {

            val cuerpo = response.errorBody()?.string()
            val detalle = JSONObject(cuerpo ?: "")
                .optString("detail")

            if (detalle.isNotBlank()) {
                detalle
            } else {
                "Error del servidor. Código: ${response.code()}"
            }

        } catch (e: Exception) {
            "Error del servidor. Código: ${response.code()}"
        }
    }

    private fun obtenerEstadoLegible(estado: String): String {

        return when (estado.uppercase(Locale.ROOT)) {

            "PENDIENTE" -> "Pendiente"
            "RECHAZADA" -> "Rechazada"
            "PAGADA" -> "Pagada"
            "CANCELADA" -> "Cancelada"
            "PARCIALMENTE_ENVIADA" -> "Envío parcial"
            "ENVIADA" -> "Enviada"
            "ENTREGADA" -> "Entregada"

            else -> estado
                .replace("_", " ")
                .lowercase(Locale("es", "CO"))
                .replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatearFecha(fecha: String): String {

        return try {

            val fechaHora = try {

                OffsetDateTime.parse(fecha).toLocalDateTime()

            } catch (e: Exception) {

                Instant.parse(fecha)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            }

            fechaHora.format(
                DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm",
                    Locale("es", "CO")
                )
            )

        } catch (e: Exception) {
            fecha
        }
    }

    private fun formatearMoneda(valor: Double): String {

        val formato = NumberFormat.getCurrencyInstance(
            Locale("es", "CO")
        )

        formato.maximumFractionDigits = 0
        formato.minimumFractionDigits = 0

        return formato.format(valor)
    }

    override fun onResume() {
        super.onResume()
        if (checkoutAbierto) {
            checkoutAbierto = false
            cargarDetalle()
        }
    }

    override fun onDestroy() {

        detalleCall?.cancel()
        cancelarCall?.cancel()
        pagoCall?.cancel()

        super.onDestroy()
    }
}
