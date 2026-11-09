package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;

public record VentaPorPeliculaResponse(String pelicula, Long boletos, BigDecimal total) {
}