
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
    val access_token: String?,
    val mensaje: String,
    val token_type: String,
    val usuario: UsuarioLoginRespuesta,
    val rol: String?
)

data class UsuarioLoginRespuesta(
    val IdUsuario: Int,
    val Nombres: String,
    val Apellidos: String,
    val Correo: String,
    val FotoPerfil: String?,
    val Estado: String,
    val Rol: String
)

data class UsuarioItem(
    val nombre: String,
    val correo: String,
    val rol: String,
    val estado: String
)
data class SolicitudRecuperacionRequest(
    val correo: String
)

data class VerificarCodigoRequest(
    val correo: String,
    val codigo: String
)

data class RestablecerContrasenaRequest(
    val token: String,
    val nueva_contrasena: String
)

data class RecuperacionResponse(
    val mensaje: String
)

data class VerificarCodigoResponse(
    val mensaje: String,
    val token: String
)