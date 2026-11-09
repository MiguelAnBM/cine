package com.comfenalco.cine.dto.request;

import java.math.BigDecimal;

import com.comfenalco.cine.model.enums.CategoriaProducto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductoConfiteriaRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite máximo {max} caracteres")
    String nombre,

    @Size(max = 5000, message = "La descripción admite máximo {max} caracteres")
    String descripcion,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio admite máximo {integer} enteros y {fraction} decimales")
    BigDecimal precio,

    @Size(max = 500, message = "La URL admite máximo {max} caracteres")
    @Pattern(
        regexp = "^https?://\\S+$", 
        message = "La URL debe empezar con http:// o https://")
    String imagenUrl,

    @NotNull(message = "La categoría es obligatoria")
    CategoriaProducto categoria
) {
    // Se ejecuta antes de la validación: deja los datos limpios para el servicio
    public ProductoConfiteriaRequest {
        nombre = (nombre == null) ? null : nombre.trim();
        descripcion = (descripcion == null || descripcion.isBlank()) ? null : descripcion.trim();
        imagenUrl = (imagenUrl == null || imagenUrl.isBlank()) ? null : imagenUrl.trim();
    }
}