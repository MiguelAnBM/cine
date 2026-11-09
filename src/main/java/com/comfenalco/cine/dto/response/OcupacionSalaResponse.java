package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.comfenalco.cine.model.Asientos;

public record OcupacionSalaResponse(
    String sala, Long funciones, Long boletosVendidos, Long capacidad, BigDecimal porcentaje
) {
    // Constructor para la consulta HQL: calcula la capacidad (80 asientos por función) y el porcentaje
    public OcupacionSalaResponse(String sala, Long funciones, Long boletosVendidos) {
        this(sala, funciones, boletosVendidos,
                funciones * Asientos.FILAS * Asientos.COLUMNAS,
                porcentaje(boletosVendidos, funciones * Asientos.FILAS * Asientos.COLUMNAS));
    }

    private static BigDecimal porcentaje(long vendidos, long capacidad) {
        if (capacidad == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return BigDecimal.valueOf(vendidos * 100L)
                .divide(BigDecimal.valueOf(capacidad), 1, RoundingMode.HALF_UP);
    }
}