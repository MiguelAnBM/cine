# Plan de implementación — CineComfenalco MVC

## 1. Objetivo

Completar la aplicación Spring Boot con una capa MVC Thymeleaf que cubra los 6 wireframes y la pantalla de Administración, manteniendo el backend actual y evitando sobreingeniería.

## 2. Decisiones cerradas

- Inicio, Cartelera, Funciones, Boletos y Cafetería son públicos.
- Solo `/administracion/**` y `/reportes/**` requieren login de administrador.
- Los taquilleros no inician sesión.
- El administrador elige al taquillero en la venta.
- `VentaService.vender` valida que el usuario elegido sea un taquillero activo; el dropdown no es la única defensa.
- El administrador elige al taquillero y autoriza la anulación.
- `VentaService.anular` conserva su regla actual: solo administrador activo.
- El dropdown de anulación solo filtra facturas por taquillero.
- CRUD lógico de salas y taquilleros: crear, listar, editar y activar/desactivar.
- Sin borrado físico de salas ni taquilleros.
- El formulario de taquillero envía `rol=TAQUILLERO` como campo oculto; el controlador lo sobrescribe antes de llamar al servicio.
- El único precio editable es `Sala.precioBoleto`.
- No existe recargo separado ni precio base por función.
- No se agregan campos de precio nuevos.
- No se modifica `VentaRequest`.
- No se modifica el modelo `Usuario`, `Sala` ni `Funcion`.
- No se crea login personalizado: se usa la página por defecto de Spring Security.
- La factura demo del administrador se reasigna a Laura para que Reportes > Ventas por taquillero no muestre administradores.
- Reportes se implementa en la fase de Administración; no es una pantalla pública.
- La serie diaria de Ingresos se muestra en la plantilla para que `ventasPorDia` tenga consumidor.

## 3. Alcance

### Incluye

- Login mínimo de administrador.
- CRUD de salas.
- CRUD de taquilleros.
- Anulación de facturas desde Administración.
- Pantalla de Reportes.
- Las 6 pantallas de los wireframes: 5 públicas y Reportes solo para administrador.
- Venta de boletos y cafetería con selección de taquillero.
- Layout común, sidebar, perfil, tema claro/oscuro y mensajes flash.
- Pruebas mínimas de seguridad, rutas, venta y anulación.
- README y guion de presentación.

### Excluye

- Login de taquilleros.
- Roles múltiples o `@PreAuthorize`.
- CRUD de administradores.
- Borrado físico de salas, taquilleros, películas o funciones.
- Recargo separado por tipo de sala.
- Precio base por función.
- Precio independiente por función.
- REST, API JSON, SPA o framework JavaScript.
- Gráficas externas.
- Anulación fuera de Administración.
- Cambios en concurrencia, `horaFin`, reportes duplicados o rediseño de datos demo.
- Eliminación de métodos de servicio preexistentes sin uso; se auditan y se documentan, pero no se podan en esta entrega.
- Pruebas exhaustivas de todas las reglas de negocio.

## 4. Arquitectura

### Paquetes nuevos

```text
com.comfenalco.cine.controller
com.comfenalco.cine.controller.administracion
```

### Controladores

```text
InicioController
CarteleraController
FuncionController
BoletoController
CafeteriaController
ReporteController
AdministracionController
SalaAdminController
TaquilleroAdminController
GlobalModelAttributes
```

### Templates

```text
templates/layout.html
templates/fragments/sidebar.html
templates/fragments/flash.html
templates/inicio.html
templates/cartelera/lista.html
templates/cartelera/form.html
templates/funciones/lista.html
templates/funciones/form.html
templates/funciones/detalle.html
templates/boletos/index.html
templates/cafeteria/index.html
templates/cafeteria/producto-form.html
templates/reportes/index.html
templates/administracion/index.html
templates/administracion/salas/lista.html
templates/administracion/salas/form.html
templates/administracion/taquilleros/lista.html
templates/administracion/taquilleros/form.html
templates/administracion/anulaciones.html
```

### Estáticos

```text
static/css/app.css
static/js/app.js
```

No se crea `login.html`.

## 5. Rutas

### Públicas

| Método | Ruta | Acción |
|---|---|---|
| GET | `/` | Inicio |
| GET | `/cartelera` | Listado de películas |
| GET | `/cartelera/nueva` | Formulario de película |
| POST | `/cartelera` | Crear película |
| GET | `/cartelera/{id}/editar` | Editar película |
| POST | `/cartelera/{id}` | Guardar película |
| POST | `/cartelera/{id}/estado` | Activar o desactivar película |
| GET | `/funciones` | Listado paginado |
| GET | `/funciones/nueva` | Formulario de función |
| POST | `/funciones` | Crear función |
| GET | `/funciones/{id}` | Detalle de función |
| GET | `/funciones/{id}/editar` | Editar función |
| POST | `/funciones/{id}` | Guardar función |
| POST | `/funciones/{id}/cancelar` | Cancelar función |
| GET | `/boletos` | Selector de función y mapa de asientos |
| POST | `/boletos/venta` | Vender boletos |
| GET | `/cafeteria` | Productos y carrito |
| POST | `/cafeteria/carrito/agregar` | Agregar producto |
| POST | `/cafeteria/carrito/quitar` | Quitar producto |
| POST | `/cafeteria/carrito/vaciar` | Vaciar carrito |
| POST | `/cafeteria/pagar` | Pagar pedido |
| GET | `/cafeteria/productos/nuevo` | Formulario de producto |
| POST | `/cafeteria/productos` | Crear producto |

### Solo administrador

| Método | Ruta | Acción |
|---|---|---|
| GET | `/administracion` | Inicio de Administración |
| GET | `/administracion/salas` | Listado de salas |
| GET | `/administracion/salas/nueva` | Formulario de sala |
| POST | `/administracion/salas` | Crear sala |
| GET | `/administracion/salas/{id}/editar` | Editar sala y precio |
| POST | `/administracion/salas/{id}` | Guardar sala |
| POST | `/administracion/salas/{id}/estado` | Activar o desactivar sala |
| GET | `/administracion/taquilleros` | Listado de taquilleros |
| GET | `/administracion/taquilleros/nuevo` | Formulario de taquillero |
| POST | `/administracion/taquilleros` | Crear taquillero |
| GET | `/administracion/taquilleros/{id}/editar` | Editar taquillero |
| POST | `/administracion/taquilleros/{id}` | Guardar taquillero |
| POST | `/administracion/taquilleros/{id}/estado` | Activar o desactivar taquillero |
| GET | `/administracion/anulaciones` | Selección de taquillero y facturas |
| POST | `/administracion/anulaciones/{idFactura}/anular` | Anular factura |
| GET | `/reportes` | Reportes del día |

### Seguridad por defecto

```text
GET  /login
POST /logout
```

No requieren controlador propio.

## 6. Fases

### Fase 0 — Línea base

**Objetivo:** confirmar que el proyecto compila y que los cambios existentes no se revierten.

**Tareas**

- [ ] Ejecutar `git status --short`.
- [ ] Identificar cambios previos y no revertirlos.
- [ ] Ejecutar `.\mvnw.cmd clean test`.
- [ ] Confirmar que los tests actuales terminan sin fallos.

**Verificación**

- [ ] `.\mvnw.cmd clean test` verde.
- [ ] No hay archivos eliminados accidentalmente.
- [ ] No se crean commits.

**Bloqueo:** no avanzar si los tests base fallan.

---

### Fase 1 — Seguridad mínima y administrador

**Objetivo:** permitir login únicamente al administrador y proteger Administración y Reportes.

**Archivos**

- `src/main/java/com/comfenalco/cine/config/SeguridadConfig.java`
- `src/main/java/com/comfenalco/cine/config/DatosIniciales.java`
- `src/main/java/com/comfenalco/cine/service/UsuarioService.java`
- `src/main/java/com/comfenalco/cine/service/impl/UsuarioServiceImpl.java`

**Tareas**

- [ ] Agregar `UserDetailsService` que cargue solo administradores activos.
- [ ] Configurar `SecurityFilterChain`:
  - [ ] `/administracion/**` y `/reportes/**` con `hasRole("ADMINISTRADOR")`.
  - [ ] Estáticos, `/login` y `/error` con `permitAll()`.
  - [ ] Cualquier otra ruta con `permitAll()`.
- [ ] Usar `formLogin` y `logout` por defecto de Spring.
- [ ] No crear plantilla de login.
- [ ] Inyectar `PasswordEncoder` en `DatosIniciales`.
- [ ] Guardar la contraseña del administrador con BCrypt.
- [ ] Mantener al taquillero con contraseña nula.
- [ ] Agregar `UsuarioService.buscarPorUsername(String)`.

**Credenciales demo**

```text
admin / admin123
```

**Verificación**

- [ ] `.\mvnw.cmd clean test` verde.
- [ ] Iniciar app con `.\mvnw.cmd spring-boot:run`.
- [ ] `GET /` responde sin login.
- [ ] `GET /reportes` redirige a `/login`.
- [ ] `GET /administracion` redirige a `/login`.
- [ ] Login con `admin` / `admin123` funciona.
- [ ] Login con `laura` falla.
- [ ] Con sesión de admin, `/reportes` y `/administracion` cargan.

**Bloqueo:** no avanzar si el administrador sembrado no puede iniciar sesión.

---

### Fase 2 — Servicios de lectura y venta segura

**Objetivo:** obtener taquilleros y facturas sin cambiar la firma de venta ni la regla de anulación, y validar el rol del taquillero en el servicio.

**Archivos**

- `UsuarioService.java`
- `UsuarioServiceImpl.java`
- `UsuarioRepository.java`
- `VentaService.java`
- `VentaServiceImpl.java`
- `FacturaRepository.java`

**Tareas**

- [ ] Agregar `UsuarioService.listarTaquilleros()`.
- [ ] Agregar `UsuarioService.listarTaquillerosActivos()`.
- [ ] Agregar `UsuarioRepository.findByRolOrderByNombreCompleto(RolUsuario)`.
- [ ] Agregar `UsuarioRepository.findByRolAndEstadoOrderByNombreCompleto(RolUsuario, EstadoUsuario)`.
- [ ] Reutilizar `findByUsernameAndEstado` para el login y `buscarPorUsername`.
- [ ] Agregar `VentaService.listarEmitidasPorTaquillero(Long)`.
- [ ] Agregar consulta `findEmitidasPorUsuario` en `FacturaRepository`.
- [ ] Incluir `JOIN FETCH f.usuario`.
- [ ] Filtrar solo `EstadoFactura.EMITIDA`.
- [ ] Ordenar por `fechaHora` descendente.
- [ ] Reutilizar `FacturaResumenResponse`.
- [ ] Mantener la firma de `VentaService.vender`.
- [ ] Agregar en `VentaServiceImpl.vender` la validación de rol `RolUsuario.TAQUILLERO` y estado `EstadoUsuario.ACTIVO`.
- [ ] No modificar `VentaService.anular`.
- [ ] No modificar `VentaRequest`.
- [ ] No agregar entidades ni migraciones.

**Verificación**

- [ ] `.\mvnw.cmd -q -DskipTests compile`.
- [ ] `.\mvnw.cmd clean test`.
- [ ] Los tests existentes siguen verdes.
- [ ] La lista de taquilleros contiene solo usuarios con rol `TAQUILLERO`.
- [ ] La lista de facturas de Laura contiene la factura sembrada emitida.
- [ ] La consulta de facturas usa `JOIN FETCH f.usuario` y no consulta el usuario por cada fila.
- [ ] Una venta con ID de administrador se rechaza.
- [ ] Una venta con taquillero inactivo se rechaza.

---

### Fase 3 — Shell web y layout común

**Objetivo:** crear la estructura visual compartida.

**Archivos**

- `templates/layout.html`
- `templates/fragments/sidebar.html`
- `templates/fragments/flash.html`
- `static/css/app.css`
- `static/js/app.js`
- `GlobalModelAttributes.java`
- `WebExceptionHandler.java`

**Tareas**

- [ ] Crear layout con sidebar, contenido y mensajes flash.
- [ ] Crear sidebar con Inicio, Cartelera, Funciones, Boletos, Cafetería, Administración y Reportes.
- [ ] Marcar enlace activo según la sección.
- [ ] Mostrar perfil:
  - [ ] Admin autenticado: nombre del administrador.
  - [ ] Visitante: “Invitado” y enlace de login.
- [ ] Mostrar logout solo con sesión de admin.
- [ ] Agregar toggle claro/oscuro con `localStorage`.
- [ ] Agregar `usuarioActual` y `esAdmin` al modelo global.
- [ ] No consultar la base de datos para visitantes anónimos.
- [ ] Mantener `WebExceptionHandler`.
- [ ] Agregar manejo de `NoEncontradoException` con flash y redirect.
- [ ] Todos los formularios POST usan `th:action` para incluir CSRF.

**Verificación**

- [ ] `.\mvnw.cmd clean test`.
- [ ] `/` carga sin excepción de plantilla.
- [ ] El CSS y JS cargan sin 404.
- [ ] El toggle de tema funciona.
- [ ] El sidebar no muestra enlaces rotos.
- [ ] Con sesión de admin aparece el nombre y logout.
- [ ] Sin sesión aparece “Invitado” y login.

---

### Fase 4 — Pantallas públicas

**Objetivo:** implementar las 5 pantallas accesibles sin login. Reportes se implementa en la Fase 5.5.

#### 4.1 Inicio

**Tareas**

- [ ] Crear `InicioController`.
- [ ] Cargar `ReporteService.resumen(hoy, hoy)`.
- [ ] Cargar `FuncionService.proximas(5)`.
- [ ] Cargar `FuncionService.peliculasEnCartelera()`.
- [ ] Mostrar tarjetas de accesos rápidos.
- [ ] “Ver todas” enlaza a `/funciones`.

#### 4.2 Cartelera

**Tareas**

- [ ] Crear `CarteleraController`.
- [ ] Listar películas activas con `PeliculaService.listarPorEstado(ACTIVA)`.
- [ ] Crear formulario con `PeliculaRequest`.
- [ ] Implementar crear y editar.
- [ ] “Eliminar” ejecuta `cambiarEstado(id, INACTIVA)`.
- [ ] Mostrar confirmación antes de desactivar.
- [ ] No borrar físicamente.

#### 4.3 Funciones

**Tareas**

- [ ] Crear `FuncionController`.
- [ ] Listar con `FuncionService.listar(PageRequest.of(page, 5))`.
- [ ] Mostrar “Mostrando 5 de X funciones”.
- [ ] Crear formulario con `FuncionRequest`.
- [ ] Cargar películas activas y salas activas para los selectores.
- [ ] Implementar crear, editar, ver y cancelar.
- [ ] El botón de basura ejecuta `cancelar`.
- [ ] Mostrar cantidad de facturas afectadas si aplica.

#### 4.4 Boletos

**Tareas**

- [ ] Crear `BoletoController`.
- [ ] Cargar funciones próximas para el selector.
- [ ] Cargar taquilleros activos.
- [ ] Cargar mapa de asientos con `DetalleBoletoService.mapaDeAsientos`.
- [ ] Mostrar 80 asientos A1–H10.
- [ ] Checkboxes de asientos con máximo 15.
- [ ] Calcular total en pantalla con JS.
- [ ] Incluir método de pago.
- [ ] Incluir dropdown obligatorio de taquillero.
- [ ] Enviar `idTaquillero` como parámetro separado.
- [ ] Construir y validar `VentaRequest`.
- [ ] Llamar `VentaService.vender(idTaquillero, request)`.
- [ ] Redirigir con flash después de vender.
- [ ] No crear wizard de dos pasos.
- [ ] No crear página de comprobante.

#### 4.5 Cafetería

**Tareas**

- [ ] Crear `CafeteriaController`.
- [ ] Listar productos activos.
- [ ] Filtrar por categoría.
- [ ] Crear formulario de producto con `ProductoConfiteriaRequest`.
- [ ] Implementar “Añadir producto”.
- [ ] Carrito en sesión con `Map<Long, Integer>`.
- [ ] Agregar, quitar y vaciar.
- [ ] Mostrar subtotales y total.
- [ ] Incluir dropdown obligatorio de taquillero.
- [ ] Incluir método de pago.
- [ ] Al pagar, reconstruir `VentaRequest` con productos.
- [ ] Revalidar precios en servidor.
- [ ] Limpiar carrito después del pago.
- [ ] No crear entidad Carrito.

**Verificación de Fase 4**

- [ ] `.\mvnw.cmd clean test`.
- [ ] `GET /`, `/cartelera`, `/funciones`, `/boletos`, `/cafeteria` cargan sin login.
- [ ] Crear, editar y desactivar película funciona.
- [ ] Crear, editar y cancelar función funciona.
- [ ] Vender boletos con taquillero seleccionado funciona.
- [ ] La factura queda asociada al taquillero correcto.
- [ ] Vender cafetería con taquillero seleccionado funciona.
- [ ] El carrito se vacía después del pago.
- [ ] Una venta con administrador o taquillero inactivo se rechaza.

---

### Fase 5 — Administración y Reportes

**Objetivo:** implementar CRUD lógico de salas y taquilleros, anulación de facturas y la pantalla de Reportes.

#### 5.1 Inicio de Administración

- [ ] Crear `GET /administracion`.
- [ ] Mostrar accesos a Salas, Taquilleros y Anulaciones.
- [ ] Usar el layout común.

#### 5.2 CRUD de salas

**Tareas**

- [ ] Crear `SalaAdminController`.
- [ ] Listar con `SalaService.listar()`.
- [ ] Crear con `SalaRequest`.
- [ ] Editar nombre, tipo y `precioBoleto`.
- [ ] Activar/desactivar con `SalaService.cambiarEstado`.
- [ ] No borrar físicamente.
- [ ] Mantener la validación que impide desactivar una sala con funciones programadas.

**Precio**

- [ ] El formulario de sala incluye `precioBoleto`.
- [ ] El cambio aplica solo a ventas futuras.
- [ ] Las facturas anteriores no se recalculan.
- [ ] No agregar recargo separado.
- [ ] No agregar precio base por función.

#### 5.3 CRUD de taquilleros

**Tareas**

- [ ] Crear `TaquilleroAdminController`.
- [ ] Listar solo usuarios con rol `TAQUILLERO`.
- [ ] Crear con nombre y username.
- [ ] Reutilizar `UsuarioCrearRequest`.
- [ ] El formulario envía `rol=TAQUILLERO` como campo oculto para pasar la validación.
- [ ] El controlador sobrescribe `rol` con `RolUsuario.TAQUILLERO` antes de llamar al servicio.
- [ ] No solicitar contraseña.
- [ ] Editar con `UsuarioEditarRequest`.
- [ ] Activar/desactivar con `UsuarioService.cambiarEstado`.
- [ ] No borrar físicamente.
- [ ] No permitir cambiar el rol.

#### 5.4 Anulaciones

**Tareas**

- [ ] Crear `GET /administracion/anulaciones`.
- [ ] Dropdown con taquilleros, incluidos inactivos para consultar facturas históricas.
- [ ] Mostrar facturas emitidas del taquillero seleccionado.
- [ ] Usar `VentaService.listarEmitidasPorTaquillero`.
- [ ] Botón “Anular” por factura.
- [ ] Enviar `idTaquillero` junto con la factura.
- [ ] Obtener el administrador autenticado con `Authentication` y `UsuarioService.buscarPorUsername`.
- [ ] Llamar `VentaService.anular(adminId, idFactura)`.
- [ ] Mantener la regla actual de anulación: solo administrador activo.
- [ ] Mostrar flash de éxito o error.
- [ ] Redirigir a la lista con el mismo taquillero seleccionado.

#### 5.5 Reportes

**Tareas**

- [ ] Crear `ReporteController`.
- [ ] Ruta `/reportes` solo para admin.
- [ ] Recibir fecha opcional, por defecto hoy.
- [ ] Llamar los 7 métodos de `ReporteService` usando la misma fecha.
- [ ] Mostrar el resumen de Ingresos.
- [ ] Mostrar la serie diaria con `ventasPorDia` dentro de Ingresos.
- [ ] Mostrar Ocupación, Películas, Taquilleros, Productos y Funciones.
- [ ] Usar tablas simples, sin librería de gráficas.
- [ ] No dejar atributos de modelo sin usar en la plantilla.
- [ ] Reasignar la factura demo `facturaWeb` de `DatosIniciales` a Laura, no al administrador.
- [ ] Verificar que `ventasPorTaquillero` no liste administradores.

**Verificación de Fase 5**

- [ ] `GET /administracion` carga con admin.
- [ ] Crear sala y editar su precio.
- [ ] Desactivar sala sin funciones programadas funciona.
- [ ] Desactivar sala con funciones programadas muestra error.
- [ ] Crear taquillero sin contraseña.
- [ ] Editar nombre y username.
- [ ] Desactivar taquillero.
- [ ] El taquillero inactivo no aparece en venta.
- [ ] El taquillero inactivo sí aparece para consultar sus facturas históricas.
- [ ] El admin anula una factura de un taquillero.
- [ ] La factura queda en estado `ANULADA`.
- [ ] El asiento vuelve a estar disponible.
- [ ] La factura histórica conserva su total aunque cambie el precio de la sala.
- [ ] Reportes carga con admin.
- [ ] Reportes sin sesión redirige a login.
- [ ] La serie diaria de Ingresos se muestra y usa `ventasPorDia`.
- [ ] Ventas por taquillero no muestra al administrador.

---

### Fase 6 — Pruebas, código muerto y regresión final

**Objetivo:** verificar rutas, seguridad, venta, anulación y ausencia de código muerto nuevo sin crear una suite excesiva.

**Dependencia**

- [ ] Agregar `spring-security-test` con alcance `test` en `pom.xml`.

**Pruebas MVC**

- [ ] Crear `MvcSmokeTests` con `@SpringBootTest` y `@AutoConfigureMockMvc`.
- [ ] Verificar que `GET /` responde sin login.
- [ ] Verificar que `GET /reportes` redirige a `/login`.
- [ ] Verificar que `GET /administracion` redirige a `/login`.
- [ ] Verificar login del admin sembrado.
- [ ] Verificar que `/reportes` responde con admin.
- [ ] Verificar que `/administracion`, `/administracion/salas` y `/administracion/taquilleros` responden con admin.
- [ ] Verificar que `/boletos` contiene opciones de taquillero.

**Prueba de venta**

- [ ] Crear una prueba de servicio con transacción y rollback.
- [ ] Llamar `VentaService.vender(adminId, request)` con una solicitud válida.
- [ ] Verificar que se rechaza con `ReglaNegocioException`.
- [ ] Verificar que un taquillero activo sí puede vender.

**Prueba de anulación**

- [ ] Crear una prueba de servicio con transacción y rollback.
- [ ] Usar la factura sembrada de Laura.
- [ ] Obtener el ID del administrador.
- [ ] Llamar `VentaService.anular(adminId, facturaId)`.
- [ ] Verificar estado `ANULADA`.
- [ ] Verificar que no se cambió la regla de permisos del servicio.

**Auditoría de código muerto**

- [ ] Verificar que cada método nuevo de servicio tenga al menos un consumidor.
- [ ] Verificar que cada endpoint nuevo esté enlazado desde una plantilla.
- [ ] Verificar que cada atributo de modelo nuevo se use en su plantilla.
- [ ] Verificar que `ventasPorDia` se muestre en Reportes.
- [ ] Verificar que no queden imports, constantes, DTOs o plantillas nuevas sin uso.
- [ ] Revisar métodos preexistentes sin uso y documentarlos; no eliminarlos sin decisión explícita.

**Verificación final**

- [ ] `.\mvnw.cmd clean test` verde.
- [ ] `.\mvnw.cmd spring-boot:run` inicia sin excepciones.
- [ ] Revisar manualmente las 7 pantallas.
- [ ] Revisar que no haya enlaces rotos.
- [ ] Revisar que no haya errores 500 en flujos normales.
- [ ] Revisar que no haya entidades JPA expuestas en templates.
- [ ] `git diff --check` sin errores.
- [ ] `git status --short` solo muestra cambios esperados.

---

### Fase 7 — Cierre y presentación

**Objetivo:** dejar instrucciones reproducibles y un guion claro para presentar el proyecto.

**Archivos**

- `README.md`

**Tareas**

- [ ] Crear `README.md`.
- [ ] Documentar stack, Java 21, H2 y comandos `.\mvnw.cmd clean test` y `.\mvnw.cmd spring-boot:run`.
- [ ] Documentar credenciales demo `admin` / `admin123`.
- [ ] Documentar rutas públicas y rutas solo para administrador.
- [ ] Documentar el flujo de venta: seleccionar taquillero activo, función y asientos.
- [ ] Documentar el flujo de anulación: admin, taquillero, factura y autorización.
- [ ] Documentar que los reportes usan una fecha y que la demo puede usar la fecha de mañana.
- [ ] Preparar guion de demostración: login admin, venta pública, CRUD de taquillero, cambio de precio, anulación y reportes.
- [ ] Hacer un ensayo completo antes de presentar.
- [ ] Confirmar que `.\mvnw.cmd clean test` sigue verde al cierre.
- [ ] No crear commits sin pedido explícito.

**Verificación**

- [ ] Una persona puede arrancar el proyecto siguiendo solo el README.
- [ ] El guion cubre las 7 pantallas.
- [ ] El guion muestra una venta, una anulación y un reporte con datos.
- [ ] El README no incluye credenciales reales ni secretos.

## 8. Matriz de aceptación

- [ ] Un visitante puede usar Inicio, Cartelera, Funciones, Boletos y Cafetería.
- [ ] Un visitante no puede entrar a Administración ni Reportes.
- [ ] El administrador inicia sesión con `admin` / `admin123`.
- [ ] El taquillero `laura` no puede iniciar sesión.
- [ ] El administrador puede crear, editar y desactivar salas.
- [ ] El administrador puede modificar el precio final de una sala.
- [ ] El administrador puede crear, editar y desactivar taquilleros.
- [ ] El taquillero seleccionado en una venta queda asociado a la factura.
- [ ] Una venta con administrador o taquillero inactivo se rechaza en el servicio.
- [ ] El administrador puede elegir un taquillero y anular una factura.
- [ ] La anulación sigue requiriendo administrador en el servicio.
- [ ] Reportes > Ventas por taquillero no muestra administradores.
- [ ] La serie diaria de Reportes usa `ventasPorDia`.
- [ ] El README permite arrancar y demostrar el proyecto.
- [ ] No hay borrado físico de salas ni taquilleros.
- [ ] No hay recargo separado ni precio base por función.
- [ ] No hay login de taquilleros.
- [ ] No hay código nuevo sin uso.
- [ ] No hay errores de compilación, plantillas o CSRF en flujos normales.

## 9. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Binding de records en Thymeleaf | Usar `th:object`, `th:field` y `@ModelAttribute`; probar cada formulario. |
| Errores CSRF en POST | Todos los formularios usan `th:action`; pruebas con `csrf()`. |
| `LazyInitializationException` | Controladores solo usan DTOs; `open-in-view=false` se mantiene. |
| Precios históricos alterados | `DetalleBoleto` conserva `precioUnitario`; no se recalcula. |
| Venta con rol o estado incorrecto | `VentaServiceImpl.vender` valida rol `TAQUILLERO` y estado `ACTIVO`; no confiar solo en el dropdown. |
| Anulación por actor incorrecto | El servicio recibe el ID del admin autenticado, no el del dropdown. |
| Formularios largos con errores | Usar `BindingResult` y repoblar selectores en el mismo método. |
| Pantalla Administración sin wireframe | Mantenerla simple y coherente con el layout. |
| Datos demo con funciones futuras | Reasignar la factura demo a Laura y usar la fecha de mañana en la demostración; documentarlo en el README. |
| Reportes con datos incompletos | Mostrar `ventasPorDia` y verificar que Ventas por taquillero no incluya administradores. |
| Código muerto preexistente | Auditarlo y documentarlo; no podar métodos de servicio sin decisión explícita. |

## 10. Criterio de terminado

La implementación termina cuando:

1. `.\mvnw.cmd clean test` pasa completo.
2. La aplicación inicia sin errores.
3. Las 5 pantallas públicas funcionan sin login.
4. Reportes funciona solo con administrador.
5. Administración exige login de administrador.
6. El CRUD lógico de salas y taquilleros funciona.
7. La venta valida taquillero activo y rol `TAQUILLERO`.
8. La anulación la autoriza el administrador y filtra por taquillero.
9. Reportes muestra la serie diaria y no lista administradores como taquilleros.
10. No se modificaron reglas de negocio fuera del alcance.
11. No hay borrado físico ni campos de precio nuevos.
12. El README permite arrancar y presentar el proyecto.
13. No hay código nuevo sin uso, enlaces rotos ni errores 500 en flujos normales.
