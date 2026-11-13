package com.comfenalco.cine.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Solo nombre completo: el rol siempre es TAQUILLERO, no hay username ni contraseña
public record UsuarioCrearRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite máximo {max} caracteres")
    String nombreCompleto
) {
    public UsuarioCrearRequest {
        nombreCompleto = (nombreCompleto == null) ? null : nombreCompleto.trim();
    }

    @Override
    public String toString() {
        return "UsuarioCrearRequest[nombreCompleto=" + nombreCompleto + "]";
    }
}
