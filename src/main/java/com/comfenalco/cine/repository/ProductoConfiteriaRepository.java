package com.comfenalco.cine.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.comfenalco.cine.model.ProductoConfiteria;
import com.comfenalco.cine.model.enums.EstadoProducto;

public interface ProductoConfiteriaRepository extends JpaRepository<ProductoConfiteria, Long> {

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    List<ProductoConfiteria> findByEstado(EstadoProducto estado);
    
    Page<ProductoConfiteria> findByNombreContainingIgnoreCaseAndEstado(
            String nombre, EstadoProducto estado, Pageable pageable);
}
