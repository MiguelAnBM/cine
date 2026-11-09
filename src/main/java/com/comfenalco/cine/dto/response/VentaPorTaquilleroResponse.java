package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;

public record VentaPorTaquilleroResponse(
    String taquillero, Long facturas,
    BigDecimal totalBoletos, BigDecimal totalConfiteria, BigDecimal total
) {
}