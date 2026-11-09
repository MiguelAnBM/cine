package com.comfenalco.cine.dto.request;

import java.util.List;
import java.util.Locale;

import com.comfenalco.cine.model.enums.MetodoPago;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// No incluye precios ni usuario: los precios salen de la BD y el taquillero, de la sesión
public record VentaRequest(

    Long idFuncion,   // opcional: solo si la venta incluye boletos

    @Size(max = 15, message = "Máximo {max} asientos por venta")
    List<
    @NotBlank(message = "Asiento inválido")
    @Pattern(regexp = "^[A-H]([1-9]|10)$", message = "Asiento inválido") String> asientos,

    @NotNull(message = "El método de pago es obligatorio") 
    MetodoPago metodoPago,

    @Size(max = 100, message = "El nombre admite máximo {max} caracteres") 
    String nombreCliente,

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo admite máximo {max} caracteres")
    String correoCliente,

    @Valid @Size(max = 20, message = "Máximo {max} productos distintos por venta")
    List<ItemConfiteriaRequest> productos
) {
    // Se ejecuta antes de la validación: asientos en mayúscula y vacíos convertidos a null
    public VentaRequest {
        asientos = (asientos == null) ? null
                : asientos.stream().map(a -> a == null ? null : a.trim().toUpperCase(Locale.ROOT)).toList();
        nombreCliente = vacioANull(nombreCliente);
        correoCliente = vacioANull(correoCliente);
    }

    private static String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}