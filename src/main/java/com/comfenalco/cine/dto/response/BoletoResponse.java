package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.comfenalco.cine.model.DetalleBoleto;
import com.comfenalco.cine.model.Funcion;

public record BoletoResponse(
    String pelicula, String sala, LocalDate fecha, LocalTime horaInicio,
    String asiento, BigDecimal precio
) {
    public static BoletoResponse de(DetalleBoleto b) {
        Funcion f = b.getFuncion();
        return new BoletoResponse(
                f.getPelicula().getTitulo(), f.getSala().getNombre(),
                f.getFecha(), f.getHoraInicio(),
                b.getCodigoAsiento(), b.getPrecioUnitario());
    }
}