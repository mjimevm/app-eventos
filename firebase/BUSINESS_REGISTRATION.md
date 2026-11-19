# Registro empresarial: diagnóstico y operación

Proyecto configurado: `guatevivo-d5f67`. No se modificaron Gradle ni `google-services.json`.

## Causa comprobada en el cliente

El serializador de Firestore interpreta los getters Kotlin `isDeleted()` e
`isDiscoverable()` como propiedades `deleted` y `discoverable`. Sin anotaciones,
`UserDto` y `PublicProfileDto` no envían los campos que exigen las reglas de
`firebase/friends.rules`.

Una prueba con `CustomClassMapper` del SDK instalado reprodujo antes de corregir:

```text
{..., deleted=false, discoverable=false, accountType=BUSINESS, ...}
expected isDeleted=false; actual null
```

La proyección pública tampoco cumplía `validProfile`: incluía nombres fuera de
`hasOnly(...)` y faltaban `isDeleted` e `isDiscoverable`. Con esas reglas, el batch
se rechaza. Se añadieron `@get:PropertyName` y `@field:PropertyName` a ambas
propiedades de ambos DTO, para escritura y lectura con los nombres canónicos.
No se relajaron las reglas para aceptar datos mal serializados.

Además, el ViewModel ocultaba la excepción con un mensaje genérico. La navegación
leía el tipo de cuenta una sola vez al iniciar la app, antes de registrar al
usuario. Ahora muestra el código y mensaje de Firebase, distingue el fallo
parcial, conserva `BUSINESS` y abre el perfil empresarial tras confirmar el batch.
Al entrar después a Perfil, consulta el tipo de la sesión actual y muestra errores
de lectura en lugar de asumir una cuenta personal.

## Operaciones reales y punto de fallo

1. `FirebaseAuth.createUserWithEmailAndPassword(email.trim().lowercase(Locale.ROOT), password).await()`.
   La contraseña se conserva exactamente. Un fallo aquí no escribe ningún perfil.
   Revisar Email/Password habilitado, política de contraseñas, red, cuotas y el
   código original (`ERROR_EMAIL_ALREADY_IN_USE`, `ERROR_OPERATION_NOT_ALLOWED`,
   etc.). No se ha verificado la configuración administrativa activa.
2. Con el UID autenticado, `UserFirebaseImpl.createUser` realiza un único
   `WriteBatch.commit().await()` con dos operaciones `set`:
   - `projects/guatevivo-d5f67/databases/(default)/documents/users/{uid}`:
     perfil privado; `uid` igual a la sesión, email normalizado de Authentication,
     `accountType = "BUSINESS"`, nombre/contactos y booleanos canónicos.
   - `projects/guatevivo-d5f67/databases/(default)/documents/publicProfiles/{uid}`:
     solo `uid`, `name`, `nameNormalized`, `photoUrl`, `accountType`,
     `isDiscoverable`, `isDeleted`. Nunca email, teléfono, dirección o descripción.
3. Solo después de confirmar la escritura se publica `isSuccess` y se navega al
   perfil empresarial. Si Firestore falla, la cuenta Auth ya existe pero el batch
   no queda confirmado: no se muestra éxito ni se elimina automáticamente Auth.
   La pantalla muestra UID, ambas rutas, código y mensaje originales.

`PERMISSION_DENIED` en un batch no identifica por sí solo cuál de los dos
documentos o condiciones fue rechazado. No se hicieron escrituras de prueba en
producción ni se consultaron las reglas desplegadas: el defecto y su
incompatibilidad con las reglas locales están reproducidos; la causa del rechazo
en el proyecto activo requiere contrastar el mensaje real y el ruleset desplegado.

## Regla necesaria si sigue apareciendo PERMISSION_DENIED

Un administrador debe contrastar y fusionar los bloques **completos** de
`users/{uid}`, `publicProfiles/{uid}` y sus funciones `owner`, `validProfile`,
`matchesUser`, `synced`, `userPath` y `profilePath` de `firebase/friends.rules`
con las reglas activas de **guatevivo-d5f67**. Ese archivo es parcial: conservar las
reglas de eventos y demás colecciones. No desplegarlo como reemplazo completo.

Las condiciones necesarias para el registro son exactamente las existentes allí:

- `users/{uid}` permite `create` solo al propietario autenticado, con UID igual
  a la ruta, email igual a `request.auth.token.email.lower()`, tipo `USER` o
  `BUSINESS` y proyección válida/sincronizada mediante `getAfter`.
- `publicProfiles/{uid}` permite `create` solo al propietario, con el perfil privado
  presente después del mismo batch, campos públicos limitados por `hasOnly`, nombre
  de 1–120 caracteres, nombre normalizado de 1–240, booleanos válidos,
  `isDeleted == false` y todos los valores coincidentes con `users/{uid}`.
- La lectura `get` de `users/{uid}` se permite solo al propietario; no hacen falta
  listados, consultas por correo ni lectura pública para registrar.
- Las actualizaciones conservan UID, correo y tipo de cuenta. No permitir cambiar
  `accountType` de cuentas existentes como reparación del registro.

No usar `allow read, write: if true`, permisos para cualquier usuario autenticado,
desactivar App Check ni introducir credenciales administrativas en Android. No
son necesarios índices de Firestore ni privilegios IAM del usuario Android para
estas escrituras puntuales. La autorización del cliente depende de las reglas y
de la sesión Firebase, no de conceder roles administrativos al dispositivo.

Referencia: [reglas de operaciones atómicas y getAfter](https://firebase.google.com/docs/firestore/security/rules-conditions).

## Reintento y cuentas parciales

Mientras se conserva este ViewModel, «Reintentar guardar perfil» repite solo el
batch original con el mismo UID. Los campos quedan bloqueados para evitar que el
reintento se dirija a otra cuenta. Se verifica UID y correo de la sesión antes de
escribir. No se repite `createUserWithEmailAndPassword`, que produciría un correo
ya utilizado. El fallo y su causa se conservan si las reglas siguen denegando.

El reintento no persiste al cerrar el proceso o abandonar definitivamente el
formulario. Para cuentas parciales anteriores, revisar el UID existente y reparar
el perfil bajo una sesión autenticada del propietario o mediante un procedimiento
administrativo controlado; no borrar cuentas ni convertir cuentas personales
existentes en empresariales. Si la cuenta Auth ya existe, el registro real sigue
mostrando el error de correo utilizado.

## Alternativa DEMO exclusiva de Debug

En Registro empresarial, «Abrir perfil empresarial DEMO (local)» abre una pantalla
aislada identificada como **DEMO · SOLO DEBUG**, con empresa ficticia y tipo
`BUSINESS`. No utiliza repositorios, no autentica, no consulta ni escribe Firebase,
no modifica la sesión real y no ofrece acciones de publicación. Salir vuelve al
registro real. No se activa automáticamente ante un error.

La implementación está en `src/debug`. `src/release` contiene únicamente una
función vacía, sin botón ni perfil ficticio. No se añadieron variantes ni cambios
de Gradle. La demostración no acredita que el registro real funcione en producción.

## Comprobación

```text
gradlew.bat :app:testDebugUnitTest :app:assembleDebug --console=plain
```

Pruebas del registro: nombres y lectura de campos con el serializador real,
proyección sin datos privados, persistencia de `BUSINESS`, validaciones, errores
de Auth/Firestore, espera del commit, doble clic y reintento con control de sesión.
Los fakes de las pruebas no contactan el proyecto real. La validación de reglas
desplegadas y una creación real con credenciales autorizadas siguen pendientes.

Resultado del 19/11/2026: **BUILD SUCCESSFUL**; 125 pruebas, 0 fallos, 0 errores.
APK generado: `app/build/outputs/apk/debug/app-debug.apk` (21 129 911 bytes).
`git diff --check` del flujo revisado no reportó errores. No hay cambios en Gradle
ni en `google-services.json`. Durante la primera recompilación hubo un error
transitorio en `SearchPage.kt`, que otro proceso corrigió; esta revisión no editó
ese archivo.

## Archivos modificados por esta revisión

Rutas relativas a `app/src/main/java/plat/proyecto/guatevivo/`, salvo indicación:

- `core/firebase/funciones/user/model/UserDTO.kt`: nombres canónicos en Firestore.
- `core/firebase/funciones/amigos/model/PublicProfileDto.kt`: misma corrección
  exclusivamente para la proyección que se escribe durante el registro.
- `core/firebase/funciones/user/firebase/UserFirebaseImpl.kt`: validar UID/tipo
  antes de guardar y normalizar correo independientemente del idioma.
- `core/repository/AuthRepository.kt`: error por etapa, contexto del batch y
  reintento del perfil bajo la misma sesión.
- `core/repository/RegistrationException.kt`: excepción con UID parcial y
  extracción del código original de Firebase.
- `paginas/register/ui/business/RegisterBusinessViewModel.kt`: validaciones,
  propagación de errores, `BUSINESS`, reintento y control de doble envío.
- `paginas/register/ui/business/RegisterBusinessState.kt`: UID del registro parcial.
- `paginas/register/ui/business/CreateBussinesAccount.kt`: campos bloqueados en
  reintento, restauración del formulario, mensaje/botón de recuperación y acceso DEMO.
- `paginas/register/RegisterNavHost.kt`: destino empresarial después del registro.
- `core/NavHost.kt`: retirar la lectura única/obsoleta del tipo de cuenta.
- `paginas/profile/ProfileNavHost.kt`: resolver el perfil de la sesión actual.
- `app/src/debug/java/plat/proyecto/guatevivo/paginas/register/ui/business/BusinessDemoEntry.kt`:
  perfil local ficticio aislado.
- `app/src/release/java/plat/proyecto/guatevivo/paginas/register/ui/business/BusinessDemoEntry.kt`:
  implementación vacía para excluir la demostración.
- `app/src/test/java/plat/proyecto/guatevivo/core/firebase/RegistrationSerializationTest.kt`:
  regresión con serializador real.
- `app/src/test/java/plat/proyecto/guatevivo/paginas/register/ui/business/RegisterBusinessViewModelTest.kt`:
  pruebas del flujo y errores.
- `firebase/BUSINESS_REGISTRATION.md`: este informe.

`AuthFirebaseImpl` y `UserRepository` fueron revisados y conservados: esperan las
operaciones del SDK y propagan excepciones; el ocultamiento estaba en el ViewModel.
Se conservaron los cambios previos presentes en el repositorio.
