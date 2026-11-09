package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;

import com.comfenalco.cine.model.ProductoConfiteria;
import com.comfenalco.cine.model.enums.CategoriaProducto;
import com.comfenalco.cine.model.enums.EstadoProducto;

public record ProductoConfiteriaResponse(
    Long id, String nombre, String descripcion, BigDecimal precio,
    String imagenUrl, CategoriaProducto categoria, EstadoProducto estado
) {
    public static ProductoConfiteriaResponse de(ProductoConfiteria p) {
        return new ProductoConfiteriaResponse(
                p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
                p.getImagenUrl(), p.getCategoria(), p.getEstado());
    }
}
