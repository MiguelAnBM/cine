package com.comfenalco.cine.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemConfiteriaRequest(
    @NotNull(message = "El producto es obligatorio") Long idProducto,

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es {value}")
    @Max(value = CANTIDAD_MAXIMA, message = "La cantidad máxima es {value}")
    Integer cantidad
) {
    public static final int CANTIDAD_MAXIMA = 50;
}