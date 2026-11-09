package com.comfenalco.cine.dto.request;

import com.comfenalco.cine.model.enums.Clasificacion;
import com.comfenalco.cine.model.enums.Genero;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PeliculaRequest(
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título admite máximo {max} caracteres")
    String titulo,

    @NotBlank(message = "La sinopsis es obligatoria")
    @Size(max = 5000, message = "La sinopsis admite máximo {max} caracteres")
    String sinopsis,

    @NotBlank(message = "La duración es obligatoria")
    @Pattern(
        regexp = "^(?!0H 0?0M$)[0-9]H [0-5]?[0-9]M$",
        message = "La duración debe tener el formato '2H 30M' y ser mayor a cero")
    String duracion,

    @NotNull(message = "La clasificación es obligatoria") Clasificacion clasificacion,
    @NotNull(message = "El género es obligatorio") Genero genero,

    @Size(max = 2000, message = "La URL admite máximo {max} caracteres")
    @Pattern(
        regexp = "^https?://\\S+$", 
        message = "La URL debe empezar con http:// o https://")
    String imagenUrl
) {
    // Se ejecuta antes de la validación: deja los datos limpios para el servicio
    public PeliculaRequest {
        titulo = titulo == null ? null : titulo.trim();
        sinopsis = sinopsis == null ? null : sinopsis.trim();
        imagenUrl = (imagenUrl == null || imagenUrl.isBlank()) ? null : imagenUrl.trim();
    }
}
