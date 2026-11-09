package com.comfenalco.cine.dto.request;

import java.math.BigDecimal;

import com.comfenalco.cine.model.enums.TipoSala;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalaRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre admite máximo {max} caracteres")
    String nombre,

    @NotNull(message = "El tipo de sala es obligatorio")
    TipoSala tipo,

    @NotNull(message = "El precio del boleto es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio admite máximo {integer} enteros y {fraction} decimales")
    BigDecimal precioBoleto
) {
    // Se ejecuta antes de la validación: deja el nombre limpio para el servicio
    public SalaRequest {
        nombre = (nombre == null) ? null : nombre.trim();
    }
}