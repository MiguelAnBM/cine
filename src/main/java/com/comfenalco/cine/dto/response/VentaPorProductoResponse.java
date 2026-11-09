package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;

public record VentaPorProductoResponse(String producto, Long unidades, BigDecimal total) {
}