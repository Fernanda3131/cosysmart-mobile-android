
package com.example.cosysmart.payments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.home.ActivityHome
import com.example.cosysmart.network.EstadoPagoWompiResponse
import com.example.cosysmart.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

// Activity de confirmación de pago

class ActivityPaymentConfirmation : AppCompatActivity() {

    private lateinit var tvOrderNumber: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var badgeStatus: TextView

    private lateinit var tvDeliveryRecipient: TextView
    private lateinit var tvDeliveryAddress: TextView
    private lateinit var tvDeliveryContact: TextView

    private lateinit var layoutOrderItems: LinearLayout
    private lateinit var tvOrderTotal: TextView

    private var idOrden: Int = -1
    private var metodoPago: String = ""
    private var consultaEnCurso = false

    private val formatoMoneda: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_confirmation)

        inicializarVistas()
        cargarDatosOrden()
        configurarBotones()
    }

    override fun onResume() {
        super.onResume()

        if (metodoPago == "TARJETA" || metodoPago == "PSE") {
            consultarEstadoPago()
        }
    }

    private fun inicializarVistas() {
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod)
        badgeStatus = findViewById(R.id.badgeStatus)

        tvDeliveryRecipient = findViewById(R.id.tvDeliveryRecipient)
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress)
        tvDeliveryContact = findViewById(R.id.tvDeliveryContact)

        layoutOrderItems = findViewById(R.id.layoutOrderItems)
        tvOrderTotal = findViewById(R.id.tvOrderTotal)
    }

    private fun cargarDatosOrden() {

        idOrden = intent.getIntExtra("ID_ORDEN", -1)

        tvOrderNumber.text = if (idOrden > 0) {
            "Número de orden: #$idOrden"
        } else {
            "Número de orden no disponible"
        }

        metodoPago = intent.getStringExtra("METODO_PAGO") ?: ""

        tvPaymentMethod.text = when (metodoPago) {
            "TARJETA" -> "Tarjeta"
            "PSE" -> "PSE"
            "CONTRA_ENTREGA" -> "Pago contra entrega"
            else -> "Método no disponible"
        }

        val estadoInicial = intent.getStringExtra("ESTADO_PAGO") ?: "PENDIENTE"
        actualizarEstadoVisual(estadoInicial)

        val nombre = intent.getStringExtra("NOMBRE_DESTINATARIO") ?: ""
        val ciudad = intent.getStringExtra("CIUDAD_ENTREGA") ?: ""
        val direccion = intent.getStringExtra("DIRECCION_ENTREGA") ?: ""
        val telefono = intent.getStringExtra("TELEFONO_DESTINATARIO") ?: ""

        tvDeliveryRecipient.text =
            nombre.ifBlank { "Destinatario no disponible" }

        tvDeliveryAddress.text =
            direccion.ifBlank { "Dirección no disponible" }

        tvDeliveryContact.text = buildString {
            if (telefono.isNotBlank()) {
                append("Tel: $telefono")
            }

            if (ciudad.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(ciudad)
            }
        }.ifBlank {
            "Datos de contacto no disponibles"
        }

        val total = intent.getDoubleExtra("TOTAL_COMPRA", 0.0)
        tvOrderTotal.text = formatoMoneda.format(total)

        mostrarProductos()
    }

    private fun consultarEstadoPago() {

        if (idOrden <= 0 || consultaEnCurso) {
            return
        }

        consultaEnCurso = true

        RetrofitClient.instance
            .consultarEstadoPagoWompi(idOrden)
            .enqueue(object : Callback<EstadoPagoWompiResponse> {

                override fun onResponse(
                    call: Call<EstadoPagoWompiResponse>,
                    response: Response<EstadoPagoWompiResponse>
                ) {
                    consultaEnCurso = false

                    if (!response.isSuccessful) {
                        return
                    }

                    val estado = response.body() ?: return

                    if (estado.idOrden != idOrden) {
                        return
                    }

                    // EstadoPagoOrden representa el estado registrado
                    // para la orden en el backend.
                    val estadoActual = estado.estadoPagoOrden
                        .ifBlank { estado.estadoPago }

                    actualizarEstadoVisual(estadoActual)
                }

                override fun onFailure(
                    call: Call<EstadoPagoWompiResponse>,
                    t: Throwable
                ) {
                    consultaEnCurso = false

                    // No se cambia el estado a aprobado ni rechazado
                    // cuando no es posible consultar el backend.
                }
            })
    }

    private fun actualizarEstadoVisual(estado: String) {

        val estadoNormalizado = estado.trim().uppercase(Locale.ROOT)

        when (estadoNormalizado) {

            "PAGADA", "APROBADO", "APPROVED" -> {
                badgeStatus.text = "PAGO APROBADO"
                badgeStatus.setBackgroundColor(
                    Color.parseColor("#E8EFE5")
                )
                badgeStatus.setTextColor(
                    Color.parseColor("#4E7043")
                )
            }

            "RECHAZADO", "DECLINADO", "DECLINED" -> {
                badgeStatus.text = "PAGO RECHAZADO"
                badgeStatus.setBackgroundColor(
                    Color.parseColor("#FCE8E6")
                )
                badgeStatus.setTextColor(
                    Color.parseColor("#B3261E")
                )
            }

            "CONTRA_ENTREGA" -> {
                badgeStatus.text = "PAGO CONTRA ENTREGA"
                badgeStatus.setBackgroundColor(
                    Color.parseColor("#E8EFE5")
                )
                badgeStatus.setTextColor(
                    Color.parseColor("#4E7043")
                )
            }

            else -> {
                badgeStatus.text = "PAGO PENDIENTE"
                badgeStatus.setBackgroundColor(
                    Color.parseColor("#FFF1DC")
                )
                badgeStatus.setTextColor(
                    Color.parseColor("#98651E")
                )
            }
        }
    }

    private fun mostrarProductos() {

        layoutOrderItems.removeAllViews()

        val nombres = intent.getStringArrayListExtra("PRODUCT_NAMES")
            ?: arrayListOf()

        val cantidades = intent.getStringArrayListExtra("PRODUCT_QUANTITIES")
            ?: arrayListOf()

        val subtotales = intent.getStringArrayListExtra("PRODUCT_SUBTOTALS")
            ?: arrayListOf()

        if (nombres.isEmpty()) {
            val mensaje = TextView(this).apply {
                text = "No hay detalles de productos disponibles."
                textSize = 12f
                setTextColor(Color.parseColor("#7A6F68"))
            }

            layoutOrderItems.addView(mensaje)
            return
        }

        for (i in nombres.indices) {

            val nombre = nombres[i]
            val cantidad = cantidades.getOrNull(i) ?: "1"
            val subtotal = subtotales.getOrNull(i)?.toDoubleOrNull() ?: 0.0

            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
            }

            val textoProducto = TextView(this).apply {
                text = "$nombre (x$cantidad)"
                textSize = 12f
                setTextColor(Color.parseColor("#7A6F68"))

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val textoSubtotal = TextView(this).apply {
                text = formatoMoneda.format(subtotal)
                textSize = 12f
                setTextColor(Color.parseColor("#2C221E"))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            fila.addView(textoProducto)
            fila.addView(textoSubtotal)

            layoutOrderItems.addView(fila)
        }
    }

    private fun configurarBotones() {

        val btnViewOrder =
            findViewById<MaterialButton>(R.id.btnViewOrder)

        val btnKeepShopping =
            findViewById<MaterialButton>(R.id.btnKeepShopping)

        btnViewOrder.text = "Volver al inicio"

        btnViewOrder.setOnClickListener {
            volverAlInicio()
        }

        btnKeepShopping.setOnClickListener {
            volverAlInicio()
        }
    }

    private fun volverAlInicio() {
        val intent = Intent(
            this,
            ActivityHome::class.java
        ).apply {
            putExtra("USER_ROLE", "buyer")

            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        startActivity(intent)
        finish()
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }
}
