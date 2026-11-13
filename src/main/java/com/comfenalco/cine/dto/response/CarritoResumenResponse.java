package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Representa el resumen consolidado del pedido de cafetería en la sesión actual.
 */
public record CarritoResumenResponse(
        List<RenglonCarritoResponse> lineas,
        BigDecimal total
) {
}

