
package com.example.cosysmart.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("usuarios/")
    fun registrarUsuario(
        @Body usuario: UsuarioRequest
    ): Call<UsuarioResponse>

    @POST("auth/login")
    fun loginUsuario(
        @Body request: LoginRequest
    ): Call<LoginResponse>
    
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


    @GET("auth/me")
    fun obtenerUsuarioActual(): Call<UsuarioActualResponse>

    @Multipart
    @PUT("usuarios/me/foto")
    fun actualizarFotoPerfil(
        @Part foto: MultipartBody.Part
    ): Call<FotoPerfilResponse>

    @GET("categorias/")
    fun listarCategorias(): Call<List<CategoriaResponse>>

    @Multipart
    @POST("productos/")
    fun crearProducto(
        @Part("Nombre") nombre: RequestBody,
        @Part("IdCategoria") idCategoria: RequestBody,
        @Part("Precio") precio: RequestBody,
        @Part("Descripcion") descripcion: RequestBody?,
        @Part("Peso") peso: RequestBody?,
        @Part("UnidadPeso") unidadPeso: RequestBody?,
        @Part("Alto") alto: RequestBody?,
        @Part("Ancho") ancho: RequestBody?,
        @Part("Profundidad") profundidad: RequestBody?,
        @Part("Color") color: RequestBody?,
        @Part("Stock") stock: RequestBody,
        @Part fotos: List<MultipartBody.Part>
    ): Call<CrearProductoResponse>

    @GET("productos/")
    fun listarProductos(
        @Query("vista") vista: String
    ): Call<List<ProductoResponse>>

    @Multipart
    @PUT("productos/{id_producto}")
    fun actualizarProducto(
        @Path("id_producto") idProducto: Int,
        @Part("Nombre") nombre: RequestBody?,
        @Part("IdCategoria") idCategoria: RequestBody?,
        @Part("Precio") precio: RequestBody?,
        @Part("Descripcion") descripcion: RequestBody?,
        @Part("Peso") peso: RequestBody?,
        @Part("UnidadPeso") unidadPeso: RequestBody?,
        @Part("Alto") alto: RequestBody?,
        @Part("Ancho") ancho: RequestBody?,
        @Part("Profundidad") profundidad: RequestBody?,
        @Part("Color") color: RequestBody?,
        @Part("Stock") stock: RequestBody?,
        @Part fotos: List<MultipartBody.Part>?
    ): Call<ResponseBody>


    @PATCH("productos/{id_producto}/estado")
    fun cambiarEstadoProducto(
        @Path("id_producto") idProducto: Int,
        @Body request: EstadoProductoRequest
    ): Call<ResponseBody>

    @GET("usuarios/")
    fun listarUsuariosAdmin(
        @Query("busqueda") busqueda: String? = null,
        @Query("estado") estado: String? = null,
        @Query("id_rol") idRol: Int? = null
    ): Call<List<UsuarioAdminResponse>>


    @GET("usuarios/{id_usuario}")
    fun obtenerUsuarioAdmin(
        @Path("id_usuario") idUsuario: Int
    ): Call<UsuarioAdminResponse>

    @GET("usuarios/roles/activos")
    fun listarRolesAdmin(): Call<List<RolAdminResponse>>


    @POST("usuarios/admin")
    fun crearUsuarioAdmin(
        @Body request: UsuarioAdminCrearRequest
    ): Call<UsuarioAdminResponse>

    @PUT("usuarios/{id_usuario}")
    fun actualizarUsuarioAdmin(
        @Path("id_usuario") idUsuario: Int,
        @Body request: UsuarioAdminActualizarRequest
    ): Call<UsuarioAdminResponse>

    @DELETE("usuarios/{id_usuario}")
    fun desactivarUsuarioAdmin(
        @Path("id_usuario") idUsuario: Int
    ): Call<UsuarioAdminMensajeResponse>

    @PATCH("usuarios/{id_usuario}/reactivar")
    fun reactivarUsuarioAdmin(
        @Path("id_usuario") idUsuario: Int
    ): Call<UsuarioAdminMensajeResponse>
}
