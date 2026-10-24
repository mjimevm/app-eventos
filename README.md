# GuateVivo

GuateVivo es una aplicación móvil para Android desarrollada con Kotlin y Jetpack Compose, cuyo objetivo es facilitar el descubrimiento, organización y publicación de eventos en Guatemala.

La aplicación permitirá a los usuarios explorar eventos de diferentes categorías, buscar actividades de su interés, guardar eventos y consultar aquellos a los que planean asistir. También contará con un sistema de amistades para conectar usuarios y conocer los eventos a los que asistirán sus amigos.

Además, GuateVivo permitirá que empresas y organizadores creen cuentas especiales para publicar y administrar sus propios eventos, incluyendo información como fechas, ubicaciones, descripciones e imágenes.

## Servicios

### Firebase Authentication

Se utilizará para gestionar la autenticación de usuarios y empresas mediante correo electrónico y contraseña. Permitirá registrar cuentas, iniciar sesión, cerrar sesión y mantener sesiones autenticadas.

**Requisitos:** crear un proyecto en Firebase, registrar la aplicación Android y habilitar la autenticación mediante correo electrónico y contraseña.

### Cloud Firestore

Se utilizará como base de datos en la nube para almacenar y sincronizar la información de GuateVivo, incluyendo:

- Perfiles de usuarios y empresas.
- Eventos publicados por los organizadores.
- Eventos guardados y eventos a los que asistirán los usuarios.
- Listas de amistades y solicitudes de amistad.
- Información de ubicaciones y enlaces de Google Maps.
- Referencias a las imágenes de los eventos.

Permitirá consultar y actualizar información compartida entre diferentes usuarios de la aplicación.

**Requisitos:** habilitar Cloud Firestore en Firebase y configurar las reglas de seguridad correspondientes.

### Firebase Cloud Storage

Se utilizará para almacenar las imágenes que las empresas y organizadores seleccionen al crear o editar eventos.

Las imágenes se subirán a Firebase Cloud Storage y sus referencias se guardarán en Cloud Firestore, permitiendo que otros usuarios puedan visualizarlas desde la aplicación.

**Requisitos:** habilitar Cloud Storage en Firebase, configurar las reglas de seguridad y utilizar el plan Blaze de Firebase.

### Google Maps (enlaces externos)

Se utilizarán enlaces de Google Maps para permitir que los usuarios consulten la ubicación de los eventos.

Al crear un evento, los organizadores podrán ingresar la dirección del lugar y un enlace de Google Maps. Posteriormente, los usuarios podrán abrir dicho enlace desde la aplicación para consultar la ubicación y obtener indicaciones de cómo llegar.

Esta integración se realizará mediante Intents de Android, por lo que no será necesario utilizar Google Maps SDK, configurar una API Key ni implementar un mapa interactivo dentro de la aplicación.

## Librerías

### Jetpack Compose y Material 3

Se utilizarán para desarrollar la interfaz gráfica de GuateVivo mediante componentes declarativos, permitiendo crear pantallas modernas, reutilizables y consistentes con el tema visual de la aplicación.

### Navigation Compose

Se utilizará para gestionar la navegación entre las diferentes pantallas y grafos anidados de la aplicación, incluyendo autenticación, registro, inicio, búsqueda, creación de eventos, perfiles y configuración.

### ViewModel, Kotlin Coroutines y StateFlow

Se utilizarán para implementar la arquitectura MVI (Model-View-Intent), gestionar el estado de las pantallas y realizar operaciones asíncronas sin bloquear la interfaz de usuario.

### Room

Se utilizará como base de datos local para almacenar información de eventos guardados y datos consultados previamente, permitiendo conservar información relevante dentro del dispositivo y facilitar su consulta sin conexión a internet.

### Firebase Android SDK

Se utilizará para integrar Firebase Authentication, Cloud Firestore y Firebase Cloud Storage con la aplicación Android, permitiendo gestionar la autenticación, el almacenamiento de imágenes y la comunicación con los datos almacenados en la nube.

### Coil

Se utilizará para cargar y mostrar las imágenes de eventos y perfiles almacenadas en Firebase Cloud Storage, mediante su integración con Jetpack Compose.

Permitirá visualizar imágenes remotas de manera eficiente

### AndroidX Activity Compose

Se utilizará para integrar funcionalidades nativas de Android dentro de Jetpack Compose, incluyendo el selector de imágenes del dispositivo mediante Android Photo Picker y contratos de actividad. Esto permitirá que los organizadores seleccionen imágenes para sus eventos antes de subirlas a Firebase Cloud Storage.
