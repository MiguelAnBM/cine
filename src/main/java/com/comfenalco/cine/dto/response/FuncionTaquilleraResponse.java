package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record FuncionTaquilleraResponse(
    String pelicula, String sala, LocalDate fecha, LocalTime horaInicio,
    Long boletos, BigDecimal ingresos
) {
}