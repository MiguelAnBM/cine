package com.comfenalco.cine.service;

import java.util.List;

import com.comfenalco.cine.dto.request.ProductoConfiteriaRequest;
import com.comfenalco.cine.dto.response.ProductoConfiteriaResponse;
import com.comfenalco.cine.model.enums.CategoriaProducto;
import com.comfenalco.cine.model.enums.EstadoProducto;

public interface ProductoConfiteriaService {

    List<ProductoConfiteriaResponse> listar();

    ProductoConfiteriaResponse buscarPorId(Long id);

    List<ProductoConfiteriaResponse> listarPorEstado(EstadoProducto estado);

    List<ProductoConfiteriaResponse> listarPorCategoria(CategoriaProducto categoria, EstadoProducto estado);

    List<ProductoConfiteriaResponse> buscarPorNombre(String nombre, EstadoProducto estado);

    ProductoConfiteriaResponse crear(ProductoConfiteriaRequest request);

    ProductoConfiteriaResponse editar(Long id, ProductoConfiteriaRequest request);

    void cambiarEstado(Long id, EstadoProducto nuevoEstado);
}