package com.comfenalco.cine.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record FuncionRequest(
    @NotNull(message = "La película es obligatoria") 
    Long idPelicula,

    @NotNull(message = "La sala es obligatoria") 
    Long idSala,

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser pasada")
    LocalDate fecha,

    @NotNull(message = "La hora de inicio es obligatoria") 
    LocalTime horaInicio
) {
    // Sin segundos: 18:00:30 y 18:00:00 serían horas distintas para el UNIQUE
    public FuncionRequest {
        horaInicio = (horaInicio == null) ? null : horaInicio.truncatedTo(ChronoUnit.MINUTES);
    }
}
