# CosySmart Mobile App

**CosySmart** es una plataforma móvil diseñada para conectar a usuarios con necesidades de optimización de espacios y soluciones de diseño interior, ofreciendo un catálogo de muebles multifuncionales y la posibilidad de realizar compras directamente a fabricantes y vendedores.

## Características de la aplicación

- **UI/UX intuitiva:** Interfaz nativa desarrollada para Android mediante Kotlin y XML Layouts, utilizando ConstraintLayout y componentes de Material Design.
- **Mobiliario inteligente:** Exploración de muebles ergonómicos y funcionales para hogares modernos y espacios reducidos.
- **Conexión directa:** Plataforma orientada a conectar compradores con vendedores y fabricantes.
- **Módulos de comercio electrónico:** Gestión del flujo de compra mediante catálogo, carrito de compras (`ActivityCart`), proceso de pago (`ActivityCheckout`) y confirmación de compra (`ActivityPaymentConfirmation`).
- **Arquitectura multirol:** Soporte para tres tipos de usuarios: Administrador, Vendedor y Comprador.
- **Navegación por roles:** Gestión de las opciones de navegación mediante `BottomNavigationHelper`.
- **Autenticación de usuarios:** Registro e inicio de sesión mediante una API REST, con validación de credenciales.
- **Autenticación biométrica:** Inicio de sesión mediante huella dactilar utilizando las capacidades nativas de Android.

## Arquitectura y Backend

CosySmart utiliza una arquitectura cliente-servidor compuesta por una aplicación Android y un backend independiente.

### Aplicación móvil

Desarrollada en Kotlin, se encarga de la interfaz de usuario, navegación, gestión de sesiones y comunicación con el backend mediante solicitudes HTTP.

### Backend – cosysmart-backend

Desarrollado con **Python y FastAPI**, proporciona una API REST para gestionar las operaciones del sistema.

Entre sus responsabilidades se encuentran:

- Registro y gestión de usuarios.
- Autenticación y validación de credenciales.
- Administración de roles y permisos.
- Generación y validación de tokens JWT.
- Gestión de la autenticación biométrica.
- Comunicación con la base de datos.

### Base de datos

Se utiliza **Microsoft SQL Server** para almacenar y administrar la información del sistema.

Entre las entidades principales se encuentran:

- Usuarios.
- Roles.
- UsuarioRoles.
- Información relacionada con los módulos de la plataforma.

### Seguridad y autenticación

- **bcrypt:** Protección de contraseñas mediante funciones de hash.
- **JWT (JSON Web Token):** Gestión de sesiones y autenticación entre Android y FastAPI.
- **Android BiometricPrompt:** Presentación del diálogo nativo de autenticación biométrica.
- **AndroidKeyStore:** Generación y almacenamiento seguro de claves criptográficas para la autenticación biométrica.
- **ECDSA P-256 / SHA-256:** Firma y verificación criptográfica del proceso de autenticación biométrica.

## Tecnologías empleadas

| Área | Tecnología |
|---|---|
| Desarrollo móvil | Android Studio |
| Lenguaje móvil | Kotlin |
| Interfaz | XML, ConstraintLayout |
| Componentes UI | Material Design |
| Comunicación HTTP | Retrofit |
| Backend | Python, FastAPI |
| Base de datos | Microsoft SQL Server |
| ORM | SQLAlchemy |
| Autenticación | JWT y bcrypt |
| Biometría | BiometricPrompt y AndroidKeyStore |
| Criptografía biométrica | ECDSA P-256 / SHA-256 |
| Control de versiones | Git y GitHub |

---

## Conexión y pruebas con dispositivo físico (ADB)

Para conectar la aplicación Android ejecutada en un dispositivo físico con el backend local que se encuentra en el computador, se puede utilizar el comando ADB Reverse.

### 1. Verificar el dispositivo conectado

```bash
adb devices
```

### 2. Configurar el puente de comunicación

```bash
adb reverse tcp:8000 tcp:8000
```

Este comando permite que las solicitudes realizadas desde el dispositivo Android hacia `localhost:8000` sean redirigidas al puerto 8000 del computador.

### 3. Ejecutar el backend

Iniciar el servidor FastAPI en el puerto 8000.

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 4. Verificar la API

Abrir la documentación interactiva de FastAPI desde el navegador del computador:

```text
http://localhost:8000/docs
```

**Nota:** La configuración de ADB Reverse está orientada a pruebas con un dispositivo físico conectado por USB. Para utilizar la aplicación sin este puente, será necesario configurar una dirección de red accesible desde el dispositivo.

---

## Estado del proyecto

CosySmart integra una aplicación móvil Android con un backend desarrollado en FastAPI y una base de datos SQL Server. La solución implementa autenticación multirol, gestión de sesiones mediante JWT y autenticación biométrica mediante mecanismos nativos de Android.