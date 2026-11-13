package com.comfenalco.cine.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.comfenalco.cine.model.enums.MetodoPago;

// Dto de pago exclusivo de cafetería: solo datos del comprador y método de pago.
// La función y los asientos son nulos porque no hay boletos de cine en esta venta.
public record PagoCafeteriaRequest(

    @NotNull(message = "El método de pago es obligatorio")
    MetodoPago metodoPago,

    @Size(max = 100, message = "El nombre admite máximo {max} caracteres")
    String nombreCliente,

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo admite máximo {max} caracteres")
    String correoCliente
) {
}
