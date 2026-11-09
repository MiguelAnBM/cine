package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenVentasResponse(
    LocalDate desde, LocalDate hasta,
    BigDecimal totalBoletos, BigDecimal totalConfiteria, BigDecimal total,
    long facturas, long boletos, long funcionesRealizadas
) {
}