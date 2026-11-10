# Conectar MovChia con Firebase

Guía paso a paso para dejar la app hablando con el proyecto real de Firebase.
Todo lo que sigue se hace una sola vez por proyecto, salvo el paso 1 que debe
repetir cada persona que clone el repositorio.

## 1. Archivo `google-services.json` (cada desarrollador)

1. Consola de Firebase → ícono de engranaje → **Configuración del proyecto**.
2. Pestaña **General** → sección **Tus apps** → app Android `com.movchia.app`.
   Si no existe, créala con **Agregar app** y ese nombre de paquete exacto.
3. Botón **Descargar google-services.json**.
4. Copia el archivo a `app/google-services.json` (reemplaza el placeholder).

El archivo está en `.gitignore`: no se sube al repositorio porque este es
público. Si falta, Gradle copia `app/google-services.example.json` para que el
proyecto compile, pero Firebase no funcionará hasta usar el archivo real.

## 2. Authentication

1. **Compilación → Authentication → Comenzar**.
2. Pestaña **Sign-in method** → habilitar **Correo electrónico/contraseña**.
3. Pestaña **Users** → **Agregar usuario**: correo institucional y contraseña del
   primer administrador (rol root). Anota el **UID** que aparece en la tabla.

## 3. Firestore

1. **Compilación → Firestore Database → Crear base de datos**.
2. Modo **producción** (las reglas del repositorio reemplazarán las de la consola).
3. Ubicación recomendada: `southamerica-east1` (São Paulo) por latencia desde
   Colombia. No se puede cambiar después.
4. Crear a mano el documento del primer administrador:
   - Colección `users` → documento con ID = el UID del paso 2.3.
   - Campo `role` (string) = `root`.
   - Campo `email` (string) = el correo del administrador.

   Sin este documento las reglas rechazan cualquier escritura del CMS.

## 4. Storage

1. **Compilación → Storage → Comenzar**, misma ubicación que Firestore.
2. Los archivos de contenido se guardarán bajo la carpeta `contents/`.

## 5. Desplegar reglas e índices desde el repositorio

Las reglas de seguridad y los índices compuestos viven en el repo
(`firestore.rules`, `firestore.indexes.json`, `storage.rules`). Para
aplicarlos:

```bash
npm install -g firebase-tools
firebase login
firebase use --add        # elige el proyecto y asígnale el alias "default"
firebase deploy --only firestore,storage
```

Alternativa sin CLI: pegar el contenido de `firestore.rules` en
**Firestore → Reglas** y el de `storage.rules` en **Storage → Reglas**. Los
índices se pueden crear desde el enlace que Firestore imprime en Logcat la
primera vez que una consulta falla con `FAILED_PRECONDITION`.

## 6. Datos de prueba

En compilaciones debug, el botón naranja flotante incluye **Sembrar datos de
prueba**. Requiere haber iniciado sesión como administrador (paso 2) y escribe
contenido de ejemplo en `contents`, `traffic_code`, `service_points` y
`quiz_questions`. Se puede ejecutar varias veces: los documentos usan IDs
fijos y se sobreescriben.

## 7. Verificación

1. Instalar la app en debug, elegir un grupo de edad y comprobar que el Home
   muestra el contenido sembrado en lugar de los textos de respaldo.
2. Entrar por **Acceso Admin** con el usuario del paso 2 y confirmar que el
   panel lista el contenido.
3. Generar un APK release (`gradlew assembleRelease`) y repetir el punto 1:
   las keep rules de R8 deben mantener los modelos legibles.

## Colecciones y campos

| Colección | Uso | Campos clave |
|---|---|---|
| `contents` | Videos, cuentos, noticias, campañas, FAQ | `type`, `targetGroups[]`, `published`, `publishedAt`, `mediaUrl`, `thumbnailUrl` |
| `traffic_code` | Artículos del Manual de Tránsito | `chapter`, `articleNumber`, `title`, `body`, `simplifiedBody`, `order` |
| `service_points` | Sedes y puntos de atención | `name`, `address`, `phone`, `schedule`, `latitude`, `longitude` |
| `quiz_questions` | Quiz y Adivina la Señal | `question`, `imageUrl`, `options[]`, `correctIndex`, `explanation`, `targetGroups[]`, `points` |
| `opinions` | Opiniones ciudadanas | `text`, `ageGroup`, `status` (PENDING/APPROVED/REJECTED), `createdAt` |
| `users` | Personal del CMS | `role` (gestor/root), `email` |

Los valores de `targetGroups` y `ageGroup` son los nombres del enum
`AgeGroup`: `KIDS`, `ADULTS`, `SENIORS`. Los de `type` son los de
`ContentType`: `VIDEO`, `STORY`, `CARTOON`, `GAME`, `NEWS`, `CAMPAIGN`, `FAQ`.
