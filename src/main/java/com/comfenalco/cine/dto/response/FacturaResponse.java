package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.comfenalco.cine.model.Factura;
import com.comfenalco.cine.model.enums.EstadoFactura;
import com.comfenalco.cine.model.enums.MetodoPago;

public record FacturaResponse(
    Long id, LocalDateTime fechaHora, MetodoPago metodoPago, String taquillero,
    String nombreCliente, String correoCliente,
    EstadoFactura estado, LocalDateTime fechaAnulacion,
    List<BoletoResponse> boletos, List<ProductoVendidoResponse> productos,
    BigDecimal total
) {
    public static FacturaResponse de(Factura f) {
        return new FacturaResponse(
                f.getId(), f.getFechaHora(), f.getMetodoPago(), f.getUsuario().getNombreCompleto(),
                f.getNombreCliente(), f.getCorreoCliente(),
                f.getEstado(), f.getFechaAnulacion(),
                f.getBoletos().stream().map(BoletoResponse::de).toList(),
                f.getProductos().stream().map(ProductoVendidoResponse::de).toList(),
                f.calcularTotal());
    }
}