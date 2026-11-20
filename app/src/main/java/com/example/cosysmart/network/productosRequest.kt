
package com.example.cosysmart.network

import com.google.gson.annotations.SerializedName

data class CategoriaResponse(
    @SerializedName("IdCategoria")
    val idCategoria: Int,

    @SerializedName("Nombre")
    val nombre: String
)

data class CrearProductoResponse(
    @SerializedName("mensaje")
    val mensaje: String,

    @SerializedName("producto")
    val producto: ProductoRegistradoResponse,

    @SerializedName("imagenes")
    val imagenes: List<String>
)

data class ProductoRegistradoResponse(
    @SerializedName("IdProducto")
    val idProducto: Int,

    @SerializedName("IdVendedor")
    val idVendedor: Int,

    @SerializedName("IdCategoria")
    val idCategoria: Int,

    @SerializedName("Nombre")
    val nombre: String,

    @SerializedName("Descripcion")
    val descripcion: String?,

    @SerializedName("Precio")
    val precio: Double,

    @SerializedName("Peso")
    val peso: Double?,

    @SerializedName("UnidadPeso")
    val unidadPeso: String?,

    @SerializedName("Alto")
    val alto: Double?,

    @SerializedName("Ancho")
    val ancho: Double?,

    @SerializedName("Profundidad")
    val profundidad: Double?,

    @SerializedName("Color")
    val color: String?,

    @SerializedName("Stock")
    val stock: Int
)

data class ProductoResponse(
    @SerializedName("IdProducto")
    val idProducto: Int,

    @SerializedName("IdVendedor")
    val idVendedor: Int,

    @SerializedName("IdCategoria")
    val idCategoria: Int,

    @SerializedName("NombreCategoria")
    val nombreCategoria: String?,

    @SerializedName("Nombre")
    val nombre: String,

    @SerializedName("Descripcion")
    val descripcion: String?,

    @SerializedName("Precio")
    val precio: Double,

    @SerializedName("Peso")
    val peso: Double?,

    @SerializedName("UnidadPeso")
    val unidadPeso: String?,

    @SerializedName("Alto")
    val alto: Double?,

    @SerializedName("Ancho")
    val ancho: Double?,

    @SerializedName("Profundidad")
    val profundidad: Double?,

    @SerializedName("Color")
    val color: String?,

    @SerializedName("Stock")
    val stock: Int,

    @SerializedName("Estado")
    val estado: Int,

    @SerializedName("imagenes")
    val imagenes: List<String>
)

data class EstadoProductoRequest(
    @SerializedName("Estado")
    val estado: Int
)


// MODELOS DEL CARRITO

data class AgregarProductoCarritoRequest(
    @SerializedName("IdProducto")
    val idProducto: Int,

    @SerializedName("Cantidad")
    val cantidad: Int
)

data class ActualizarCantidadCarritoRequest(
    @SerializedName("Cantidad")
    val cantidad: Int
)

data class ProductoCarritoResponse(
    @SerializedName("IdProducto")
    val idProducto: Int,

    @SerializedName("Nombre")
    val nombre: String,

    @SerializedName("Descripcion")
    val descripcion: String?,

    @SerializedName("Precio")
    val precio: Double,

    @SerializedName("Stock")
    val stock: Int,

    @SerializedName("Imagenes")
    val imagenes: List<String> = emptyList()
)

data class DetalleCarritoResponse(
    @SerializedName("IdDetalleCarrito")
    val idDetalleCarrito: Int,

    @SerializedName("Cantidad")
    val cantidad: Int,

    @SerializedName("Producto")
    val producto: ProductoCarritoResponse,

    @SerializedName("Subtotal")
    val subtotal: Double
)

data class CarritoResponse(
    @SerializedName("IdCarrito")
    val idCarrito: Int,

    @SerializedName("Estado")
    val estado: String?,

    @SerializedName("Detalles")
    val detalles: List<DetalleCarritoResponse> = emptyList(),

    @SerializedName("Total")
    val total: Double
)
