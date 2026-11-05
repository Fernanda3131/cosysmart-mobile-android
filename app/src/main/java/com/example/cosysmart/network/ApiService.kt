package com.example.cosysmart.network
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("usuarios/")
    fun registrarUsuario(@Body usuario: UsuarioRequest): Call<UsuarioResponse>
}