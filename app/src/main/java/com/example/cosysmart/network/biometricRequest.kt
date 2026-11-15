
package com.example.cosysmart.network

import com.google.gson.annotations.SerializedName

data class RegisterDeviceRequest(
    @SerializedName("NombreDispositivo")
    val NombreDispositivo: String,

    @SerializedName("ClavePublica")
    val ClavePublica: String,

    @SerializedName("Algoritmo")
    val Algoritmo: String = "ECDSA-P256-SHA256"
)

data class RegisterDeviceResponse(
    @SerializedName("IdDispositivo")
    val IdDispositivo: Int,

    @SerializedName("IdUsuario")
    val IdUsuario: Int,

    @SerializedName("NombreDispositivo")
    val NombreDispositivo: String,

    @SerializedName("IdCredencial")
    val IdCredencial: String,

    @SerializedName("Algoritmo")
    val Algoritmo: String,

    @SerializedName("BiometriaHabilitada")
    val BiometriaHabilitada: Boolean,

    @SerializedName("Estado")
    val Estado: String,

    @SerializedName("FechaVinculacion")
    val FechaVinculacion: String? = null,

    @SerializedName("UltimoAcceso")
    val UltimoAcceso: String? = null
)

data class ChallengeRequest(
    @SerializedName("Correo")
    val Correo: String,

    @SerializedName("IdCredencial")
    val IdCredencial: String
)

data class ChallengeResponse(
    @SerializedName("ChallengeId")
    val ChallengeId: String,

    @SerializedName("Nonce")
    val Nonce: String,

    @SerializedName("FechaExpiracion")
    val FechaExpiracion: String
)

data class VerifyBiometricRequest(
    @SerializedName("Correo")
    val Correo: String,

    @SerializedName("IdCredencial")
    val IdCredencial: String,

    @SerializedName("ChallengeId")
    val ChallengeId: String,

    @SerializedName("Nonce")
    val Nonce: String,

    @SerializedName("Firma")
    val Firma: String
)

data class BiometriaLoginResponse(
    @SerializedName("mensaje")
    val mensaje: String,

    @SerializedName("access_token")
    val access_token: String?,

    @SerializedName("token_type")
    val token_type: String?,

    @SerializedName("usuario")
    val usuario: UsuarioLoginRespuesta
)
