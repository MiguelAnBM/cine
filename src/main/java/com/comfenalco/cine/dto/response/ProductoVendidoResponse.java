package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;

import com.comfenalco.cine.model.DetalleConfiteria;

public record ProductoVendidoResponse(
    String nombre, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal
) {
    public static ProductoVendidoResponse de(DetalleConfiteria d) {
        return new ProductoVendidoResponse(
                d.getProductoConfiteria().getNombre(), d.getCantidad(),
                d.getPrecioUnitario(), d.calcularSubtotal());
    }
}