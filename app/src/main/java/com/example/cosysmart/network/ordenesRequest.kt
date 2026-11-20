package com.example.cosysmart.network

import com.google.gson.annotations.SerializedName

data class CrearOrdenRequest(
    @SerializedName("NombreDestinatario")
    val nombreDestinatario: String,

    @SerializedName("TelefonoDestinatario")
    val telefonoDestinatario: String,

    @SerializedName("CiudadEntrega")
    val ciudadEntrega: String,

    @SerializedName("DireccionEntrega")
    val direccionEntrega: String,

    @SerializedName("NotasEntrega")
    val notasEntrega: String? = null,

    @SerializedName("MetodoPago")
    val metodoPago: String,

    @SerializedName("Latitud")
    val latitud: Double? = null,

    @SerializedName("Longitud")
    val longitud: Double? = null
)

data class OrdenResponse(
    @SerializedName("IdOrden")
    val idOrden: Int,

    @SerializedName("IdUsuario")
    val idUsuario: Int,

    @SerializedName("FechaOrden")
    val fechaOrden: String,

    @SerializedName("EstadoOrden")
    val estadoOrden: String,

    @SerializedName("EstadoPago")
    val estadoPago: String,

    @SerializedName("MetodoPago")
    val metodoPago: String,

    @SerializedName("Subtotal")
    val subtotal: Double,

    @SerializedName("Total")
    val total: Double,

    @SerializedName("NombreDestinatario")
    val nombreDestinatario: String,

    @SerializedName("TelefonoDestinatario")
    val telefonoDestinatario: String,

    @SerializedName("CiudadEntrega")
    val ciudadEntrega: String,

    @SerializedName("DireccionEntrega")
    val direccionEntrega: String,

    @SerializedName("NotasEntrega")
    val notasEntrega: String?,

    @SerializedName("Latitud")
    val latitud: Double?,

    @SerializedName("Longitud")
    val longitud: Double?,

    @SerializedName("Detalles")
    val detalles: List<DetalleOrdenResponse> = emptyList()
)

data class DetalleOrdenResponse(
    @SerializedName("IdDetalleOrden")
    val idDetalleOrden: Int,

    @SerializedName("IdProducto")
    val idProducto: Int,

    @SerializedName("NombreProducto")
    val nombreProducto: String,

    @SerializedName("PrecioUnitario")
    val precioUnitario: Double,

    @SerializedName("Cantidad")
    val cantidad: Int,

    @SerializedName("Subtotal")
    val subtotal: Double
)

data class CancelarOrdenResponse(
    @SerializedName("mensaje")
    val mensaje: String,

    @SerializedName("IdOrden")
    val idOrden: Int,

    @SerializedName("EstadoOrden")
    val estadoOrden: String,

    @SerializedName("EstadoPago")
    val estadoPago: String,

    @SerializedName("productosDevueltos")
    val productosDevueltos: Int
)

data class OrdenDetalleResponse(

    @SerializedName("IdOrden")
    val idOrden: Int,

    @SerializedName("IdUsuario")
    val idUsuario: Int,

    @SerializedName("FechaOrden")
    val fechaOrden: String,

    @SerializedName("EstadoOrden")
    val estadoOrden: String,

    @SerializedName("EstadoPago")
    val estadoPago: String,

    @SerializedName("MetodoPago")
    val metodoPago: String,

    @SerializedName("Subtotal")
    val subtotal: Double,

    @SerializedName("Total")
    val total: Double,

    @SerializedName("NombreDestinatario")
    val nombreDestinatario: String,

    @SerializedName("TelefonoDestinatario")
    val telefonoDestinatario: String,

    @SerializedName("CiudadEntrega")
    val ciudadEntrega: String,

    @SerializedName("DireccionEntrega")
    val direccionEntrega: String,

    @SerializedName("NotasEntrega")
    val notasEntrega: String?,

    @SerializedName("Latitud")
    val latitud: Double?,

    @SerializedName("Longitud")
    val longitud: Double?,

    @SerializedName("Detalles")
    val detalles: List<DetalleOrdenResponse> = emptyList()
)