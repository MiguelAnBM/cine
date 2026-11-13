# División del Proyecto Cine Comfenalco — Trabajo Vertical entre 3 Integrantes

## 1. Objetivo

Dividir la construcción **de este mismo proyecto** (no una versión nueva) entre 3 integrantes, de forma **vertical**: cada persona implementa su objeto de punta a punta — entidad, enums, DTOs relacionados, repositorio, servicio(s), controlador(es) y vista(s) (templates).

Quedan **por aparte** (no se asignan a ningún corte vertical):

- Clases generales: `config/`, `utils/`, `exception/`, `CineApplication.java`, `application.properties`.
- Layout y fragments compartidos: `layout.html`, `fragments/sidebar.html`, `fragments/topbar.html`, `fragments/flash.html`.
- MVC global: `GlobalModelAttributes.java`, páginas de error.
- **CSS, imágenes y JS** (`static/`).

## 2. Criterio de reparto

Cada integrante recibe un dominio de negocio coherente y autocontenido. Regla aplicada:

1. Quien crea una entidad crea sus enums, sus DTOs relacionados y todo lo que toca ese objeto.
2. Las vistas exactas de cada flujo se asignan a quien posee el objeto del flujo.
3. Los DTOs compartidos entre dos flujos del mismo integrante se quedan con él (ej. `VentaRequest` lo usan boletos y cafetería: ambos son de Delany).
4. Los fragments compartidos solo se asignan al integrante dueño si el fragment es específico de su objeto (se asigna `fragments/factura-modal.html` a Delany). El resto es compartido y queda aparte.
5. `js/asientos.js` es del flujo de boletos (Delany) pero se entrega dentro del grupo de recursos estáticos.

## 3. Vista rápida de los 3 cortes

| Corte | Dominio | Entidades | Enums | Repositorios | Servicios | Controladores | Templates |
|---|---|---|---|---|---|---|---|
| **Daniel** | Catálogo y Programación | Pelicula, Sala, Funcion | 6 | 3 | 4 (7 archivos) | 3 | 7 |
| **Delany** | Punto de Venta (boletos + cafetería + facturas) | ProductoConfiteria, Factura, DetalleBoleto, DetalleConfiteria (+ `Asientos`) | 4 | 4 | 4 (8 archivos) | 3 | 5 |
| **Miguel** | Usuarios, Administración y Reportes | Usuario | 2 | 1 | 2 (4 archivos) | 4 | 8 |

---

## 4. Daniel — Catálogo y Programación (Película · Sala · Función)

Responsable del catálogo de películas, las salas y la programación de funciones.

### Entidades (`model/`)
- `src/main/java/com/comfenalco/cine/model/Pelicula.java`
- `src/main/java/com/comfenalco/cine/model/Sala.java`
- `src/main/java/com/comfenalco/cine/model/Funcion.java`

### Enums (`model/enums/`)
- `Clasificacion.java` — de `Pelicula`
- `Genero.java` — de `Pelicula`
- `EstadoPelicula.java` — de `Pelicula`
- `TipoSala.java` — de `Sala`
- `EstadoSala.java` — de `Sala`
- `EstadoFuncion.java` — de `Funcion`

### Repositorios (`repository/`)
- `PeliculaRepository.java`
- `SalaRepository.java` (incluye `ocupacionPorSala(desde, hasta)` → devuelve `OcupacionSalaResponse`, DTO de Miguel)
- `FuncionRepository.java`

### Servicios (`service/` + `service/impl/`)
- `PeliculaService.java` + `impl/PeliculaServiceImpl.java`
- `SalaService.java` + `impl/SalaServiceImpl.java`
- `FuncionService.java` + `impl/FuncionServiceImpl.java`
- `FuncionVendibilidad.java` (clase helper estática, sin interfaz; la usan `PeliculaServiceImpl`, `SalaServiceImpl` y `FuncionServiceImpl`)

### Controladores (`controller/`)
- `CarteleraController.java` — `/cartelera`
- `FuncionController.java` — `/funciones`
- `administracion/SalaAdminController.java` — `/administracion/salas`

### DTOs (`dto/`)
- request: `PeliculaRequest.java`, `FuncionRequest.java`, `SalaRequest.java`
- response: `PeliculaResponse.java`, `FuncionResponse.java`, `SalaResponse.java`

### Vistas exactas (templates)
- `src/main/resources/templates/cartelera/lista.html`
- `src/main/resources/templates/cartelera/form.html`
- `src/main/resources/templates/funciones/lista.html`
- `src/main/resources/templates/funciones/detalle.html`
- `src/main/resources/templates/funciones/form.html`
- `src/main/resources/templates/administracion/salas/lista.html`
- `src/main/resources/templates/administracion/salas/form.html`

### Dependencias que debe respetar
- `FuncionServiceImpl` llama a `DetalleBoletoService` (de Delany) para contar boletos y bloquear edición/eliminación de funciones con boletos vendidos. Acordar la interfaz `DetalleBoletoService` con Delany antes de codificar.
- `PeliculaServiceImpl` y `SalaServiceImpl` consultan `FuncionRepository` para impedir desactivar película/sala con funciones vigentes (usa `FuncionVendibilidad`).

---

## 5. Delany — Punto de Venta (Boletos · Cafetería · Facturas)

Responsable de todo el flujo de venta: selección de función y asientos, carrito de confitería, cobro, emisión e impresión de factura.

### Entidades (`model/`)
- `src/main/java/com/comfenalco/cine/model/ProductoConfiteria.java`
- `src/main/java/com/comfenalco/cine/model/Factura.java`
- `src/main/java/com/comfenalco/cine/model/DetalleBoleto.java`
- `src/main/java/com/comfenalco/cine/model/DetalleConfiteria.java`
- `src/main/java/com/comfenalco/cine/model/Asientos.java` (clase de constantes, no entidad JPA; filas A–H, columnas 1–10)

### Enums (`model/enums/`)
- `CategoriaProducto.java` — de `ProductoConfiteria`
- `EstadoProducto.java` — de `ProductoConfiteria`
- `MetodoPago.java` — de `Factura`
- `EstadoFactura.java` — de `Factura`

### Repositorios (`repository/`)
- `ProductoConfiteriaRepository.java`
- `FacturaRepository.java` (interfaz `Repository`, sin delete expuesto)
- `DetalleBoletoRepository.java` (interfaz `Repository`)
- `DetalleConfiteriaRepository.java` (interfaz `Repository`)

### Servicios (`service/` + `service/impl/`)
- `ProductoConfiteriaService.java` + `impl/ProductoConfiteriaServiceImpl.java`
- `CarritoService.java` + `impl/CarritoServiceImpl.java` (`@SessionScope`)
- `DetalleBoletoService.java` + `impl/DetalleBoletoServiceImpl.java`
- `VentaService.java` + `impl/VentaServiceImpl.java` (único creador/modificador de facturas)

### Controladores (`controller/`)
- `BoletoController.java` — `/boletos`
- `CafeteriaController.java` — `/cafeteria`
- `FacturaController.java` — `/facturas` (incluye `/facturas/{id}/descargar-txt`)

### DTOs (`dto/`)
- request: `VentaRequest.java` (compartido por boletos y cafetería), `ItemConfiteriaRequest.java`, `PagoCafeteriaRequest.java`, `ProductoConfiteriaRequest.java`
- response: `AsientoResponse.java`, `BoletoResponse.java`, `ProductoVendidoResponse.java`, `ProductoConfiteriaResponse.java`, `RenglonCarritoResponse.java`, `CarritoResumenResponse.java`, `FacturaResponse.java`, `FacturaResumenResponse.java`, `FacturaAdminResponse.java`
- resumen: `ConteoPorFuncion.java` (lo consume `DetalleBoletoServiceImpl`)

### Vistas exactas (templates)
- `src/main/resources/templates/boletos/index.html`
- `src/main/resources/templates/cafeteria/index.html`
- `src/main/resources/templates/cafeteria/producto-form.html`
- `src/main/resources/templates/facturas/imprimir.html`
- `src/main/resources/templates/fragments/factura-modal.html` (fragment específico de factura; lo reutilizan también `boletos/index.html`, `cafeteria/index.html` y `administracion/facturas.html`)

### Dependencias que debe respetar
- `VentaServiceImpl` bloquea la función con `FuncionRepository.findByIdForUpdate` (lock pesimista, de Daniel), lee `Pelicula`/`Sala`/`Funcion` (Daniel) y llama a `UsuarioService.obtenerEntidad` (Miguel).
- `VentaServiceImpl` usa `DetalleBoletoService.validarDisponibles` antes de persistir.
- La venta de cafetería es una `Factura` sin boletos (sin función asociada).
- La anulación libera asientos (`asiento_activo`) vía `factura.anular()`; la consume Miguel (`AdministracionController`).

---

## 6. Miguel — Usuarios, Administración y Reportes

Responsable de usuarios/taquilleros (incluye autenticación), panel de administración, anulaciones, facturas históricas y reportes. También lleva la página de inicio (dashboard con resumen del día, basado en `ReporteService`).

### Entidad (`model/`)
- `src/main/java/com/comfenalco/cine/model/Usuario.java`

### Enums (`model/enums/`)
- `RolUsuario.java` — de `Usuario`
- `EstadoUsuario.java` — de `Usuario`

### Repositorio (`repository/`)
- `UsuarioRepository.java`

### Servicios (`service/` + `service/impl/`)
- `UsuarioService.java` + `impl/UsuarioServiceImpl.java`
- `ReporteService.java` + `impl/ReporteServiceImpl.java`

### Controladores (`controller/`)
- `InicioController.java` — `/` y `/inicio`
- `ReporteController.java` — `/reportes`
- `administracion/AdministracionController.java` — `/administracion` (panel, anulaciones, facturas)
- `administracion/TaquilleroAdminController.java` — `/administracion/taquilleros`

### DTOs (`dto/`)
- request: `UsuarioCrearRequest.java`, `UsuarioEditarRequest.java`
- response: `UsuarioResponse.java`, `ResumenVentasResponse.java`, `VentaDiariaResponse.java`, `VentaPorPeliculaResponse.java`, `VentaPorProductoResponse.java`, `VentaPorTaquilleroResponse.java`, `FuncionTaquilleraResponse.java`, `OcupacionSalaResponse.java`
- resumen: `ConteoPorUsuario.java`, `TotalPorUsuario.java`

### Vistas exactas (templates)
- `src/main/resources/templates/inicio.html`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/reportes/index.html`
- `src/main/resources/templates/administracion/index.html`
- `src/main/resources/templates/administracion/anulaciones.html`
- `src/main/resources/templates/administracion/facturas.html`
- `src/main/resources/templates/administracion/taquilleros/lista.html`
- `src/main/resources/templates/administracion/taquilleros/form.html`

### Dependencias que debe respetar
- `ReporteServiceImpl` lee repositorios de los 3 cortes (`FacturaRepository`, `DetalleBoletoRepository`, `DetalleConfiteriaRepository` de Delany; `FuncionRepository`, `SalaRepository` de Daniel) y usa `ValidadorRango` (grupo general) y `FuncionTaquilleraResponse`/`OcupacionSalaResponse`.
- `InicioController` usa `FuncionService`, `PeliculaService`, `SalaService` (Daniel) y `ReporteService` (propio).
- `AdministracionController` usa `UsuarioService` (propio) y `VentaService` (Delany) para anular facturas y listarlas.
- `UsuarioService` es consumido por todos los cortes (sidebar, login, dropdowns de taquillero). Exponerlo temprano y estable.

---

## 7. Grupo GENERAL (por aparte, no vertical)

Archivos transversales que no pertenecen a ningún corte. Deben existir primero porque todos dependen de ellos. Se sugiere asignarlos a Miguel (corte más liviano en backend) o rotarlos; lo importante es que estén listos antes de integrar.

- `src/main/java/com/comfenalco/cine/CineApplication.java`
- `src/main/resources/application.properties`
- `src/main/java/com/comfenalco/cine/config/AppConfig.java` (bean `Clock`)
- `src/main/java/com/comfenalco/cine/config/SeguridadConfig.java` (reglas de seguridad, `UserDetailsService`, `/login`, BCrypt)
- `src/main/java/com/comfenalco/cine/utils/ValidadorRango.java`
- `src/main/java/com/comfenalco/cine/exception/NoEncontradoException.java`
- `src/main/java/com/comfenalco/cine/exception/ReglaNegocioException.java`
- `src/main/java/com/comfenalco/cine/exception/WebExceptionHandler.java`
- `src/main/java/com/comfenalco/cine/controller/GlobalModelAttributes.java` (`@ControllerAdvice`: inyecta `nombreUsuarioActual`, `esAdmin`, `inicialesUsuario` en todas las vistas)
- `src/main/resources/templates/layout.html` (fragment `pagina(titulo, seccionActiva, contenido)`)
- `src/main/resources/templates/fragments/sidebar.html`
- `src/main/resources/templates/fragments/topbar.html`
- `src/main/resources/templates/fragments/flash.html`
- `src/main/resources/templates/error/404.html`
- `src/main/resources/templates/error/500.html`

## 8. Recursos estáticos (por aparte)

### CSS → una persona
- `src/main/resources/static/css/app.css`
- `src/main/resources/static/css/tokens.css`
- `src/main/resources/static/css/pages.css`
- `src/main/resources/static/css/icons.css`

### Imágenes → una persona
- `src/main/resources/static/img/posters/default.jpg`
- `src/main/resources/static/img/products/default.jpg`

### JS (se entrega junto con los estáticos)
- `src/main/resources/static/js/app.js`
- `src/main/resources/static/js/asientos.js` (lógica del mapa de asientos; la usa `boletos/index.html` de Delany)

Los posters e imágenes reales de películas/productos los agrega quien gestione contenido; aquí solo cuentan los assets del repo.

---

## 9. Orden de integración sugerido

1. **Base general** (sección 7) + recursos estáticos (sección 8): esqueleto que arranca vacío.
2. **Miguel — `UsuarioService` primero** (los demás cortes lo necesitan para login, sidebar y dropdowns). En paralelo, **Daniel** modela `Pelicula/Sala/Funcion` (nadie depende de sus enums aún).
3. **Daniel** completa servicios y controladores de funciones.
4. **Delany** integra contra Daniel y Miguel (lock de función, validación de boletos, `UsuarioService`).
5. **Miguel** completa reportes y navegación de administración al final (lee los repositorios de todos).
6. Integración final: probar flujo completo cartelera → función → boletos → factura → anulación → reporte.

## 10. Acuerdos entre cortes (contratos que no se pueden romper sin avisar)

- `DetalleBoletoService`: `validarDisponibles`, `tieneBoletosVendidos`, `contarBoletos`, `contarBoletosPorFuncion` — lo consumen Daniel y Delany.
- `UsuarioService`: obtención de entidad y listados de taquilleros — lo consumen los 3 cortes (incl. `GlobalModelAttributes`).
- `VentaService`: `vender(...)` y `anular(...)` — lo consume Miguel (`AdministracionController`).
- `FuncionVendibilidad`: reglas de "función vendible / finalizada" — usada por Daniel y Miguel.
- `VentaRequest` es el payload único de checkout boletos y cafetería (Delany).
- `fragments/factura-modal.html` (Delany) es reutilizado por `administracion/facturas.html` (Miguel): cambios acordados entre ambos.
- `layout.html` y `fragments/*` (general): cualquier cambio afecta a todas las vistas.

## 11. Checklist por persona

### Daniel
- [ ] Enums: Clasificacion, Genero, EstadoPelicula, TipoSala, EstadoSala, EstadoFuncion
- [ ] Entidades: Pelicula, Sala, Funcion
- [ ] Repositorios: PeliculaRepository, SalaRepository, FuncionRepository
- [ ] Servicios: PeliculaService(+Impl), SalaService(+Impl), FuncionService(+Impl), FuncionVendibilidad
- [ ] DTOs: PeliculaRequest, PeliculaResponse, FuncionRequest, FuncionResponse, SalaRequest, SalaResponse
- [ ] Controladores: CarteleraController, FuncionController, SalaAdminController
- [ ] Vistas: cartelera/lista, cartelera/form, funciones/lista, funciones/detalle, funciones/form, administracion/salas/lista, administracion/salas/form

### Delany
- [ ] Enums: CategoriaProducto, EstadoProducto, MetodoPago, EstadoFactura
- [ ] Entidades: ProductoConfiteria, Factura, DetalleBoleto, DetalleConfiteria, Asientos
- [ ] Repositorios: ProductoConfiteriaRepository, FacturaRepository, DetalleBoletoRepository, DetalleConfiteriaRepository
- [ ] Servicios: ProductoConfiteriaService(+Impl), CarritoService(+Impl), DetalleBoletoService(+Impl), VentaService(+Impl)
- [ ] DTOs: VentaRequest, ItemConfiteriaRequest, PagoCafeteriaRequest, ProductoConfiteriaRequest, ProductoConfiteriaResponse, AsientoResponse, BoletoResponse, ProductoVendidoResponse, RenglonCarritoResponse, CarritoResumenResponse, FacturaResponse, FacturaResumenResponse, FacturaAdminResponse, ConteoPorFuncion
- [ ] Controladores: BoletoController, CafeteriaController, FacturaController
- [ ] Vistas: boletos/index, cafeteria/index, cafeteria/producto-form, facturas/imprimir, fragments/factura-modal

### Miguel
- [ ] Enums: RolUsuario, EstadoUsuario
- [ ] Entidad: Usuario
- [ ] Repositorio: UsuarioRepository
- [ ] Servicios: UsuarioService(+Impl), ReporteService(+Impl)
- [ ] DTOs: UsuarioCrearRequest, UsuarioEditarRequest, UsuarioResponse, ResumenVentasResponse, VentaDiariaResponse, VentaPorPeliculaResponse, VentaPorProductoResponse, VentaPorTaquilleroResponse, FuncionTaquilleraResponse, OcupacionSalaResponse, ConteoPorUsuario, TotalPorUsuario
- [ ] Controladores: InicioController, ReporteController, AdministracionController, TaquilleroAdminController
- [ ] Vistas: inicio, login, reportes/index, administracion/index, administracion/anulaciones, administracion/facturas, administracion/taquilleros/lista, administracion/taquilleros/form

### Grupo general (aparte)
- [ ] CineApplication.java, application.properties
- [ ] config/AppConfig.java, config/SeguridadConfig.java
- [ ] utils/ValidadorRango.java
- [ ] exception/NoEncontradoException.java, ReglaNegocioException.java, WebExceptionHandler.java
- [ ] controller/GlobalModelAttributes.java
- [ ] templates/layout.html, fragments/sidebar.html, fragments/topbar.html, fragments/flash.html, error/404.html, error/500.html

### Recursos estáticos (aparte)
- [ ] CSS: app.css, tokens.css, pages.css, icons.css
- [ ] Imágenes: static/img/posters/default.jpg, static/img/products/default.jpg
- [ ] JS: app.js, asientos.js (este último alineado con Delany)

## 12. Cobertura total

Los 3 cortes + grupo general + recursos estáticos cubren el 100% de los archivos de `src/`: 133 archivos fuente repartidos sin solapamiento (verificado contra el árbol del repositorio).
