
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
import com.example.cosysmart.network.CrearOrdenRequest
import com.example.cosysmart.network.OrdenResponse

// Sevice para las peticiones a la API
interface ApiService {

    // USUARIOS Y AUTENTICACIÓN

    @POST("usuarios/")
    fun registrarUsuario(
        @Body usuario: UsuarioRequest
    ): Call<UsuarioResponse>

    @POST("auth/login")
    fun loginUsuario(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @GET("auth/me")
    fun obtenerUsuarioActual(): Call<UsuarioActualResponse>

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

    @Multipart
    @PUT("usuarios/me/foto")
    fun actualizarFotoPerfil(
        @Part foto: MultipartBody.Part
    ): Call<FotoPerfilResponse>


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


    // CATEGORÍAS

    @GET("categorias/")
    fun listarCategorias(): Call<List<CategoriaResponse>>


    // PRODUCTOS

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

    @GET("productos/{id_producto}")
    fun obtenerProductoPorId(
        @Path("id_producto") idProducto: Int
    ): Call<ProductoResponse>

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


    // ADMINISTRACIÓN DE USUARIOS

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


    // CARRITO DE COMPRAS

    @POST("carrito/agregar")
    fun agregarProductoCarrito(
        @Body datos: AgregarProductoCarritoRequest
    ): Call<CarritoResponse>

    @GET("carrito/")
    fun obtenerCarrito(): Call<CarritoResponse>

    @PATCH("carrito/detalles/{id_detalle}")
    fun actualizarCantidadCarrito(
        @Path("id_detalle") idDetalle: Int,
        @Body datos: ActualizarCantidadCarritoRequest
    ): Call<CarritoResponse>

    @DELETE("carrito/detalles/{id_detalle}")
    fun eliminarProductoCarrito(
        @Path("id_detalle") idDetalle: Int
    ): Call<CarritoResponse>

    @DELETE("carrito/")
    fun vaciarCarrito(): Call<CarritoResponse>
    // ÓRDENES

    @POST("ordenes/crear")
    fun crearOrden(
        @Body request: CrearOrdenRequest
    ): Call<OrdenResponse>

    @GET("ordenes/")
    fun obtenerHistorialOrdenes(): Call<List<OrdenResponse>>

    @GET("ordenes/{id_orden}")
    fun obtenerOrdenPorId(
        @Path("id_orden") idOrden: Int
    ): Call<OrdenResponse>

    @PATCH("ordenes/{id_orden}/cancelar")
    fun cancelarOrden(
        @Path("id_orden") idOrden: Int
    ): Call<CancelarOrdenResponse>

    @GET("pagos/wompi/pendiente/{id_orden}")
    fun obtenerPagoWompiPendiente(
        @Path("id_orden") idOrden: Int
    ): Call<PagoWompiResponse>

    @POST("pagos/wompi/iniciar")
    fun iniciarPagoWompi(
        @Body request: IniciarPagoRequest
    ): Call<PagoWompiResponse>

    @GET("pagos/wompi/estado/{id_orden}")
    fun consultarEstadoPagoWompi(
        @Path("id_orden") idOrden: Int
    ): Call<EstadoPagoWompiResponse>

}
