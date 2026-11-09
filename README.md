# La Guarida Impresionante

Web de un proyecto personal **sin ánimo de lucro** para mostrar piezas impresas en 3D. Los visitantes pueden explorar el catálogo, guardar sus piezas favoritas y, más adelante, escribir al creador para pedirlas y ayudar a cubrir el coste del filamento y el mantenimiento de la impresora.

> Esta web **no es una tienda**: no hay precios ni pasarela de pago. Las piezas se ofrecen como apoyo al creador.

## Funcionalidades

**Parte pública y de usuario**
- Catálogo de piezas con filtros por categoría.
- Detalle de cada pieza con carrusel de imágenes de tamaño fijo, material, tiempo de impresión y categoría.
- Registro e inicio de sesión (contraseñas cifradas con BCrypt).
- Favoritos: corazón en las tarjetas y en el detalle, y página "Mis favoritos".
- Diseño colorido, responsive y con Bootstrap 5.

**Administración** (solo rol `ADMIN`)
- Listado de piezas, con opción de ocultar, mostrar y borrar.
- Alta y edición de piezas con varias imágenes, elección de la imagen de portada y vista previa antes de guardar.
- Las imágenes se suben a Cloudinary, en una carpeta propia del proyecto.
- Creación automática del administrador inicial al arrancar.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Actuator) |
| Plantillas | Thymeleaf + Thymeleaf Extras Spring Security |
| Base de datos | PostgreSQL |
| Imágenes | Cloudinary |
| Interfaz | Bootstrap 5.3 (CDN), CSS y JavaScript propios |
| Build | Maven |

## Estructura del proyecto

```
src/main/java/com/guaridaludica/guaridaimpresionante/
├── config/        Seguridad, Cloudinary y carga inicial de datos
├── controller/    Controladores web y formularios
├── model/         Entidades JPA (Producto, ImagenProducto, Usuario, Favorito, Rol)
├── repository/    Repositorios Spring Data
├── security/      UserDetailsService
└── service/       Lógica de negocio (Cloudinary, administración, favoritos)

src/main/resources/
├── application.yaml          Configuración común
├── application-dev.yaml      Perfil de desarrollo (no se sube al repositorio)
├── application-prod.yaml     Perfil de producción (variables de entorno)
├── static/
│   ├── css/                  Estilos por módulos (base, componentes, layout, home, catalogo, pieza, formularios, admin)
│   ├── js/                   Scripts por pantalla
│   └── img/                  Logos y favicon
└── templates/
    ├── fragments/            Cabecera, navbar y pie reutilizables
    └── admin/                Pantallas de administración
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
| `DB_URL` | URL JDBC de PostgreSQL (con `?currentSchema=guarida3d`) |
| `DB_USER` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `ADMIN_EMAIL` | Email del administrador inicial |
| `ADMIN_PASSWORD` | Contraseña del administrador inicial |
| `CLOUDINARY_CLOUD_NAME` | Nombre de la cuenta de Cloudinary |
| `CLOUDINARY_API_KEY` | API key de Cloudinary |
| `CLOUDINARY_API_SECRET` | API secret de Cloudinary |

En producción `ddl-auto` está en `validate`: las tablas deben existir antes de arrancar.

El endpoint `/actuator/health` es público y sirve para comprobar que la aplicación está viva (por ejemplo, desde un servicio de monitorización externo).

El despliegue previsto es en [Render](https://render.com/) con PostgreSQL en la nube.

## Roles

| Rol | Estado | Permisos |
|---|---|---|
| `USUARIO` | Implementado | Favoritos y, próximamente, consultas y solicitudes |
| `ADMIN` | Implementado | Todo lo anterior y la gestión de piezas |
| `ENCARGADO` | Previsto | Leer y responder mensajes, sin gestionar piezas |

## Hoja de ruta

- [x] Catálogo, filtros y detalle de pieza con carrusel
- [x] Registro, login y administrador inicial
- [x] Administración de piezas con imágenes en Cloudinary
- [x] Favoritos
- [ ] Conversaciones y mensajes (empiezan como consulta y pasan a solicitud al confirmarlas)
- [ ] Estados de la solicitud (solicitada, aceptada, en impresión, lista, entregada)
- [ ] Rol `ENCARGADO` con bandeja de mensajes
- [ ] Estado de la impresora editable desde administración
- [ ] Casilla de política de privacidad en el registro y dirección opcional en los datos del usuario
- [ ] Despliegue en producción

## Seguridad

- Las contraseñas se guardan cifradas con BCrypt.
- Las credenciales de la base de datos y de Cloudinary nunca se suben al repositorio.
- Las rutas `/admin/**` solo son accesibles para el rol `ADMIN`.

## Licencia

Pendiente de elegir.
