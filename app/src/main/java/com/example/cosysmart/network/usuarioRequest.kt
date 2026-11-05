package com.example.cosysmart.network

data class UsuarioRequest(
    val Nombres: String,
    val Apellidos: String,
    val Correo: String,
    val Telefono: String?,
    val Contrasena: String,
    val FotoPerfil: String?
)
data class UsuarioResponse(
    val IdUsuario: Int?,
    val Nombres: String,
    val Correo: String,
    val Mensaje: String?
)