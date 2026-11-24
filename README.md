# La Guarida Maker

Web de un proyecto personal **sin ánimo de lucro** para mostrar piezas impresas en 3D. Los visitantes pueden explorar el catálogo, guardar sus piezas favoritas y escribir al creador para pedirlas y ayudar a cubrir el coste del filamento y el mantenimiento de la impresora.

> Esta web **no es una tienda**: no hay pasarela de pago ni precios públicos. El precio se acuerda en la conversación con cada persona y las piezas se ofrecen como apoyo al creador.

## Funcionalidades

**Parte pública y de usuario**
- Catálogo de piezas con filtros por categoría, buscador, descripciones resumidas en tarjetas y paginación.
- Detalle de cada pieza con carrusel de imágenes de tamaño fijo, descripción completa, materiales disponibles, dimensiones, tiempo de impresión y categoría.
- Registro e inicio de sesión (contraseñas cifradas con BCrypt). El registro exige aceptar la política de privacidad y guarda la fecha de aceptación.
- Perfil de usuario (`/perfil`): consulta de datos básicos, gestión de direcciones de envío y opción para **borrar la propia cuenta** (con confirmación y contraseña, siempre que no existan solicitudes en curso).
- Favoritos: corazón en las tarjetas y en el detalle, y página "Mis favoritos".
- Conversaciones con el creador sobre una pieza: la conversación se crea al enviar el primer mensaje, y desde ella se puede **solicitar** o **cancelar** la pieza.
- Seguimiento de la solicitud: estado, precio acordado y avisos de mensajes sin leer en el menú y en "Mis mensajes".
- Home dinámica: etiqueta amarilla de **novedad** y tarjeta con las piezas que se están imprimiendo, su avance y la cola de solicitudes.
- Página de política de privacidad (`/privacidad`).
- Diseño colorido, responsive y con Bootstrap 5.

**Administración** (roles `ADMIN` y `ENCARGADO`, según se indica)
- Piezas (`ADMIN`): listado con vista rápida de coste de producción y precio sugerido, avisos visuales de stock bajo (`0` y `1`), actualización rápida de stock conservando filtros/página y ancla, opción de ocultar, mostrar y borrar; alta y edición con varias imágenes, elección de la portada, vista previa, asignación de filamentos con gramos exactos, datos económicos y casilla de **novedad**.
- Filamentos (`ADMIN`): gestión de inventario de materiales/colores, disponibilidad y precio por kilogramo (`€/kg`).
- Calculadora de costes y margen (`ADMIN`): herramienta interactiva para calcular el coste real de fabricación (consumo multicolor de filamentos, desgaste/electricidad, colchón de fallos y extras) y el precio sugerido según margen (redondeado al alza a pasos de `0,50 €`). Permite cargar cualquier pieza del catálogo automáticamente y guardar el cálculo directamente en su ficha.
- Categorías (`ADMIN`): alta, edición, orden y visibilidad.
- Impresora (`ADMIN`): lista de piezas que se están imprimiendo (hasta 4), con buscador para elegirlas y porcentaje de avance editable.
- Bandeja de mensajes (`ADMIN` y `ENCARGADO`): responder a los usuarios, aceptar solicitudes fijando el precio y hacer avanzar el estado.
- Las imágenes se suben a Cloudinary, en una carpeta propia del proyecto.
- Creación automática del administrador inicial al arrancar.

## Solicitudes y estados

Cada conversación tiene un estado que avanza así:

```
CONSULTA → SOLICITADA → ACEPTADA → EN_IMPRESION → LISTA → ENTREGADA
```

| Paso | Quién lo hace |
|---|---|
| Consulta → Solicitada | La persona usuaria, desde la conversación |
| Solicitada → Aceptada | El equipo, indicando el precio acordado |
| Aceptada → En impresión → Lista → Entregada | El equipo |
| Cancelar | El usuario hasta que se acepta; el equipo, antes de la entrega |

- Cada cambio de estado y cada mensaje activa el aviso de "sin leer" de la otra parte.
- Una conversación **entregada** o **cancelada** queda cerrada y ya no admite mensajes.
- Si alguien vuelve a escribir sobre una pieza con la conversación cerrada, empieza una conversación nueva.
- Solo el dueño de una conversación puede abrirla desde la parte de usuario.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Actuator) |
| Plantillas | Thymeleaf + Thymeleaf Extras Spring Security |
| Base de datos | PostgreSQL (esquema `guarida3d`, alojado en Supabase en producción) |
| Imágenes | Cloudinary |
| Interfaz | Bootstrap 5.3 (CDN), CSS y JavaScript propios |
| Build y Despliegue | Maven, Render |

## Estructura del proyecto

```
src/main/java/com/guaridaludica/guaridaimpresionante/
├── config/        Seguridad, Cloudinary y carga inicial de datos
├── controller/    Controladores web y formularios (catálogo, calculadora, piezas, filamentos, cuenta, etc.)
├── model/         Entidades JPA (Producto, ImagenProducto, Filamento, ProductoFilamento,
│                  Categoria, Usuario, Direccion, Favorito, Conversacion, Mensaje, Impresion)
│                  y enumerados (Rol, EstadoConversacion)
├── repository/    Repositorios Spring Data
├── security/      UserDetailsService
├── service/       Lógica de negocio (conversaciones, impresiones, avisos, administración,
│                  cuenta, filamentos, favoritos, categorías y Cloudinary)
└── util/          Utilidades (paginación)

src/main/resources/
├── application.yaml          Configuración común
├── application-dev.yaml      Perfil de desarrollo (no se sube al repositorio)
├── application-prod.yaml     Perfil de producción (variables de entorno)
├── static/
│   ├── css/                  Estilos por módulos (base, componentes, layout, home, catalogo,
│   │                         pieza, formularios, admin, conversaciones)
│   ├── js/                   Scripts por pantalla (calculadora, formularios dinámicos, etc.)
│   └── img/                  Logos y favicon
└── templates/
    ├── fragments/            Cabecera, navbar, pie, hilo de mensajes y paginación reutilizables
    ├── perfil/               Configuración de cuenta y direcciones de envío
    └── admin/                Pantallas de administración (piezas, filamentos, calculadora, etc.)
```

## Requisitos

- JDK 21
- Maven (o el wrapper incluido, `./mvnw`)
- PostgreSQL
- Una cuenta de [Cloudinary](https://cloudinary.com/) (el plan gratuito es suficiente)

## Puesta en marcha en local

### 1. Base de datos

El proyecto usa un esquema propio dentro de la base de datos. Créalo antes del primer arranque; Hibernate crea las tablas, pero no el esquema:

```sql
CREATE SCHEMA guarida3d;
```

### 2. Configuración del perfil `dev`

Crea `src/main/resources/application-dev.yaml` con tus datos (este fichero está en `.gitignore` y no debe subirse):

```yaml
spring:
  thymeleaf:
    cache: false
  datasource:
    url: jdbc:postgresql://localhost:5432/NOMBRE_DE_TU_BASE?currentSchema=guarida3d
    username: tu_usuario
    password: tu_password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        default_schema: guarida3d
    show-sql: true

app:
  admin:
    email: tu_email@ejemplo.com
    password: una_contraseña_larga
  cloudinary:
    cloud-name: tu_cloud_name
    api-key: tu_api_key
    api-secret: tu_api_secret
```

### 3. Arrancar

```bash
./mvnw spring-boot:run
```

La aplicación queda en <http://localhost:8080>. En el perfil `dev`, si la tabla de piezas está vacía, se cargan un par de piezas de ejemplo.

Para entrar en la administración, inicia sesión con el email y la contraseña definidos en `app.admin` y pulsa **Administración** en la barra de navegación.

## Producción

Se activa con la variable `SPRING_PROFILES_ACTIVE=prod`. Todos los datos sensibles se leen de variables de entorno, y no se guardan en el repositorio:

| Variable | Descripción |
|---|---|
| `DB_URL` | URL JDBC de PostgreSQL en Supabase (con `?currentSchema=guarida3d`) |
| `DB_USER` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `ADMIN_EMAIL` | Email del administrador inicial |
| `ADMIN_PASSWORD` | Contraseña del administrador inicial |
| `CLOUDINARY_CLOUD_NAME` | Nombre de la cuenta de Cloudinary |
| `CLOUDINARY_API_KEY` | API key de Cloudinary |
| `CLOUDINARY_API_SECRET` | API secret de Cloudinary |

En producción `ddl-auto` está en `validate`: las tablas y columnas deben existir antes de arrancar. Cada vez que se añade una entidad o un campo (por ejemplo, las tablas `filamentos` y `producto_filamento`, o las columnas `coste_produccion`, `margen_beneficio_aplicado` y `precio_sugerido` en `productos`), hay que crearlo también en la base de datos de producción antes de desplegar.

El endpoint `/actuator/health` es público y sirve para comprobar que la aplicación está viva (por ejemplo, desde un servicio de monitorización externo).

El despliegue está configurado en [Render](https://render.com/) conectado a [Supabase](https://supabase.com/).

## Roles

| Rol | Permisos |
|---|---|
| `USUARIO` | Favoritos, conversaciones y solicitudes de piezas |
| `ENCARGADO` | Lo anterior y la bandeja de mensajes: leer, responder y cambiar el estado de las solicitudes. No gestiona piezas, filamentos, calculadora, categorías ni impresora |
| `ADMIN` | Todo lo anterior y la gestión de piezas, filamentos, calculadora de costes, categorías e impresora |

## Hoja de ruta

- [x] Catálogo, filtros y detalle de pieza con carrusel
- [x] Registro, login y administrador inicial
- [x] Administración de piezas con imágenes en Cloudinary
- [x] Favoritos
- [x] Conversaciones y mensajes con avisos de mensajes sin leer
- [x] Estados de la solicitud y precio acordado
- [x] Rol `ENCARGADO` con bandeja de mensajes
- [x] Estado de la impresora editable desde administración
- [x] Etiqueta de novedad dinámica en la home
- [x] Casilla y página de política de privacidad
- [x] Gestión de inventario de filamentos (`€/kg`) y asignación multicolor por gramos
- [x] Calculadora de costes y margen integrada con persistencia en piezas
- [x] Despliegue en producción (Render + Supabase PostgreSQL)
- [x] Direcciones de envío en el perfil de usuario
- [x] Botón "Borrar mi cuenta" en el perfil de usuario
- [x] Gestión y borrado de cuentas de usuario desde administración (`ADMIN`)
- [ ] Historial y estadísticas de ventas

## Seguridad

- Las contraseñas se guardan cifradas con BCrypt.
- Las credenciales de la base de datos y de Cloudinary nunca se suben al repositorio.
- Las rutas `/admin/mensajes/**` son accesibles para `ADMIN` y `ENCARGADO`; el resto de `/admin/**`, solo para `ADMIN`.
- Cada conversación solo la puede abrir su dueño o el equipo.

## Licencia

Pendiente de elegir.