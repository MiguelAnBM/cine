package com.comfenalco.cine.dto.request;

import java.util.Locale;

import com.comfenalco.cine.model.enums.RolUsuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioCrearRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite máximo {max} caracteres")
    String nombreCompleto,

    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 4, max = 50, message = "El usuario debe tener entre {min} y {max} caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$",
             message = "El usuario solo admite letras, números, punto, guion y guion bajo")
    String username,

    @NotNull(message = "El rol es obligatorio")
    RolUsuario rol,

    // Solo para ADMINISTRADOR. BCrypt ignora lo que pase de 72 bytes
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre {min} y {max} caracteres")
    String contrasena
) {
    public UsuarioCrearRequest {
        nombreCompleto = (nombreCompleto == null) ? null : nombreCompleto.trim();
        username = (username == null) ? null : username.trim().toLowerCase(Locale.ROOT);
        // La contraseña no se recorta: los espacios pueden ser parte de ella
        contrasena = (contrasena == null || contrasena.isBlank()) ? null : contrasena;
    }

    // Evita que un log imprima la contraseña
    @Override
    public String toString() {
        return "UsuarioCrearRequest[username=" + username + ", rol=" + rol + "]";
    }
}
