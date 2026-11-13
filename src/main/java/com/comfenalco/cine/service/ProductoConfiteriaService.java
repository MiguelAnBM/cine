package com.comfenalco.cine.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.comfenalco.cine.dto.request.ProductoConfiteriaRequest;
import com.comfenalco.cine.dto.response.ProductoConfiteriaResponse;
import com.comfenalco.cine.model.enums.CategoriaProducto;

public interface ProductoConfiteriaService {

    ProductoConfiteriaResponse buscarPorId(Long id);

    List<ProductoConfiteriaResponse> listarPorCategoria(CategoriaProducto categoria);

    ProductoConfiteriaResponse crear(ProductoConfiteriaRequest request);

    ProductoConfiteriaResponse editar(Long id, ProductoConfiteriaRequest request);

    void cambiarEstado(Long id, com.comfenalco.cine.model.enums.EstadoProducto nuevoEstado);

    // Devuelve los productos activos indexados por ID en un solo mapa.
    // Útil para cargar varios productos del carrito con una sola consulta (evita N+1).
    Map<Long, ProductoConfiteriaResponse> buscarActivosPorIds(Collection<Long> ids);
}
