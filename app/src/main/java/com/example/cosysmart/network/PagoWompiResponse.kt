
package com.example.cosysmart.network

import com.google.gson.annotations.SerializedName

data class FirmaWompiResponse(
    @SerializedName("integrity")
    val integrity: String
)

data class PagoWompiResponse(
    @SerializedName("mensaje")
    val mensaje: String,

    @SerializedName("IdPago")
    val idPago: Int,

    @SerializedName("IdOrden")
    val idOrden: Int,

    @SerializedName("public_key")
    val publicKey: String,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("amount_in_cents")
    val amountInCents: Long,

    @SerializedName("reference")
    val reference: String,

    @SerializedName("signature")
    val signature: FirmaWompiResponse,

    @SerializedName("payment_method")
    val paymentMethod: String
)
data class IniciarPagoRequest(
    @SerializedName("IdOrden")
    val idOrden: Int
)

data class EstadoPagoWompiResponse(

    @SerializedName("IdOrden")
    val idOrden: Int,

    @SerializedName("IdPago")
    val idPago: Int,

    @SerializedName("EstadoPago")
    val estadoPago: String,

    @SerializedName("EstadoPagoOrden")
    val estadoPagoOrden: String,

    @SerializedName("EstadoOrden")
    val estadoOrden: String,

    @SerializedName("MetodoPago")
    val metodoPago: String,

    @SerializedName("MontoCentavos")
    val montoCentavos: Long,

    @SerializedName("IdTransaccionWompi")
    val idTransaccionWompi: String?,

    @SerializedName("FechaActualizacion")
    val fechaActualizacion: String?
)
