package com.comfenalco.cine.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comfenalco.cine.model.ProductoConfiteria;
import com.comfenalco.cine.model.enums.CategoriaProducto;
import com.comfenalco.cine.model.enums.EstadoProducto;

public interface ProductoConfiteriaRepository extends JpaRepository<ProductoConfiteria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<ProductoConfiteria> findAllByOrderByNombre();

    List<ProductoConfiteria> findByCategoriaOrderByNombre(CategoriaProducto categoria);

    // Devuelve productos cuyos IDs están en la lista y cuyo estado coincide.
    // Se usa para cargar en lote los productos del carrito (una consulta, sin N+1).
    List<ProductoConfiteria> findByIdInAndEstado(Collection<Long> ids, EstadoProducto estado);
}
