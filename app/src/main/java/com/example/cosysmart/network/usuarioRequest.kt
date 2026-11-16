
package com.example.cosysmart.network

// ==============================
// REGISTRO
// ==============================

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

// ==============================
// LOGIN
// ==============================

data class LoginRequest(
    val Correo: String,
    val Contrasena: String
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

data class LoginResponse(
    val access_token: String?,
    val mensaje: String,
    val token_type: String,
    val usuario: UsuarioLoginRespuesta,
    val rol: String?
)

// ==============================
// USUARIO
// ==============================

data class UsuarioItem(
    val nombre: String,
    val correo: String,
    val rol: String,
    val estado: String
)

data class UsuarioActualResponse(
    val IdUsuario: Int,
    val Nombres: String,
    val Apellidos: String,
    val Correo: String,
    val FotoPerfil: String?,
    val Estado: String,
    val Rol: String
)

data class FotoPerfilResponse(
    val mensaje: String,
    val FotoPerfil: String
)

// ==============================
// RECUPERACIÓN DE CONTRASEÑA
// ==============================

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

// ==============================
// ADMINISTRACIÓN DE USUARIOS
// ==============================

data class UsuarioAdminResponse(
    val IdUsuario: Int,
    val Nombres: String,
    val Apellidos: String,
    val Correo: String,
    val Telefono: String? = null,
    val FotoPerfil: String? = null,
    val FechaRegistro: String,
    val UltimoAcceso: String? = null,
    val Estado: String,
    val Roles: List<String> = emptyList()
)

data class RolAdminResponse(
    val IdRol: Int,
    val Nombre: String
)

// CREAR USUARIO

data class UsuarioAdminCrearRequest(
    val Nombres: String,
    val Apellidos: String,
    val Correo: String,
    val Telefono: String?,
    val Contrasena: String,
    val Roles: List<Int>
)

// ACTUALIZAR USUARIO

data class UsuarioAdminActualizarRequest(
    val Nombres: String,
    val Apellidos: String,
    val Correo: String,
    val Telefono: String?,
    val Contrasena: String?,
    val Roles: List<Int>
)

// RESPUESTA AL DESACTIVAR

data class UsuarioAdminMensajeResponse(
    val IdUsuario: Int,
    val Mensaje: String
)
