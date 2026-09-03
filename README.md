# 🧾 Global Invoice

## 👨‍💻 Autor

**Juan Sierra**<br>
Full-Stack Software Developer

## 🎯 Contexto de la prueba tecnica

La prueba tecnica plantea el desarrollo de una solucion para **Global Invoice**, una empresa que necesita administrar el ciclo basico de sus facturas desde una interfaz web segura. No se trata solo de registrar un valor: cada factura puede pertenecer a una categoria tributaria distinta y debe calcular sus impuestos de acuerdo con sus reglas.

La aplicacion permite trabajar con facturas **nacionales**, de **exportacion** y de **gobierno**. Las de exportacion requieren un codigo de aduana; las demas no. Para evitar inconsistencias, el numero de factura debe ser unico y el sistema conserva el subtotal, los impuestos aplicados, las retenciones y el total final.

El caso de negocio tambien separa las responsabilidades de sus usuarios: el **Operador** registra facturas y consulta su informacion, mientras que el **Auditor** puede consultar las facturas ya registradas y analizar sus totales en un dashboard. Esta solucion implementa dichos flujos mediante un frontend Angular y un API REST con Spring Boot, autenticados con JWT y conectados a PostgreSQL.

## ✨ Requerimientos cumplidos

| Requerimiento | Implementacion |
| --- | --- |
| Inicio de sesion | Endpoint de autenticacion y pantalla de login con usuario, contrasena y opcion para mostrarla. |
| Seguridad | JWT en cada solicitud protegida, sesion sin estado y contrasenas almacenadas como hashes BCrypt. |
| Roles | Operador crea, lista y consulta facturas. Auditor consulta el listado, el detalle y el dashboard. |
| Tipos de factura | NATIONAL, EXPORT y GOVERNMENT, configurados inicialmente en PostgreSQL. |
| Reglas tributarias | Patron Strategy para calcular IVA, retencion y total segun el tipo de factura. |
| Factura de exportacion | Exige codigo de aduana; los otros tipos no lo permiten. |
| Consultas | Listado con informacion basica y vista de detalle con el desglose tributario completo. |
| Total en letras | Integracion SOAP con DataFlex Number Conversion para obtener el total en palabras sin la etiqueta `dollars`. |
| Dashboard | Totales por tipo de factura, representados en un grafico circular con porcentajes. |
| Integridad de datos | Numero de factura unico, incluso entre tipos de factura distintos. |
| Datos iniciales | `data.sql` idempotente inserta usuarios y configuraciones tributarias sin duplicarlos al reiniciar. |
| Calidad | Pruebas unitarias, de controlador, repositorio, seguridad e integracion. Cobertura minima de 80% en backend. |
| Integracion continua | GitHub Actions ejecuta pruebas y construccion de backend y frontend al integrar cambios en `main`. |

## 👥 Roles y permisos

| Rol | Acciones permitidas |
| --- | --- |
| `OPERATOR` | Crear facturas, consultar listado y consultar detalle. |
| `AUDITOR` | Consultar listado, detalle y dashboard. |

## 🧰 Tecnologias

### Backend

- Java 21 y Spring Boot 4
- Spring MVC, Spring Data JPA y Spring Security
- JWT con JJWT
- PostgreSQL
- Maven, Lombok y Bean Validation
- JUnit 5, Mockito, MockMvc y JaCoCo

### Frontend

- Angular 22 y TypeScript
- RxJS y formularios reactivos
- Chart.js para el dashboard
- SCSS y fuente Roboto
- Iconos Lucide mediante `@ng-icons`

### Automatizacion

- GitHub Actions
- Node.js 22 en CI
- PostgreSQL 16 en CI

## 🏗️ Arquitectura

El frontend consume el API mediante rutas relativas y, en desarrollo, Angular redirige `/api` a `http://localhost:8080` mediante su proxy.

```text
Navegador (Angular)
        |
        | HTTP + JWT
        v
API REST (Controllers)
        |
        v
Servicios de aplicacion
        |
        +--> Estrategias tributarias
        +--> Cliente SOAP de conversion a letras
        |
        v
Repositorios JPA
        |
        v
PostgreSQL
```

El backend esta organizado por responsabilidad:

- `controller`: expone los endpoints REST.
- `service`: coordina los casos de uso.
- `strategy`: encapsula el calculo de impuestos para cada tipo.
- `repository`: accede a PostgreSQL mediante JPA.
- `entity` y `dto`: separan el modelo persistente de los contratos HTTP.
- `security`: autentica JWT y aplica permisos por rol.
- `exception`: representa errores de negocio.

## 🗂️ Estructura del proyecto

```text
Proyecto/
|- backend/                 # API REST Spring Boot
|  |- src/main/java/        # Capas, seguridad y reglas de negocio
|  |- src/main/resources/   # Configuracion y data.sql
|  `- src/test/             # Pruebas backend
|- frontend/                # Aplicacion Angular
|  |- src/app/core/         # Servicios, interceptor y guards
|  |- src/app/features/     # Login, facturas y dashboard
|  |- src/app/shared/       # Shell y utilidades compartidas
|  `- src/app/**/*.spec.ts  # Pruebas frontend
`- .github/workflows/ci.yml # Pipeline de integracion continua
```

## ⚙️ Requisitos previos

- Java 21.
- PostgreSQL 16 o compatible.
- Node.js 22 y npm.
- Git.

## 🗄️ Configuracion de PostgreSQL

Desde una consola de PostgreSQL, cree el usuario y la base de datos:

```sql
CREATE USER globalinvoice WITH PASSWORD 'root1';
CREATE DATABASE global_invoice OWNER globalinvoice;
```

La aplicacion usa por defecto estas propiedades:

```yaml
url: jdbc:postgresql://localhost:5432/global_invoice
username: globalinvoice
password: ${DB_PASSWORD:root1}
```

Para sobrescribir la contrasena sin cambiar el codigo, antes de iniciar el backend defina la variable de entorno en PowerShell:

```powershell
$env:DB_PASSWORD = "tu_contrasena"
```

Hibernate crea o actualiza las tablas y despues ejecuta `backend/src/main/resources/data.sql`. El script registra de forma idempotente los tres tipos tributarios y los usuarios `operator` y `auditor`; las contrasenas se conservan como hashes BCrypt, nunca en texto plano.

## 🚀 Ejecucion local

### 1. Iniciar el backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

El API quedara disponible en `http://localhost:8080`.

### 2. Iniciar el frontend

En otra consola:

```powershell
cd frontend
npm ci
npm start
```

Abra `http://localhost:4200/login`. El proxy de Angular enviara las peticiones `/api` al backend local.

## 🔐 Seguridad y acceso

El login recibe usuario y contrasena en `POST /api/v1/auth/login`. Si las credenciales son validas, responde un token JWT. El frontend lo conserva solo durante la sesion del navegador y agrega el encabezado `Authorization: Bearer <token>` a las llamadas protegidas.

Las credenciales de prueba se cargan mediante `data.sql` y los usuarios disponibles son:

| Usuario | Rol |
| --- | --- |
| `operator` | `OPERATOR` |
| `auditor` | `AUDITOR` |

> Nota: las contrasenas demo se almacenan como hashes BCrypt en la base de datos. Para la prueba tecnica, use las credenciales configuradas en su entorno local, sin publicar hashes o secretos reales.

## 🔌 Endpoints principales

| Metodo | Endpoint | Rol | Descripcion |
| --- | --- | --- | --- |
| `POST` | `/api/v1/auth/login` | Publico | Autentica y genera un JWT. |
| `POST` | `/api/v1/invoices` | `OPERATOR` | Crea una factura y calcula sus valores tributarios. |
| `GET` | `/api/v1/invoices` | `OPERATOR`, `AUDITOR` | Lista las facturas con informacion basica. |
| `GET` | `/api/v1/invoices/{invoiceId}` | `OPERATOR`, `AUDITOR` | Obtiene el detalle completo y el total en letras. |
| `GET` | `/api/v1/dashboard/invoices-by-type` | `AUDITOR` | Obtiene los totales agrupados por tipo. |

Ejemplo de creacion de una factura nacional:

```json
{
  "invoiceNumber": "INV-001",
  "type": "NATIONAL",
  "subtotal": 100000.00
}
```

Ejemplo de factura de exportacion:

```json
{
  "invoiceNumber": "INV-002",
  "type": "EXPORT",
  "subtotal": 100000.00,
  "customsCode": "ADU-2026-001"
}
```

## 🧮 Reglas tributarias

La configuracion inicial se conserva en la tabla `invoice_type_config`:

| Tipo | IVA | Retencion |
| --- | ---: | ---: |
| `NATIONAL` | 19% | 0% |
| `EXPORT` | 0% | 0% |
| `GOVERNMENT` | 19% | 5% |

El patron Strategy permite que cada tipo tenga su propia clase de calculo. Esto evita condicionales extensos y hace mas simple agregar nuevos tipos tributarios.

## ⚠️ Manejo de errores

El API devuelve una estructura uniforme con fecha, codigo HTTP, mensaje y errores por campo cuando corresponde.

| Situacion | Codigo HTTP | Ejemplo de resultado |
| --- | ---: | --- |
| Credenciales invalidas o token ausente/invalido | `401` | `Credenciales invalidas` o acceso no autenticado. |
| Rol sin permiso | `403` | Acceso denegado. |
| Datos de entrada invalidos | `400` | Errores por campo, por ejemplo `subtotal`. |
| Factura inexistente | `404` | Factura no encontrada. |
| Numero de factura duplicado | `409` | Error asociado a `invoiceNumber`. |
| Falla de la conversion externa | `502` | No fue posible convertir el total a letras. |

## 🧪 Pruebas y cobertura

### Backend

```powershell
cd backend
.\mvnw.cmd clean verify
```

Este comando compila, ejecuta las pruebas y valida con JaCoCo una cobertura minima del **80% de instrucciones**. Las pruebas cubren autenticacion, autorizacion, controladores, servicios, repositorios, estrategias tributarias, validaciones e integracion con el cliente de conversion.

El reporte de cobertura se genera en `backend/target/site/jacoco/index.html`.

### Frontend

```powershell
cd frontend
npm run test -- --watch=false
npm run build
```

Las pruebas validan los servicios HTTP, interceptor de autenticacion, guards de rol, login, creacion de facturas y formatos de valores.

## 🔄 Integracion continua

El workflow [ci.yml](.github/workflows/ci.yml) se ejecuta con cada `push` a la rama `main`.

- Backend: inicia PostgreSQL, configura Java 21 y ejecuta `clean verify`.
- Frontend: configura Node.js 22, instala dependencias con `npm ci`, ejecuta pruebas y genera el build de produccion.
- La verificacion del backend incluye la regla de cobertura JaCoCo.

## 📌 Decisiones tecnicas destacadas

- **JWT y roles:** protege los recursos desde el backend y complementa la experiencia de navegacion con guards en Angular.
- **DTOs separados de entidades:** evita exponer detalles internos de persistencia por HTTP.
- **Strategy para impuestos:** cada tipo de factura conserva su propia regla de calculo.
- **Restriccion unica de factura:** se valida en el servicio y tambien se respeta en la base de datos.
- **Datos idempotentes:** `ON CONFLICT DO NOTHING` evita datos duplicados en reinicios.
- **Precision monetaria:** los valores se representan con `BigDecimal` en backend y se muestran con dos decimales en frontend.
- **Diseno:** interfaz Angular con fuente Roboto y una linea visual inspirada en Davivienda.
