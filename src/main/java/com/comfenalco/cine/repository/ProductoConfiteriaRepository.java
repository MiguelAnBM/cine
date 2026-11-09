package com.comfenalco.cine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comfenalco.cine.model.ProductoConfiteria;
import com.comfenalco.cine.model.enums.CategoriaProducto;
import com.comfenalco.cine.model.enums.EstadoProducto;

public interface ProductoConfiteriaRepository extends JpaRepository<ProductoConfiteria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<ProductoConfiteria> findByEstadoOrderByNombre(EstadoProducto estado);

    List<ProductoConfiteria> findByCategoriaAndEstadoOrderByNombre(CategoriaProducto categoria, EstadoProducto estado);

    List<ProductoConfiteria> findByNombreContainingIgnoreCaseAndEstadoOrderByNombre(
            String nombre, EstadoProducto estado);
}