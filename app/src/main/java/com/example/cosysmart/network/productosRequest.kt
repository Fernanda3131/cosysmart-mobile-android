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