package com.comfenalco.cine.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.DetalleBoletoService;
import com.comfenalco.cine.service.UsuarioService;
import com.comfenalco.cine.service.VentaService;
import com.comfenalco.cine.utils.ValidadorRango;
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
import com.comfenalco.cine.model.enums.EstadoFuncion;
import com.comfenalco.cine.model.enums.EstadoProducto;
import com.comfenalco.cine.model.enums.EstadoUsuario;
import com.comfenalco.cine.model.enums.RolUsuario;
import com.comfenalco.cine.repository.FacturaRepository;
import com.comfenalco.cine.repository.FuncionRepository;
import com.comfenalco.cine.repository.ProductoConfiteriaRepository;

@Service
@Transactional(readOnly = true)
public class VentaServiceImpl implements VentaService {

    // Política asumida: no se venden boletos una vez iniciada la función
    private static final int MINUTOS_VENTA_TRAS_INICIO = 0;

    private static final String RESTRICCION_ASIENTO = "uq_boleto_funcion_asiento";

    private final FacturaRepository facturaRepository;
    private final FuncionRepository funcionRepository;
    private final ProductoConfiteriaRepository productoRepository;
    private final UsuarioService usuarioService;
    private final DetalleBoletoService detalleBoletoService;

    public VentaServiceImpl(FacturaRepository facturaRepository,
                            FuncionRepository funcionRepository,
                            ProductoConfiteriaRepository productoRepository,
                            UsuarioService usuarioService,
                            DetalleBoletoService detalleBoletoService) {
        this.facturaRepository = facturaRepository;
        this.funcionRepository = funcionRepository;
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.detalleBoletoService = detalleBoletoService;
    }

    @Override
    @Transactional
    public FacturaResponse vender(Long idUsuario, VentaRequest request) {

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

    @Override
    public List<FacturaResumenResponse> listarPorRango(LocalDate desde, LocalDate hasta) {

        ValidadorRango.validar(desde, hasta);

        // Rango (desde 00:00, hasta + 1 día 00:00)
        return facturaRepository.findPorRango(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay())
                .stream()
                .map(FacturaResumenResponse::de)
                .toList();
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

        LocalDateTime ahora = LocalDateTime.now();
        for (DetalleBoleto boleto : factura.getBoletos()) {
            Funcion funcion = boleto.getFuncion();
            boolean comenzo = !LocalDateTime.of(funcion.getFecha(), funcion.getHoraInicio()).isAfter(ahora);

            if (comenzo && funcion.getEstado() != EstadoFuncion.CANCELADA) {
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

    private Funcion bloquearFuncionVendible(Long idFuncion) {

        Funcion funcion = funcionRepository.findByIdForUpdate(idFuncion)
                .orElseThrow(() -> new NoEncontradoException("La función no existe"));

        if (funcion.getEstado() != EstadoFuncion.PROGRAMADA) {
            throw new ReglaNegocioException("No se pueden vender boletos para esta función");
        }

        LocalDateTime inicio = LocalDateTime.of(funcion.getFecha(), funcion.getHoraInicio());
        if (LocalDateTime.now().isAfter(inicio.plusMinutes(MINUTOS_VENTA_TRAS_INICIO))) {
            throw new ReglaNegocioException("La función ya comenzó");
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
}