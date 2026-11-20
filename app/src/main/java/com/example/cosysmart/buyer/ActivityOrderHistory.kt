package com.example.cosysmart.buyer

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cosysmart.R
import com.example.cosysmart.network.OrdenResponse
import com.example.cosysmart.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Activity para ver el historial de pedidos

class ActivityOrderHistory : AppCompatActivity() {

    private lateinit var rvOrderHistory: RecyclerView
    private lateinit var progressOrderHistory: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var layoutError: LinearLayout
    private lateinit var tvError: TextView
    private lateinit var adapter: OrderHistoryAdapter

    private var historialCall: Call<List<OrdenResponse>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        inicializarVistas()
        configurarLista()
        configurarEventos()

        cargarHistorial()
    }

    private fun inicializarVistas() {
        rvOrderHistory = findViewById(R.id.rvOrderHistory)
        progressOrderHistory = findViewById(R.id.progressOrderHistory)
        layoutEmpty = findViewById(R.id.layoutOrderHistoryEmpty)
        layoutError = findViewById(R.id.layoutOrderHistoryError)
        tvError = findViewById(R.id.tvOrderHistoryError)
    }

    private fun configurarLista() {
        adapter = OrderHistoryAdapter(emptyList()) { idOrden ->

            val intent = android.content.Intent(
                this,
                ActivityOrderDetail::class.java
            )

            intent.putExtra("idOrden", idOrden)

            startActivity(intent)
        }

        rvOrderHistory.layoutManager = LinearLayoutManager(this)
        rvOrderHistory.adapter = adapter
        rvOrderHistory.setHasFixedSize(true)
    }

    private fun configurarEventos() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnRetryOrderHistory
        ).setOnClickListener {
            cargarHistorial()
        }
    }

    private fun cargarHistorial() {
        mostrarCargando()

        historialCall?.cancel()

        historialCall = RetrofitClient.instance.obtenerHistorialOrdenes()

        historialCall?.enqueue(object : Callback<List<OrdenResponse>> {

            override fun onResponse(
                call: Call<List<OrdenResponse>>,
                response: Response<List<OrdenResponse>>
            ) {
                if (isFinishing || isDestroyed) return

                if (response.isSuccessful) {
                    val pedidos = response.body()

                    if (pedidos == null) {
                        mostrarError("El servidor no devolvió información.")
                        return
                    }

                    if (pedidos.isEmpty()) {
                        mostrarVacio()
                    } else {
                        adapter.actualizarPedidos(pedidos)
                        mostrarLista()
                    }

                } else {
                    mostrarError(
                        "No fue posible consultar tus pedidos. " +
                                "Código: ${response.code()}"
                    )
                }
            }

            override fun onFailure(
                call: Call<List<OrdenResponse>>,
                t: Throwable
            ) {
                if (call.isCanceled || isFinishing || isDestroyed) return

                mostrarError(
                    "No se pudo conectar con el servidor. " +
                            "Verifica tu conexión e inténtalo nuevamente."
                )
            }
        })
    }

    private fun mostrarCargando() {
        progressOrderHistory.visibility = View.VISIBLE
        rvOrderHistory.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
        layoutError.visibility = View.GONE
    }

    private fun mostrarLista() {
        progressOrderHistory.visibility = View.GONE
        rvOrderHistory.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
        layoutError.visibility = View.GONE
    }

    private fun mostrarVacio() {
        progressOrderHistory.visibility = View.GONE
        rvOrderHistory.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
        layoutError.visibility = View.GONE
    }

    private fun mostrarError(mensaje: String) {
        progressOrderHistory.visibility = View.GONE
        rvOrderHistory.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
        layoutError.visibility = View.VISIBLE
        tvError.text = mensaje
    }

    override fun onDestroy() {
        historialCall?.cancel()
        super.onDestroy()
    }
}