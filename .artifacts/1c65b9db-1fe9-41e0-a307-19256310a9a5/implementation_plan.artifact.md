# Mejora de Estructura de CreateNormalAccount

Se mejorará el archivo `CreateNormalAccount.kt` para que siga el patrón de diseño y estructura establecido en `CreateEventPage.kt`, proporcionando una experiencia de usuario más consistente y una base de código más organizada.

## User Review Required

> [!NOTE]
> Se mantendrá el componente `InputField` localmente dentro del archivo por solicitud del usuario, pero se estandarizará su diseño con el resto del proyecto.

## Proposed Changes

### [Component Name] paginas

#### [MODIFY] [CreateNormalAccount.kt](file:///D:/Personal/programacion/Plataformas Moviles/Proyecto/RepoGithub/app-eventos/app/src/main/java/plat/proyecto/guatevivo/paginas/CreateNormalAccount.kt)
- Se implementará `Scaffold` para manejar la estructura básica de la página.
- Se usará `TopBar` con botón de retroceso.
- Se añadirá soporte para scroll vertical mediante `verticalScroll`.
- Se agruparán los campos del formulario dentro de un `Card` con bordes suaves, siguiendo el estilo de `CreateEventPage`.
- Se reemplazará el uso de `rememberTextFieldState` (API experimental) por `mutableStateOf` y `value/onValueChange` para mantener consistencia con las otras páginas.
- Se integrará el componente local `InputField` para reducir la repetición de código.
- Se ajustarán los colores y tipografías para que coincidan con el `MaterialTheme` del proyecto.

## Verification Plan

### Manual Verification
- Previsualizar el componente `CreateNormalAccountPreview` en Android Studio para verificar que el diseño coincida con el estilo de `CreateEventPage`.
- Verificar que el formulario sea desplazable en pantallas pequeñas.
- Asegurarse de que el botón de "Crear Cuenta" esté posicionado correctamente al final del formulario.
