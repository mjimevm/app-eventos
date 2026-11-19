# Amigos exclusivamente por correo electrónico

## Flujo y privacidad

La pantalla Amigos utiliza un campo de correo y «Enviar solicitud». No busca nombres, no consulta emails en Firestore y no envía solicitudes mediante UID desde Android. Aceptar, rechazar, cancelar, abrir perfiles relacionados y eliminar amistades conservan los documentos y operaciones existentes.

El correo se recorta en los extremos y convierte a minúsculas con una regla independiente del idioma. Se validan formato y longitudes; los espacios internos son un error, no se eliminan para formar una dirección distinta. Se conservan puntos y etiquetas `+`. No se eliminan acentos ni se reinterpretan alias. El formulario admite direcciones ASCII habituales; el backend repite la misma validación. Authentication resuelve el **correo principal** de la cuenta, no correos alternativos específicos de proveedores.

Android obtiene un ID token con el SDK cliente de Firebase Auth y llama mediante HTTPS a `sendFriendRequestByEmail`. No se añadió Firebase Functions SDK ni Admin SDK a Android, y no se cambió Gradle. No se siguen redirecciones que pudieran reenviar el token a otro servidor.

La función verifica el token, incluida su revocación, exige un perfil válido del emisor y resuelve el destinatario con **Admin Auth `getUserByEmail` en el servidor**. El cuerpo solo admite `{ "email": "..." }`; no admite un UID de emisor/destinatario controlado por el cliente. No hay endpoint de consulta de disponibilidad ni listado de correos.

La respuesta solo contiene `SENT`, `PENDING` o `FRIENDS`, sin UID, emails, nombres ni contactos del destinatario. Una cuenta desconocida, deshabilitada, eliminada o sin perfil utilizable devuelve el mismo error genérico; nunca un éxito simulado. Saber un correo permite intentar una solicitud aunque el antiguo `isDiscoverable` sea falso: esa bandera ya no controla invitaciones. No existe búsqueda pública y la antigua opción «Permitir que me encuentren» se retiró del formulario.

Se limita a **5 intentos por minuto y 20 por día UTC por UID**, contando también direcciones desconocidas y reintentos. El contador privado no guarda los emails intentados. Es un envío autenticado con efecto real, no una garantía de que nadie pueda inferir existencia a partir del resultado de un envío; los límites y la ausencia de un lookup público reducen el abuso. Aplicar además las cuotas y controles de abuso del proyecto antes de producción.

## Operaciones atómicas

Se mantiene `friendRequests` y el ID dirigido `SHA256(senderUid UTF-8).hex + SHA256(receiverUid UTF-8).hex`. La función lee ambas direcciones en una transacción: rechaza solicitudes propias, devuelve `PENDING` si ya hay una solicitud y `FRIENDS` si ya hay amistad. Solo devuelve `SENT` después de confirmar la transacción. REJECTED permite un nuevo envío con el mismo ID. Los envíos cruzados y los reintentos no crean dos amistades.

En la misma transacción se preparan las proyecciones mínimas de perfiles antiguos, sin publicar datos de contacto ni exigir que el destinatario haya abierto Amigos. Los campos heredados de nombre normalizado y descubrimiento se conservan por compatibilidad, pero no sirven para listar cuentas.

El receptor puede aceptar/rechazar PENDING. El emisor puede cancelar PENDING. Cualquiera de los dos puede eliminar ACCEPTED. Estas operaciones siguen usando Firestore desde el cliente y requieren las reglas siguientes. No se escriben los antiguos espejos `users/*/friends`.

## Reglas exactas que deben desplegarse

Fusionar **todos los bloques de `friends.rules`** con el ruleset del proyecto, conservando los bloques existentes de eventos/asistencia. No reemplazar el ruleset entero con este archivo parcial. Retirar cualquier `allow read/write` amplio superpuesto: una denegación no anula otro permiso que también coincida.

| Ruta | Acceso del cliente permitido |
| --- | --- |
| `users/{uid}` | Solo el propietario lee su documento. Listados y consultas por email denegados. Creación/actualización propia exige proyección coherente mediante `getAfter`; eliminación propia exige retirar la proyección en el mismo batch. |
| `publicProfiles/{uid}` | **Listados denegados**. Lectura puntual solo propia o con solicitud PENDING/amistad ACCEPTED. La antigua bandera `isDiscoverable=true` ya no permite leer perfiles ajenos. La proyección admite únicamente identidad visible, foto y tipo de cuenta, nunca email/teléfono/dirección. |
| `friendRequests/{id}` | Solo los participantes leen; las consultas deben incluir emisor/receptor autenticado y límite de 50. **Creación y reenvío PENDING por clientes denegados**: solo la función Admin puede realizarlos. El receptor puede cambiar PENDING a ACCEPTED/REJECTED sin alterar participantes; el emisor puede borrar PENDING y ambos pueden borrar ACCEPTED. |
| `friendRequestRateLimits/{uid}` | Todas las lecturas y escrituras de clientes denegadas; solo Admin. |
| `users/{uid}/friends/{other}` | Espejos antiguos de solo uso administrativo; lectura/escritura del cliente denegada. |

Desplegar los **dos índices de relaciones** de `friends.indexes.json`: `senderUid + status` y `receiverUid + status`. No se necesita ningún índice por email ni el anterior índice público de nombres; este último puede retirarse administrativamente si ya no tiene otros usos.

## Configuración pendiente de la función

No hay en este repositorio un proyecto Functions desplegado ni credenciales de administración. Integrar `friends-lifecycle.cjs` y `send-friend-request-email.cjs`, juntos en la misma carpeta, en un proyecto Cloud Functions configurado con `firebase-admin` y `firebase-functions`. Su punto de entrada debe exportarlos, por ejemplo:

```js
Object.assign(exports, require("./friends-lifecycle.cjs"));
```

En ese proyecto backend configurado, desplegar específicamente:

```text
firebase deploy --only functions:sendFriendRequestByEmail --project ID_REAL_DEL_PROYECTO
```

La función HTTP de segunda generación está configurada en `us-central1`. La cuenta de servicio del backend necesita permisos para consultar usuarios de Authentication y leer/escribir/transaccionar en Firestore: revisar `firebaseauth.users.get` y los roles adecuados, por ejemplo `roles/firebaseauth.viewer` y `roles/datastore.user`. No proporcionar estas credenciales a Android.

El endpoint HTTP debe ser alcanzable a nivel de IAM (`invoker: public`); **cada petición sigue requiriendo un Firebase ID token válido verificado por el handler**. Un token de usuario de Firebase no es un token de invocación IAM de Cloud Run. No resolver un 403 abriendo documentos privados de Firestore.

Copiar la **URL HTTPS real que entregue el despliegue** en `app/src/main/res/values/friends_backend.xml`, recurso `friend_requests_endpoint`, y volver a compilar. El recurso está vacío deliberadamente: no se inventa un dominio ni se simula un endpoint desplegado. El transporte solo acepta hosts HTTPS de Cloud Functions/Cloud Run, sin credenciales, query ni fragmento.

La migración administrativa anterior `migrate_friends.py` sigue disponible para consolidar IDs aleatorios y espejos históricos antes de habilitar el nuevo flujo. Es opcional si los registros ya usan IDs estables; usar mantenimiento y copia de seguridad. No borrar `_migrations/friends_v1`, que evita resucitar espejos históricos. No se ha ejecutado contra Firebase.

Mantener/integrar los hooks `syncFriendProfile` y `hideDeletedAuthProfile` para bajas/cambios administrativos. Las bajas masivas de Authentication requieren retirar las proyecciones en la propia tarea administrativa o reconciliarlas, porque no disparan el hook individual. Estos hooks tampoco se han desplegado desde esta sesión.

## Diagnóstico de PERMISSION_DENIED

No se consultaron las reglas activas del proyecto; no puede atribuirse el error observado a una regla concreta sin verificar ese despliegue. El código **no lo oculta ni lo convierte en éxito**.

- Si aparece al abrir Amigos: revisar acceso propio a `users/{uid}` y `publicProfiles/{uid}`, sus escrituras de compatibilidad y las consultas de `friendRequests`. Desplegar los bloques e índices anteriores. Un índice faltante suele producir FAILED_PRECONDITION, no debe tratarse como lista vacía.
- FAILED_PRECONDITION indicando servicio no configurado: falta la URL real del recurso Android; todavía no se llamó al backend.
- HTTP 404: verificar URL/despliegue; si el handler respondió NOT_FOUND, la cuenta destino no está disponible. Ninguno se trata como envío exitoso.
- HTTP 401: sesión/token inválido o revocado; volver a autenticar.
- HTTP 403 / PERMISSION_DENIED: revisar invocación IAM y permisos de la cuenta de servicio; si viene del SDK cliente, revisar las reglas Firestore. Incluso un 403 HTML de IAM se muestra como PERMISSION_DENIED.
- HTTP 429 / RESOURCE_EXHAUSTED: se alcanzó el límite de intentos; esperar. No se reintenta automáticamente.
- Si el servidor confirmó el envío pero falló después la recarga del listado, la pantalla muestra el error de actualización y conserva el código de permisos; no presenta un estado SENT de éxito. El reintento es idempotente y devolverá PENDING si ya se había guardado.

## Verificación

Ejecutar `gradlew.bat :app:testDebugUnitTest`, `gradlew.bat :app:assembleDebug` y `node --test app/src/test/js/friends-email.test.cjs`. Los Fakes están exclusivamente en `src/test`; las pruebas del handler no contactan Firebase real.

Antes de activar producción: probar reglas en Emulator Suite y dos dispositivos/cuentas reales, validación de token, permisos IAM/Admin, email desconocido, cuenta propia, bajas, doble envío/cruzados, aceptación/rechazo/cancelación, eliminación, falta de red y códigos 401/403/404/429. Intentar desde un cliente crear PENDING por UID o leer emails/listar perfiles; debe denegarse. No se han desplegado reglas, índices o funciones ni ejecutado esas comprobaciones reales.

Referencias: [Admin Auth y búsqueda por correo](https://firebase.google.com/docs/auth/admin/manage-users), [verificación de ID tokens](https://firebase.google.com/docs/auth/admin/verify-id-tokens), [funciones HTTP](https://firebase.google.com/docs/functions/http-events), [transacciones](https://firebase.google.com/docs/firestore/manage-data/transactions).
