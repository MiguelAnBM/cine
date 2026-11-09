package com.comfenalco.cine.dto.request;

import java.util.Locale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioEditarRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite máximo {max} caracteres")
    String nombreCompleto,

    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 4, max = 50, message = "El usuario debe tener entre {min} y {max} caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$",
             message = "El usuario solo admite letras, números, punto, guion y guion bajo")
    String username
) {
    public UsuarioEditarRequest {
        nombreCompleto = (nombreCompleto == null) ? null : nombreCompleto.trim();
        username = (username == null) ? null : username.trim().toLowerCase(Locale.ROOT);
    }
}