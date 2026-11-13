package com.comfenalco.cine.service.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ProductoConfiteriaResponse buscarPorId(Long id) {
        return ProductoConfiteriaResponse.de(obtenerEntidad(id));
    }

    @Override
    public List<ProductoConfiteriaResponse> listarPorCategoria(CategoriaProducto categoria) {
        if (categoria == null) {
            return productoRepository.findAllByOrderByNombre().stream()
                    .map(ProductoConfiteriaResponse::de)
                    .toList();
        }
        return productoRepository.findByCategoriaOrderByNombre(categoria).stream()
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

    @Override
    public Map<Long, ProductoConfiteriaResponse> buscarActivosPorIds(Collection<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        // Una sola consulta batch: todos los productos activos cuyos IDs están en la lista
        List<ProductoConfiteria> productos = productoRepository.findByIdInAndEstado(
                ids, EstadoProducto.ACTIVO);

        return productos.stream()
                .collect(Collectors.toMap(
                        ProductoConfiteria::getId,
                        ProductoConfiteriaResponse::de,
                        (a, b) -> a, // si hubiera duplicados de ID, nos quedamos con el primero
                        LinkedHashMap::new // preserva orden de inserción
                ));
    }

    private ProductoConfiteria obtenerEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("El producto no existe"));
    }
}
