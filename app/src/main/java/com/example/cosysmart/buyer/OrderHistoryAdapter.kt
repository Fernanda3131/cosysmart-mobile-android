
package com.example.cosysmart.buyer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cosysmart.R
import com.example.cosysmart.network.OrdenResponse
import java.text.NumberFormat
import java.util.Locale

// Activity para ver el historial de pedidos

class OrderHistoryAdapter(
    private var pedidos: List<OrdenResponse> = emptyList(),
    private val onPedidoClick: (Int) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    class OrderHistoryViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvOrderNumber: TextView =
            itemView.findViewById(R.id.tvOrderNumber)

        val tvOrderStatus: TextView =
            itemView.findViewById(R.id.tvOrderStatus)

        val tvOrderDate: TextView =
            itemView.findViewById(R.id.tvOrderDate)

        val tvOrderTotal: TextView =
            itemView.findViewById(R.id.tvOrderTotal)

        val tvViewOrderDetail: TextView =
            itemView.findViewById(R.id.tvViewOrderDetail)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderHistoryViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)

        return OrderHistoryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: OrderHistoryViewHolder,
        position: Int
    ) {

        val pedido = pedidos[position]

        holder.tvOrderNumber.text =
            "Pedido #${pedido.idOrden}"

        holder.tvOrderStatus.text =
            "Estado: ${obtenerEstadoLegible(pedido.estadoOrden)}"

        holder.tvOrderDate.text =
            "Fecha: ${pedido.fechaOrden}"

        holder.tvOrderTotal.text =
            "Total: ${formatearMoneda(pedido.total)}"

        holder.tvViewOrderDetail.setOnClickListener {
            onPedidoClick(pedido.idOrden)
        }

        holder.itemView.setOnClickListener {
            onPedidoClick(pedido.idOrden)
        }
    }

    override fun getItemCount(): Int {
        return pedidos.size
    }

    fun actualizarPedidos(nuevaLista: List<OrdenResponse>) {
        pedidos = nuevaLista
        notifyDataSetChanged()
    }

    private fun obtenerEstadoLegible(estado: String): String {
        return when (estado.uppercase(Locale.ROOT)) {
            "PENDIENTE" -> "Pendiente"
            "CANCELADA" -> "Cancelada"
            "ENVIADA" -> "Enviada"
            "ENTREGADA" -> "Entregada"
            "PARCIALMENTE_ENVIADA" -> "Envío parcial"
            else -> estado.replace("_", " ")
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
}
