package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaDiariaResponse(
    LocalDate fecha, BigDecimal boletos, BigDecimal confiteria, BigDecimal total
) {
}