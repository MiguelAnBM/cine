package com.comfenalco.cine.dto.resumen;

import java.math.BigDecimal;

// Fila interna de una consulta: total vendido por un usuario
public record TotalPorUsuario(Long idUsuario, BigDecimal total) {
}