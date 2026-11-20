# CosySmart Mobile App

**CosySmart** es una aplicación móvil nativa para Android orientada a conectar compradores con vendedores y fabricantes de muebles multifuncionales. Su objetivo es facilitar la búsqueda de soluciones de mobiliario para hogares modernos, espacios reducidos y necesidades de optimización del espacio.

La aplicación integra módulos de autenticación, gestión de productos, carrito de compras, órdenes, pagos de prueba y visualización de mapas.

## Características de la aplicación

- **Interfaz nativa:** desarrollada para Android con Kotlin, XML Layouts, ConstraintLayout y componentes de Material Design.
- **Arquitectura multirol:** soporte para Administrador, Vendedor y Comprador, con navegación y funcionalidades según el rol.
- **Autenticación:** registro e inicio de sesión mediante una API REST, con gestión de sesiones y tokens JWT.
- **Gestión de productos:** creación de productos con información descriptiva, categorías y fotografías.
- **Selección de imágenes:** integración de cámara y galería para seleccionar entre una y cinco fotografías por producto, con validaciones de formato y tamaño.
- **Carrito de compras:** permite agregar productos, consultar el contenido, actualizar cantidades, calcular subtotales, eliminar productos y vaciar el carrito.
- **Proceso de compra:** integración con el backend para crear órdenes y consultar su información.
- **Pagos:** integración en desarrollo con Wompi Sandbox para pruebas de pago mediante los métodos habilitados.
- **Historial y detalle de órdenes:** consulta de las órdenes y visualización de su información y estado.
- **Mapas y ubicación:** integración de Google Maps en Android para visualizar el mapa y obtener la ubicación actual del dispositivo.
- **Navegación por roles:** gestión de las opciones de navegación mediante `BottomNavigationHelper`.
- **Perfil de usuario:** consulta y actualización de información personal y fotografía para Compradores y Vendedores.

## Arquitectura y backend

CosySmart utiliza una arquitectura cliente-servidor compuesta por una aplicación Android y un backend independiente.

### Aplicación móvil

Desarrollada en Kotlin, se encarga de:

- Presentar la interfaz y gestionar la navegación.
- Administrar las sesiones de usuario.
- Consumir los servicios REST mediante Retrofit.
- Gestionar productos, imágenes y carrito de compras.
- Permitir el proceso de creación y consulta de órdenes.
- Integrar Google Maps y las funcionalidades de ubicación del dispositivo.
- Abrir el flujo de pago de Wompi Sandbox desde el dispositivo.

### Backend – cosysmart-backend

Desarrollado con **Python y FastAPI**, proporciona una API REST para gestionar las operaciones del sistema.

Entre sus responsabilidades se encuentran:

- Registro, autenticación y gestión de usuarios.
- Administración de roles y permisos.
- Generación y validación de tokens JWT.
- Gestión de productos, categorías e imágenes.
- Administración del carrito de compras.
- Creación y consulta de órdenes.
- Gestión de estados de las órdenes.
- Integración de pagos mediante Wompi Sandbox.
- Gestión de los procesos de autenticación biométrica.
- Comunicación con la base de datos.

### Base de datos

Se utiliza **Microsoft SQL Server** para almacenar y administrar la información del sistema.

Entre las entidades principales se encuentran:

- Usuarios.
- Roles y UsuarioRoles.
- Productos y categorías.
- Información relacionada con las imágenes de productos.
- Carritos y sus productos.
- Órdenes y sus detalles.
- Registros relacionados con los pagos.
- Información asociada a la autenticación biométrica.

## Mapas y geolocalización

La aplicación integra **Google Maps SDK for Android** para mostrar mapas dentro de la interfaz móvil.

Actualmente, la integración permite:

- Visualizar el mapa con calles y detalles geográficos.
- Obtener y mostrar la ubicación actual del dispositivo.
- Utilizar la funcionalidad de mapas en un dispositivo Android físico.

La configuración de la clave de Google Maps se realiza mediante `local.properties`, evitando incluir la clave directamente en el código fuente.

**Alcance actual:** la visualización del mapa y la obtención de la ubicación actual están implementadas y probadas. Esto no implica que estén implementados el seguimiento de pedidos, la ubicación de vendedores, el cálculo de rutas o el envío y almacenamiento de coordenadas de entrega en el backend.

## Seguridad y autenticación

La aplicación y el backend utilizan mecanismos de seguridad para proteger las operaciones y la información de los usuarios.

- **bcrypt:** protección de contraseñas mediante funciones de hash.
- **JWT (JSON Web Token):** autenticación y gestión de sesiones entre Android y FastAPI.
- **Interceptor de Retrofit:** incorporación del token de autenticación en las solicitudes protegidas.
- **Android BiometricPrompt y AndroidKeyStore:** tecnologías previstas para la autenticación biométrica nativa en Android.
- **ECDSA P-256 / SHA-256:** mecanismos criptográficos utilizados en el diseño del proceso de autenticación biométrica.

El backend dispone de los servicios y endpoints del proceso biométrico. La integración completa de Android con AndroidKeyStore y BiometricPrompt debe considerarse pendiente de implementación y validación.

## Integración de pagos con Wompi

CosySmart incorpora una integración de pruebas con **Wompi Sandbox** para el proceso de pago de las órdenes.

El flujo contempla:

1. Solicitar al backend la información del intento de pago.
2. Obtener la referencia, el monto y la firma de integridad.
3. Abrir el checkout de Wompi en el navegador externo.
4. Recibir las notificaciones de eventos de Wompi en el backend.
5. Actualizar el estado del pago y de la orden mediante la validación del evento recibido.
6. Consultar el estado del pago desde la aplicación Android.

La confirmación del pago depende de la validación del evento recibido por el backend. El retorno del navegador o la redirección de Wompi no deben considerarse, por sí solos, una confirmación de pago aprobado.

**Importante:** esta integración corresponde al entorno Sandbox. No representa una pasarela habilitada para cobros reales en producción. La recepción y el procesamiento de notificaciones deben validarse de extremo a extremo antes de considerar finalizado el flujo.

## Tecnologías empleadas

| Área | Tecnología |
|---|---|
| Desarrollo móvil | Android Studio |
| Lenguaje móvil | Kotlin |
| Interfaz | XML, ConstraintLayout |
| Componentes UI | Material Design |
| Comunicación HTTP | Retrofit |
| Mapas | Google Maps SDK for Android |
| Ubicación | Servicios de ubicación de Android |
| Backend | Python, FastAPI |
| Base de datos | Microsoft SQL Server |
| ORM | SQLAlchemy |
| Autenticación | JWT y bcrypt |
| Pagos | Wompi Sandbox |
| Biometría | Android BiometricPrompt y AndroidKeyStore (integración Android pendiente) |
| Criptografía biométrica | ECDSA P-256 / SHA-256 |
| Control de versiones | Git y GitHub |

---

## Conexión y pruebas con dispositivo físico (ADB)

Para conectar la aplicación Android ejecutada en un dispositivo físico con el backend local del computador, se puede utilizar el comando `ADB Reverse`.

### 1. Verificar el dispositivo conectado

Conectar el dispositivo mediante USB y ejecutar:

```bash
adb devices
```

El dispositivo debe aparecer en la lista de dispositivos conectados.

### 2. Configurar el puente de comunicación

Ejecutar:

```bash
adb reverse tcp:8000 tcp:8000
```

Este comando permite que las solicitudes realizadas desde el dispositivo Android hacia `localhost:8000` sean redirigidas al puerto 8000 del computador.

### 3. Ejecutar el backend

Iniciar el servidor FastAPI desde el directorio del proyecto backend:

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

El módulo de inicio debe corresponder a la estructura real del proyecto.

### 4. Verificar la API

Abrir la documentación interactiva de FastAPI desde el navegador del computador:

```text
http://localhost:8000/docs
```

**Nota:** ADB Reverse está orientado a pruebas con un dispositivo físico conectado por USB. Si se utiliza otro método de conexión, será necesario configurar una dirección de red accesible desde el dispositivo.

---

## Configuración de Google Maps

La clave de Google Maps debe configurarse localmente en el proyecto Android mediante el archivo `local.properties`.

No se recomienda publicar este archivo ni incluir claves privadas en el repositorio.

Para utilizar Google Maps es necesario contar con un proyecto de Google Cloud, habilitar los servicios correspondientes y configurar las restricciones de la clave según la aplicación Android.

---

## Estado actual del proyecto

| Módulo | Estado |
|---|---|
| Autenticación y sesiones con JWT | Implementado |
| Arquitectura multirol | Implementada |
| Perfil de Comprador y Vendedor | Implementado |
| Creación de productos | Implementada y probada |
| Selección de fotografías desde cámara y galería | Implementada y probada |
| Lectura, actualización y eliminación de productos | Pendiente |
| Carrito de compras | Implementado y probado |
| Integración de Google Maps | Implementada y probada |
| Obtención de ubicación actual del dispositivo | Implementada y probada |
| Creación y consulta de órdenes | Implementación realizada; validación integral pendiente |
| Historial y detalle de órdenes | Implementados; validación integral pendiente |
| Gestión de estados y cancelación de órdenes | Requiere validación funcional completa |
| Integración de Wompi Sandbox | En proceso de validación integral |
| Geolocalización de vendedores y seguimiento de pedidos | Pendiente |
| Autenticación biométrica en Android | Integración pendiente |

### Consideraciones pendientes

- Completar las operaciones de lectura, actualización y eliminación de productos.
- Validar el proceso de compra de extremo a extremo, evitando órdenes duplicadas y descuentos incorrectos de inventario.
- Verificar la recepción y el procesamiento de eventos de Wompi Sandbox.
- Completar la validación de los estados de pago y de las órdenes.
- Revisar la gestión de inventario cuando una orden queda pendiente de pago o el comprador abandona el proceso.
- Implementar, si se requiere, la ubicación de vendedores, las direcciones de entrega y el seguimiento de pedidos.
- Integrar y probar la autenticación biométrica nativa en Android.

---

**CosySmart – Aplicación móvil Android**