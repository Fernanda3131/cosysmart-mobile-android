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
data class LoginRequest(
    val Correo: String,
    val Contrasena: String
)

data class LoginResponse(
    val mensaje: String,
    val access_token: String,
    val token_type: String,
    val usuario: UsuarioLoginRespuesta
)

data class UsuarioLoginRespuesta(
    val IdUsuario: Int,
    val Nombres: String,
    val Apellidos: String,
    val Correo: String,
    val FotoPerfil: String?,
    val Estado: String
)