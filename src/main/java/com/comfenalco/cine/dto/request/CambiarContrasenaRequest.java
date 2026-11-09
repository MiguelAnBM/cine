package com.comfenalco.cine.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaRequest(
    @NotBlank(message = "La contraseña actual es obligatoria")
    @Size(max = 72, message = "La contraseña admite máximo {max} caracteres")
    String contrasenaActual,

    @NotBlank(message = "La contraseña nueva es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña nueva debe tener entre {min} y {max} caracteres")
    String contrasenaNueva
) {
    // Evita que un log imprima la contraseña
    @Override
    public String toString() {
        return "CambiarContrasenaRequest[***]";
    }
}