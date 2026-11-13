package com.comfenalco.cine.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.DetalleBoletoService;
import com.comfenalco.cine.service.FuncionVendibilidad;
import com.comfenalco.cine.service.UsuarioService;
import com.comfenalco.cine.service.VentaService;
import com.comfenalco.cine.dto.response.FacturaAdminResponse;
import com.comfenalco.cine.dto.response.FacturaResponse;
import com.comfenalco.cine.dto.response.FacturaResumenResponse;
import com.comfenalco.cine.dto.request.ItemConfiteriaRequest;
import com.comfenalco.cine.dto.request.VentaRequest;
import com.comfenalco.cine.exception.NoEncontradoException;
import com.comfenalco.cine.exception.ReglaNegocioException;
import com.comfenalco.cine.model.DetalleBoleto;
import com.comfenalco.cine.model.DetalleConfiteria;
import com.comfenalco.cine.model.Factura;
import com.comfenalco.cine.model.Funcion;
import com.comfenalco.cine.model.ProductoConfiteria;
import com.comfenalco.cine.model.Usuario;
import com.comfenalco.cine.model.enums.EstadoFactura;
import com.comfenalco.cine.model.enums.EstadoProducto;
import com.comfenalco.cine.model.enums.EstadoUsuario;
import com.comfenalco.cine.model.enums.RolUsuario;
import com.comfenalco.cine.repository.FacturaRepository;
import com.comfenalco.cine.repository.FuncionRepository;
import com.comfenalco.cine.repository.ProductoConfiteriaRepository;

import java.time.Clock;

@Service
@Transactional(readOnly = true)
public class VentaServiceImpl implements VentaService {

    private static final String RESTRICCION_ASIENTO = "uq_boleto_funcion_asiento";

    private final FacturaRepository facturaRepository;
    private final FuncionRepository funcionRepository;
    private final ProductoConfiteriaRepository productoRepository;
    private final UsuarioService usuarioService;
    private final DetalleBoletoService detalleBoletoService;
    private final Clock reloj;

    public VentaServiceImpl(FacturaRepository facturaRepository,
                            FuncionRepository funcionRepository,
                            ProductoConfiteriaRepository productoRepository,
                            UsuarioService usuarioService,
                            DetalleBoletoService detalleBoletoService,
                            Clock reloj) {
        this.facturaRepository = facturaRepository;
        this.funcionRepository = funcionRepository;
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.detalleBoletoService = detalleBoletoService;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public FacturaResponse vender(Long idUsuario, VentaRequest request) {

        validarSolicitud(request);

        boolean hayBoletos = request.asientos() != null && !request.asientos().isEmpty();
        boolean hayProductos = request.productos() != null && !request.productos().isEmpty();

        if (!hayBoletos && !hayProductos) {
            throw new ReglaNegocioException("La venta debe incluir al menos un boleto o un producto");
        }
        if (hayBoletos && request.idFuncion() == null) {
            throw new ReglaNegocioException("Indica la función de los boletos");
        }
        if (!hayBoletos && request.idFuncion() != null) {
            throw new ReglaNegocioException("Selecciona al menos un asiento");
        }

        // 1. Lo primero es bloquear la función: las ventas de la misma función se hacen en fila
        Funcion funcion = hayBoletos ? bloquearFuncionVendible(request.idFuncion()) : null;

        // 2. Quién vende
        Usuario taquillero = usuarioService.obtenerEntidad(idUsuario);
        if (taquillero.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ReglaNegocioException("El usuario no está activo");
        }
        if (taquillero.getRol() != RolUsuario.TAQUILLERO) {
            throw new ReglaNegocioException("Solo un taquillero activo puede realizar ventas");
        }

        // 3. Asientos: existen, no se repiten y están libres
        if (hayBoletos) {
            detalleBoletoService.validarDisponibles(funcion.getId(), request.asientos());
        }

        // 4. Se arma la factura. Los precios salen siempre de la BD
        Factura factura = new Factura(taquillero, request.metodoPago(),
                request.nombreCliente(), request.correoCliente());

        if (hayBoletos) {
            for (String codigo : request.asientos()) {
                factura.agregarBoleto(new DetalleBoleto(funcion, codigo));
            }
        }

        if (hayProductos) {
            for (Map.Entry<Long, Integer> linea : consolidar(request.productos()).entrySet()) {
                factura.agregarProducto(crearDetalleConfiteria(linea.getKey(), linea.getValue()));
            }
        }

        // 5. Se guarda la factura con todo su contenido, en una sola transacción
        try {
            factura = facturaRepository.save(factura);
        } catch (DataIntegrityViolationException ex) {
            if (esAsientoDuplicado(ex)) {
                throw new ReglaNegocioException(
                        "Uno de los asientos acaba de ser vendido. Actualiza el mapa e intenta de nuevo");
            }
            throw ex;
        }

        return FacturaResponse.de(factura);
    }

    @Override
    public FacturaResponse buscarPorId(Long id) {

        Factura factura = facturaRepository.findDetalleById(id)
                .orElseThrow(() -> new NoEncontradoException("La factura no existe"));

        // Es la misma entidad: esta consulta solo termina de cargar sus productos
        factura = facturaRepository.findConProductosById(id)
                .orElseThrow(() -> new NoEncontradoException("La factura no existe"));

        return FacturaResponse.de(factura);
    }

    // Pagina primero las facturas en base de datos; luego carga boletos y productos
    // de solo los IDs de la página (evita cargar todas las facturas del taquillero).
    // Tamaño máximo de página limitado a 50 para no traer demasiadas facturas de una vez.
    @Override
    public Page<FacturaResumenResponse> listarEmitidasPorTaquillero(Long idTaquillero, Pageable pageable) {

        if (idTaquillero == null) {
            return Page.empty();
        }

        // 1. Paginación en base de datos: trae solo la página solicitada
        Page<Factura> paginaFacturas = facturaRepository.findEmitidasPorUsuarioPaginada(
                idTaquillero, pageable);

        if (paginaFacturas.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2. Extrae los IDs de la página para cargar detalles en lote (sin N+1)
        List<Long> idsFacturas = paginaFacturas.getContent().stream()
                .map(Factura::getId)
                .toList();

        // 3. Carga boletos y productos de esos IDs (2 consultas batch, no por factura).
        // Hibernate hidrata las mismas entidades en el contexto de persistencia:
        // después de estas queries, las facturas de la página ya tienen boletos y productos.
        facturaRepository.findConBoletosByIdIn(idsFacturas);
        facturaRepository.findConProductosByIdIn(idsFacturas);

        // 4. Mapea a DTO preservando el orden de la página original
        List<FacturaResumenResponse> resumen = new ArrayList<>();
        for (Factura f : paginaFacturas.getContent()) {
            resumen.add(FacturaResumenResponse.de(f));
        }

        return new PageImpl<>(resumen, pageable, paginaFacturas.getTotalElements());
    }

    // Todas las facturas con filtros opcionales. Mismo patrón que listarEmitidasPorTaquillero:
    // pagina en BD, luego carga boletos y productos de la página en 2 queries batch.
    @Override
    public Page<FacturaAdminResponse> listarFacturasFiltradas(Long idTaquillero, EstadoFactura estado,
                                                              LocalDateTime desde, LocalDateTime hasta,
                                                              Pageable pageable) {

        // 1. Paginación en base de datos con filtros opcionales
        Page<Factura> paginaFacturas = facturaRepository.buscarFacturasFiltradas(
                idTaquillero, estado, desde, hasta, pageable);

        if (paginaFacturas.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2. IDs de la página para cargar detalles en lote (sin N+1)
        List<Long> idsFacturas = paginaFacturas.getContent().stream()
                .map(Factura::getId)
                .toList();

        // 3. Hidrata boletos y productos en el contexto de persistencia
        facturaRepository.findConBoletosByIdIn(idsFacturas);
        facturaRepository.findConProductosByIdIn(idsFacturas);

        // 4. Mapea a DTO preservando el orden de la página original
        List<FacturaAdminResponse> resumen = new ArrayList<>();
        for (Factura f : paginaFacturas.getContent()) {
            resumen.add(FacturaAdminResponse.de(f));
        }

        return new PageImpl<>(resumen, pageable, paginaFacturas.getTotalElements());
    }

    @Override
    @Transactional
    public FacturaResponse anular(Long idUsuario, Long idFactura) {

        // El usuario solo sirve para comprobar el permiso: no se guarda en la factura
        Usuario usuario = usuarioService.obtenerEntidad(idUsuario);
        if (usuario.getRol() != RolUsuario.ADMINISTRADOR || usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ReglaNegocioException("Solo un administrador activo puede anular facturas");
        }

        Factura factura = facturaRepository.findDetalleById(idFactura)
                .orElseThrow(() -> new NoEncontradoException("La factura no existe"));

        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new ReglaNegocioException("La factura ya está anulada");
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);
        for (DetalleBoleto boleto : factura.getBoletos()) {
            Funcion funcion = boleto.getFuncion();
            boolean comenzo = !LocalDateTime.of(funcion.getFecha(), funcion.getHoraInicio()).isAfter(ahora);

            if (comenzo) {
                throw new ReglaNegocioException(
                        "No se puede anular: una de las funciones de la factura ya comenzó");
            }
        }

        factura.anular();   // sin save(): Hibernate guarda al terminar la transacción

        factura = facturaRepository.findConProductosById(idFactura)   // termina de cargar los productos para el comprobante
                .orElseThrow(() -> new NoEncontradoException("La factura no existe"));

        return FacturaResponse.de(factura);
    }

    // MÉTODOS PRIVADOS

    private void validarSolicitud(VentaRequest request) {

        if (request == null) {
            throw new ReglaNegocioException("La venta no es válida");
        }
    }

    private Funcion bloquearFuncionVendible(Long idFuncion) {

        Funcion funcion = funcionRepository.findByIdForUpdate(idFuncion)
                .orElseThrow(() -> new NoEncontradoException("La función no existe"));

        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime inicio = LocalDateTime.of(funcion.getFecha(), funcion.getHoraInicio());

        if (!FuncionVendibilidad.esVendible(inicio, funcion.getPelicula().duracionEnMinutos(), ahora)) {
            throw new ReglaNegocioException("No se pueden vender boletos para esta función");
        }

        return funcion;
    }

    // Una sola línea por producto (UNIQUE id_factura + id_producto): se suman las repetidas
    private Map<Long, Integer> consolidar(List<ItemConfiteriaRequest> items) {

        Map<Long, Integer> cantidades = new LinkedHashMap<>();

        for (ItemConfiteriaRequest item : items) {
            if (item == null || item.idProducto() == null || item.cantidad() == null) {
                throw new ReglaNegocioException("Uno de los productos de la venta no es válido");
            }
            cantidades.merge(item.idProducto(), item.cantidad(), Integer::sum);
        }

        cantidades.values().forEach(cantidad -> {
            if (cantidad > ItemConfiteriaRequest.CANTIDAD_MAXIMA) {
                throw new ReglaNegocioException(
                        "La cantidad máxima por producto es " + ItemConfiteriaRequest.CANTIDAD_MAXIMA);
            }
        });

        return cantidades;
    }

    private DetalleConfiteria crearDetalleConfiteria(Long idProducto, Integer cantidad) {

        ProductoConfiteria producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new NoEncontradoException("Uno de los productos no existe"));

        if (producto.getEstado() != EstadoProducto.ACTIVO) {
            throw new ReglaNegocioException("El producto «" + producto.getNombre() + "» no está disponible");
        }

        return new DetalleConfiteria(producto, cantidad);
    }

    private boolean esAsientoDuplicado(DataIntegrityViolationException ex) {
        String mensaje = ex.getMostSpecificCause().getMessage();
        // H2 escribe el nombre de la restricción en mayúsculas y MySQL en minúsculas
        return mensaje != null && mensaje.toLowerCase(Locale.ROOT).contains(RESTRICCION_ASIENTO);
    }

    // -------------------------------------------------------------------------
    // Generación del comprobante en texto plano
    // -------------------------------------------------------------------------

    @Override
    public String generarTextoFactura(FacturaResponse f) {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("                 CINE COMFENALCO                  \n");
        sb.append("           FACTURA DE VENTA ELECTRÓNICA           \n");
        sb.append("==================================================\n");
        sb.append(String.format("Factura No.:       #FACT-%05d%n", f.id()));
        sb.append(String.format("Fecha y Hora:      %s%n",
                f.fechaHora() != null ? f.fechaHora().toString().replace('T', ' ') : "N/A"));
        sb.append(String.format("Taquillero:        %s%n",
                f.taquillero() != null ? f.taquillero() : "General"));
        sb.append(String.format("Cliente:           %s%n",
                (f.nombreCliente() != null && !f.nombreCliente().isBlank()) ? f.nombreCliente() : "Cliente General"));
        if (f.correoCliente() != null && !f.correoCliente().isBlank()) {
            sb.append(String.format("Correo:            %s%n", f.correoCliente()));
        }
        sb.append(String.format("Método de Pago:    %s%n", f.metodoPago()));
        sb.append(String.format("Estado Factura:    %s%n", f.estado()));
        sb.append("--------------------------------------------------\n");

        if (f.boletos() != null && !f.boletos().isEmpty()) {
            sb.append("BOLETOS DE CINE:\n");
            for (var b : f.boletos()) {
                sb.append(String.format(" * %s | %s | Asiento: %s | $%s%n",
                        b.pelicula(), b.sala(), b.asiento(), b.precio()));
                sb.append(String.format("   Función: %s %s%n", b.fecha(), b.horaInicio()));
            }
            sb.append("--------------------------------------------------\n");
        }

        if (f.productos() != null && !f.productos().isEmpty()) {
            sb.append("CONFITERÍA Y CAFETERÍA:\n");
            for (var p : f.productos()) {
                sb.append(String.format(" * %dx %s (Unit: $%s) = $%s%n",
                        p.cantidad(), p.nombre(), p.precioUnitario(), p.subtotal()));
            }
            sb.append("--------------------------------------------------\n");
        }

        sb.append(String.format("TOTAL PAGADO:      $%s%n", f.total()));
        sb.append("==================================================\n");
        sb.append("      ¡Gracias por su visita a Cine Comfenalco!   \n");
        sb.append("==================================================\n");
        return sb.toString();
    }
}