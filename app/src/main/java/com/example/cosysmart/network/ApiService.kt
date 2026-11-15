
package com.example.cosysmart.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // REGISTRO
    @POST("usuarios/")
    fun registrarUsuario(
        @Body usuario: UsuarioRequest
    ): Call<UsuarioResponse>

    // LOGIN
    @POST("auth/login")
    fun loginUsuario(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    // BIOMETRÍA
    @POST("biometria/dispositivos")
    fun registrarDispositivo(
        @Body request: RegisterDeviceRequest
    ): Call<RegisterDeviceResponse>

    @POST("biometria/desafio")
    fun solicitarDesafio(
        @Body request: ChallengeRequest
    ): Call<ChallengeResponse>

    @POST("biometria/verificar")
    fun verificarBiometria(
        @Body request: VerifyBiometricRequest
    ): Call<BiometriaLoginResponse>

    @POST("auth/recuperar")
    fun solicitarRecuperacion(
        @Body request: SolicitudRecuperacionRequest
    ): Call<RecuperacionResponse>

    @POST("auth/verificar-codigo")
    fun verificarCodigoRecuperacion(
        @Body request: VerificarCodigoRequest
    ): Call<VerificarCodigoResponse>

    @POST("auth/restablecer-contrasena")
    fun restablecerContrasena(
        @Body request: RestablecerContrasenaRequest
    ): Call<RecuperacionResponse>
}
