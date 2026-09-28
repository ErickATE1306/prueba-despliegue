# Inventario de productos

Aplicación de demostración para registrar, buscar, editar y eliminar productos.
Está hecha con Java 25, Spring Boot 4 y Gradle. Los datos se mantienen en memoria:
al reiniciar la aplicación se cargan de nuevo cuatro productos de ejemplo.

## Ejecutar en Windows

Se necesita JDK 25. Desde la carpeta del proyecto:

```powershell
.\gradlew.bat bootRun
```

Abre <http://localhost:8080>. Para ejecutar las pruebas:

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
- `repository`: almacenamiento temporal en memoria.
- `service`: reglas de negocio y búsqueda.
- `controller`: API REST y respuestas de error.
- `templates/index.html`: página principal.
- `static/css/styles.css`: estilos de la página.
- `static/js/app.js`: interacción de la página con la API.

La configuración de Docker, Kubernetes y Jenkins se hará después, paso a paso.
