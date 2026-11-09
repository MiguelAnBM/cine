package com.comfenalco.cine.dto.response;

import java.time.LocalDateTime;

import com.comfenalco.cine.model.Factura;
import com.comfenalco.cine.model.enums.MetodoPago;

// Para listados: sin datos personales del cliente y sin total (el total se calcula en reportes)
public record FacturaResumenResponse(
    Long id, LocalDateTime fechaHora, MetodoPago metodoPago, String taquillero
) {
    public static FacturaResumenResponse de(Factura f) {
        return new FacturaResumenResponse(
                f.getId(), f.getFechaHora(), f.getMetodoPago(), f.getUsuario().getNombreCompleto());
    }
}