# CosySmart Mobile App

**CosySmart** es una plataforma móvil diseñada para conectar a usuarios con necesidades de optimización de espacio y soluciones de diseño interior, ofreciendo un catálogo curado de muebles multifuncionales y compra directa con fabricantes.

### Características de la app
- **UI/UX Intuitiva:** Interfaz nativa desarrollada con ConstraintLayout para una experiencia fluida y adaptable a múltiples resoluciones.
- **Mobiliario Inteligente:** Exploración de piezas ergonómicas y funcionales para hogares modernos y pequeños espacios.
- **Conexión Directa:** Plataforma ágil para conectar usuarios con productores verificados.
- **Módulos de E-commerce y Pagos:** Gestión completa del flujo del comprador incluyendo Catálogo, Carrito de Compras (`ActivityCart`), Pasarela de Pagos (`ActivityCheckout`) y Confirmación de Compra (`ActivityPaymentConfirmation`).
- **Arquitectura Multi-Rol:** Soporte centralizado para perfiles de Administrador, Vendedor y Comprador mediante controladores de navegación como `BottomNavigationHelper`.

### Arquitectura y Backend
- **API REST Python (`cosysmart-backend`):** Integración robusta con un backend desarrollado en Python para gestionar las peticiones del sistema, procesamiento de pagos y sincronización de datos de la plataforma.

### Tecnologías empleadas
- **Lenguaje / Entorno:** Android Studio, Kotlin / Java.
- **Backend:** Python (API REST con `cosysmart-backend`).
- **Diseño UI:** XML Layouts, ConstraintLayout, Material Design Components.
- **Control de Versiones:** Git & GitHub.

---

## Conexión y Pruebas con Dispositivo Físico (ADB)

Para conectar tu aplicación Android corriendo en un entorno local con el backend y el entorno físico conectado por USB, utiliza el siguiente comando de puente de depuración de Android (ADB):

```bash
adb -s A76X6R5605000158 reverse tcp:8000 tcp:8000