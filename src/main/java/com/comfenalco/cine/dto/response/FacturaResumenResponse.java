package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.comfenalco.cine.model.Factura;
import com.comfenalco.cine.model.enums.MetodoPago;

// Para listados de facturas emitidas: incluye cliente y total para el módulo de anulación
public record FacturaResumenResponse(
        Long id, LocalDateTime fechaHora, MetodoPago metodoPago, String taquillero,
        String nombreCliente, BigDecimal total) {
    public static FacturaResumenResponse de(Factura f) {
        return new FacturaResumenResponse(
                f.getId(),
                f.getFechaHora(),
                f.getMetodoPago(),
                f.getUsuario().getNombreCompleto(),
                f.getNombreCliente(),
                f.calcularTotal());
    }
}