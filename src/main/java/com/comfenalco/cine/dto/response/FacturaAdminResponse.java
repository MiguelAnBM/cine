package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.comfenalco.cine.model.Factura;
import com.comfenalco.cine.model.enums.EstadoFactura;
import com.comfenalco.cine.model.enums.MetodoPago;

// Panel de Facturas de administración: todas las facturas con estado y fecha de anulación
public record FacturaAdminResponse(
        Long id, LocalDateTime fechaHora, MetodoPago metodoPago, String taquillero,
        String nombreCliente, BigDecimal total, EstadoFactura estado,
        LocalDateTime fechaAnulacion) {
    public static FacturaAdminResponse de(Factura f) {
        return new FacturaAdminResponse(
                f.getId(),
                f.getFechaHora(),
                f.getMetodoPago(),
                f.getUsuario().getNombreCompleto(),
                f.getNombreCliente(),
                f.calcularTotal(),
                f.getEstado(),
                f.getFechaAnulacion());
    }
}
