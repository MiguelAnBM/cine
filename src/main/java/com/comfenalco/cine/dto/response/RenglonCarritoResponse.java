package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;

/**
 * Representa un producto añadido al carrito de cafetería con sus cantidades y subtotal calculado.
 */
public record RenglonCarritoResponse(
        Long idProducto,
        String nombre,
        BigDecimal precioUnitario,
        int cantidad,
        BigDecimal subtotal
) {
}

