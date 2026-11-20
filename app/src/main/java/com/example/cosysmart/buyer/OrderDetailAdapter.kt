
package com.example.cosysmart.buyer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cosysmart.R
import com.example.cosysmart.network.DetalleOrdenResponse
import java.text.NumberFormat
import java.util.Locale

// Activity para ver el detalle de un pedido

class OrderDetailAdapter(
    private var detalles: List<DetalleOrdenResponse> = emptyList()
) : RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder>() {

    class OrderDetailViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvProductName: TextView =
            itemView.findViewById(R.id.tvDetailProductName)

        val tvProductQuantity: TextView =
            itemView.findViewById(R.id.tvDetailProductQuantity)

        val tvProductPrice: TextView =
            itemView.findViewById(R.id.tvDetailProductPrice)

        val tvProductSubtotal: TextView =
            itemView.findViewById(R.id.tvDetailProductSubtotal)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderDetailViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)

        return OrderDetailViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: OrderDetailViewHolder,
        position: Int
    ) {

        val detalle = detalles[position]

        holder.tvProductName.text = detalle.nombreProducto
        holder.tvProductQuantity.text = "Cantidad: ${detalle.cantidad}"
        holder.tvProductPrice.text =
            "Precio unitario: ${formatearMoneda(detalle.precioUnitario)}"

        holder.tvProductSubtotal.text =
            "Subtotal: ${formatearMoneda(detalle.subtotal)}"
    }

    override fun getItemCount(): Int {
        return detalles.size
    }

    fun actualizarDetalles(nuevaLista: List<DetalleOrdenResponse>) {
        detalles = nuevaLista
        notifyDataSetChanged()
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
