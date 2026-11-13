package com.comfenalco.cine.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioEditarRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite máximo {max} caracteres")
    String nombreCompleto
) {
    public UsuarioEditarRequest {
        nombreCompleto = (nombreCompleto == null) ? null : nombreCompleto.trim();
    }
}
