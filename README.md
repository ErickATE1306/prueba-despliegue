# Inventario de productos

Aplicación de demostración para registrar, buscar, editar y eliminar productos.
Está hecha con Java 25, Spring Boot 4, Gradle y PostgreSQL. Los productos
persisten en la base de datos al reiniciar la aplicación.

## Ejecutar en Windows

Se necesita JDK 25 y PostgreSQL en ejecución. Crea la base de datos
`inventario_productos` y configura la conexión en PowerShell, desde la carpeta
del proyecto:

```powershell
$env:DB_URL = ''
$env:DB_USER = ''
$env:DB_PASSWORD = '<contraseña de PostgreSQL>'
.\gradlew.bat bootRun
```

La aplicación crea la tabla `productos` si aún no existe. La base de datos
comienza vacía; puedes cargar ejemplos con `APP_DEMO_DATA_ENABLED=true` si lo
necesitas. Abre <http://localhost:8080>. Para ejecutar las pruebas:

```powershell
.\gradlew.bat test
```

## API

| Método | Ruta | Acción |
| --- | --- | --- |
| `GET` | `/api/productos` | Listar productos; acepta `?busqueda=texto` |
| `GET` | `/api/productos/{id}` | Consultar un producto |
| `POST` | `/api/productos` | Crear |
| `PUT` | `/api/productos/{id}` | Actualizar |
| `DELETE` | `/api/productos/{id}` | Eliminar |

El cuerpo de `POST` y `PUT` usa `codigo`, `nombre`, `categoria`, `precio` y `stock`.
El endpoint `/actuator/health` permite comprobar si la aplicación responde.

## Capas

- `model`: datos del producto.
- `dto`: entrada y salida de la API con validaciones.
- `repository`: almacenamiento persistente en PostgreSQL mediante JDBC.
- `service`: reglas de negocio y búsqueda.
- `controller`: API REST y respuestas de error.
- `templates/index.html`: página principal.
- `static/css/styles.css`: estilos de la página.
- `static/js/app.js`: interacción de la página con la API.

Las pruebas usan una base H2 temporal y no necesitan PostgreSQL. Para ejecutar
la aplicación en Docker o Kubernetes se debe configurar `DB_URL`, `DB_USER` y
`DB_PASSWORD` en el contenedor. Dentro del contenedor, `localhost` apunta al
propio contenedor, no al PostgreSQL instalado en Windows.
