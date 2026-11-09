package com.comfenalco.cine.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.ProductoConfiteriaService;
import com.comfenalco.cine.dto.request.ProductoConfiteriaRequest;
import com.comfenalco.cine.dto.response.ProductoConfiteriaResponse;
import com.comfenalco.cine.exception.NoEncontradoException;
import com.comfenalco.cine.exception.ReglaNegocioException;
import com.comfenalco.cine.model.ProductoConfiteria;
import com.comfenalco.cine.model.enums.CategoriaProducto;
import com.comfenalco.cine.model.enums.EstadoProducto;
import com.comfenalco.cine.repository.ProductoConfiteriaRepository;

@Service
@Transactional(readOnly = true)
public class ProductoConfiteriaServiceImpl implements ProductoConfiteriaService {

    private final ProductoConfiteriaRepository productoRepository;

    public ProductoConfiteriaServiceImpl(ProductoConfiteriaRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public List<ProductoConfiteriaResponse> listar() {
        return productoRepository.findAll().stream()
                .map(ProductoConfiteriaResponse::de)
                .toList();
    }

    @Override
    public ProductoConfiteriaResponse buscarPorId(Long id) {
        return ProductoConfiteriaResponse.de(obtenerEntidad(id));
    }

    @Override
    public List<ProductoConfiteriaResponse> listarPorEstado(EstadoProducto estado) {
        return productoRepository.findByEstadoOrderByNombre(estado).stream()
                .map(ProductoConfiteriaResponse::de)
                .toList();
    }

    @Override
    public List<ProductoConfiteriaResponse> listarPorCategoria(CategoriaProducto categoria, EstadoProducto estado) {
        return productoRepository.findByCategoriaAndEstadoOrderByNombre(categoria, estado).stream()
                .map(ProductoConfiteriaResponse::de)
                .toList();
    }

    @Override
    public List<ProductoConfiteriaResponse> buscarPorNombre(String nombre, EstadoProducto estado) {
        String filtro = (nombre == null) ? "" : nombre.trim();
        return productoRepository.findByNombreContainingIgnoreCaseAndEstadoOrderByNombre(filtro, estado).stream()
                .map(ProductoConfiteriaResponse::de)
                .toList();
    }

    @Override
    @Transactional
    public ProductoConfiteriaResponse crear(ProductoConfiteriaRequest request) {

        if (productoRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new ReglaNegocioException("Ya existe un producto con ese nombre");
        }

        ProductoConfiteria producto = new ProductoConfiteria(
                request.nombre(),
                request.descripcion(),
                request.precio(),
                request.imagenUrl(),
                request.categoria());

        return ProductoConfiteriaResponse.de(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoConfiteriaResponse editar(Long id, ProductoConfiteriaRequest request) {

        ProductoConfiteria producto = obtenerEntidad(id);

        // AndIdNot: el producto no se compara consigo mismo
        if (productoRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre(), id)) {
            throw new ReglaNegocioException("Ya existe otro producto con ese nombre");
        }

        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setImagenUrl(request.imagenUrl());
        producto.setCategoria(request.categoria());

        return ProductoConfiteriaResponse.de(producto);   // sin save(): Hibernate guarda los cambios al terminar la transacción
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, EstadoProducto nuevoEstado) {

        ProductoConfiteria producto = obtenerEntidad(id);

        if (producto.getEstado() == nuevoEstado) {
            return;   // nada que cambiar
        }

        producto.setEstado(nuevoEstado);
    }

    private ProductoConfiteria obtenerEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("El producto no existe"));
    }
}